package ch.brotzilla.monalisa.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import ch.brotzilla.monalisa.db.Database;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class OldFormatConverter2 {

    public static final String FileExtension = ".mldb.zip";

    protected final File dbFile, output;

    protected static String autoName(File dbFile) {
        String name = dbFile.getName();
        final int last = name.lastIndexOf('.');
        if (last > 0) {
            name = name.substring(0, last);
        }
        return name + FileExtension;
    }

    protected static void loadDatabase(Database db, List<byte[]> genomes, List<Database.FileEntry> fileEntries) throws IOException, SQLiteException {
        System.out.println("Loading database...");
        db.queryAllGenomesCompressed(genomes);
        db.queryAllFiles(fileEntries);
        System.out.println("Loaded " + genomes.size() + " genomes.");
        System.out.println("Loaded " + fileEntries.size() + " files.");
    }

    protected static int processGenes(GenomeEntry rawGenome, Multimap<Integer, IndexEntry> geneMap) {
        int collisions = 0;
        rawGenome.entries = new IndexEntry[rawGenome.genes.length];
        outer: for (int i = 0; i < rawGenome.genes.length; i++) {
            final int crc = rawGenome.crcs[i];
            final byte[] gene = rawGenome.genes[i];
            final Collection<IndexEntry> knowns = geneMap.get(crc);
            for (final IndexEntry known : knowns) {
                if (Utils.equals(gene, known.gene)) {
                    rawGenome.entries[i] = known;
                    continue outer;
                }
            }
            final IndexEntry e = new IndexEntry(crc, gene);
            rawGenome.entries[i] = e;
            knowns.add(e);
            if (knowns.size() > 1) {
                ++collisions;
            }
        }
        return collisions;
    }

    protected static void computeGeneMap(List<byte[]> rawData, GenomeEntry[] genomeEntries, Multimap<Integer, IndexEntry> geneMap) throws IOException {
        System.out.println("Decoding genomes, merging genes...");
        final int steps = rawData.size() / 20 > 0 ? rawData.size() / 20 : 1;
        int collisions = 0, totalGenes = 0;
        for (int i = 0; i < rawData.size(); i++) {
            final byte[] rawGenome = rawData.get(i);
            final GenomeEntry genomeEntry = decodeRawGenome(rawGenome);
            totalGenes += genomeEntry.genes.length;
            genomeEntries[i] = genomeEntry;
            genomeEntry.computeCRCs();
            collisions += processGenes(genomeEntry, geneMap);
            genomeEntry.genes = null; // free some space
            genomeEntry.crcs = null; // free some more space
            if (i > 0 && i % steps == 0) {
                System.out.print(Math.round((100.0 / rawData.size() * i) * 10) / 10 + "% ");
            }
        }
        if ((rawData.size() - 1) % steps != 0) {
            System.out.println("100%");
        } else {
            System.out.println();
        }
        System.out.println("Found " + geneMap.size() + " distinct genes out of " + totalGenes + ". (Got " + collisions + " crc collisions)");
        System.out.println("");
    }

    protected static IndexEntry[] computeSortedIndex(Multimap<Integer, IndexEntry> geneMap) {
        final int size = geneMap.size();
        final IndexEntry[] result = geneMap.values().toArray(new IndexEntry[size]);
        Arrays.sort(result);
        for (int i = 0; i < result.length; i++) {
            result[i].index = i + 1;
        }
        return result;
    }

    protected static void readBytes(DataInputStream in, byte[] dst, int offset, int length) throws IOException {
        int read = 0;
        while (read < length) {
            int current = in.read(dst, offset + read, length - read);
            if (current == -1) {
                throw new IOException("Unable to deserialize gene, end of file reached");
            }
            read += current;
        }
    }

    protected static byte[] deserializeRawGene(DataInputStream in) throws IOException {
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

    protected static GenomeEntry deserializeRawGenome(DataInputStream in) throws IOException {
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
        return new GenomeEntry(head, genes);
    }

    protected static GenomeEntry decodeRawGenome(byte[] rawData) throws IOException {
        if (rawData == null || rawData.length == 0)
            return null;

        return deserializeRawGenome(Compression.din(rawData));
    }

    protected static class IndexEntry implements Comparable<IndexEntry> {

        public int index;

        public final int crc;
        public final byte[] gene;

        public IndexEntry(int crc, byte[] gene) {
            this.crc = crc;
            this.gene = gene;
        }

        @Override
        public int compareTo(IndexEntry o) {
            if (this == o)
                return 0;
            if (o == null || gene.length > o.gene.length)
                return 1;
            if (gene.length < o.gene.length)
                return -1;
            final byte[] tg = this.gene, og = o.gene;
            for (int i = 0; i < tg.length; i++) {
                int tb = tg[i] & 0xFF, ob = og[i] & 0xFF;
                if (tb == ob)
                    continue;
                if (tb > ob)
                    return 1;
                return -1;
            }
            return 0;
        }
    }

    protected static class GenomeEntry {

        public byte[] head;
        public byte[][] genes;
        public int[] crcs;
        public IndexEntry[] entries;

        public GenomeEntry(byte[] head, byte[][] genes) {
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

        public void serialize(OutputStream output) throws IOException {
            output.write(head);
            final DataOutputStream dout = new DataOutputStream(output);
            for (final IndexEntry e : entries) {
                dout.writeInt(e.index);
            }
        }
    }

    protected static class DBSerializer {

        private final IndexEntry[] index;
        private final GenomeEntry[] genomes;

        private IndexEntry[][] getChunks() {
            final List<IndexEntry[]> chunks = Lists.newArrayList();
            final List<IndexEntry> chunk = Lists.newArrayList();
            int size = 0;
            for (int i = 0; i < index.length; i++) {
                final IndexEntry e = index[i];
                if (e.gene.length > size) {
                    if (chunk.size() > 0) {
                        chunks.add(chunk.toArray(new IndexEntry[chunk.size()]));
                        chunk.clear();
                    }
                    size = e.gene.length;
                }
            }
            if (chunk.size() > 0) {
                chunks.add(chunk.toArray(new IndexEntry[chunk.size()]));
            }
            return chunks.toArray(new IndexEntry[chunks.size()][]);
        }

        public DBSerializer(IndexEntry[] index, GenomeEntry[] genomes) {
            this.index = index;
            this.genomes = genomes;
        }

        public void serialize(DataOutputStream output) throws IOException {
            Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
            final IndexEntry[][] chunks = getChunks();
            output.writeInt(0); // version
            output.writeInt(chunks.length); // number of chunks
            for (final IndexEntry[] chunk : chunks) {
                output.writeInt(chunk.length); // number of entries in chunk
                output.writeInt(chunk[0].gene.length); // size of each entry
                for (final IndexEntry e : chunk) {
                    output.write(e.gene);
                }
            }
            output.writeInt(genomes.length); // number of genomes
            for (final GenomeEntry genome : genomes) {
                output.write(genome.head); // header of the genome
                for (final IndexEntry e : genome.entries) {
                    output.writeInt(e.index);
                }
            }
        }

        public void serialize(File output) throws IOException {
            try (final FileOutputStream fout = new FileOutputStream(output)) {
                final BufferedOutputStream bout = new BufferedOutputStream(fout);
                final GZIPOutputStream gzout = new GZIPOutputStream(bout);
                final DataOutputStream dout = new DataOutputStream(gzout);
                serialize(dout);
                dout.flush();
                gzout.finish();
                gzout.flush();
                bout.flush();
            }
        }
    }

    protected static class ZipFileSerializer {

        private final IndexEntry[] index;
        private final GenomeEntry[] genomes;
        private final Database.FileEntry[] files;

        public ZipFileSerializer(IndexEntry[] index, GenomeEntry[] genomes, Database.FileEntry[] files) {
            this.index = index;
            this.genomes = genomes;
            this.files = files;
        }

        public void serialize(File output) throws ZipException, IOException {
            try (final FileOutputStream fout = new FileOutputStream(output)) {
                try (final ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout, 10 * 1024 * 1024))) {
                    final long time = System.currentTimeMillis();
                    zout.setLevel(9);
                    for (final Database.FileEntry fe : files) {
                        final ZipEntry ze = new ZipEntry("files/" + fe.id);
                        zout.putNextEntry(ze);
                        zout.write(fe.decode());
                        zout.closeEntry();
                    }
                    for (final IndexEntry ie : index) {
                        final ZipEntry ze = new ZipEntry("genes/" + ie.index);
                        zout.putNextEntry(ze);
                        zout.write(ie.gene);
                        zout.closeEntry();
                    }
                    int i = 0;
                    for (final GenomeEntry ge : genomes) {
                        final ZipEntry ze = new ZipEntry("genomes/" + i++);
                        zout.putNextEntry(ze);
                        ge.serialize(zout);
                        zout.closeEntry();
                    }
                    System.out.println("Time: " + (System.currentTimeMillis() - time) + " ms");
                }
            }
        }

    }

    public OldFormatConverter2(File dbFile, File output, boolean autoName) {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        Preconditions.checkArgument(dbFile.isFile(), "The parameter 'dbFile' has to be a regular file (" + dbFile + ")");
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        if (autoName) {
            output = new File(output, autoName(dbFile));
        }
        if (output.exists()) {
            if (output.isFile()) {
                output.delete();
            } else {
                throw new IllegalArgumentException("The parameter 'output' is invalid");
            }
        }
        this.dbFile = dbFile;
        this.output = output;
    }

    public File getDBFile() {
        return dbFile;
    }

    public File getOutputFile() {
        return output;
    }

    public void serialize() throws IOException, SQLiteException {

        final List<byte[]> rawData = Lists.newArrayList();
        final List<Database.FileEntry> fileEntries = Lists.newArrayList();

        try (final Database db = Database.openDatabase(dbFile)) {
            loadDatabase(db, rawData, fileEntries);
        }

        final GenomeEntry[] genomeEntries = new GenomeEntry[rawData.size()];
        final Multimap<Integer, IndexEntry> geneMap = ArrayListMultimap.create();

        computeGeneMap(rawData, genomeEntries, geneMap);

        rawData.clear(); // free some space

        final IndexEntry[] geneEntries = computeSortedIndex(geneMap);

        System.out.println("Writing file '" + output + "'...");
        final ZipFileSerializer serializer = new ZipFileSerializer(geneEntries, genomeEntries, fileEntries.toArray(new Database.FileEntry[fileEntries.size()]));
        serializer.serialize(output);
        System.out.println("Finished.");
    }

    public static void main(String[] args) throws IOException, SQLiteException {
        final File output = new File("./data/output/");
        final File[] dbs = output.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().equals("crab.mldb");
            }
        });
        for (final File db : dbs) {
            final OldFormatConverter2 c = new OldFormatConverter2(db, output, true);
            c.serialize();
        }
    }

    private static void dumpPlainText(File output, IndexEntry[] index, GenomeEntry[] rawGenomes) throws IOException {
        output = new File(output.toString() + ".dump");
        System.out.println("Dumping index to '" + output + "'...");
        try (BufferedWriter w = new BufferedWriter(new FileWriter(output))) {
            for (final IndexEntry e : index) {
                w.write(num(e.index, 6) + ": " + hex(e.crc) + " / " + num(e.gene.length, 4) + " = " + hex(e.gene) + "\n");
            }
            for (final GenomeEntry g : rawGenomes) {
                w.write("head = " + hex(g.head) + ", genes = " + index(g.entries) + "\n");
            }
            w.flush();
        }
        System.out.println("Finished!");
    }

    private static String num(int i, int n) {
        String r = Integer.toString(i);
        while (r.length() < n) {
            r = "0" + r;
        }
        return r;
    }

    private static String hex(int i) {
        String r = Long.toHexString(i & 0xFFFFFFFFL);
        while (r.length() < 8) {
            r = "0" + r;
        }
        return r;
    }

    private static String hex(byte[] v) {
        final StringBuilder b = new StringBuilder(v.length * 2);
        for (final byte d : v) {
            final String s = Integer.toHexString((int) (d & 0xFF));
            if (s.length() == 1) {
                b.append("0" + s);
            } else {
                b.append(s);
            }
        }
        return b.toString();
    }

    private static String index(IndexEntry[] entries) {
        final StringBuilder b = new StringBuilder(entries.length * 8);
        for (final IndexEntry e : entries) {
            String s = Long.toHexString(e.index & 0xFFFFFFFFL);
            if (s.length() > 6) {
                throw new IllegalStateException("Internal error: index too large");
            }
            while (s.length() < 6) {
                s = "0" + s;
            }
            b.append(s);
        }
        return b.toString();
    }

}
