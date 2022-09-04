package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Branch implements Serializable {
    private String branchName;
    private Commit headCommit;
    private Stage stage;

    public Branch(String name, Commit commit) {
        this.branchName = name;
        this.headCommit = commit;
        this.stage = new Stage();
    }

    public void addFile(File fileToAdd) {
        Blob blob = new Blob(fileToAdd);
        if (stage.getRemovedFileMap().containsKey(fileToAdd.getName())) {
            stage.removeFromRemoval(fileToAdd);
        }
        // check whether the file is changed to the original one
        if (!headCommit.containSameCommit(fileToAdd.getName(), blob)) {
            stage.stageFile(blob);
        } else {
            stage.removeFromStage(fileToAdd);
        }
    }

    public void removeFile(File fileToRemove) {
        stage.removeFromStage(fileToRemove);
        if (headCommit.isCommitted(fileToRemove.getName())) {
            stage.stageRemoval(fileToRemove);
            restrictedDelete(fileToRemove);
        }
    }

    public void commit(Commit newCommit) {
        headCommit.addChild(newCommit);
        headCommit = newCommit;
        headCommit.commitStage(stage);
    }


    public String getBranchName() {
        return branchName;
    }

    public Stage getStage() {
        return stage;
    }

    public Commit getHeadCommit() {
        return headCommit;
    }

    public void setHeadCommit(Commit headCommit) {
        this.headCommit = headCommit;
    }

    public void clearStage() {
        stage.clearStage();
    }
}
