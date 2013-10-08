package ch.brotzilla.monalisa.db;

import java.io.File;
import java.io.IOException;

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
    public static final String InsertFileQuery = "INSERT INTO files VALUES (?1, ?2, ?3, ?4)";
    public static final String InsertGenomeQuery = "INSERT INTO genomes VALUES (?1, ?2, ?3, ?4)";
    
    protected final SQLiteConnection conn;
    
    protected final SQLiteStatement selectLatestGenomeQuery;
    protected final SQLiteStatement insertFileQuery, insertGenomeQuery;
    
    protected Transaction transaction;
    
    private Database(SQLiteConnection conn) throws SQLiteException {
        Preconditions.checkNotNull(conn, "The parameter 'conn' must not be null");
        Preconditions.checkState(conn.isOpen(), "The connection to the database has to be open");
        this.conn = conn;
        selectLatestGenomeQuery = conn.prepare(SelectLatestGenomeQuery);
        insertFileQuery = conn.prepare(InsertFileQuery);
        insertGenomeQuery = conn.prepare(InsertGenomeQuery);
    }
    
    public File getDatabaseFile() {
        return conn.getDatabaseFile();
    }
    
    public Genome queryLatestGenome() throws SQLiteException, IOException {
        Genome result = null;
        selectLatestGenomeQuery.reset();
        if (selectLatestGenomeQuery.step()) {
            result = Compression.decodeGenome(selectLatestGenomeQuery.columnBlob(1));
        }
        return result;
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
        insertGenome(genome.fitness, genome.selected, genome.genes.length, encoded);
    }
    
    public void insertGenome(double fitness, int selected, int polygons, byte[] data) throws SQLiteException {
        insertGenomeQuery.reset();
        insertGenomeQuery.bind(1, fitness);
        insertGenomeQuery.bind(2, selected);
        insertGenomeQuery.bind(3, polygons);
        insertGenomeQuery.bind(4, data);
        insertGenomeQuery.step();
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
        final Database db = new Database(conn);
        
        try (final Transaction t = db.begin()) {
            for (final String query : Schema.getCreateDatabaseQueries()) {
                db.conn.exec(query);
            }
            db.conn.exec("PRAGMA count_changes=OFF");
            db.conn.exec("PRAGMA journal_mode=MEMORY");
            db.conn.exec("PRAGMA temp_store=MEMORY");
        }
        return db;
    }
    
    public static void main(String[] args) throws Exception {
        try (final Database db = createDatabase(new File("output/test.mldb"))) {
            System.out.println(db.queryLatestGenome());
        }
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
        
        public Transaction(Database db) throws SQLiteException {
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
