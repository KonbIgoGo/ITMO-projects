package workerpool

import (
	"context"
	"sync"
)

// Accumulator is a function type used to aggregate values of type T into a result of type R.
// It must be thread-safe, as multiple goroutines will access the accumulator function concurrently.
// Each worker will produce intermediate results, which are combined with an initial or
// accumulated value.
type Accumulator[T, R any] func(current T, accum R) R

// Transformer is a function type used to transform an element of type T to another type R.
// The function is invoked concurrently by multiple workers, and therefore must be thread-safe
// to ensure data integrity when accessed across multiple goroutines.
// Each worker independently applies the transformer to its own subset of data, and although
// no shared state is expected, the transformer must handle any internal state in a thread-safe
// manner if present.
type Transformer[T, R any] func(current T) R

// Searcher is a function type for exploring data in a hierarchical manner.
// Each call to Searcher takes a parent element of type T and returns a slice of T representing
// its child elements. Since multiple goroutines may call Searcher concurrently, it must be
// thread-safe to ensure consistent results during recursive  exploration.
//
// Important considerations:
//  1. Searcher should be designed to avoid race conditions, particularly if it captures external
//     variables in closures.
//  2. The calling function must handle any state or values in closures, ensuring that
//     captured variables remain consistent throughout recursive or hierarchical search paths.
type Searcher[T any] func(parent T) []T

// Pool is the primary interface for managing worker pools, with support for three main
// operations: Transform, Accumulate, and List. Each operation takes an input channel, applies
// a transformation, accumulation, or list expansion, and returns the respective output.
type Pool[T, R any] interface {
	// Transform applies a transformer function to each item received from the input channel,
	// with results sent to the output channel. Transform operates concurrently, utilizing the
	// specified number of workers. The number of workers must be explicitly defined in the
	// configuration for this function to handle expected workloads effectively.
	// Since multiple workers may call the transformer function concurrently, it must be
	// thread-safe to prevent race conditions or unexpected results when handling shared or
	// internal state. Each worker independently applies the transformer function to its own
	// data subset.
	Transform(ctx context.Context, workers int, input <-chan T, transformer Transformer[T, R]) <-chan R

	// Accumulate applies an accumulator function to the items received from the input channel,
	// with results accumulated and sent to the output channel. The accumulator function must
	// be thread-safe, as multiple workers concurrently update the accumulated result.
	// The output channel will contain intermediate accumulated results as R
	Accumulate(ctx context.Context, workers int, input <-chan T, accumulator Accumulator[T, R]) <-chan R

	// List expands elements based on a searcher function, starting
	// from the given element. The searcher function finds child elements for each parent,
	// allowing exploration in a tree-like structure.
	// The number of workers should be configured based on the workload, ensuring each worker
	// independently processes assigned elements.
	List(ctx context.Context, workers int, start T, searcher Searcher[T])
}

type poolImpl[T, R any] struct{}

func New[T, R any]() *poolImpl[T, R] {
	return &poolImpl[T, R]{}
}

// accumulation worker
// do accumulation things
// accumulates items of type T to accumulating type R
func accumulationWorker[T, R any](
	ctx context.Context,
	data *R,
	input <-chan T,
	accumulator Accumulator[T, R]) {
	for {
		select {
		case <-ctx.Done():
			return
		case val, ok := <-input:
			if !ok {
				return
			}
			*data = accumulator(val, *data)
		}
	}
}

func (p *poolImpl[T, R]) Accumulate(
	ctx context.Context,
	workers int,
	input <-chan T,
	accumulator Accumulator[T, R],
) <-chan R {
	var wg sync.WaitGroup
	accumulated := make([]R, workers)
	// accumulation workers initialization
	// if done signal comes before initialization finished
	// initialization stops
	for i := 0; i < workers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			// worker initialization
			accumulationWorker(ctx, &accumulated[i], input, accumulator)
		}()
	}
	// resulting unbuffered channel
	// immediatly returned
	// close worker works even after res is returned
	var res chan R = make(chan R)
	// channel close worker
	go func() {
		defer close(res)
		// wait for all workers finish their work
		wg.Wait()
		// send accumulated values to res channel
		// processing done signal
		for _, val := range accumulated {
			select {
			case <-ctx.Done():
				return
			case res <- val:
			}
		}
	}()

	return res
}

