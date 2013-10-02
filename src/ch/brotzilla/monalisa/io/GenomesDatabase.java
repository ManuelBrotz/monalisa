package ch.brotzilla.monalisa.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import ch.brotzilla.monalisa.evolution.genes.Genome;

import com.google.common.base.Preconditions;

public class GenomesDatabase {

    protected static final String tableName = "genomes";
    protected static final String createTableQuery = "CREATE TABLE " + tableName + " (selected INTEGER NOT NULL PRIMARY KEY, fitness REAL NOT NULL, json TEXT NOT NULL)";
    protected static final String createFitnessIndexQuery = "CREATE INDEX fitness_index ON " + tableName + "(fitness)";
    
    protected final File dbFile;
    protected final SqlJetDb database;
    protected final ISqlJetTable table;
    
    @SuppressWarnings("serial")
    public static class DatabaseException extends RuntimeException  {
        public DatabaseException(Exception cause) {
            super(Preconditions.checkNotNull(cause, "The parameter 'cause' must not be null").getMessage(), cause);
        }
    }
    
    public GenomesDatabase(File dbFile) throws SqlJetException {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        this.dbFile = dbFile;
        if (dbFile.exists()) {
            this.database = SqlJetDb.open(dbFile, true);
        } else {
            this.database = createDatabase(dbFile);
        }
        this.table = database.getTable(tableName);
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
                final ISqlJetCursor cursor = db.getTable(tableName).open();
                try {
                    if (cursor.eof()) {
                        return null;
                    } else {
                        return Genome.fromJson(cursor.getString("json"));
                    }
                } finally {
                    cursor.close();
                }
            }
        }).getResult();
    }
    
    public static void convertOldStorageFormat(File folder, File dbFile) throws SqlJetException, IOException {
        Preconditions.checkNotNull(folder, "The parameter 'folder' must not be null");
        Preconditions.checkArgument(folder.isDirectory(), "The parameter 'folder' has to be a directory");
        
        final LinkedList<File> files = new LinkedList<File>();
        folder.listFiles(new FileLister(files));
        
        final SqlJetDb db = createDatabase(dbFile);
        try {
            final TextReader txt = new TextReader(1024 * 100);
            new Transaction<Void>(db, SqlJetTransactionMode.WRITE) {
                @Override
                public Void run() throws Exception {
                    final ISqlJetTable table = db.getTable(tableName);
                    for (final File file : files) {
                        final String json = txt.readTextFile(file);
                        final Genome genome = Genome.fromJson(json);
                        table.insert(genome.selected, genome.fitness, json);
                    }
                    return null;
                }
            };
        } finally {
            db.close();
        }
    }
    
    private static SqlJetDb createDatabase(File dbFile) throws SqlJetException {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        Preconditions.checkArgument(!dbFile.exists(), "The parameter 'dbFile' must not exist");
        
        final SqlJetDb db = SqlJetDb.open(dbFile, true);
        new Transaction<Void>(db, SqlJetTransactionMode.WRITE) {
            @Override 
            public Void run() throws SqlJetException {
                db.createTable(createTableQuery);
                db.createIndex(createFitnessIndexQuery);
                return null;
            }
        };
        
        return db;
    }
    
    private abstract class Read<T> extends Transaction<T> {
        public Read() {
            super(database, SqlJetTransactionMode.READ_ONLY);
        }
    }
    
    private abstract class Write<T> extends Transaction<T> {
        public Write() {
            super(database, SqlJetTransactionMode.WRITE);
        }
    }
    
    private static abstract class Transaction<T extends Object> {
        
        protected final SqlJetDb db;
        
        private T result = null;
        
        public Transaction(final SqlJetDb db, final SqlJetTransactionMode mode) {
            Preconditions.checkNotNull(db, "The parameter 'db' must not be null");
            this.db = db;
            try {
                db.beginTransaction(mode);
                try {
                    result = run();
                } catch (Exception e) {
                    db.rollback();
                    throw e;
                }
                db.commit();
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
        
        public T getResult() {
            return result;
        }
        
        public abstract T run() throws Exception;
    }
    
    private static class FileLister implements FileFilter {
        
        private final List<File> list;

        public FileLister(List<File> list) {
            Preconditions.checkNotNull(list, "The parameter 'list' must not be null");
            this.list = list;
        }
        
        @Override
        public boolean accept(File pathname) {
            if (pathname.isFile() && pathname.getName().endsWith(".genome")) {
                list.add(pathname);
            }
            return false;
        }
    }
}
