package ch.brotzilla.monalisa.evolution.genes;

import java.util.zip.CRC32;

import com.google.common.base.Preconditions;

public class RawGenome {
    
    public byte[] head;
    public byte[][] genes;
    public int[] crcs;
    public int[] indexes;
    
    public RawGenome(byte[] head, byte[][] genes) {
        Preconditions.checkNotNull(head, "The parameter 'head' must not be null");
        Preconditions.checkNotNull(genes, "The parameter 'genes' must not be null");
        this.head = head;
        this.genes = genes;
    }
    
    public void computeCRCs() {
        if (genes != null & crcs == null) {
            final int[] result = new int[genes.length];
            final CRC32 crc = new CRC32();
            for (int i = 0; i < genes.length; i++) {
                crc.reset();
                crc.update(genes[i]);
                result[i] = (int) (crc.getValue() & 0xFFFFFFFF);
            }
            this.crcs = result;
        }
    }
}