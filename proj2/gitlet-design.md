# Gitlet Design Document

**Name**: XIAOBU

## Classes and Data Structures

### Commit

Represents a single snapshot of the project. It records the commit message,
creation time, parent commit(s), and the complete mapping of tracked file
names to the UIDs of their contents (blobs).

#### Fields

1. `String message` — the commit message.
2. `Date timestamp` — when the commit was created. The initial commit uses
   `new Date(0)` (the Unix epoch, 00:00:00 UTC on Jan 1, 1970); all other
   commits use `new Date()`.
3. `List<String> parents` — UIDs of the parent commits. Empty for the
   initial commit; one element for a normal commit; two elements for a merge
   commit, stored as `[current HEAD, merged branch head]`.
4. `TreeMap<String, String> files` — maps each tracked file name to the UID
   of the blob holding its contents. A `TreeMap` keeps iteration order
   stable, so the serialized bytes (and therefore the SHA-1 UID) are
   reproducible across runs.

#### Important methods

- `String uid()` — returns `Utils.sha1(Utils.serialize(this))`. It is
  computed on demand and never stored as a field, so the hash cannot depend
  on itself.
- `void dump()` (optional) — prints the commit for debugging via
  `DumpObj`.

### Repository

The driver of the whole system. Every command is implemented as a static
method here; it also owns all file-system paths and persistence helpers.

#### Fields

1. `static final File CWD` — the current working directory.
2. `static final File GITLET_DIR` — `CWD/.gitlet`, the repository root.
3. `static final File COMMITS_DIR` — `.gitlet/commits` (one file per
   commit, named by UID).
4. `static final File BLOBS_DIR` — `.gitlet/blobs` (one file per blob,
   named by UID).
5. `static final File REFS_DIR` / `HEADS_DIR` — `.gitlet/refs` and
   `.gitlet/refs/heads` (one file per branch).
6. `static final File HEAD_FILE` — `.gitlet/HEAD`, a plain-text file
   holding the current branch name.
7. `static final File INDEX_FILE` — `.gitlet/index`, the serialized
   staging area.

#### Methods

- One public static method per command: `init`, `add`, `commit`, `rm`,
  `log`, `globalLog`, `find`, `status`, `checkout`, `branch`,
  `rmBranch`, `reset`, `merge`, plus `remoteAdd`, `remoteRm`,
  `fetch`, `push`, `pull`.
- Private helpers: `getBranch()`, `getHead()`, `readCommit(uid)`,
  `saveCommit(commit)`, `blobOf(commit, fileName)`, `saveBlob(uid,
  contents)`, `readStage()`, `writeStage(stage)`,
  `checkoutBlob(blobUID, fileName)`, `untrackedInTheWay(targetCommit)`,
  `findSplitPoint(uid1, uid2)`.

### Stage

The staging area (the "index") recording what the next commit will contain.

#### Fields

1. `TreeMap<String, String> additions` — files staged for addition,
   mapping file name -> blob UID.
2. `TreeSet<String> removals` — file names staged for removal.

### Blob

Not a Java class but a core data structure: the raw byte contents of a file,
stored at `.gitlet/blobs/<UID>` where `UID = sha1(file contents)`. Files
with identical contents automatically share one blob.

## Algorithms

### init

1. If `.gitlet` already exists, fail with `A Gitlet version-control system
   already exists in the current directory.`
2. Create the directory layout, then create the initial commit
   (`message = "initial commit"`, `timestamp = new Date(0)`, no parents,
   empty file map). Save it, write `master` into `HEAD`, and point
   `refs/heads/master` at it.

### add [file]

1. If the file does not exist, fail with `File does not exist.`
2. Compute `sha1(contents)`; write the blob if not already on disk.
3. If the blob equals the one stored for that file in the current HEAD
   commit, unstage the file (remove from both `additions` and
   `removals`); otherwise record it in `additions`.

### commit [message]

1. If no message was supplied, fail with `Please enter a commit message.`
2. If both `additions` and `removals` are empty, fail with `No changes
   added to the commit.`
3. Build the new file map by copying HEAD's map, then applying `additions`
   (put) and `removals` (remove). Create the commit with
   `parents = [HEAD uid]` and `timestamp = new Date()`. Save it, move the
   current branch pointer to it, and clear the stage.

### rm [file]

1. If the file is neither tracked in HEAD nor staged, fail with `No reason
   to remove the file.`
2. If it is only staged, unstage it.
3. If it is tracked in HEAD, delete it from the working directory and add it
   to `removals`.

### log / global-log

`log` walks the first-parent chain from HEAD; `global-log` scans every file
in `commits/`. Each entry is printed as:

```
===
commit <uid>
Merge: <first 7 hex of parent 1> <first 7 hex of parent 2>   (merge commits only)
Date: <EEE MMM d HH:mm:ss yyyy Z>
<message>
```

The date line is produced with `new SimpleDateFormat("EEE MMM d HH:mm:ss
yyyy Z", Locale.US)`, which yields the required numeric-offset form, e.g.
`Date: Thu Nov 9 17:01:33 2017 -0800`.

### find [message]

Scan all commits and print the UID of every one whose message exactly equals
the argument, one per line. If none match, fail with `Found no commit with
that message.`

### status

Print five sections, each followed by a blank line, with entries in
lexicographic order:

