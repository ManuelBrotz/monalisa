package ch.brotzilla.monalisa.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.almworks.sqlite4java.SQLiteBackup;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.google.common.base.Preconditions;

public class Database implements AutoCloseable {

    public static final DatabaseSchema Schema = new DatabaseSchema();

    public static final String SelectLatestGenomeQuery = "SELECT improvements, fitness, data FROM genomes ORDER BY improvements DESC LIMIT 1";
    public static final String SelectFileByIdQuery = "SELECT id, originalName, data FROM files WHERE id = ?1";
    public static final String SelectNumberOfGenomesQuery = "SELECT Count(selected) FROM genomes";

    public static final String SelectAllGenesQuery = "SELECT id, crc, data FROM genes ORDER BY id ASC";
    public static final String SelectAllGenomesQuery = "SELECT improvements, fitness, data FROM genomes ORDER BY improvements ASC";
    public static final String SelectAllFilesQuery = "SELECT id, originalName, data FROM files ORDER BY id ASC";

    public static final String InsertFileQuery = "INSERT INTO files (id, originalName, data) VALUES (?1, ?2, ?3)";
    public static final String InsertGenomeQuery = "INSERT INTO genomes (improvements, fitness, data) VALUES (?1, ?2, ?3)";
    public static final String InsertGeneQuery = "INSERT INTO genes (id, crc, data) VALUES (?1, ?2, ?3)";

    protected final SQLiteConnection conn;

    protected final SQLiteStatement selectLatestGenomeQuery, selectNumberOfGenomesQuery, selectFileByIdQuery;
    protected final SQLiteStatement selectAllGenesQuery, selectAllGenomesQuery, selectAllFilesQuery;
    protected final SQLiteStatement insertFileQuery, insertGenomeQuery, insertGeneQuery;

    protected Transaction transaction;

    private SQLiteStatement prepare(String sql) {
        try {
            return conn.prepare(sql);
        } catch (Exception e) {
            System.out.println("error: unable to prepare sql statement: " + sql);
            System.out.println(e.getMessage());
        }
        return null;
    }

    private void check(SQLiteStatement stmt) {
        if (stmt == null) {
            throw new IllegalStateException("Query not available");
        }
    }

    private Database(SQLiteConnection conn) throws SQLiteException {
        Preconditions.checkNotNull(conn, "The parameter 'conn' must not be null");
        Preconditions.checkState(conn.isOpen(), "The connection to the database has to be open");

        this.conn = conn;

        this.selectLatestGenomeQuery = prepare(SelectLatestGenomeQuery);
        this.selectNumberOfGenomesQuery = prepare(SelectNumberOfGenomesQuery);
        this.selectFileByIdQuery = prepare(SelectFileByIdQuery);

        this.selectAllGenesQuery = prepare(SelectAllGenesQuery);
        this.selectAllGenomesQuery = prepare(SelectAllGenomesQuery);
        this.selectAllFilesQuery = prepare(SelectAllFilesQuery);

        this.insertFileQuery = prepare(InsertFileQuery);
        this.insertGenomeQuery = prepare(InsertGenomeQuery);
        this.insertGeneQuery = prepare(InsertGeneQuery);
    }

    public File getDatabaseFile() {
        return conn.getDatabaseFile();
    }

    public int queryNumberOfGenomes() throws SQLiteException {
        check(selectNumberOfGenomesQuery);
        selectNumberOfGenomesQuery.reset();
        if (selectNumberOfGenomesQuery.step()) {
            return selectNumberOfGenomesQuery.columnInt(0);
        }
        return -1;
    }

    public GenomeEntry queryLatestGenome() throws SQLiteException, IOException {
        check(selectLatestGenomeQuery);
        selectLatestGenomeQuery.reset();
        if (selectLatestGenomeQuery.step()) {
            return new GenomeEntry(selectLatestGenomeQuery.columnInt(0), selectLatestGenomeQuery.columnDouble(1), selectLatestGenomeQuery.columnBlob(2));
        }
        return null;
    }

    public int queryAllGenes(List<GeneEntry> output) throws SQLiteException {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        check(selectAllGenesQuery);
        selectAllGenesQuery.reset();
        int count = 0;
        while (selectAllGenesQuery.step()) {
            output.add(new GeneEntry(selectAllGenesQuery.columnInt(0), selectAllGenesQuery.columnInt(1), selectAllGenesQuery.columnBlob(2)));
            ++count;
        }
        return count;
    }

    public int queryAllGenomes(List<GenomeEntry> output) throws SQLiteException {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        check(selectAllGenomesQuery);
        selectAllGenomesQuery.reset();
        int count = 0;
        while (selectAllGenomesQuery.step()) {
            output.add(new GenomeEntry(selectAllGenomesQuery.columnInt(0), selectAllGenomesQuery.columnDouble(1), selectAllGenomesQuery.columnBlob(2)));
            ++count;
        }
        return count;
    }

    public int queryAllFiles(List<FileEntry> output) throws SQLiteException {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        check(selectAllFilesQuery);
        selectAllFilesQuery.reset();
        int count = 0;
        while (selectAllFilesQuery.step()) {
            output.add(new FileEntry(selectAllFilesQuery.columnString(0), selectAllFilesQuery.columnString(1), selectAllFilesQuery.columnBlob(2)));
            ++count;
        }
        return count;
    }

    public FileEntry queryFileById(String id) throws SQLiteException {
        Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        check(selectFileByIdQuery);
        selectFileByIdQuery.reset();
        selectFileByIdQuery.bind(1, id);
        if (selectFileByIdQuery.step()) {
            return new FileEntry(selectFileByIdQuery.columnString(0), selectFileByIdQuery.columnString(1), selectFileByIdQuery.columnBlob(2));
        }
        return null;
    }

    public void insertFile(String id, String originalName, byte[] data) throws SQLiteException {
        Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        Preconditions.checkArgument(!id.isEmpty(), "The parameter 'id' must not be empty");
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        check(insertFileQuery);
        insertFileQuery.reset();
        insertFileQuery.bind(1, id);
        insertFileQuery.bind(2, originalName);
        insertFileQuery.bind(3, data);
        insertFileQuery.step();
    }

    public void insertGenome(int improvements, double fitness, byte[] data) throws SQLiteException {
        Preconditions.checkArgument(improvements >= 0, "The parameter 'improvements' has to be greater than or equal to zero");
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        check(insertGenomeQuery);
        insertGenomeQuery.reset();
        insertGenomeQuery.bind(1, improvements);
        insertGenomeQuery.bind(2, fitness);
        insertGenomeQuery.bind(3, data);
        insertGenomeQuery.step();
    }

    public void insertGene(int index, int crc, byte[] data) throws SQLiteException {
        Preconditions.checkArgument(index > 0, "The parameter 'index' has to be greater than zero");
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        check(insertGeneQuery);
        insertGeneQuery.reset();
        insertGeneQuery.bind(1, index);
        insertGeneQuery.bind(2, crc);
        insertGeneQuery.bind(3, data);
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

    public static class GeneEntry {

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

    public static class GenomeEntry {

        private final int improvements;
        private final double fitness;
        private final byte[] data;

        public GenomeEntry(int improvements, double fitness, byte[] data) {
            Preconditions.checkArgument(improvements >= 0, "The parameter 'improvements' has to be greater than or equal to zero");
            Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
            this.improvements = improvements;
            this.fitness = fitness;
            this.data = data;
        }

        public int getImprovements() {
            return improvements;
        }

        public double getFitness() {
            return fitness;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class FileEntry {

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

        public byte[] getData() throws IOException {
            return data;
        }
    }
}
