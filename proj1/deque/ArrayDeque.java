package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {

    private T[] items;
    private int size;
    private int head;
    private int tail;

    /** Creat an empty ArrayDeque */
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        head = 0;
        tail = 0;
    }

    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];

        for (int i = 0; i < size; i++){
            int index = (head + i) % items.length;
            a[i] = items[index];
        }

        items = a;
        head = 0;
        tail = size;
    }

    /** Add at the front of Deque */
    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }

        if (head == 0) {
            head = items.length - 1;
        } else {
            head -= 1;
        }

        items[head] = item;
        size += 1;
    }

    /** Add at the last of Deque */
    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }

        items[tail] = item;
        tail = (tail + 1) % items.length;
        size += 1;
    }

    /** Return size of deque */
    @Override
    public int size() {
        return size;
    }

    /** Print out item from first to last */
    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(items[(head + i) % items.length] + " ");
        }
        System.out.println();
    }

    /** Removes and returns the item at the front of the deque.
     * If no such item exists, returns null. */
    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        if ((size < items.length / 4) && (size > 4)) {
            resize(items.length / 4);
        }

        T item = items[head];
        items[head] = null;

        head = (head + 1) % items.length;
        size -= 1;
        return item;
    }

    /** Removes and returns the item at the back of the deque.
     * If no such item exists, returns null. */
    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        if ((size <= items.length / 4) && (size >= 4)) {
            resize(items.length / 4);
        }

        tail = (tail - 1 + items.length) % items.length;

        T item = items[tail];
        items[tail] = null;

        size -= 1;
        return item;
    }

    /**  Gets the item at the given index */
    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        return items[(head + index) % items.length];
    }

    /** Returns an Iterator into ME */
    public Iterator<T> iterator(){
        return new ArrayIterator();
    }

    private class ArrayIterator implements Iterator<T> {
        private int wisPos;

        public ArrayIterator(){
            wisPos = 0;
        }

        @Override
        public boolean hasNext() {
            return wisPos < size;
        }

        @Override
        public T next() {
            T item = get(wisPos);
            wisPos += 1;
            return item;
        }
    }

    /** Returns whether or not the parameter o is equal to the Deque.
     * o is considered equal if it is a Deque and if it contains the same contents
     * (as governed by the generic T’s equals method) in the same order.
     */
    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Deque)) {
            return false;
        }

        if (((Deque<?>)o).size() != this.size){
            return false;
        }

        if (o == this) {
            return true;
        }

        for (int i = 0; i < this.size(); i += 1){
            Object item = ((Deque<?>)o).get(i);
            if(!this.get(i).equals(item)){
                return false;
            }
        }
        return true;
    }
}
