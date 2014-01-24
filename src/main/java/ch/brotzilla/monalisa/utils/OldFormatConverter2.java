package ch.brotzilla.monalisa.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import ch.brotzilla.monalisa.db.Database;
import ch.brotzilla.monalisa.db.Database.FileEntry;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class OldFormatConverter2 {

    protected final File dbFile, output;

    protected static String autoName(File dbFile, String fileExtension) {
        String name = dbFile.getName();
        final int last = name.lastIndexOf('.');
        if (last > 0) {
            name = name.substring(0, last);
        }
        return name + fileExtension;
    }

    protected static void loadDatabase(Database db, List<byte[]> genomes, List<Database.FileEntry> fileEntries) throws IOException, SQLiteException {
        System.out.println("Loading database '" + db.getDatabaseFile().getName() + "'...");
        db.queryAllGenomesCompressed(genomes);
        db.queryAllFiles(fileEntries);
        System.out.println("Loaded " + genomes.size() + " genomes.");
        System.out.println("Loaded " + fileEntries.size() + " files.");
    }

    protected static Multimap<Integer, IndexEntry> mergeGenes(GenomeEntry[] genomeEntries) throws IOException {

        final Multimap<Integer, IndexEntry> geneMap = ArrayListMultimap.create();

        System.out.print("Merging genes... ");

        final int size = genomeEntries.length;
        final Progress p = new Progress(size, 20);
        int collisions = 0, totalGenes = 0;

        for (final GenomeEntry genomeEntry : genomeEntries) {
            totalGenes += genomeEntry.genes.length;
            collisions += genomeEntry.mergeGenes(geneMap);
            p.step();
        }

        p.finish();
        System.out.println("Found " + geneMap.size() + " distinct genes out of " + totalGenes + ". (Got " + collisions + " crc collisions)");

        return geneMap;
    }

    protected static IndexEntry[] computeSortedIndex(Multimap<Integer, IndexEntry> geneMap) {
        final int size = geneMap.size();
        final IndexEntry[] result = geneMap.values().toArray(new IndexEntry[size]);
        System.out.println("Sorting index...");
        Arrays.sort(result);
        for (int i = 0; i < size; i++) {
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

    protected static GeneEntry deserializeRawGene(DataInputStream in) throws IOException {
        Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
        final byte version = in.readByte();
        Preconditions.checkArgument(version == 0, "Unable to deserialize gene, version not supported");
        final int color = in.readInt();
        final int length = in.readByte() & 0xFF;
        Preconditions.checkArgument(length >= 3, "Unable to deserialize gene, too few coordinates");
        final byte[] coords = new byte[length * 4];
        readBytes(in, coords, 0, length * 4);
        return new GeneEntry(color, coords);
    }

    protected static GenomeEntry deserializeRawGenome(DataInputStream in) throws IOException {
        Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
        final byte version = in.readByte();
        Preconditions.checkState(version == 0, "Unable to deserialize genome, version not supported");
        final int background = in.readInt();
        final double fitness = in.readDouble();
        final int numberOfImprovements = in.readInt();
        final int numberOfMutations = in.readInt();
        in.readInt(); // unused
        final int length = in.readInt();
        Preconditions.checkState(length > 0, "Unable to deserialize genome, too few genes");
        final GeneEntry[] genes = new GeneEntry[length];
        for (int i = 0; i < length; i++) {
            genes[i] = deserializeRawGene(in);
        }
        return new GenomeEntry(background, fitness, numberOfImprovements, numberOfMutations, genes);
    }

    protected static GenomeEntry decodeRawGenome(byte[] rawGenome) throws IOException {
        final ByteArrayInputStream bin = new ByteArrayInputStream(rawGenome);
        final GZIPInputStream gzin = new GZIPInputStream(bin);
        final DataInputStream din = new DataInputStream(gzin);
        return deserializeRawGenome(din);
    }

    protected static GenomeEntry[] decodeRawGenomes(List<byte[]> rawGenomes) throws IOException {
        final GenomeEntry[] result = new GenomeEntry[rawGenomes.size()];
        final Progress p = new Progress(rawGenomes.size(), 20);
        System.out.print("Decoding genomes... ");
        for (int i = 0; i < rawGenomes.size(); i++) {
            result[i] = decodeRawGenome(rawGenomes.get(i));
            p.step();
        }
        p.finish();
        return result;
    }
    
    protected static class Progress {
        
        private final int size, frac;
        private int pos = 0;
        
        public Progress(int size, int steps) {
            this.size = size;
            this.frac = Math.max(1, steps < size ? size / steps : 1);
        }
        
        public void step() {
            if (pos >= size) {
                return;
            }
            if (pos > 0 && pos % frac == 0) {
                System.out.print(Math.round((100.0 / size * pos) * 10) / 10 + "% ");
            }
            ++pos;
        }
        
        public void finish() {
            if ((size - 1) % frac != 0) {
                System.out.println("100%");
            } else {
                System.out.println();
            }
        }
    }

    protected static class IndexEntry implements Comparable<IndexEntry> {

        public int index;

        public final int crc;
        public final GeneEntry gene;

        public IndexEntry(int crc, GeneEntry gene) {
            this.crc = crc;
            this.gene = gene;
        }

        @Override
        public int compareTo(IndexEntry o) {
            if (this == o)
                return 0;
            if (gene.coords.length > o.gene.coords.length)
                return 1;
            if (gene.coords.length < o.gene.coords.length)
                return -1;
            try {
                final byte[] tc = this.gene.serialize(), oc = o.gene.serialize();
                for (int i = 0; i < tc.length; i++) {
                    byte tb = tc[i], ob = oc[i];
                    if (tb == ob)
                        continue;
                    if (tb > ob)
                        return 1;
                    return -1;
                }
                if (gene.color > o.gene.color)
                    return 1;
                if (gene.color < o.gene.color)
                    return -1;
                return 0;
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    protected static class GeneEntry {

        public final int color;
        public final byte[] coords;

        private byte[] serialized = null, compressed = null;

        public GeneEntry(int color, byte[] coords) {
            Preconditions.checkNotNull(coords, "The parameter 'coords' must not be null");
            Preconditions.checkArgument(coords.length % 4 == 0, "The length of the parameter 'coords' has to be a multiple of 4");
            this.color = color;
            this.coords = coords;
        }

        public void serialize(DataOutputStream dout) throws IOException {
            dout.writeByte(1);
            dout.writeInt(color);
            dout.writeByte(coords.length / 4);
            dout.write(coords);
        }

        public byte[] serialize() throws IOException {
            if (serialized != null) {
                return serialized;
            }
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final DataOutputStream dout = new DataOutputStream(bout);
            serialize(dout);
            dout.flush();
            serialized = bout.toByteArray();
            return serialized;
        }

        public byte[] compress() throws IOException {
            if (compressed != null) {
                return compressed;
            }
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final GZIPOutputStream gzout = new GZIPOutputStream(bout);
            final DataOutputStream dout = new DataOutputStream(gzout);
            serialize(dout);
            gzout.finish();
            gzout.flush();
            compressed = bout.toByteArray();
            return compressed;
        }
    }

    protected static class GenomeEntry {

        public final int background;
        public final double fitness;
        public final int numberOfImprovements;
        public final int numberOfMutations;

        public final IndexEntry[] entries;

        private GeneEntry[] genes;
        private byte[] serialized = null;

        private int[] computeCRCs() throws IOException {
            final int[] result = new int[genes.length];
            final CRC32 crc = new CRC32();
            for (int i = 0; i < genes.length; i++) {
                crc.reset();
                crc.update(genes[i].serialize());
                result[i] = (int) (crc.getValue() & 0xFFFFFFFF);
            }
            return result;
        }

        public GenomeEntry(int background, double fitness, int numberOfImprovements, int numberOfMutations, GeneEntry[] genes) {
            Preconditions.checkNotNull(genes, "The parameter 'genes' must not be null");
            this.background = background;
            this.fitness = fitness;
            this.numberOfImprovements = numberOfImprovements;
            this.numberOfMutations = numberOfMutations;
            this.genes = genes;
            this.entries = new IndexEntry[genes.length];
        }

        public int mergeGenes(Multimap<Integer, IndexEntry> geneMap) throws IOException {
            if (genes == null) {
                throw new IllegalStateException("Cannot merge genes");
            }
            final int[] crcs = computeCRCs();
            int collisions = 0;
            outer: for (int i = 0; i < genes.length; i++) {
                final int crc = crcs[i];
                final GeneEntry gene = genes[i];
                final Collection<IndexEntry> knowns = geneMap.get(crc);
                for (final IndexEntry known : knowns) {
                    if (Utils.equals(gene.serialize(), known.gene.serialize())) {
                        entries[i] = known;
                        continue outer;
                    }
                }
                final IndexEntry e = new IndexEntry(crc, gene);
                entries[i] = e;
                knowns.add(e);
                if (knowns.size() > 1) {
                    ++collisions;
                }
            }
            return collisions;
        }

        public void serialize(DataOutputStream dout) throws IOException {
            dout.writeByte(1);
            dout.writeInt(background);
            dout.writeDouble(fitness);
            dout.writeInt(numberOfImprovements);
            dout.writeInt(numberOfMutations);
            dout.writeInt(entries.length);
            for (final IndexEntry e : entries) {
                dout.writeInt(e.index);
            }
        }

        public byte[] serialize() throws IOException {
            if (serialized != null) {
                return serialized;
            }
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final DataOutputStream dout = new DataOutputStream(bout);
            serialize(dout);
            dout.flush();
            serialized = bout.toByteArray();
            return serialized;
        }
    }

    protected static abstract class Serializer {

        private final IndexEntry[] index;
        private final GenomeEntry[] genomes;
        private final Database.FileEntry[] files;

        public Serializer(IndexEntry[] index, GenomeEntry[] genomes, Database.FileEntry[] files) {
            this.index = index;
            this.genomes = genomes;
            this.files = files;
        }

        public IndexEntry[] getIndex() {
            return index;
        }

        public GenomeEntry[] getGenomes() {
            return genomes;
        }

        public Database.FileEntry[] getFiles() {
            return files;
        }

        public abstract String getFileExtension();

        public abstract void serialize(File output) throws Exception;
    }

    protected static class FlatFileSerializer extends Serializer {

        private IndexEntry[][] getChunks() throws IOException {
            final List<IndexEntry[]> chunks = Lists.newArrayList();
            final List<IndexEntry> chunk = Lists.newArrayList();
            final IndexEntry[] index = getIndex();
            int size = 0;
            for (int i = 0; i < index.length; i++) {
                final IndexEntry e = index[i];
                if (e.gene.serialize().length > size) {
                    if (chunk.size() > 0) {
                        chunks.add(chunk.toArray(new IndexEntry[chunk.size()]));
                        chunk.clear();
                    }
                    size = e.gene.serialize().length;
                }
            }
            if (chunk.size() > 0) {
                chunks.add(chunk.toArray(new IndexEntry[chunk.size()]));
            }
            return chunks.toArray(new IndexEntry[chunks.size()][]);
        }

        public FlatFileSerializer(IndexEntry[] index, GenomeEntry[] genomes, Database.FileEntry[] files) {
            super(index, genomes, files);
        }

        public void serialize(DataOutputStream output) throws IOException {
            Preconditions.checkNotNull(output, "The parameter 'output' must not be null");

            output.writeInt(0); // version

            final IndexEntry[][] chunks = getChunks();
            output.writeInt(chunks.length); // number of chunks
            for (final IndexEntry[] chunk : chunks) {
                output.writeInt(chunk.length); // number of entries in chunk
                output.writeInt(chunk[0].gene.serialize().length); // size of
                                                                   // each entry
                for (final IndexEntry e : chunk) {
                    output.write(e.gene.serialize());
                }
            }

            final GenomeEntry[] genomes = getGenomes();
            output.writeInt(genomes.length); // number of genomes
            for (final GenomeEntry genome : genomes) {
                output.write(genome.serialize());
            }

            final Database.FileEntry[] files = getFiles();
            output.writeInt(files.length); // number of files
            for (final Database.FileEntry file : files) {
                output.write(file.decode());
            }
        }

        public void serialize(File output) throws Exception {
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

        @Override
        public String getFileExtension() {
            return ".mlc";
        }
    }

    protected static class ZipFileSerializer extends Serializer {

        public ZipFileSerializer(IndexEntry[] index, GenomeEntry[] genomes, Database.FileEntry[] files) {
            super(index, genomes, files);
        }

        public void serialize(File output) throws Exception {
            try (final FileOutputStream fout = new FileOutputStream(output)) {
                try (final ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout, 10 * 1024 * 1024))) {
                    final DataOutputStream dout = new DataOutputStream(zout);
                    zout.setLevel(9);
                    for (final Database.FileEntry fe : getFiles()) {
                        final ZipEntry ze = new ZipEntry("files/" + fe.getId());
                        zout.putNextEntry(ze);
                        zout.write(fe.decode());
                        zout.closeEntry();
                    }
                    for (final IndexEntry ie : getIndex()) {
                        final ZipEntry ze = new ZipEntry("genes/" + ie.index);
                        zout.putNextEntry(ze);
                        zout.write(ie.gene.compress());
                        zout.closeEntry();
                    }
                    int i = 0;
                    for (final GenomeEntry ge : getGenomes()) {
                        final ZipEntry ze = new ZipEntry("genomes/" + i++);
                        zout.putNextEntry(ze);
                        ge.serialize(dout);
                        zout.closeEntry();
                    }
                }
            }
        }

        @Override
        public String getFileExtension() {
            return ".zip";
        }

    }

    protected static class SqliteSerializer extends Serializer {

        public SqliteSerializer(IndexEntry[] index, GenomeEntry[] genomes, FileEntry[] files) {
            super(index, genomes, files);
        }

        @Override
        public String getFileExtension() {
            return ".sqlite";
        }

        @Override
        public void serialize(File output) throws Exception {
            try (final Database db = Database.createDatabase(output)) {
                try (final Database.Transaction tr = db.begin()) {
                    for (final IndexEntry ie : getIndex()) {
                        db.insertGene(ie.index, ie.crc, ie.gene.serialize());
                    }
                }
                try (final Database.Transaction tr = db.begin()) {
                    for (final GenomeEntry ge : getGenomes()) {
                        db.insertGenome(ge.numberOfImprovements, ge.fitness, ge.serialize());
                    }
                }
                try (final Database.Transaction tr = db.begin()) {
                    for (final Database.FileEntry fe : getFiles()) {
                        db.insertFile(fe.getId(), fe.getOriginalName(), fe.decode());
                    }
                }
            }
        }

    }

    protected static class SqlJetSerializer extends Serializer {

        public SqlJetSerializer(IndexEntry[] index, GenomeEntry[] genomes, FileEntry[] files) {
            super(index, genomes, files);
        }

        public void serialize(File output) throws Exception {
            final SqlJetDb db = SqlJetDb.open(output, true);
            try {
                db.createTable("CREATE TABLE genes (idx INTEGER NOT NULL PRIMARY KEY, crc INTEGER NOT NULL, data BLOB NOT NULL)");
                db.beginTransaction(SqlJetTransactionMode.WRITE);
                try {
                    final ISqlJetTable table = db.getTable("genes");
                    for (final IndexEntry ie : getIndex()) {
                        table.insert(ie.index, ie.crc, ie.gene.serialize());
                    }
                } finally {
                    db.commit();
                }

                db.createTable("CREATE TABLE genomes (idx INTEGER NOT NULL PRIMARY KEY, improvement INTEGER NOT NULL, fitness REAL NOT NULL, data BLOB NOT NULL)");
                db.beginTransaction(SqlJetTransactionMode.WRITE);
                try {
                    final ISqlJetTable table = db.getTable("genomes");
                    int i = 1;
                    for (final GenomeEntry ge : getGenomes()) {
                        table.insert(i++, ge.numberOfImprovements, ge.fitness, ge.serialize());
                    }
                } finally {
                    db.commit();
                }

                db.createTable("CREATE TABLE files (id TEXT NOT NULL PRIMARY KEY, originalName TEXT, compressed INTEGER NOT NULL, data BLOB NOT NULL)");
                db.beginTransaction(SqlJetTransactionMode.WRITE);
                try {
                    final ISqlJetTable table = db.getTable("files");
                    for (final Database.FileEntry fe : getFiles()) {
                        table.insert(fe.getId(), fe.getOriginalName(), 0, fe.decode());
                    }
                } finally {
                    db.commit();
                }
            } finally {
                db.close();
            }
        }

        @Override
        public String getFileExtension() {
            return ".sqljet";
        }
    }

    public OldFormatConverter2(File dbFile, File output) {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        Preconditions.checkArgument(dbFile.isFile(), "The parameter 'dbFile' has to be a regular file (" + dbFile + ")");
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        Preconditions.checkArgument(output.isDirectory(), "The parameter 'output' has to be a directory");
        this.dbFile = dbFile;
        this.output = output;
    }

    public File getDBFile() {
        return dbFile;
    }

    public File getOutputFile(String fileExtension) {
        return new File(output, autoName(dbFile, fileExtension));
    }

    public void serialize() throws Exception {

        final List<byte[]> rawGenomes = Lists.newArrayList();
        final List<Database.FileEntry> fileEntries = Lists.newArrayList();

        try (final Database db = Database.openDatabase(dbFile)) {
            loadDatabase(db, rawGenomes, fileEntries);
        }

        final GenomeEntry[] genomeEntries = decodeRawGenomes(rawGenomes);
        rawGenomes.clear(); // free some space

        final Multimap<Integer, IndexEntry> geneMap = mergeGenes(genomeEntries);
        final IndexEntry[] indexEntries = computeSortedIndex(geneMap);

        final Serializer serializer = new SqliteSerializer(indexEntries, genomeEntries, fileEntries.toArray(new Database.FileEntry[fileEntries.size()]));
        final File output = getOutputFile(serializer.getFileExtension());

        System.out.println("Writing file '" + output + "'...");
        if (output.exists()) {
            if (output.isFile()) {
                output.delete();
            } else {
                throw new IllegalArgumentException("Invalid output file " + output);
            }
        }
        serializer.serialize(output);
        System.out.println("Finished.");
    }

    public static void main(String[] args) throws Exception {
        final File input = new File("./data/output/backups/");
        final File output = new File("./data/output/");
        final File[] dbs = input.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".mldb");
            }
        });
        for (final File db : dbs) {
            final OldFormatConverter2 c = new OldFormatConverter2(db, output);
            c.serialize();
        }
    }
}
