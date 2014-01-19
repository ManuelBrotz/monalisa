package ch.brotzilla.monalisa.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.zip.CRC32;

import ch.brotzilla.monalisa.db.Database;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class DBCompressor {

    public static final String FileExtension = ".mlc";

    private final File dbFile, output;

    private String autoName(File dbFile) {
        String name = dbFile.getName();
        final int last = name.lastIndexOf('.');
        if (last > 0) {
            name = name.substring(0, last);
        }
        return name + FileExtension;
    }

    private List<byte[]> loadGenomes() throws IOException, SQLiteException {
        try (final Database db = Database.openDatabase(dbFile)) {
            List<byte[]> result = Lists.newArrayList();
            System.out.println("Loading genomes...");
            db.queryAllGenomesCompressed(result);
            System.out.println("Loaded " + result.size() + " genomes.");
            return result;
        }
    }
    
    private int processGenes(RawGenome genome, Multimap<Integer, Entry> genes) {
        int collisions = 0;
        genome.indexes = new int[genome.genes.length];
        outer: for (int i = 0; i < genome.genes.length; i++) {
            final int crc = genome.crcs[i];
            final byte[] gene = genome.genes[i];
            final Collection<Entry> knowns = genes.get(crc);
            for (final Entry known : knowns) {
                if (Utils.equals(gene, known.gene)) {
                    genome.indexes[i] = known.index;
                    continue outer;
                }
            }
            final Entry e = new Entry(crc, gene);
            genome.indexes[i] = e.index;
            knowns.add(e);
            if (knowns.size() > 1) {
                ++collisions;
            }
        }
        return collisions;
    }
    
    private void processGenomes(List<byte[]> input, List<RawGenome> genomes, Multimap<Integer, Entry> genes) throws IOException {
        System.out.println("Decoding genomes, merging genes...");
        final int steps = input.size() / 20;
        int collisions = 0, totalGenes = 0;
        for (int i = 0; i < input.size(); i++) {
            final byte[] rawGenome = input.get(i);
            final RawGenome genome = decodeRawGenome(rawGenome);
            totalGenes += genome.genes.length;
            genomes.add(genome);
            genome.computeCRCs();
            collisions += processGenes(genome, genes);
            genome.genes = null; // free some space 
            if (i > 0 && i % steps == 0) {
                System.out.print(Math.round((100.0 / input.size() * i) * 10) / 10 + "% ");
            }
        }
        if ((input.size() - 1) % steps != 0) {
            System.out.println("100%");
        } else {
            System.out.println();
        }
        System.out.println("Found " + genes.size() + " distinct genes out of " + totalGenes + ". (Got " + collisions + " crc collisions)");
        System.out.println("");
    }
    
    private static void readBytes(DataInputStream in, byte[] dst, int offset, int length) throws IOException {
        int read = 0;
        while (read < length) {
            int current = in.read(dst, offset + read, length - read);
            if (current == -1) {
                throw new IOException("Unable to deserialize gene, end of file reached");
            }
            read += current;
        }
    }

    public static byte[] deserializeRawGene(DataInputStream in) throws IOException {
        Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
        final byte[] head = new byte[6];
        readBytes(in, head, 0, head.length);
        final byte version = head[0];
        Preconditions.checkArgument(version == 0, "Unable to deserialize gene, version not supported");
        final int length = head[5] & 0xFF;
        Preconditions.checkArgument(length >= 3, "Unable to deserialize gene, too few coordinates");
        final byte[] result = new byte[head.length + (length * 4)];
        System.arraycopy(head, 0, result, 0, head.length);
        readBytes(in, result, head.length, length * 4);
        return result;
    }
    
    public static RawGenome deserializeRawGenome(DataInputStream in) throws IOException {
        Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
        final int hlen = 29;
        final byte[] head = new byte[hlen];
        readBytes(in, head, 0, head.length);
        final byte version = head[0];
        Preconditions.checkState(version == 0, "Unable to deserialize genome, version not supported");
        final int length = ((head[hlen - 4] & 0xFF) << 24) | ((head[hlen - 3] & 0xFF) << 16) | ((head[hlen - 2] & 0xFF) << 8) | (head[hlen - 1] & 0xFF);
        Preconditions.checkState(length > 0, "Unable to deserialize genome, too few genes");
        final byte[][] genes = new byte[length][];
        for (int i = 0; i < length; i++) {
                genes[i] = deserializeRawGene(in);
        }
        return new RawGenome(head, genes);
    }

    public static RawGenome decodeRawGenome(byte[] input) throws IOException {
        if (input == null || input.length == 0) 
            return null;
        
        return deserializeRawGenome(Compression.din(input));
    }
    
    private static class Entry {
        
        private static int nextIndex = 0;
        
        public final int index, crc;
        public final byte[] gene;
        
        public Entry(int crc, byte[] gene) {
            this.index = nextIndex++;
            this.crc = crc;
            this.gene = gene;
        }
    }
    
    private static class RawGenome {
        
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

    public DBCompressor(File dbFile, File output, boolean autoName) {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        Preconditions.checkArgument(dbFile.isFile(), "The parameter 'dbFile' has to be a regular file (" + dbFile + ")");
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        output = new File(output, autoName(dbFile));
        Preconditions.checkArgument(!output.exists(), "The parameter 'output' already exists (" + output + ")");
        this.dbFile = dbFile;
        this.output = output;
    }

    public File getDBFile() {
        return dbFile;
    }

    public File getOutputFile() {
        return output;
    }

    public void compress() throws IOException, SQLiteException {
        try (final Database db = Database.openDatabase(dbFile)) {

            final List<byte[]> rawGenomes = loadGenomes();

            final List<RawGenome> genomes = Lists.newArrayListWithCapacity(rawGenomes.size());
            final Multimap<Integer, Entry> genes = ArrayListMultimap.create();

            processGenomes(rawGenomes, genomes, genes);
            
            rawGenomes.clear(); // free some space
        }
    }

    public static void main(String[] args) throws IOException, SQLiteException {
        final DBCompressor c = new DBCompressor(new File("./data/output/vaduz.mldb"), new File("./data/output/"), true);
        c.compress();
    }

}
