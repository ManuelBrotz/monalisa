package ch.brotzilla.monalisa.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import ch.brotzilla.monalisa.db.Database;
import ch.brotzilla.monalisa.db.DatabaseSchema;
import ch.brotzilla.monalisa.db.Transaction;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.ImageData;
import ch.brotzilla.monalisa.io.TextReader;

import com.google.common.base.Preconditions;

public final class OldFormatConverter {

    private OldFormatConverter() {}

    public static void convertOldSessionFolder(File sessionFolder, File dbFile) throws SqlJetException, IOException {
        Preconditions.checkNotNull(sessionFolder, "The parameter 'sessionFolder' must not be null");

        sessionFolder = sessionFolder.getAbsoluteFile();
        dbFile = dbFile.getAbsoluteFile();
        
        System.out.println("Converting oldstyle session folder into modern session database.");

        if (!sessionFolder.isDirectory()) {
            throw new IOException("Session folder not found: " + sessionFolder);
        }
        
        if (dbFile.exists()) {
            throw new IOException("Database file already exists: " + dbFile);
        }
        
        final File genomesFolder = new File(sessionFolder, "genomes").getAbsoluteFile();
        if (!genomesFolder.isDirectory()) {
            throw new IOException("Required subfolder not found: " + genomesFolder);
        }
        
        final File imageFile = new File(sessionFolder, "input.png");
        final File mapFile = new File(sessionFolder, "importance-map.png");
        if (!imageFile.isFile()) {
            throw new IOException("Required input image not found: " + imageFile);
        }

        System.out.println("Session folder: " + sessionFolder);
        System.out.println("Input image   : " + imageFile);
        if (mapFile.exists()) {
            System.out.println("Importance map: " + mapFile);
        }
        System.out.println("Database file : " + dbFile);

        final LinkedList<File> files = listGenomeFiles(genomesFolder);
        final SqlJetDb db = createDatabase(dbFile);
        
        try {
            importImage(db, "target-image", imageFile, ImageData.read(Utils.readImage(imageFile)));
            if (mapFile.isFile()) {
                importImage(db, "importance-map", mapFile, ImageData.read(ImageIO.read(mapFile)));
            }
            importGenomes(db, files);
        } finally {
            db.close();
        }
        
        long dbSize = dbFile.length();
        System.out.println("Size of generated database: " + formatSize(dbSize));
    }
    
    public static void convertAllSessionFolders(File root) throws IOException, SqlJetException {
        Preconditions.checkNotNull(root, "The parameter 'root' must not be null");
        
        root = root.getAbsoluteFile();
        
        if (!root.isDirectory()) {
            throw new IOException("Root folder not found: " + root);
        }
        
        System.out.println("Converting all old style sessions in folder:");
        System.out.println(root);
        
        final File[] sessions = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && (new File(pathname, "genomes").isDirectory() && (new File(pathname, "input.png").isFile()));
            }
        });
        
        if (sessions.length == 0) {
            System.out.println("No sessions found. Goodbye!");
            return;
        }
        
        System.out.println("Found " + sessions.length + " sessions: ");
        String names = "";
        for (final File session : sessions) {
            names += (names.isEmpty() ? "" : ", ") + session.getName();
        }
        System.out.println(names);
        
        for (final File session : sessions) {
            convertOldSessionFolder(session, new File(root, newSessionName(session)));
        }
    }
    
    public static void main(String[] args) throws IOException, SqlJetException {
        convertAllSessionFolders(new File("output/"));
    }
    
    private static String newSessionName(File sessionFolder) {
        String name = sessionFolder.getName();
        if (name.endsWith(".png")) {
            name = name.substring(0, name.length()-4);
        }
        return name + ".mldb";
    }
    
    private static LinkedList<File> listGenomeFiles(File sessionFolder) {
        try {
            final LinkedList<File> files = new LinkedList<File>();
            System.out.print("Scanning for *.genome files... ");
            sessionFolder.listFiles(new FileLister(files));
            System.out.println("Done! (" + files.size() + " files)");
            return files;
        } catch (Exception e) {
            System.out.println("Error!");
            throw e;
        }
    }
    
    private static SqlJetDb createDatabase(File dbFile) throws SqlJetException {
        try {
            System.out.print("Creating database... ");
            final SqlJetDb db = Database.createDatabase(dbFile);
            System.out.println("Done!");
            return db;
        } catch (SqlJetException e) {
            System.out.println("Error!");
            throw e;
        }
    }
    
    private static void importImage(SqlJetDb db, final String id, final File imageFile, final ImageData data) throws SqlJetException {
        try {
            System.out.print("Importing image file '" + imageFile.getName() + "' with id '" + id + "'... ");
            new Transaction<Void>(db, SqlJetTransactionMode.WRITE) {
                @Override
                public Void transaction() throws Exception {
                    final ISqlJetTable table = getDb().getTable(DatabaseSchema.tblFiles.getName());
                    final byte[] encoded = Compression.encode(data);
                    table.insert(id, imageFile.getAbsolutePath(), 1, encoded);
                    return null;
                }
            }.execute();
            System.out.println("Done!");
        } catch (SqlJetException e) {
            System.out.println("Error!");
            throw e;
        }
    }
    
    private static void importGenomes(SqlJetDb db, final LinkedList<File> files) throws SqlJetException {
        try {
            System.out.print("Converting genome files... ");
            final TextReader txt = new TextReader(1024 * 100);
            new Transaction<Void>(db, SqlJetTransactionMode.WRITE) {
                @Override
                public Void transaction() throws Exception {
                    final ISqlJetTable table = getDb().getTable(DatabaseSchema.tblGenomes.getName());
                    final int total = files.size();
                    int counter = 0, percentDone = 0;
                    for (final File file : files) {
                        final String json = txt.readTextFile(file);
                        final byte[] data = Compression.encode(json);
                        final Genome genome = Genome.fromJson(json);
                        table.insert(genome.fitness, genome.selected, genome.genes.length, data);
                        ++counter;
                        final int p = (int) (100d / total * counter);
                        if (p - percentDone >= 10) {
                            System.out.print(p + "% ");
                            percentDone = p;
                        }
                    }
                    if (percentDone < 100) {
                        System.out.print("100% ");
                    }
                    return null;
                }
            }.execute();
            System.out.println("Done!");
        } catch (SqlJetException e) {
            System.out.println("Error!");
            throw e;
        }
    }
    
    private static String formatSize(long size) {
        final int kb = 1024, mb = 1024 * 1024;
        String result = "";
        if (size >= mb) {
            long tmp = size / mb;
            size = size - tmp * mb;
            result += tmp + "mb ";
        }
        if (size > kb) {
            long tmp = size / kb;
            size = size - tmp * kb;
            result += tmp + "kb ";
        }
        result += size + "b";
        return result.trim();
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
