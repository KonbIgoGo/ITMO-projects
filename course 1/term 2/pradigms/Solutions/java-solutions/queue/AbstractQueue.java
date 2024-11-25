package queue;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractQueue implements Queue {
    protected int size;

    public void enqueue(Object element) {
        assert element != null;
        size++;
        enqueueImpl(element);
    }

    protected abstract void enqueueImpl(Object element);

    public Object element() {
        assert size > 0;
        return elementImpl();
    }

    protected abstract Object elementImpl();

    public Object dequeue() {
        assert size > 0;
        size--;
        return dequeueImpl();
    }

    protected abstract Object dequeueImpl();

    public void clear() {
        size = 0;
        clearImpl();
    }

    protected abstract void clearImpl();
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void dedup() {
        List<Object> vals = new ArrayList<>();
        while (size > 0) {
            if (vals.isEmpty()) {
                vals.add(dequeue());
                continue;
            }
            Object res = dequeue();
            if (!vals.get(vals.size() - 1).equals(res)) {
                vals.add(res);
            }
        }
        for (Object i : vals) {
            enqueue(i);
        }
//        dedupImpl();
    }

//    protected void dedupImpl(){};
}
