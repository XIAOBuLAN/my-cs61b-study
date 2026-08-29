package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

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
     *      -other_branch
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
        if (message == null || message.isEmpty()) {
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

        for (Map.Entry<String, String> entry : stage.getAdditions().entrySet() ) {
            String fileName = entry.getKey();
            String fileUID = entry.getValue();
            newFiles.put(fileName, fileUID);
        }

        Commit newCommit = new Commit(message, new Date(), List.of(parent.getUID()), newFiles);

        saveCommit(newCommit);

        writeContents(join(HEADS_DIR, getBranch()), newCommit.getUID()); //修改HEAD uid（移动头指针）

        writeStage(new Stage()); //清空暂存区
    }

    /** Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it
     * for removal and remove the file from the working directory
     * if the user has not already done so
     * (do not remove it unlessit is tracked in the current commit).
     * */
    public static void rm(String fileName) {
        Stage stage = readStage();
        Commit head = readCommit(getHead());

        boolean tracked = head.getFiles().containsKey(fileName);
        boolean staged = stage.getAdditions().containsKey(fileName);

        if ( (!tracked) && (!staged) ) {
            throw error("No reason to remove the file.");
        }

        if (staged) {
            stage.getAdditions().remove(fileName);
        }

        if (tracked) {
            stage.getRemovals().add(fileName);
            if (join(CWD, fileName).isFile()) {
                restrictedDelete(fileName);
            }
        }
        writeStage(stage);
    }

    private static void log(Commit c){
        System.out.println("===");
        System.out.println("commit " + c.getUID());

        if (c.getParentHash().size() > 1){
            System.out.println("Merge: " + c.getParentHash().get(0).substring(0, 7) + " " + c.getParentHash().get(1).substring(0, 7));
        }

        Formatter formTime = new Formatter();
        formTime.format(Locale.US,"Date: %1$ta %1$tb %1$te %1$tT %1$tY %1$tz", c.getTimestamp());
        System.out.println(formTime.toString());
        System.out.println(c.getMessage());

        System.out.println();

        List<String> parentHash = c.getParentHash();
        if (parentHash.isEmpty()) {
            return;
        }

        Commit parentCommit = readCommit(parentHash.get(0));
        log(parentCommit);
    }

    public static void log() {
        Commit head = readCommit(getHead());
        log(head);
    }

    public static void globalLog() {
        List<String> commitList = plainFilenamesIn(COMMITS_DIR);
        for (String commitName : commitList) {
            Commit c = readCommit(commitName);

            System.out.println("===");
            System.out.println("commit " + c.getUID());

            if (c.getParentHash().size() > 1){
                System.out.println("Merge: " + c.getParentHash().get(0).substring(0, 7) + " " + c.getParentHash().get(1).substring(0, 7));
            }

            Formatter formTime = new Formatter();
            formTime.format(Locale.US,"Date: %1$ta %1$tb %1$te %1$tT %1$tY %1$tz", c.getTimestamp());
            System.out.println(formTime.toString());
            System.out.println(c.getMessage());

            System.out.println();
        }
    }

    /** Prints out the ids of all commits that have the given commit message,
     * one per line. If there are multiple such commits, it prints the ids out
     * on separate lines. The commit message is a single operand;
     * to indicate a multiword message, put the operand in quotation marks */
    public static void find(String message) {
        List<String> commitList = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;

        for (String commitName : commitList) {
            Commit c = readCommit(commitName);
            String string = c.getMessage();

            if (string.equals(message)) {
                System.out.println(c.getUID());
                found = true;
            }
        }
        if (!found) {
            throw error("Found no commit with that message.");
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");

        String currentPointer = readContentsAsString(HEAD_FILE);
        System.out.println("*" + currentPointer);

        List<String> branchNameList = plainFilenamesIn(HEADS_DIR);
        for(String branchName : branchNameList){
            if(!branchName.equals(currentPointer)){
                System.out.println(branchName);
            }
        }

        System.out.println();
        System.out.println("=== Staged Files ===");

        Stage stage = readStage();

        TreeMap<String, String> additionMap = stage.getAdditions();
        for (Map.Entry<String, String> entry : additionMap.entrySet()){
            System.out.println(entry.getKey());
        }

        System.out.println();
        System.out.println("=== Removed Files ===");

        TreeSet<String> removalSet = stage.getRemovals();
        for (String fileName : removalSet){
            System.out.println(fileName);
        }

        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        printModification();

        System.out.println();
        System.out.println("=== Untracked Files ===");
        printUntracked();
    }

    /** Choose which kind of checkout to operate:
     * 1.java gitlet.Main checkout -- [file name]
     * 2.java gitlet.Main checkout [commit id] -- [file name]
     * 3.java gitlet.Main checkout [branch name]
     * */
    public static void checkout(String[] args) {
        if (args.length == 3 && args[1].equals("--")) {
            checkoutFile(getHead(), args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            checkoutFile(args[1], args[3]);
        } else if (args.length == 2) {
            checkoutBranch(args[1]);
        } else {
            throw error("Incorrect operands.");
        }
    }
    private static void checkoutFile(String commitUID, String fileName) {
        if (!join(COMMITS_DIR, commitUID).isFile()) {
            throw error("No commit with that id exists.");
        }

        Commit commit = readCommit(commitUID);
        String blobUID = blobOf(commit, fileName);

        if (blobUID == null) {
            throw error("File does not exist in that commit.");
        }

        writeContents(join(CWD, fileName), readContents(join(BLOBS_DIR, blobUID)));
    }

    private static void checkoutBranch(String branchName) {
        if (!join(HEADS_DIR, branchName).isFile()) {
            throw error("No such branch exists.");
        }

        String currentBranch = readContentsAsString(HEAD_FILE);
        if (branchName.equals(currentBranch)) {
            throw error("No need to checkout the current branch.");
        }

        String targetUID = readContentsAsString(join(HEADS_DIR, branchName));
        Commit target = readCommit(targetUID);

        resetWorkingDir(target);

        writeContents(HEAD_FILE, branchName);
        writeStage(new Stage());
    }

    /** Creates a new branch with the given name,
     * and points it at the current head commit.
     * A branch is nothing more than a name for a reference
     * (a SHA-1 identifier) to a commit node.
     * This command does NOT immediately switch to the newly created branch (just as in real Git).
     * Before you ever call branch, your code should
     * be running with a default branch called “master”. */
    public static void branch(String name) {
        if (join(HEADS_DIR, name).isFile()) {
            throw error("A branch with that name already exists.");
        }

        writeContents(join(HEADS_DIR, name), getHead());
    }

    public static void rmBranch(String name) {
        if (!join(HEADS_DIR, name).isFile()) {
            throw error("A branch with that name does not exist.");
        }

        String currentBranch = readContentsAsString(HEAD_FILE);
        if (name.equals(currentBranch)) {
            throw error("Cannot remove the current branch.");
        }
        restrictedDelete(join(HEADS_DIR, name));
    }

    public static void reset(String uid) {
        if (!join(COMMITS_DIR, uid).isFile()) {
            throw error("No commit with that id exists.");
        }

        Commit target = readCommit(uid);
        resetWorkingDir(target);

        writeContents(join(HEADS_DIR, getBranch()), uid);
        writeStage(new Stage());
    }

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



    private static void checkUntrackedOverwrite(Commit target) {
        Commit current = readCommit(getHead());
        Stage stage = readStage();

        for (String fileName : target.getFiles().keySet()) {
            File workingFile = join(CWD, fileName);
            if (!workingFile.isFile()) {
                continue;
            }

            boolean tracked = current.getFiles().containsKey(fileName);
            boolean staged = stage.getAdditions().containsKey(fileName);

            if (!tracked && !staged) {
                throw error("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
    }

    private static void resetWorkingDir(Commit target){
        Commit current = readCommit(getHead());
        checkUntrackedOverwrite(target);

        for (String fileName : target.getFiles().keySet()) {
            String blobUID = blobOf(target, fileName);
            writeContents(join(CWD, fileName), readContents(join(BLOBS_DIR, blobUID)));
        }

        for (String fileName : current.getFiles().keySet()) {
            if (!target.getFiles().containsKey(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }
    }

    private static void printModification(){
        Commit head = readCommit(getHead());
        Stage stage = readStage();

        for (String fileName : head.getFiles().keySet()) {
            String headBlob = head.getFiles().get(fileName);
            String stagedBlob = stage.getAdditions().get(fileName);

            if(!join(CWD, fileName).isFile()){
                if (!stage.getRemovals().contains(fileName)) {
                    System.out.println(fileName + " (deleted)");
                }
            } else {
                File workingFile = join(CWD, fileName);
                String workingBlob = sha1(readContents(workingFile));
                if (workingBlob.equals(headBlob)) {
                    continue;
                } else {
                    if (stagedBlob == null || !workingBlob.equals(stagedBlob)){
                        System.out.println(fileName + " (modified)");
                    }
                }
            }
        }
    }

    private static void printUntracked(){
        Commit head = readCommit(getHead());
        Stage stage = readStage();

        for(String fileName : plainFilenamesIn(CWD)){
            if (head.getFiles().containsKey(fileName)) {
                continue;
            } else if (stage.getAdditions().containsKey(fileName)) {
                continue;
            } else {
                System.out.println(fileName);
            }
        }
    }
}
