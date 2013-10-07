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
    
    protected final SQLiteConnection db;
    
    protected final SQLiteStatement selectLatestGenomeQuery;
    protected final SQLiteStatement insertFileQuery, insertGenomeQuery;
    
    private Database(SQLiteConnection db) throws SQLiteException {
        Preconditions.checkNotNull(db, "The parameter 'db' must not be null");
        Preconditions.checkState(db.isOpen(), "The connection to the database has to be open");
        this.db = db;
        selectLatestGenomeQuery = db.prepare(SelectLatestGenomeQuery);
        insertFileQuery = db.prepare(InsertFileQuery);
        insertGenomeQuery = db.prepare(InsertGenomeQuery);
    }
    
    public File getDatabaseFile() {
        return db.getDatabaseFile();
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
    
    public void execute(String sql) throws SQLiteException {
        db.exec(sql);
    }
    
    public void begin() throws SQLiteException {
        db.exec("BEGIN");
    }
    
    public void commit() throws SQLiteException {
        db.exec("COMMIT"); 
    }
    
    @Override
    public void close() {
        db.dispose();
    }
    
    public static Database createDatabase(File dbFile) throws SQLiteException, IOException {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        if (dbFile.exists()) {
            throw new IOException("Database already exists (" + dbFile + ")");
        }
        final SQLiteConnection conn = new SQLiteConnection(dbFile);
        conn.open(true);
        final Database db = new Database(conn);
        db.begin();
        try {
            for (final String query : Schema.getCreateDatabaseQueries()) {
                db.execute(query);
            }
            db.execute("PRAGMA count_changes=OFF");
            db.execute("PRAGMA journal_mode=MEMORY");
            db.execute("PRAGMA temp_store=MEMORY");
        } finally {
            db.commit();
        }
        return db;
    }
    
    public static void main(String[] args) throws Exception {
        try (final Database db = createDatabase(new File("output/test.mldb"))) {
            System.out.println(db.queryLatestGenome());
        }
    }
}
