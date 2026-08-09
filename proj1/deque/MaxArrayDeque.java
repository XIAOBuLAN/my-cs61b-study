package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private final Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c){
        super();
        this.comparator = c;
    }

    private T getMax(Comparator<T> c){
        if (isEmpty()) {
            return null;
        }

        T max = this.get(0);

        for (int i = 1; i < this.size(); i++) {
            T item = this.get(i);
            if (c.compare(item, max) > 0) {
                max = item;
            }
        }
        return max;
    }

    public T max(){
        return getMax(this.comparator);
    }

    public T max(Comparator<T> c){
        return getMax(c);
    }
}
