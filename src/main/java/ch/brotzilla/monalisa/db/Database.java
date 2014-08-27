package ch.brotzilla.monalisa.db;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.ImageData;
import ch.brotzilla.monalisa.utils.Compression;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.google.common.base.Preconditions;

public class Database implements AutoCloseable {

    public static final DatabaseSchema Schema = new DatabaseSchema();
    public static final String SelectLatestGenomeQuery = "SELECT selected, data FROM genomes ORDER BY selected DESC LIMIT 1";
    public static final String SelectFileByIdQuery = "SELECT id, data FROM files WHERE id = ?1";
    public static final String SelectNumberOfGenomesQuery = "SELECT Count(selected) FROM genomes";
    public static final String SelectSettingByIdQuery = "SELECT id, value FROM settings WHERE id = ?1"; 
    public static final String InsertFileQuery = "INSERT INTO files VALUES (?1, ?2, ?3, ?4)";
    public static final String InsertGenomeQuery = "INSERT INTO genomes VALUES (?1, ?2, ?3, ?4)";
    public static final String MergeSettingQuery = "INSERT OR REPLACE INTO settings VALUES (?1, ?2)";
    
    protected final SQLiteConnection conn;
    
    protected final SQLiteStatement selectLatestGenomeQuery, selectFileByIdQuery, selectNumberOfGenomesQuery, selectSettingByIdQuery;
    protected final SQLiteStatement insertFileQuery, insertGenomeQuery, mergeSettingQuery;
    
    protected Transaction transaction;
    
    private Database(SQLiteConnection conn) throws SQLiteException {
        Preconditions.checkNotNull(conn, "The parameter 'conn' must not be null");
        Preconditions.checkState(conn.isOpen(), "The connection to the database has to be open");
        this.conn = conn;
        this.selectLatestGenomeQuery = conn.prepare(SelectLatestGenomeQuery);
        this.selectFileByIdQuery = conn.prepare(SelectFileByIdQuery);
        this.selectNumberOfGenomesQuery = conn.prepare(SelectNumberOfGenomesQuery);
        this.selectSettingByIdQuery = conn.prepare(SelectSettingByIdQuery);
        this.insertFileQuery = conn.prepare(InsertFileQuery);
        this.insertGenomeQuery = conn.prepare(InsertGenomeQuery);
        this.mergeSettingQuery = conn.prepare(MergeSettingQuery);
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
    
    public Genome queryLatestGenome() throws SQLiteException, IOException {
        selectLatestGenomeQuery.reset();
        if (selectLatestGenomeQuery.step()) {
            return Compression.decodeGenome(selectLatestGenomeQuery.columnBlob(1));
        }
        return null;
    }
    
    public ImageData queryImage(String id) throws SQLiteException, IOException {
        Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        selectFileByIdQuery.reset();
        selectFileByIdQuery.bind(1, id);
        if (selectFileByIdQuery.step()) {
            return Compression.decodeImageData(selectFileByIdQuery.columnBlob(1));
        }
        return null;
    }
    
    public void insertImage(String id, String originalName, ImageData data) throws IOException, SQLiteException {
        final byte[] encoded = Compression.encode(data);
        insertFile(id, originalName, true, encoded);
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
        insertGenome(genome.fitness, genome.numberOfImprovements, genome.countPolygons(), encoded);
    }
    
    public void insertGenome(double fitness, int numberOfImprovements, int polygons, byte[] data) throws SQLiteException {
        insertGenomeQuery.reset();
        insertGenomeQuery.bind(1, fitness);
        insertGenomeQuery.bind(2, numberOfImprovements);
        insertGenomeQuery.bind(3, polygons);
        insertGenomeQuery.bind(4, data);
        insertGenomeQuery.step();
    }

    public String querySetting(String id) throws SQLiteException, UnsupportedEncodingException {
        final byte[] value = querySettingData(id);
        if (value == null || value.length == 0) {
            return "";
        }
        return new String(value, "UTF-8");
    }

    public byte[] querySettingData(String id) throws SQLiteException {
        Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        selectSettingByIdQuery.reset();
        selectSettingByIdQuery.bind(1, id);
        if (selectSettingByIdQuery.step()) {
            return selectSettingByIdQuery.columnBlob(1);
        }
        return null;
    }

    public void updateSetting(String id, String value) throws UnsupportedEncodingException, SQLiteException {
        updateSetting(id, (value == null || value.isEmpty()) ? null : value.getBytes("UTF-8"));
    }
    
    public void updateSetting(String id, byte[] value) throws SQLiteException {
        Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        if (value != null && value.length == 0) {
            value = null;
        }
        mergeSettingQuery.reset();
        mergeSettingQuery.bind(1, id);
        mergeSettingQuery.bind(2, value);
        mergeSettingQuery.step();
    }

    public Transaction begin() throws SQLiteException {
        if (transaction != null) {
            transaction.enter();
            return transaction;
        }
        this.transaction = new Transaction(this);
        return transaction;
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
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        if (dbFile.exists()) {
            throw new IOException("Database already exists (" + dbFile + ")");
        }
        final SQLiteConnection conn = new SQLiteConnection(dbFile);
        conn.open(true);
        
        conn.exec("BEGIN");
        for (final String query : Schema.getCreateDatabaseQueries()) {
            conn.exec(query);
        }
        conn.exec("PRAGMA count_changes=OFF");
        conn.exec("PRAGMA journal_mode=MEMORY");
        conn.exec("PRAGMA temp_store=MEMORY");
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
}
