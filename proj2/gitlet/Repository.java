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
 *  @author TODO
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
     *      -master --txt document containing the head uid
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

        String uid = initial.getUID();
        writeObject(join(COMMITS_DIR, uid), initial);

        writeContents(HEAD_FILE, "master");
        writeContents(join(HEADS_DIR, "master"), uid);
    }

    public static void add(String fileName) {}
    public static void commit(String message) {}
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

    /** helping methods */

    private static String getBranch() {
        return null;
    }         // 读 HEAD 文件
    private static String getHead() {
        return null;
    }           // 当前分支指向的 commit UID
    private static Commit readCommit(String uid) {
        return null;
    }
    private static void saveCommit(Commit c) {

    }  // 写 commits/<uid>
    private static String blobOf(Commit c, String fileName) {
        return null;
    } // 该提交中文件的 blob UID（无则 null）
    private static String fileBlobUID(File f) {
        return null;
    } // sha1(文件内容)
    private static void saveBlob(String uid, byte[] contents) {

    }
    private static Stage readStage() {
        return null;
    }

    private static void writeStage(Stage s) {
    }
    private static void checkoutFile(String uid, String fileName) {

    } // 从某提交把某文件写回工作区



}
