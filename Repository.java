package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 * It's a good idea to give a description here of what else this Class
 * does at a high level.
 *
 * @author Kanna Shan
 */
public class Repository implements Serializable {
    /**
     * add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File REPOS = join(GITLET_DIR, "repos");
    public static final File COMMITS = join(GITLET_DIR, "commits");
    public static final File BRANCHES = join(GITLET_DIR, "branches");


    /* fill in the rest of this class. */

    //    private CommitTree commitTree;
    private Branch currBranch;


    /*
     * Initialize the repository
     * All the operations are done in a repository
     */
    public Repository() {

    }

    public void init() {
        if (isGitRepo()) {
            System.out.println(
                    "A Gitlet version-control system already exists in the current directory.");
            return;
        }
        Commit commit = new Commit();
//        this.commitTree = new CommitTree(commit);
        this.currBranch = new Branch("main", commit);
        GITLET_DIR.mkdir();
        COMMITS.mkdir();
        BRANCHES.mkdir();
        File branchFile = join(BRANCHES, this.currBranch.getBranchName());
        File commitFile = join(COMMITS, commit.getId());
        writeObject(commitFile, commit);
        writeObject(branchFile, this.currBranch);
        writeObject(REPOS, this);
    }

    public void add(String filename) {
        File fileToAdd = join(CWD, filename);
        if (!fileToAdd.isFile()) {
            System.out.println("File does not exist.");
            return;
        }
        this.currBranch.addFile(fileToAdd);
        File updateBranch = join(BRANCHES, this.currBranch.getBranchName());
        writeObject(updateBranch, this.currBranch);
        writeObject(REPOS, this);
    }

    public void commit(String message) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        String currBranchName = currBranch.getBranchName();
        if (this.currBranch.getStage().isEmptyStage()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit newCommit = new Commit(message, currBranchName, currBranch.getHeadCommit());
        File updateCommitFile = join(COMMITS, this.currBranch.getHeadCommit().getId());
        File newCommitFile = join(COMMITS, newCommit.getId());
        File updateBranch = join(BRANCHES, currBranchName);
        this.currBranch.commit(newCommit);
        writeObject(updateCommitFile, newCommit.getMainParent());
        writeObject(newCommitFile, newCommit);
        writeObject(updateBranch, this.currBranch);
        writeObject(REPOS, this);
    }

    public void rm(String filename) {
        File fileToRemove = join(CWD, filename);
        if (!this.currBranch.getHeadCommit().isCommitted(filename)
                && !this.currBranch.getStage().getStagedFileMap().containsKey(filename)) {
            System.out.println("No reason to remove the file.");
        }
        this.currBranch.removeFile(fileToRemove);
        File updateBranch = join(BRANCHES, currBranch.getBranchName());
        writeObject(updateBranch, this.currBranch);
        writeObject(REPOS, this);
    }

    public void log() {
        Commit currCommit = this.currBranch.getHeadCommit();
        while (currCommit != null) {
            currCommit.logInfo();
            currCommit = currCommit.getMainParent();
        }
    }

    public void globalLog() {
        List<String> commitList = plainFilenamesIn(COMMITS);
        assert commitList != null;
        for (String commitFile : commitList) {
            File file = join(COMMITS, commitFile);
            Commit commit = readObject(file, Commit.class);
            commit.logInfo();
        }
    }

