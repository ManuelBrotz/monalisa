package ch.brotzilla.monalisa.db;

import java.io.File;
import java.io.IOException;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.utils.Compression;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.google.common.base.Preconditions;

public class Database implements AutoCloseable {

    public static final DatabaseSchema Schema = new DatabaseSchema();
    
    protected final File dbFile;
    protected final SQLiteConnection db;
    
    protected final SQLiteStatement selectLatestGenomeQuery;
    protected final SQLiteStatement insertImageQuery, insertGenomeQuery;

    public Database(File dbFile) throws SQLiteException {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        this.dbFile = dbFile;
        this.db = new SQLiteConnection(dbFile);
        if (dbFile.exists()) {
            db.open(false);
        } else {
            db.open(true);
            createDatabase();
        }
        selectLatestGenomeQuery = db.prepare("SELECT selected, data FROM genomes ORDER BY selected DESC LIMIT 1");
        insertImageQuery = db.prepare("INSERT INTO files VALUES (?, ?, ?, ?)");
        insertGenomeQuery = db.prepare("INSERT INTO genomes VALUES (?, ?, ?, ?)");
    }
    
    public Genome queryLatestGenome() throws SQLiteException, IOException {
        Genome result = null;
        selectLatestGenomeQuery.reset();
        if (selectLatestGenomeQuery.step()) {
            result = Compression.decodeGenome(selectLatestGenomeQuery.columnBlob(1));
        }
        return result;
    }
    
    public void insertImage(String id, String originalName, boolean compressed, byte[] data) throws SQLiteException {
        insertImageQuery.reset();
        insertImageQuery.bind(1, id);
        insertImageQuery.bind(2, originalName);
        insertImageQuery.bind(3, compressed ? 1 : 0);
        insertImageQuery.bind(4, data);
        insertImageQuery.step();
    }
    
    public void insertGenome(double fitness, int selected, int polygons, byte[] data) throws SQLiteException {
        insertGenomeQuery.reset();
        insertGenomeQuery.bind(1, fitness);
        insertGenomeQuery.bind(2, selected);
        insertGenomeQuery.bind(3, polygons);
        insertGenomeQuery.bind(4, data);
        insertGenomeQuery.step();
    }
    
    public void transaction() throws SQLiteException {
        db.exec("BEGIN");
    }
    
    public void commit() throws SQLiteException {
        db.exec("COMMIT"); 
    }
    
    @Override
    public void close() {
        db.dispose();
    }
    
    protected void createDatabase() throws SQLiteException {
        for (final String query : Schema.getCreateDatabaseQueries()) {
            final SQLiteStatement st = db.prepare(query);
            try {
                st.step();
            } finally {
                st.dispose();
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        try (final Database db = new Database(new File("output/test.mldb"))) {
            System.out.println(db.queryLatestGenome());
        }
    }
}
