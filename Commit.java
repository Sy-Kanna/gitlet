package gitlet;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet commit object.
 * It's a good idea to give a description here of what else this Class
 * does at a high level.
 *
 * @author Kanna Shan
 */
public class Commit implements Serializable {
    /**
     * add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private final String message;
    private final String id;
    private final Date commitTime;
    private Map<String, Blob> blobCommitMap; // filename -> blob

    // A branch can have at most two parents (merges that referencing two parents)
    // and a list of commit children
    private Commit mainParent;
    private Commit branchParent;
    private List<Commit> children;
    private String branchName;

    public Commit() {
        this.message = "initial commit";
        this.commitTime = new Date(0);
        this.blobCommitMap = new HashMap<>();
        this.branchName = "main";
        this.children = new ArrayList<>();
        this.mainParent = null;
        this.branchParent = null;
        this.id = sha1((Object) serialize(this));
    }

    // Common commit initializer
    public Commit(String message, String branchName, Commit parent) {
        this.message = message;
        this.commitTime = new Date();
        this.blobCommitMap = new HashMap<>(parent.blobCommitMap);
        this.branchName = branchName;
        this.children = new ArrayList<>();
        this.mainParent = parent;
        this.branchParent = null;
        this.id = sha1((Object) serialize(this));
    }

    // merge commit initializer
    public Commit(String message, String branchName, Commit parent, Commit branchCommit) {
        this.message = message;
        this.commitTime = new Date();
        this.blobCommitMap = new HashMap<>(parent.blobCommitMap);
        this.branchName = branchName;
        this.children = new ArrayList<>();
        this.mainParent = parent;
        this.branchParent = branchCommit;
        this.id = sha1((Object) serialize(this));
    }

    public void addChild(Commit childCommit) {
        children.add(childCommit);
    }

    public void logInfo() {
        System.out.println("===");
        System.out.println("commit " + this.id);
        System.out.println("Date: " + dateToString(this.commitTime));
        System.out.println(this.message);
        System.out.println();
    }

    public boolean isCommitted(String filename) {
        return blobCommitMap.containsKey(filename);
    }

    public boolean containSameCommit(String filename, Blob fileBlob) {
        if (isCommitted(filename)) {
            Blob blob = blobCommitMap.get(filename);
            return blob.equals(fileBlob);
        }
        return false;
    }

    public void commitStage(Stage stage) {
        Map<String, Blob> stageMap = stage.getStagedFileMap();
        Map<String, Blob> removeMap = stage.getRemovedFileMap();
//        this.blobCommitMap = new HashMap<>(mainParent.blobCommitMap);
        this.blobCommitMap.entrySet().removeIf(entry -> removeMap.containsKey(entry.getKey()));
        this.blobCommitMap.putAll(stageMap);
        stage.clearStage();
    }

    public String getId() {
        return id;
    }

    private String dateToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return format.format(date);
    }

    public Commit getMainParent() {
        return mainParent;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Blob> getBlobCommitMap() {
        return blobCommitMap;
    }

    public String getBranchName() {
        return branchName;
    }

    public int getNodeDepth() {
        int depth = 0;
        Commit current = this;
        while (current != null) {
            current = current.mainParent;
            depth++;
        }
        return depth;
    }

    public void commitFile(String fileName, Blob fileBlob) {
        this.blobCommitMap.put(fileName, fileBlob);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Commit commit = (Commit) o;
        return id.equals(commit.id);
    }
}
