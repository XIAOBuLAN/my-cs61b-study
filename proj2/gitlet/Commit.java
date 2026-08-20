package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author XIAOBU
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date timestamp;
    private List<String> parentHash;
    private TreeMap<String, String> files;

    /* TODO: fill in the rest of this class. */
    public Commit(String message, Date timestamp, List<String> parentHash, TreeMap<String, String> files) {
        this.message = message;
        this.timestamp = timestamp;
        this.parentHash = new ArrayList<>(parentHash);
        this.files = new TreeMap<>(files);
    }

    public String getUID() {
        return Utils.sha1(Utils.serialize(this));
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<String> getParentHash() {
        return parentHash;
    }

    public TreeMap<String, String> getFiles() {
        return files;
    }

    public boolean isMerge(){
        return this.parentHash.size() > 1;
    }
}
