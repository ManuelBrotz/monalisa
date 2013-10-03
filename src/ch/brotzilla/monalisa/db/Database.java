package ch.brotzilla.monalisa.db;

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

import ch.brotzilla.monalisa.db.Schema.DataType;
import ch.brotzilla.monalisa.db.Schema.Field;
import ch.brotzilla.monalisa.db.Schema.Index;
import ch.brotzilla.monalisa.db.Schema.Table;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.io.TextReader;

import com.google.common.base.Preconditions;

public class Database {

    public static class TblGenomes extends Table {

        public static final Field fFitness = new Field("fitness", DataType.Real, false, false);
        public static final Field fSelected = new Field("selected", DataType.Integer, false, true);
        public static final Field fPolygons = new Field("polygons", DataType.Integer, false, false);
        public static final Field fJson = new Field("json", DataType.Text, false, false);

        public TblGenomes() {
            super("genomes", fFitness, fSelected, fPolygons, fJson);
        }
    }
    
    public static class TblFiles extends Table {
        
        public static final Field fName = new Field("name", DataType.Text, false, true);
        public static final Field fOriginalName = new Field("originalName", DataType.Text, true, false);
        public static final Field fData = new Field("data", DataType.Blob, true, false);
        
        public TblFiles() {
            super("files", fName, fOriginalName, fData);
        }
    }
    
    public static class DBSchema extends Schema {

        public static final TblGenomes tblGenomes = new TblGenomes();
        public static final TblFiles tblFiles = new TblFiles();
        
        public static final Index idxGenomesFitness = new Index("index_genomes_fitness", tblGenomes, TblGenomes.fFitness);
        public static final Index idxFilesName = new Index("index_files_name", tblFiles, TblFiles.fName);
        
        public DBSchema() {
            super(new Tables(tblGenomes, tblFiles), new Indexes(idxGenomesFitness, idxFilesName));
        }
    }
    
    @SuppressWarnings("serial")
    public static class DatabaseException extends RuntimeException  {
        public DatabaseException(Exception cause) {
            super(Preconditions.checkNotNull(cause, "The parameter 'cause' must not be null").getMessage(), cause);
        }
    }
    
    public static final DBSchema dbSchema = new DBSchema();
    
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
        this.table = database.getTable(DBSchema.tblGenomes.name);
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
                final ISqlJetCursor cursor = db.getTable(DBSchema.tblGenomes.name).open();
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
                    final ISqlJetTable table = db.getTable(DBSchema.tblGenomes.name);
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
    
    public static SqlJetDb createDatabase(File dbFile) throws SqlJetException {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        Preconditions.checkArgument(!dbFile.exists(), "The parameter 'dbFile' must not exist");
        
        final SqlJetDb db = SqlJetDb.open(dbFile, true);
        new Transaction<Void>(db, SqlJetTransactionMode.WRITE) {
            @Override 
            public Void run() throws SqlJetException {
                for (Table t : dbSchema.tables.getTables()) {
                    db.createTable(t.createTableQuery);
                }
                for (Index i : dbSchema.indexes.getIndexes()) {
                    db.createIndex(i.createIndexQuery);
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
    
    protected static class FileLister implements FileFilter {
        
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
