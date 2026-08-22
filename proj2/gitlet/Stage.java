package gitlet;

import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

public class Stage implements Serializable {

    public TreeMap<String, String> additions = new TreeMap<>();
    public TreeSet<String> removals = new TreeSet<>();

    /** construction */
    public Stage(){
        additions = new TreeMap<>();
        removals = new TreeSet<>();
    }

    /** Returns the TreeMap of additions */
    public TreeMap<String, String> getAdditions() {
        return additions;
    }

    /** Returns the TreeSet of removals */
    public TreeSet<String> getRemovals() {
        return removals;
    }

    /** Returns true if there is no stage */
    public boolean isEmpty(){
        return additions.isEmpty() && removals.isEmpty();
    }
}
