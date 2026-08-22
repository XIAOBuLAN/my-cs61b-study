package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author XIAOBU
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** folder structure:
     * .gitlet/ -- top level folder for all persistent data
     *   - commits/ -- folder containing all of the serialized commits whose name is its uid
     *   - blobs/ --folder containing all of the blobs
     *   - refs/
     *     -heads/
     *      -master --txt document containing the head uid
     *   -HEAD --txt document that containing head pointer whose name is "master"
     *   -index --txt document of staging area (generating after first add)
     */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File INDEX_FILE = join(GITLET_DIR, "index");



    /** Initialize the .gitlet folder at CWD */
    /** folder structure:
     * .gitlet/ -- top level folder for all persistent data
     *   - commits/ -- folder containing all of the serialized commits whose name is its uid
     *   - blobs/ --folder containing all of the blobs
     *   - refs/
     *     -heads/
     *      -master --txt document containing the head commit's uid
     *   -HEAD --txt document that containing head pointer whose name is "master"
     */
    public static void init() {
        File f = GITLET_DIR;
        if (f.exists()) {
            throw Utils.error("A Gitlet version-control system already exists in the current directory.");
        }

        HEADS_DIR.mkdirs();
        BLOBS_DIR.mkdirs();
        COMMITS_DIR.mkdirs();

        /** creat the first commit whose default message is "initial commit" and
         * timestamp is Epoch. At the same time creat an Arraylist of parents
         * and a Treemap of blobs.
         * Serialize its uid. */
        Commit initial = null;
        Date Epoch = new Date(0);
        initial = new Commit("initial commit", Epoch, new ArrayList<>(), new TreeMap<>());

        saveCommit(initial);

        writeContents(HEAD_FILE, "master");
        writeContents(join(HEADS_DIR, "master"), initial.getUID());
    }

    /** Store the blob into stage */
    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.isFile()) {
            throw error("File does not exist.");
        }

        String blobUID = fileBlobUID(file); //calculate the uid of added file's content
        saveBlob(blobUID, readContents(file)); //save the file as a blob.

        Commit head = readCommit(getHead()); //get the head commit
        String headBlob = blobOf(head, fileName); //get the uid of file in the head commit

        Stage stage = readStage();

        if (blobUID.equals(headBlob)) {
            stage.getAdditions().remove(fileName);
            stage.getRemovals().remove(fileName);
        } else {
            stage.getAdditions().put(fileName, blobUID);
            stage.getRemovals().remove(fileName);
        }
        writeStage(stage);
    }

    public static void commit(String message) {
        if (message.isEmpty()) {
            throw error("Please enter a commit message.");
        }

        Stage stage = readStage();
        if (stage.isEmpty()) {
            throw error("No changes added to the commit.");
        }

        Commit parent = readCommit(getHead());
        TreeMap<String, String> newFiles = new TreeMap<>(parent.getFiles());

        for (String fileName : stage.getRemovals()){
            newFiles.remove(fileName);
        }

    }

    public static void rm(String fileName) {}
    public static void log() {}
    public static void globalLog() {}
    public static void find(String message) {}
    public static void status() {}
    public static void checkout(String[] args) {}
    public static void branch(String name) {}
    public static void rmBranch(String name) {}
    public static void reset(String uid) {}
    public static void merge(String branchName) {}


// ================helping methods=======================

    /** reads the file named "HEAD" and returns the string of the name of current branch */
    private static String getBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    /** returns the content of the .txt document of current branch name,
     * which is the UID of current commit */
    private static String getHead() {
        return readContentsAsString(join(HEADS_DIR, getBranch()));
    }

    /** Deserialize the commit, returns the commit that stored in the "commits" folder,
     * whose name is its uid */
    private static Commit readCommit(String uid) {
        return readObject(join(COMMITS_DIR, uid), Commit.class);
    }

    /** Serializes the commit */
    private static void saveCommit(Commit c) {
        writeObject(join(COMMITS_DIR, c.getUID()), c);
    }

    /** returns the uid of the document named "fileName" in this commit
     * (if theres's nothing then returns null)*/
    private static String blobOf(Commit c, String fileName) {
        return c.getFiles().get(fileName);
    }

    /** returns the hash of the content of the file */
    private static String fileBlobUID(File f) {
        return sha1(readContents(f));
    }

    /** Save the contents as a blob.
     * If the blob already exist, then do nothing */
    private static void saveBlob(String uid, byte[] contents) {
        File blobFile = join(BLOBS_DIR, uid);
        if (!blobFile.exists()) {
            writeContents(blobFile, contents);
        }
    }

    private static Stage readStage() {
        if (!INDEX_FILE.exists()) {
            return new Stage();
        }
        return readObject(INDEX_FILE, Stage.class);
    }

    private static void writeStage(Stage s) {
        writeObject(INDEX_FILE, s);
    }

    private static void checkoutFile(String uid, String fileName) {

    } // 从某提交把某文件写回工作区

}
