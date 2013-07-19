package ch.brotzilla.monalisa.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import javax.imageio.ImageIO;

import ch.brotzilla.monalisa.genes.Genome;

import com.google.common.base.Preconditions;

public class SessionManager {

    protected final String sessionName;
    protected final File sessionDirectory;
    protected final File imagesDirectory;
    protected final File genomesDirectory;
    protected final File inputImageFile;
    protected final BufferedImage inputImage;
    protected final boolean isSessionResumed;

    protected BufferedImage importImageFile(File inputImageFile, File outputImageFile) throws IOException {
        final BufferedImage img = Utils.readImage(inputImageFile);
        ImageIO.write(img, "PNG", outputImageFile);
        return img;
    }

    public SessionManager(File sessionsRoot, File inputImageFile) throws IOException {
        Preconditions.checkNotNull(sessionsRoot, "The parameter 'sessionsRoot' must not be null");
        Preconditions.checkNotNull(inputImageFile, "The parameter 'inputImageFile' must not be null");
        Preconditions.checkArgument(inputImageFile.isFile(), "The parameter 'inputImageFile' has to be a valid file");
        final String inputName = inputImageFile.getName();
        this.sessionDirectory = checkDir(uniqueDir(sessionsRoot, inputName), true);
        this.sessionName = sessionDirectory.getName();
        this.imagesDirectory = checkDir(new File(sessionDirectory, "images"), true);
        this.genomesDirectory = checkDir(new File(sessionDirectory, "genomes"), true);
        this.inputImageFile = new File(sessionDirectory, "input.png");
        this.inputImage = importImageFile(inputImageFile, this.inputImageFile);
        this.isSessionResumed = false;
    }

    public SessionManager(File sessionToResume) throws IOException {
        Preconditions.checkNotNull(sessionToResume, "The parameter 'sessionToResume' must not be null");
        this.sessionDirectory = checkDir(sessionToResume, false);
        this.imagesDirectory = checkDir(new File(sessionDirectory, "images"), false);
        this.genomesDirectory = checkDir(new File(sessionDirectory, "genomes"), false);
        this.sessionName = this.sessionDirectory.getName();
        this.inputImageFile = new File(sessionDirectory, "input.png");
        this.inputImage = Utils.readImage(inputImageFile);
        this.isSessionResumed = true;
    }

    public boolean isSessionReady() {
        return (sessionDirectory != null && imagesDirectory != null && genomesDirectory != null) && (sessionDirectory.isDirectory() && imagesDirectory.isDirectory() && genomesDirectory.isDirectory());
    }
    
    public boolean isSessionResumed() {
        return isSessionResumed;
    }

    public String getSessionName() {
        return sessionName;
    }

    public File getSessionDirectory() {
        return sessionDirectory;
    }

    public File getImagesDirectory() {
        return imagesDirectory;
    }

    public File getGenomesDirectory() {
        return genomesDirectory;
    }

    public File getInputImageFile() {
        return inputImageFile;
    }

    public BufferedImage getInputImage() {
        return inputImage;
    }
    
    public Genome loadLatestGenome() throws IOException {
        final File[] genomes = genomesDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".genome");
            }
        });
        int latestNum = 0;
        File latestGenome = null;
        for (final File genome : genomes) {
            try {
                final int num = Integer.parseInt(genome.getName().substring(0, 6));
                if (num > latestNum) {
                    latestNum = num;
                    latestGenome = genome;
                }
            } catch (Exception e) {
                continue;
            }
        }
        if (latestGenome != null) {
            final Reader br = new BufferedReader(new FileReader(latestGenome));
            try {
                final char[] buf = new char[1024*4];
                final StringBuffer result = new StringBuffer();
                while (true) {
                    final int len = br.read(buf);
                    if (len < 0)
                        break;
                    result.append(buf, 0, len);
                }
                return Genome.fromJson(result.toString());
            } finally {
                br.close();
            }
        }
        return null;
    }
    
    public void storeGenome(Genome genome, BufferedImage image) throws IOException {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        
        final String number = pad(genome.selected, 6);
        final File genomeFile = new File(genomesDirectory, number + ".genome");
        final File imageFile = new File(imagesDirectory, number + ".png");
        
        if (genomeFile.exists())
            throw new IllegalArgumentException("File already exists: " + genomeFile);
        if (imageFile.exists())
            throw new IllegalArgumentException("File already exists: " + imageFile);
        
        final PrintWriter writer = new PrintWriter(genomeFile);
        try {
            writer.print(Genome.toJson(genome));
        } finally {
            writer.close();
        }
        
        ImageIO.write(image, "PNG", imageFile);
    }
    
    public static File checkDir(File directory, boolean canCreate) throws IOException {
        Preconditions.checkNotNull(directory, "The parameter 'directory' must not be null");
        if (!directory.exists()) {
            if (canCreate) {
                if (!directory.mkdirs())
                    throw new IOException("Error creating directory: " + directory);
            } else {
                throw new IOException("Directory not found: " + directory);
            }
        } else {
            if (!directory.isDirectory())
                throw new IOException("Not a directory: " + directory);
        }
        return directory;
    }

    public static File uniqueDir(File directory, final String name) throws IOException {
        Preconditions.checkNotNull(directory, "The parameter 'directory' must not be null");
        Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
        Preconditions.checkArgument(!name.isEmpty(), "The parameter 'name' must not be empty");
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                throw new IOException("Not a directory: " + directory);
            }
            final File[] dirs = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return pathname.getName().startsWith(name);
                    }
                    return false;
                }
            });
            if (dirs.length == 0) {
                return new File(directory, name);
            }
            final int l = name.length();
            int largest = 0;
            for (final File dir : dirs) {
                try {
                    final int value = Math.abs(Integer.parseInt(dir.getName().substring(l)));
                    if (value > largest) {
                        largest = value;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            largest++;
            return new File(directory, name + "-" + pad(largest, 3));
        } else {
            return new File(directory, name);
        }
    }

    private static String pad(long value, int length) {
        String result = "" + value;
        while (result.length() < length) {
            result = "0" + result;
        }
        return result;
    }
}
