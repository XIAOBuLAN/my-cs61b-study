package deque;

public class LinkedListDeque<T> {

    private class Node {
        public T item;
        public Node next;
        public Node prev;

        public Node(T i, Node p, Node n) {
            item = i;
            prev = p;
            next = n;
        }
    }

    private Node sentinel;
    private int size;

    /** Creat an empty Deque */
    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    public LinkedListDeque(T item) {
        sentinel = new Node(null, null, null);
        Node firstNode = new Node(item, sentinel, sentinel.next);
        sentinel.next.prev = firstNode;
        sentinel.next = firstNode;
        size = 1;
    }

    /** Add at the front of Deque */
    public void addFirst(T item) {
        Node newNode = new Node(item, sentinel, sentinel.next);
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        size += 1;
    }

    /** Add at the last of Deque */
    public void addLast(T item) {
        Node newNode = new Node(item, sentinel.prev, sentinel);
        sentinel.prev.next = newNode;
        sentinel.prev = newNode;
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
        Node current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.item + " ");
            current = current.next;
        }
        System.out.println();
    }

    /** Removes and returns the item at the front of the deque.
     * If no such item exists, returns null. */
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        Node toRemove = sentinel.next;
        T toRemoveItem = toRemove.item;

        sentinel.next = toRemove.next;
        toRemove.next.prev = sentinel;

        toRemove.next = null;
        toRemove.prev = null;

        size -= 1;
        return toRemoveItem;
    }

    /** Removes and returns the item at the back of the deque.
     * If no such item exists, returns null. */
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        Node toRemove = sentinel.prev;
        T toRemoveItem = toRemove.item;

        sentinel.prev = toRemove.prev;
        toRemove.prev.next = sentinel;

        toRemove.next = null;
        toRemove.prev = null;

        size -= 1;
        return toRemoveItem;
    }

    /**  Gets the item at the given index */
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        Node current = sentinel.next;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.item;
    }

    private T getRecursive(Node current, int index){
        if(index == 0){
            return current.item;
        }
        return getRecursive(current.next, index-1);
    }

    /** Gets the item at the given index using receursive */
    public T getRecursive(int index){
        if(index < 0 || index >= size) {
            return null;
        }
        return getRecursive(sentinel.next, index);
    }

    public static void main(String[] args) {
        LinkedListDeque<Integer> L = new LinkedListDeque<>();
        L.addFirst(1);
        L.addLast(2);
        L.addLast(3);
        System.out.println(L.getRecursive(3));
        System.out.println(L.size());
    }
}