```
=== Branches ===
*master
...
=== Staged Files ===
...
=== Removed Files ===
...
=== Modifications Not Staged For Commit ===
...
=== Untracked Files ===
...
```

- Branches: the current branch is prefixed with `*`.
- Modifications not staged: tracked files whose working-copy contents differ
  from HEAD (or from the staged version if staged), shown as
  `name (modified)`; tracked files deleted from the working directory but
  not staged for removal, shown as `name (deleted)`.
- Untracked: working-directory files neither tracked in HEAD nor staged.

### checkout

- `checkout -- [file]`: overwrite the working-copy file with the blob from
  HEAD; fail with `File does not exist in that commit.` if absent.
- `checkout [commit] -- [file]`: same, but from the given commit; also fail
  with `No commit with that id exists.` when the commit is unknown.
- `checkout [branch]`: fail with `No such branch exists.` if the branch is
  missing, `No need to checkout the current branch.` if it is current, and
  `There is an untracked file in the way; delete it, or add and commit it
  first.` if any untracked file would be overwritten or deleted. Otherwise
  sync the working directory to the branch head (deleting files tracked in
  the old branch but not in the new one), rewrite `HEAD`, and clear the
  stage.

### branch / rm-branch

- `branch [name]`: create `refs/heads/<name>` containing the current HEAD
  UID; fail with `A branch with that name already exists.` on collision.
- `rm-branch [name]`: delete the branch file; fail with `A branch with that
  name does not exist.` or `Cannot remove the current branch.`

### reset [commit]

Move the current branch pointer to the given commit and sync the working
directory exactly as `checkout [branch]` does, but without changing
`HEAD`. Same error checks (`No commit with that id exists.` and the
untracked-file guard). The stage is cleared on success.

### merge [branch]

1. Validate in order: branch exists (`A branch with that name does not
   exist.`), not the current branch (`Cannot merge a branch with itself.`),
   no uncommitted changes (`You have uncommitted changes.`), and no
   untracked file would be overwritten (`There is an untracked file in the
   way; delete it, or add and commit it first.`).
2. Compute the split point (lowest common ancestor) of HEAD and the given
   branch head.
3. If split == given branch head, print `Given branch is an ancestor of the
   current branch.` and stop.
4. If split == HEAD, fast-forward the current branch to the given branch
   head, check out its files, print `Current branch fast-forwarded.`, and
   stop (no merge commit is created).
5. Otherwise perform a three-way merge file by file ("same as split" means
   the blob equals the split-point version; "deleted" means the file is
   absent):

   | split | current | given | action |
   |---|---|---|---|
   | absent | absent | present | take given version, stage it |
   | absent | present | absent | keep current |
   | absent | present | present, same content | no action |
   | absent | present | present, different content | conflict |
   | present | same as split | same as split | no action |
   | present | same as split | changed | take given version, stage it |
   | present | changed | same as split | keep current |
   | present | changed | changed | conflict |
   | present | deleted | same as split | keep deleted |
   | present | same as split | deleted | delete file, stage removal |
   | present | deleted | deleted | no action |
   | present | changed | deleted | conflict |
   | present | deleted | changed | conflict |

   On conflict, write the file with `<<<<<<< HEAD`, `=======`, `>>>>>>>`
   markers around the two versions, stage it, and print `Encountered a merge
   conflict.`
6. Create the merge commit with message `Merged <given branch> into <current
   branch>.`, parents `[HEAD uid, given branch head uid]`, containing all
   staged changes; update the current branch pointer and clear the stage.

### remote add / remote rm / fetch / push / pull

A remote is another Gitlet repository on disk. Store the given directory path
per remote and resolve it relative to CWD each time it is used.

- `remote add [name] [dir]` — record the remote; fail with `A remote with
  that name already exists.`
- `remote rm [name]` — forget it; fail with `A remote with that name does
  not exist.`
- `fetch [remote] [branch]` — copy the remote branch's commit chain and any
  missing blobs into local storage, then record the tip UID under
  `refs/remotes/<remote>/<branch>`; fail with `Remote directory not found.`
  or `That remote does not have that branch.`
- `push [remote] [branch]` — the reverse: copy the local current branch's
  chain into the remote and update `refs/heads/<branch>` there; fail with
  `Please pull remote changes before pushing.` if the remote branch contains
  commits not in the local chain.
- `pull [remote] [branch]` — `fetch` followed by `merge` of the fetched
  branch.

## Persistence

Directory layout under `.gitlet`:

```
.gitlet/
├── HEAD                plain text: current branch name
├── index               serialized Stage object
├── commits/            serialized Commit objects, one per file, named by UID
├── blobs/              raw file contents, one per file, named by UID
└── refs/heads/         plain text: branch name -> commit UID (one file per branch)
```

- `Commit` and `Stage` are serialized with `Utils.writeObject` /
  `Utils.readObject`.
- Blobs are raw bytes handled with `Utils.writeContents` /
  `Utils.readContents`.
- `HEAD` and branch files are plain text written with `Utils.writeContents`
  and read with `Utils.readContentsAsString`.
- Objects are content-addressed by SHA-1 UID, so identical content is stored
  only once; no garbage collection or ref-counting is needed.
- Only `init` creates the repository; every other command assumes `.gitlet`
  already exists.
