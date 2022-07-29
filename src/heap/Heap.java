package heap;

public interface Heap<T extends Comparable<T>> {
    Element<T> insert(T value);
    Element<T> findMin() throws EmptyHeapException;
    T removeMin() throws EmptyHeapException;
    boolean isEmpty();
}
