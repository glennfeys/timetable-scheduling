package heap;

import heap.binary.BinaryHeap;

public class Heaps {
    public static <T extends Comparable<T>> BinaryHeap<T> newBinaryHeap() {
	    return new BinaryHeap<>();
    }
}
