package ch.brotzilla.monalisa.db;

import com.google.common.base.Preconditions;

public class GeneEntry {

    private final int id, crc;
    private final byte[] data;

    public GeneEntry(int id, int crc, byte[] data) {
        Preconditions.checkArgument(id > 0, "The parameter 'id' has to be greater than zero");
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        this.id = id;
        this.crc = crc;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public int getCrc() {
        return crc;
    }

    public byte[] getData() {
        return data;
    }
}