package ch.brotzilla.monalisa.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.ImageData;
import ch.brotzilla.monalisa.utils.Compression;

import com.almworks.sqlite4java.SQLiteBackup;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.google.common.base.Preconditions;

public class Database implements AutoCloseable {

    public static final DatabaseSchema Schema = new DatabaseSchema();
    public static final String SelectLatestGenomeQuery = "SELECT selected, data FROM genomes ORDER BY selected DESC LIMIT 1";
    public static final String SelectFileByIdQuery = "SELECT id, data FROM files WHERE id = ?1";
    public static final String SelectAllFileIdsQuery = "SELECT id FROM files";
    public static final String SelectAllFilesQuery = "SELECT id, originalName, data FROM files ORDER BY id ASC";
    public static final String SelectNumberOfGenomesQuery = "SELECT Count(selected) FROM genomes";
    public static final String SelectAllGenomesQuery = "SELECT selected, data FROM genomes ORDER BY selected ASC";
    public static final String InsertFileQuery = "INSERT INTO files VALUES (?1, ?2, ?3, ?4)";
    public static final String InsertGenomeQuery = "INSERT INTO genomes VALUES (?1, ?2, ?3, ?4)";
    public static final String InsertGeneQuery = "INSERT INTO genes (crc, data) VALUES (?1, ?2)";

    protected final SQLiteConnection conn;

    protected final SQLiteStatement selectLatestGenomeQuery, selectNumberOfGenomesQuery, selectAllGenomesQuery, selectFileByIdQuery, selectAllFileIdsQuery, selectAllFilesQuery;
    protected final SQLiteStatement insertFileQuery, insertGenomeQuery, insertGeneQuery;

    protected Transaction transaction;

    private Database(SQLiteConnection conn) throws SQLiteException {
        Preconditions.checkNotNull(conn, "The parameter 'conn' must not be null");
        Preconditions.checkState(conn.isOpen(), "The connection to the database has to be open");
        this.conn = conn;
        this.selectLatestGenomeQuery = conn.prepare(SelectLatestGenomeQuery);
        this.selectNumberOfGenomesQuery = conn.prepare(SelectNumberOfGenomesQuery);
        this.selectAllGenomesQuery = conn.prepare(SelectAllGenomesQuery);
        this.selectFileByIdQuery = conn.prepare(SelectFileByIdQuery);
        this.selectAllFileIdsQuery = conn.prepare(SelectAllFileIdsQuery);
        this.selectAllFilesQuery = conn.prepare(SelectAllFilesQuery);
        this.insertFileQuery = conn.prepare(InsertFileQuery);
        this.insertGenomeQuery = conn.prepare(InsertGenomeQuery);
        SQLiteStatement tmp = null;
        try {
            tmp = conn.prepare(InsertGeneQuery);
        } catch (Exception e) {
            // do nothing
        }
        this.insertGeneQuery = tmp;
    }

    public File getDatabaseFile() {
        return conn.getDatabaseFile();
    }

    public int queryNumberOfGenomes() throws SQLiteException {
        selectNumberOfGenomesQuery.reset();
        if (selectNumberOfGenomesQuery.step()) {
            return selectNumberOfGenomesQuery.columnInt(0);
        }
        return -1;
    }

    public int queryAllGenomesCompressed(List<byte[]> output) throws SQLiteException {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        selectAllGenomesQuery.reset();
        int count = 0;
        while (selectAllGenomesQuery.step()) {
            output.add(selectAllGenomesQuery.columnBlob(1));
            ++count;
        }
        return count;
    }

    public Genome queryLatestGenome() throws SQLiteException, IOException {
        selectLatestGenomeQuery.reset();
        if (selectLatestGenomeQuery.step()) {
            return Compression.decodeGenome(selectLatestGenomeQuery.columnBlob(1));
        }
        return null;
    }

    public void insertImage(String id, String originalName, ImageData data) throws IOException, SQLiteException {
        final byte[] encoded = Compression.encode(data);
        insertFile(id, originalName, true, encoded);
    }

    public ImageData queryImage(String id) throws SQLiteException, IOException {
        selectFileByIdQuery.reset();
        selectFileByIdQuery.bind(1, id);
        if (selectFileByIdQuery.step()) {
            return Compression.decodeImageData(selectFileByIdQuery.columnBlob(1));
        }
        return null;
    }
    
    public int queryAllFileIds(List<String> output) throws SQLiteException {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        selectAllFileIdsQuery.reset();
        int count = 0;
        while (selectAllFileIdsQuery.step()) {
            output.add(selectAllFileIdsQuery.columnString(0));
            ++count;
        }
        return count;
    }
    
