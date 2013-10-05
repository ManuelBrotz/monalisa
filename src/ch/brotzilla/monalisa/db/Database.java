package ch.brotzilla.monalisa.db;

import java.io.File;
import java.io.IOException;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import ch.brotzilla.monalisa.db.DatabaseSchema.TblFiles;
import ch.brotzilla.monalisa.db.DatabaseSchema.TblGenomes;
import ch.brotzilla.monalisa.db.schema.Index;
import ch.brotzilla.monalisa.db.schema.Table;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.ImageData;
import ch.brotzilla.monalisa.utils.Compression;

import com.google.common.base.Preconditions;

public class Database implements AutoCloseable {

    public static final DatabaseSchema Schema = new DatabaseSchema();
    
    protected final File dbFile;
    protected final SqlJetDb database;
    protected final ISqlJetTable table;

    public Database(File dbFile) throws SqlJetException {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        this.dbFile = dbFile;
        if (dbFile.exists()) {
            this.database = SqlJetDb.open(dbFile, true);
        } else {
            this.database = createDatabase(dbFile);
        }
        this.table = database.getTable(DatabaseSchema.tblGenomes.getName());
    }
    
    public boolean isOpen() {
        return database.isOpen();
    }
    
    public boolean isWritable() throws SqlJetException {
        return database.isWritable();
    }
    
    public Genome queryLatestGenome() throws SqlJetException, IOException {
        return (new Read<Genome>() {
            @Override
            public Genome transaction() throws Exception {
                final ISqlJetCursor cursor = getDb().getTable(DatabaseSchema.tblGenomes.getName()).open().reverse();
                try {
                    if (cursor.eof()) {
                        return null;
                    } else {
                        return Compression.decodeGenome(cursor.getBlobAsArray(TblGenomes.fData.getName()));
                    }
                } finally {
                    cursor.close();
                }
            }
        }).execute();
    }
    
    public ImageData queryImage(final String id) throws SqlJetException {
        return (new Read<ImageData>() {
            @Override
            public ImageData transaction() throws Exception {
                final ISqlJetCursor cursor = getDb().getTable(DatabaseSchema.tblFiles.getName()).lookup(DatabaseSchema.idxFilesName.getName(), id);
                try {
                    if (cursor.eof()) {
                        return null;
                    } else {
                        return Compression.decodeImageData(cursor.getBlobAsArray(TblFiles.fData.getName()));
                    }
                } finally {
                    cursor.close();
                }
            }
        }).execute();
    }
    
    @Override
    public void close() throws SqlJetException {
        database.close();
    }
    
    public static void main(String[] args) throws SqlJetException, IOException {
        final Database db = new Database(new File("output/rose.mldb"));
        System.out.println(db.queryLatestGenome());
        System.out.println(db.queryImage("target-image"));
        System.out.println(db.queryImage("importance-map"));
    }
    
    public static SqlJetDb createDatabase(File dbFile) throws SqlJetException {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        Preconditions.checkArgument(!dbFile.exists(), "The parameter 'dbFile' must not exist (" + dbFile + ")");
        
        final SqlJetDb db = SqlJetDb.open(dbFile, true);
        new Transaction<Void>(db, SqlJetTransactionMode.WRITE) {
            @Override 
            public Void transaction() throws SqlJetException {
                for (Table t : Schema.getTables()) {
                    db.createTable(t.getCreateTableQuery());
                }
                for (Index i : Schema.getIndexes()) {
                    db.createIndex(i.getCreateIndexQuery());
                }
                return null;
            }
        }.execute();
        
        return db;
    }
    
    protected abstract class Read<T> extends Transaction<T> {
        public Read() {
            super(database, SqlJetTransactionMode.READ_ONLY);
        }
    }
    
    protected abstract class Write<T> extends Transaction<T> {
        public Write() {
            super(database, SqlJetTransactionMode.WRITE);
        }
    }
}
