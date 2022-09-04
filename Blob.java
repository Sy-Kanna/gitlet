package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String filename;
    private byte[] content;

    private String id;

    public Blob(File file) {
        filename = file.getName();
        content = readContents(file);
        id = sha1(filename, content);
    }

    public byte[] getContent() {
        return content;
    }

    public String getFilename() {
        return filename;
    }

    public String getId() {
        return id;
    }

    public boolean equals(Blob other) {
        return Arrays.equals(this.content, other.content);
    }
}