    public void queryAllFiles(List<FileEntry> output) throws SQLiteException {
        selectAllFilesQuery.reset();
        while (selectAllFilesQuery.step()) {
            output.add(new FileEntry(
                    selectAllFilesQuery.columnString(0),
                    selectAllFilesQuery.columnString(1),
                    selectAllFilesQuery.columnBlob(2)
                    ));
        }
    }

    public void insertFile(String id, String originalName, boolean compressed, byte[] data) throws SQLiteException {
        Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        Preconditions.checkArgument(!id.isEmpty(), "The parameter 'id' must not be empty");
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        insertFileQuery.reset();
        insertFileQuery.bind(1, id);
        insertFileQuery.bind(2, originalName);
        insertFileQuery.bind(3, compressed ? 1 : 0);
        insertFileQuery.bind(4, data);
        insertFileQuery.step();
    }

    public void insertGenome(Genome genome) throws IOException, SQLiteException {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        final byte[] encoded = Compression.encode(genome);
        insertGenome(genome.fitness, genome.numberOfImprovements, genome.genes.length, encoded);
    }

    public void insertGenome(double fitness, int selected, int polygons, byte[] data) throws SQLiteException {
        insertGenomeQuery.reset();
        insertGenomeQuery.bind(1, fitness);
        insertGenomeQuery.bind(2, selected);
        insertGenomeQuery.bind(3, polygons);
        insertGenomeQuery.bind(4, data);
        insertGenomeQuery.step();
    }

    public void insertGene(int crc, byte[] data) throws SQLiteException {
        insertGeneQuery.reset();
        insertGeneQuery.bind(1, crc);
        insertGeneQuery.bind(2, data);
        insertGeneQuery.step();
    }

    public Transaction begin() throws SQLiteException {
        if (transaction != null) {
            transaction.enter();
            return transaction;
        }
        this.transaction = new Transaction(this);
        return transaction;
    }

    public void backup(File dbFile) throws SQLiteException, IOException {
        if (dbFile != null && dbFile.exists()) {
            throw new IOException("Database already exists (" + dbFile + ")");
        }
        final SQLiteBackup backup = conn.initializeBackup(dbFile);
        try {
            backup.backupStep(-1);
        } finally {
            backup.dispose();
        }
    }

    @Override
    public void close() {
        conn.dispose();
    }

    public static Database openDatabase(File dbFile) throws IOException, SQLiteException {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        if (!dbFile.isFile()) {
            throw new IOException("Database not found (" + dbFile + ")");
        }
        final SQLiteConnection conn = new SQLiteConnection(dbFile);
        conn.open(false);
        return new Database(conn);
    }

    public static Database createDatabase(File dbFile) throws SQLiteException, IOException {
        if (dbFile != null && dbFile.exists()) {
            throw new IOException("Database already exists (" + dbFile + ")");
        }
        final SQLiteConnection conn = new SQLiteConnection(dbFile);
        conn.open(true);

        conn.exec("PRAGMA synchronous   = 0");
        conn.exec("BEGIN");
        for (final String query : Schema.getCreateDatabaseQueries()) {
            try {
                conn.exec(query);
            } catch (SQLiteException s) {
                System.out.println("Error executing query: " + query);
                throw s;
            }
        }
        conn.exec("PRAGMA count_changes = OFF");
        conn.exec("PRAGMA journal_mode  = OFF");
        conn.exec("PRAGMA temp_store    = MEMORY");
        conn.exec("PRAGMA page_size     = 16384");
        conn.exec("COMMIT");

        return new Database(conn);
    }

    public static class Transaction implements AutoCloseable {

        private final Database db;
        private int count = 0;

        private void enter() throws SQLiteException {
            if (count == 0) {
                db.conn.exec("BEGIN");
            }
            ++count;
        }

        private void leave() throws SQLiteException {
            if (count <= 0) {
                throw new IllegalStateException("Transaction already closed");
            }
            --count;
            if (count == 0) {
                try {
                    db.conn.exec("COMMIT");
                } finally {
                    db.transaction = null;
                }
            }
        }

        private Transaction(Database db) throws SQLiteException {
            Preconditions.checkNotNull(db, "The parameter 'db' must not be null");
            this.db = db;
            enter();
        }

        @Override
        public void close() throws SQLiteException {
            leave();
        }
    }
    
    public static class FileEntry {
        
        public final String id, originalName;
        public final byte[] data;
        
        public FileEntry(String id, String originalName, byte[] data) {
            this.id = id;
            this.originalName = originalName;
            this.data = data;
        }
        
        public byte[] decode() throws IOException {
            final ByteArrayInputStream bin = new ByteArrayInputStream(data);
            final GZIPInputStream gzin = new GZIPInputStream(bin);
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final byte[] buf = new byte[1024];
            
            int read = gzin.read(buf, 0, buf.length);
            while (read > 0) {
                bout.write(buf, 0, read);
                read = gzin.read(buf, 0, buf.length);
            }
            
            return bout.toByteArray();
        }
    }
}
