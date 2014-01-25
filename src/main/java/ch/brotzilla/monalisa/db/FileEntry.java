package ch.brotzilla.monalisa.db;

public class FileEntry {

    private final String id, originalName;
    private final byte[] data;

    public FileEntry(String id, String originalName, byte[] data) {
        this.id = id;
        this.originalName = originalName;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public byte[] getData() {
        return data;
    }
}