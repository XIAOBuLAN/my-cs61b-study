package deque;

public class ArrayDeque<T> {

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
    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }

        items[tail] = item;
        tail = (tail + 1) % items.length;
        size += 1;
    }

    /** Ruturn true if deque is empty */
    public Boolean isEmpty() {
        if (size == 0) {
            return true;
        }
        return false;
    }

    /** Return size of deque */
    public int size() {
        return size;
    }

    /** Print out item from first to last */
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(items[(head + i) % items.length] + " ");
        }
        System.out.println();
    }

    /** Removes and returns the item at the front of the deque.
     * If no such item exists, returns null. */
    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        if ((size < items.length / 4) && (size > 4)) {
            resize(items.length / 4);
        }

        T item = items[head];
        items[head] = null;

        head += 1;
        size -= 1;
        return item;
    }

    /** Removes and returns the item at the back of the deque.
     * If no such item exists, returns null. */
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        if ((size <= items.length / 4) && (size >= 4)) {
            resize(items.length / 4);
        }

        tail = (tail - 1) % items.length;

        T item = items[tail];
        items[tail] = null;

        size -= 1;
        return item;
    }

    /**  Gets the item at the given index */
    public T get(int index) {
        int currentindex = (head + index) % items.length;

        if (currentindex >= size || currentindex < 0) {
            return null;
        }

        return items[currentindex];
    }


    public static void main(String[] args) {
        ArrayDeque<Integer> L = new ArrayDeque<>();
        L.addFirst(5);
        L.addLast(6);
        L.addLast(7);
        L.addLast(8);
        L.addFirst(9);
        L.addLast(10);
        L.addLast(11);
        L.addLast(12);
        L.addLast(13);
        L.addFirst(14);
        L.addFirst(15);

        System.out.println(L.get(14));
        System.out.println(L.removeFirst());
    }
}