    public void find(String commitMessage) {
        List<String> commitList = plainFilenamesIn(COMMITS);
        boolean flag = false;
        assert commitList != null;
        for (String commitFile : commitList) {
            File file = join(COMMITS, commitFile);
            Commit commit = readObject(file, Commit.class);
            if (commit.getMessage().equals(commitMessage)) {
                flag = true;
                System.out.println(commit.getId());
            }
        }
        if (!flag) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        System.out.println("=== Branches ===");
        List<String> branchList = plainFilenamesIn(BRANCHES);
        assert branchList != null;
        for (String branchStr : branchList) {
            File file = join(BRANCHES, branchStr);
            Branch branch = readObject(file, Branch.class);
            if (branch.getBranchName().equals(currBranch.getBranchName())) {
                System.out.println("*" + branch.getBranchName());
            } else {
                System.out.println(branch.getBranchName());
            }
        }
        System.out.println();
        Set<String> stageSet = this.currBranch.getStage().getStagedFileMap().keySet();
        Set<String> removeSet = this.currBranch.getStage().getRemovedFileMap().keySet();
        List<String> stageList = new ArrayList<>(stageSet);
        List<String> removeList = new ArrayList<>(removeSet);
        Collections.sort(stageList);
        Collections.sort(removeList);
        System.out.println("=== Staged Files ===");
        for (String filename : stageList) {
            System.out.println(filename);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String filename : removeList) {
            System.out.println(filename);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public void checkoutFile(String filename) {
        if (!this.currBranch.getHeadCommit().isCommitted(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Blob fileBlob = this.currBranch.getHeadCommit().getBlobCommitMap().get(filename);
        File fileToCheckout = join(CWD, fileBlob.getFilename());
        writeContents(fileToCheckout, (Object) fileBlob.getContent());
    }

    public void checkoutCommit(String commitId, String filename) {
        if (commitId.length() != 40) {
            List<String> commitIds = plainFilenamesIn(COMMITS);
            if (commitIds != null) {
                for (String commitStr : commitIds) {
                    if (commitStr.startsWith(commitId)) {
                        commitId = commitStr;
                        break;
                    }
                }
            }

        }
        File commitFile = join(COMMITS, commitId);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = readObject(commitFile, Commit.class);
        if (!commit.isCommitted(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob fileBlob = commit.getBlobCommitMap().get(filename);
        File fileToCheckout = join(CWD, fileBlob.getFilename());
        writeContents(fileToCheckout, (Object) fileBlob.getContent());
    }

    public void checkoutBranch(String branchName) {
        File branchFile = join(BRANCHES, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(this.currBranch.getBranchName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Branch branch = readObject(branchFile, Branch.class);
        Commit currCommit = branch.getHeadCommit();
        checkUntrackedFiles(currCommit);
        overwriteFiles(currCommit);
        this.currBranch = branch;
        this.currBranch.setHeadCommit(currCommit);
        writeObject(join(BRANCHES, branchName), this.currBranch);
        writeObject(REPOS, this);
    }

    public void branch(String branchName) {
        if (join(BRANCHES, branchName).exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branch newBranch = new Branch(branchName, currBranch.getHeadCommit());
        writeObject(join(BRANCHES, branchName), newBranch);
    }

    public void rmBranch(String branchName) {
        if (this.currBranch.getBranchName().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        List<String> branchFileNames = plainFilenamesIn(BRANCHES);
        assert branchFileNames != null;
        for (String branch : branchFileNames) {
            if (branch.equals(branchName)) {
                join(BRANCHES, branchName).delete();
                return;
            }
        }
        System.out.println("A branch with that name does not exist.");
        System.exit(0);
    }

    public void reset(String commitId) {
        File commitFile = join(COMMITS, commitId);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit resetCommit = readObject(join(COMMITS, commitId), Commit.class);
        Branch resetBranch = readObject(join(BRANCHES, resetCommit.getBranchName()), Branch.class);
        checkUntrackedFiles(resetCommit);
        overwriteFiles(resetCommit);
        this.currBranch = resetBranch;
        this.currBranch.clearStage();
        this.currBranch.setHeadCommit(resetCommit);
        writeObject(join(BRANCHES, this.currBranch.getBranchName()), this.currBranch);
        writeObject(REPOS, this);
    }

    public void merge(String otherBranchName) {
        checkMerge(otherBranchName);
        Branch otherBranch = readObject(join(BRANCHES, otherBranchName), Branch.class);
        Commit otherHeadCommit = otherBranch.getHeadCommit();
        Commit currHeadCommit = this.currBranch.getHeadCommit();
        Commit mergeCommit = new Commit(
                "Merged " + otherBranchName + " into " + currBranch.getBranchName() + ".",
                currBranch.getBranchName(), currHeadCommit, otherHeadCommit);
        checkUntrackedFiles(otherHeadCommit);
        Commit lca = findLca(otherBranchName, currHeadCommit, otherHeadCommit);
        boolean encounterConflict = false;
        Map<String, Blob> otherMap = otherHeadCommit.getBlobCommitMap();
        for (Map.Entry<String, Blob> entry : currHeadCommit.getBlobCommitMap().entrySet()) {
            File file = join(CWD, entry.getKey());
            if (otherHeadCommit.isCommitted(entry.getKey())) {
                if (!lca.containSameCommit(entry.getKey(), otherMap.get(entry.getKey()))) {
                    if (lca.containSameCommit(entry.getKey(), entry.getValue())) {
                        checkUntrackedFiles(otherHeadCommit);
                        mergeCommit.commitFile(entry.getKey(), entry.getValue());
                        writeContents(file, (Object) otherMap.get(entry.getKey()).getContent());
                    } else { // content is changed on both branches
                        if (!otherMap.get(entry.getKey()).equals(entry.getValue())) {
                            encounterConflict = true;
                            checkUntrackedFiles(otherHeadCommit);
                            processConflictFile(file, entry.getValue(),
                                    otherMap.get(entry.getKey()));
                        }
                    }
                }
            } else {
                if (lca.containSameCommit(entry.getKey(), entry.getValue())) {
                    rm(entry.getKey());
                } else if (lca.isCommitted(entry.getKey())) {
                    encounterConflict = true;
                    checkUntrackedFiles(otherHeadCommit);
                    processConflictFile(file, entry.getValue(), null);
                } // 4. committed only in the current branch
            }
        }
        for (Map.Entry<String, Blob> entry : otherHeadCommit.getBlobCommitMap().entrySet()) {
            if (!currHeadCommit.isCommitted(entry.getKey()) && !lca.isCommitted(entry.getKey())) {
                File file = join(CWD, entry.getKey());
                writeContents(file, (Object) entry.getValue().getContent());
                this.currBranch.addFile(file);
            }
        }
        if (encounterConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        this.currBranch.setHeadCommit(mergeCommit);
        writeObject(join(BRANCHES, this.currBranch.getBranchName()), this.currBranch);
        writeObject(join(COMMITS, this.currBranch.getHeadCommit().getId()),
                this.currBranch.getHeadCommit());
        writeObject(REPOS, this);
    }

    public void restore() {
        if (!isGitRepo()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        Repository temp = readObject(REPOS, Repository.class);
        this.currBranch = temp.currBranch;
        this.currBranch.setHeadCommit(
                readObject(join(COMMITS, this.currBranch.getHeadCommit().getId()), Commit.class));
    }

    private static boolean isGitRepo() {
        return GITLET_DIR.exists();
    }


    private void checkUntrackedFiles(Commit commit) {
        List<String> files = plainFilenamesIn(CWD);
        Map<String, Blob> map = commit.getBlobCommitMap();
        if (files != null) {
            for (String fileName : files) {
                File file = join(CWD, fileName);
                if (!this.currBranch.getHeadCommit().isCommitted(fileName) && commit.isCommitted(
                        fileName)
                        && !Arrays.equals(readContents(file),
                        map.get(fileName).getContent())) {
                    System.out.println(
                            "There is an untracked file in the way; delete it,"
                                    + " or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }

    private void overwriteFiles(Commit commit) {
        for (Map.Entry<String, Blob> entry : commit.getBlobCommitMap().entrySet()) {
            File file = join(CWD, entry.getKey());
            writeContents(file, (Object) entry.getValue().getContent());
        }
        // Remove the current
        List<String> currBranchFiles = plainFilenamesIn(CWD);
        if (currBranchFiles != null) {
            for (String filename : currBranchFiles) {
                if (!commit.isCommitted(filename)) {
                    File fileToDelete = join(CWD, filename);
                    restrictedDelete(fileToDelete);
                }
            }
        }
    }

    private void processConflictFile(File file, Blob currBlob, Blob givenBlob) {
        final String head = "<<<<<<< HEAD\n";
        final String split = "=======\n";
        final String end = ">>>>>>>";
        byte[] currContent = currBlob != null ? currBlob.getContent() : new byte[]{};
        byte[] givenContent = givenBlob != null ? givenBlob.getContent() : new byte[]{};
        writeContents(file, head, currContent, split, givenContent, end);
    }

    private void checkMerge(String otherBranchName) {
        if (!this.currBranch.getStage().isEmptyStage()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!join(BRANCHES, otherBranchName).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (otherBranchName.equals(this.currBranch.getBranchName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    private Commit findLca(String otherBranchName, Commit currHeadCommit, Commit otherHeadCommit) {
        Commit currCommit = currHeadCommit;
        Commit otherCommit = otherHeadCommit;
        int otherDepth = otherCommit.getNodeDepth();
        int currDepth = currCommit.getNodeDepth();

        for (int i = otherDepth; i > currDepth; i--) {
            otherCommit = otherCommit.getMainParent();
        }
        if (otherCommit.equals(currCommit)) {
            checkoutBranch(otherBranchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        for (int i = currDepth; i > otherDepth; i--) {
            currCommit = currCommit.getMainParent();
        }
        if (otherCommit.equals(currCommit)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        while (otherCommit != null && currCommit != null && !otherCommit.equals(currCommit)) {
            otherCommit = otherCommit.getMainParent();
            currCommit = currCommit.getMainParent();
        }
        Commit lca = null;
        if (otherCommit != null && otherCommit.equals(currCommit)) {
            lca = currCommit;
        }
        if (lca == null) {
            System.out.println("Bug: Fail to find the common ancestor");
        }
        return lca;
    }
}
