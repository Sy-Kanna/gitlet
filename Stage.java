package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Stage implements Serializable {

    private Map<String, Blob> removedFileMap;
    private Map<String, Blob> stagedFileMap;

    public Stage() {
        stagedFileMap = new HashMap<>();
        removedFileMap = new HashMap<>();
    }

    public void stageFile(Blob file) {
        stagedFileMap.put(file.getFilename(), file);
    }

    public void removeFromStage(File fileToRemove) {
        stagedFileMap.remove(fileToRemove.getName());
    }

    public void removeFromRemoval(File fileToRemove) {
        removedFileMap.remove(fileToRemove.getName());
    }

    public void stageRemoval(File fileToRemove) {
        removedFileMap.put(fileToRemove.getName(), new Blob(fileToRemove));
    }

    public void clearStage() {
        stagedFileMap.clear();
        removedFileMap.clear();
    }

    public Map<String, Blob> getRemovedFileMap() {
        return removedFileMap;
    }

    public Map<String, Blob> getStagedFileMap() {
        return stagedFileMap;
    }

    public boolean isEmptyStage() {
        return stagedFileMap.isEmpty() && removedFileMap.isEmpty();
    }
}
