package ch.brotzilla.monalisa.db;

import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import ch.brotzilla.monalisa.db.DatabaseSchema.TblGenomes;
import ch.brotzilla.monalisa.db.schema.Index;
import ch.brotzilla.monalisa.db.schema.Table;
import ch.brotzilla.monalisa.evolution.genes.Genome;

import com.google.common.base.Preconditions;

public class Database {

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
    
    public File getDatabaseFile() {
        return dbFile;
    }
    
    public boolean isOpen() {
        return database.isOpen();
    }
    
    public boolean isWritable() throws SqlJetException {
        return database.isWritable();
    }
    
    public Genome queryLatestGenome() throws SqlJetException {
        return (new Read<Genome>() {
            @Override
            public Genome run() throws Exception {
                final ISqlJetCursor cursor = db.getTable(DatabaseSchema.tblGenomes.getName()).open();
                try {
                    if (cursor.eof()) {
                        return null;
                    } else {
                        return Genome.fromJson(cursor.getString(TblGenomes.fJson.getName()));
                    }
                } finally {
                    cursor.close();
                }
            }
        }).getResult();
    }
    
    public static SqlJetDb createDatabase(File dbFile) throws SqlJetException {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        Preconditions.checkArgument(!dbFile.exists(), "The parameter 'dbFile' must not exist (" + dbFile + ")");
        
        final SqlJetDb db = SqlJetDb.open(dbFile, true);
        new Transaction<Void>(db, SqlJetTransactionMode.WRITE) {
            @Override 
            public Void run() throws SqlJetException {
                for (Table t : Schema.getTables()) {
                    db.createTable(t.getCreateTableQuery());
                }
                for (Index i : Schema.getIndexes()) {
                    db.createIndex(i.getCreateIndexQuery());
                }
                return null;
            }
        };
        
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
