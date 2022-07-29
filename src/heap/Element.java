package heap;

public interface Element<T extends Comparable<T>> {
    T value();
    void remove();
    void update(T value);
}