// worker for list
func listWorker[T any](
	ctx context.Context,
	data *[]T,
	searcher Searcher[T],
	layerData chan<- []T,
	layerDone chan<- int,
	layerNext <-chan int) {
	// worker not finished until there are any layers of tree
	for {
		// done signal processing
		// if layerNext channel is not closed,
		// then worker processes new layer
		// else finsh its work
		select {
		case <-ctx.Done():
			return
		case _, ok := <-layerNext:
			if !ok {
				return
			}
			// if data len not empty, then process it.
			// else there is no data means layer done
			if len(*data) > 0 {
				for i := 0; i < len(*data); i++ {
					select {
					case <-ctx.Done():
						return
					// process every data unit
					case layerData <- searcher((*data)[i]):
					}
				}
			}
			// send layer done signal
			// wait until barrier let worker
			// to start next layer
			layerDone <- 1
		}

	}
}

// split layer data to workers
func nodeSplitter[T any](nodes []T, res *[][]T, workers int) {
	counter := 0
	// clear old data
	clear(*res)
	// split nodes by workers
	for _, n := range nodes {
		if counter == workers {
			counter = 0
		}
		// append splitted result to
		// worker's specialized array of data
		// guaranteed no data race or
		// data intersection in this array
		(*res)[counter] = append((*res)[counter], n)
		counter++
	}
}

func (p *poolImpl[T, R]) List(ctx context.Context, workers int, start T, searcher Searcher[T]) {
	wg := sync.WaitGroup{}
	// data for every layer of tree
	var layerData chan []T = make(chan []T)
	// indicator of finished layer
	var layerDone chan int = make(chan int)
	// indicator to start next layer
	var layerNext chan int = make(chan int)
	// closes channels after return
	defer close(layerDone)
	defer close(layerData)
	// data for workers to process
	workersData := make([][]T, workers)
	// data from workers
	// ready to split
	initial := make([]T, 0)
	// initialize start data
	workersData[0] = append(workersData[0], start)
	// list workers initialization
	// if done signal comes before initialization finished
	// initialization stops
	for i := 0; i < workers; i++ {
		select {
		case <-ctx.Done():
			return
		default:
			wg.Add(1)
			go func() {
				defer wg.Done()
				// initialize list worker
				listWorker(ctx, &workersData[i], searcher, layerData, layerDone, layerNext)
			}()
			select {
			case <-ctx.Done():
				return
			// send signal to workers to start calculating first layer of tree
			case layerNext <- 1:
			}
		}
	}
	// barrier implementation
	// sends signal to start new layer after splitting nodes
	// if there are no nodes sends kill signal
	go func() {
		for {
			initial = make([]T, 0)
			doneCounter := 0
		loop:
			for {
				select {
				case <-ctx.Done():
					// kill signal
					close(layerNext)
					return
				case data := <-layerData:
					// if data is not empty,
					// then add it to initial
					if len(data) > 0 {
						initial = append(initial, data...)
					}
				case <-layerDone:
					doneCounter++
					// if all workers finished their layer,
					// get out of the loop
					if doneCounter == workers {
						break loop
					}
				}
			}
			// split nodes to workers
			nodeSplitter(initial, &workersData, workers)
			// if empty means there are no nodes anymore
			if len(workersData[0]) == 0 {
				// kill signal
				close(layerNext)
				return
			}
			// send signal to start calculating next layer of tree
			for i := 0; i < workers; i++ {
				layerNext <- 1
			}
		}
	}()
	// wait all workers
	wg.Wait()
}

// worker for transformer
// returns transformed data to its own
// part of outer slice
func transformWorker[T, R any](
	ctx context.Context,
	data *[]R,
	input <-chan T,
	transformer Transformer[T, R]) {
	// done signal
	// processing input value
	// and append it to a slice
	for {
		select {
		case <-ctx.Done():
			return
		case val, ok := <-input:
			if !ok {
				return
			}
			// appends value to worker's data slice
			*data = append(*data, transformer(val))
		}
	}
}

func (p *poolImpl[T, R]) Transform(
	ctx context.Context,
	workers int,
	input <-chan T,
	transformer Transformer[T, R],
) <-chan R {
	var wg sync.WaitGroup
	transformed := make([][]R, workers)
	var res chan R = make(chan R)
	// list workers initialization
	// if done signal comes
	// before initialization finished
	// initialization stops
	for i := 0; i < workers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			transformWorker(ctx, &transformed[i], input, transformer)
		}()
	}
	// closed means
	// all workers completed their work
	var finished chan struct{} = make(chan struct{})
	go func() {
		wg.Wait()
		close(finished)
	}()

	// channel close worker
	// if done just finish its work
	// if all transform workers finished
	// sends calculated value
	// to res channel
	go func() {
		defer close(res)
		select {
		case <-ctx.Done():
			return
		// if finished
		// send every got val to
		// resulting channel
		case <-finished:
			for _, valArr := range transformed {
				for _, val := range valArr {
					select {
					case <-ctx.Done():
						return
					case res <- val:
					}
				}
			}
		}
	}()
	// the result is transformated
	// channel of values
	return res
}
