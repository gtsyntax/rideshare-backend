package com.rideshare.rideshare_backend.datastructure;

import java.util.ArrayList;
import java.util.List;

public class MinHeap<T extends Comparable<T>> {
    private List<T> heap;

    public MinHeap() {
        this.heap = new ArrayList<>();
    }

    public MinHeap(List<T> elements) {
        this.heap = new ArrayList<>(elements);
        buildHeap();
    }

    private int parent(int i) {
        return (i - 1) / 2;
    }

    private int leftChild(int i) {
        return 2 * i + 1;
    }

    private int rightChild(int i) {
        return 2 * i + 2;
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    public void insert(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot insert null element");
        }

        heap.add(element);
        heapifyUp(heap.size() - 1);
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = parent(index);

            // If current element is smaller than parent, swap
            if (heap.get(index).compareTo(heap.get(parentIndex)) < 0) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    public T extractMin() {
        if (isEmpty()) {
            throw new IllegalStateException("Heap is empty");
        }

        // Save minimum
        T min = heap.get(0);

        // Move last element to root
        T lastElement = heap.get(heap.size() - 1);
        heap.set(0, lastElement);

        // Remove last element
        heap.remove(heap.size() - 1);

        // Restore heap property
        if (!isEmpty()) {
            heapifyDown(0);
        }

        return min;
    }

    private void heapifyDown(int index) {
        while (true) {
            int smallest = index;
            int left = leftChild(index);
            int right = rightChild(index);

            // Find smallest among parent and children
            if (left < heap.size() && heap.get(left).compareTo(heap.get(smallest)) < 0) {
                smallest = left;
            }

            if (right < heap.size() && heap.get(right).compareTo(heap.get(smallest)) < 0) {
                smallest = right;
            }

            // If parent is smallest, heap property is satisfied
            if (smallest == index) {
                break;
            }

            // Swap with smallest child and continue
            swap(index, smallest);
            index = smallest;
        }
    }

    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Heap is empty");
        }
        return heap.get(0);
    }

    private void buildHeap() {
        // Start from last non-leaf node and heapify down
        for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
            heapifyDown(i);
        }
    }

    public List<T> getSortedElements() {
        List<T> result = new ArrayList<>();

        // Create a copy of the heap
        MinHeap<T> copy = new MinHeap<>();
        copy.heap = new ArrayList<>(this.heap);

        // Extract all elements in sorted order
        while (!copy.isEmpty()) {
            result.add(copy.extractMin());
        }

        return result;
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public void clear() {
        heap.clear();
    }

    public List<T> getElements() {
        return new ArrayList<>(heap);
    }

    public boolean contains(T element) {
        return heap.contains(element);
    }
}
