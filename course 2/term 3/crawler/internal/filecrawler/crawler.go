package crawler

import (
	"context"
	"crawler/internal/fs"
	"crawler/internal/workerpool"
	"encoding/json"
	"errors"
	"sync"
)

// Configuration holds the configuration for the crawler, specifying the number of workers for
// file searching, processing, and accumulating tasks. The values for SearchWorkers, FileWorkers,
// and AccumulatorWorkers are critical to efficient performance and must be defined in
// every configuration.
type Configuration struct {
	SearchWorkers      int // Number of workers responsible for searching files.
	FileWorkers        int // Number of workers for processing individual files.
	AccumulatorWorkers int // Number of workers for accumulating results.
}

// Combiner is a function type that defines how to combine two values of type R into a single
// result. Combiner is not required to be thread-safe
//
// Combiner can either:
//   - Modify one of its input arguments to include the result of the other and return it,
//     or
//   - Create a new combined result based on the inputs and return it.
//
// It is assumed that type R has a neutral element (forming a monoid)
type Combiner[R any] func(current R, accum R) R

// Crawler represents a concurrent crawler implementing a map-reduce model with multiple workers
// to manage file processing, transformation, and accumulation tasks. The crawler is designed to
// handle large sets of files efficiently, assuming that all files can fit into memory
// simultaneously.
type Crawler[T, R any] interface {
	// Collect performs the full crawling operation, coordinating with the file system
	// and worker pool to process files and accumulate results. The result type R is assumed
	// to be a monoid, meaning there exists a neutral element for combination, and that
	// R supports an associative combiner operation.
	// The result of this collection process, after all reductions, is returned as type R.
	//
	// Important requirements:
	// 1. Number of workers in the Configuration is mandatory for managing workload efficiently.
	// 2. FileSystem and Accumulator must be thread-safe.
	// 3. Combiner does not need to be thread-safe.
	// 4. If an accumulator or combiner function modifies one of its arguments,
	//    it should return that modified value rather than creating a new one,
	//    or alternatively, it can create and return a new combined result.
	// 5. Context cancellation is respected across workers.
	// 6. Type T is derived by json-deserializing the file contents, and any issues in deserialization
	//    must be handled within the worker.
	// 7. The combiner function will wait for all workers to complete, ensuring no goroutine leaks
	//    occur during the process.
	Collect(
		ctx context.Context,
		fileSystem fs.FileSystem,
		root string,
		conf Configuration,
		accumulator workerpool.Accumulator[T, R],
		combiner Combiner[R],
	) (R, error)
}

type crawlerImpl[T, R any] struct{}

func New[T, R any]() *crawlerImpl[T, R] {
	return &crawlerImpl[T, R]{}
}

func (c *crawlerImpl[T, R]) Collect(
	ctx context.Context,
	fileSystem fs.FileSystem,
	root string,
	conf Configuration,
	accumulator workerpool.Accumulator[T, R],
	combiner Combiner[R],
) (R, error) {
	var res R
	// contains every error that was got
	// during doing collect
	// returns it at the end of function
	var combinedError error = nil
	// once do panic error
	// if panic adding its error
	// to combined error value
	var o sync.Once
	// locks write to combinedErr
	// avoiding data race
	var errLocker sync.Mutex

	// check if config is incorrect
	if conf.AccumulatorWorkers < 1 || conf.FileWorkers < 1 || conf.SearchWorkers < 1 {
		combinedError = errors.Join(combinedError, errors.New("incorrect workers amount"))
	}

	// slice of files ready to transform
	files := make([]string, 0)
	// locks files writing to avoid data race
	var locker sync.Mutex

	// worker pool for finding files
	// and unmarshal it
	workerPool := workerpool.New[string, T]()

	// unmarshal files and return
	// res of type T
	transformer := func(data string) (res T) {
		defer func() {
			if r := recover(); r != nil {
				o.Do(func() {
					combinedError = errors.Join(combinedError, r.(error))
				})
			}
		}()
		// open file
		file, openErr := fileSystem.Open(data)
		var decodeErr error = nil
		// if file opened
		// do decode
		if openErr == nil {
			// unmarshal json
			decodeErr = json.NewDecoder(file).Decode(&res)
			// close file io
			file.Close()
		}

		// write errors
		errLocker.Lock()
		combinedError = errors.Join(combinedError, openErr, decodeErr)
		errLocker.Unlock()
		return
	}

	searcher := func(parent string) []string {
		defer func() {
			if r := recover(); r != nil {
				o.Do(func() {
					combinedError = errors.Join(combinedError, r.(error))
				})
			}
		}()
		// get dir data
		dirData, readingErr := fileSystem.ReadDir(parent)

		// write error
		errLocker.Lock()
		combinedError = errors.Join(combinedError, readingErr)
		errLocker.Unlock()

		// inner dir paths
		var res = make([]string, 0)
		// inner file paths
		var noDir = make([]string, 0)

		// read dir data
		for _, d := range dirData {
			// define path from root to dir or file
			name := fileSystem.Join(parent, d.Name())
			if d.IsDir() {
				// adding dir to  local dir pths
				res = append(res, name)
			} else {
				// adding file to local file paths
				noDir = append(noDir, name)
			}
		}

		locker.Lock()
		// sync local file paths with global ones
		files = append(files, noDir...)
		locker.Unlock()

		return res
	}
	workerPool.List(ctx, conf.SearchWorkers, root, searcher)
	var filesData chan string = make(chan string)
	// generate unbuffered channel from slice of files
	go func() {
		// worker dies when every file
		// was sent to unbuffered channel
		// or there is done signal
		defer close(filesData)
		for _, f := range files {
			select {
			case <-ctx.Done():
				return
			case filesData <- f:
			}
		}
	}()

	// do transform
	transformed := workerPool.Transform(ctx, conf.FileWorkers, filesData, transformer)
	// worker pool for accumulating data
	resultingWp := workerpool.New[T, R]()
	// do accumulate
	accumulated := resultingWp.Accumulate(ctx, conf.AccumulatorWorkers, transformed, accumulator)
	// combine result into one value
	for val := range accumulated {
		res = combiner(val, res)
	}
	// check if there was done signal
	combinedError = errors.Join(combinedError, ctx.Err())
	return res, combinedError
}
