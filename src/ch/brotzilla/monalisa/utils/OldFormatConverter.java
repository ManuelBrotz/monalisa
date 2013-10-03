package ch.brotzilla.monalisa.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import ch.brotzilla.monalisa.db.Database;
import ch.brotzilla.monalisa.db.DatabaseSchema;
import ch.brotzilla.monalisa.db.Transaction;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.io.TextReader;

import com.google.common.base.Preconditions;

public final class OldFormatConverter {

    private OldFormatConverter() {}

    public static void convertOldStorageFormat(File folder, File dbFile) throws SqlJetException, IOException {
        Preconditions.checkNotNull(folder, "The parameter 'folder' must not be null");
        Preconditions.checkArgument(folder.isDirectory(), "The parameter 'folder' has to be a directory");
        
        final LinkedList<File> files = new LinkedList<File>();
        folder.listFiles(new FileLister(files));
        
        final SqlJetDb db = Database.createDatabase(dbFile);
        try {
            final TextReader txt = new TextReader(1024 * 100);
            new Transaction<Void>(db, SqlJetTransactionMode.WRITE) {
                @Override
                public Void run() throws Exception {
                    final ISqlJetTable table = db.getTable(DatabaseSchema.tblGenomes.getName());
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
