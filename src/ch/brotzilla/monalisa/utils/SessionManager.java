package ch.brotzilla.monalisa.utils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.imageio.ImageIO;

import ch.brotzilla.monalisa.genes.Genome;

import com.google.common.base.Preconditions;

public class SessionManager {

    protected final Params params;
    
    protected final boolean isSessionResumed;
    protected final String sessionName;

    protected final File sessionDirectory;
    protected final File imagesDirectory;
    protected final File genomesDirectory;
    protected final File inputImageFile;
    protected final File importanceMapFile;

    protected final Constraints constraints;
    protected final int[] inputPixelData;
    protected final int[] importanceMap;

    protected BufferedImage importInputImage(File input, File output) throws IOException {
        final BufferedImage img = Utils.readImage(input);
        ImageIO.write(img, "PNG", output);
        return img;
    }
    
    protected int[] extractInputPixelData(BufferedImage input) {
        final WritableRaster raster = input.getRaster();
        return (int[]) raster.getDataElements(0, 0, input.getWidth(), input.getHeight(), null);
    }
    
    protected BufferedImage importImportanceMap(File input, File output, Constraints c) throws IOException {
        if (input != null) {
            final BufferedImage img = ImageIO.read(input);
            if (img.getType() != BufferedImage.TYPE_BYTE_GRAY)
                throw new IllegalArgumentException("Importance map has to be a grayscale image: " + input);
            if (img.getWidth() != c.getWidth())
                throw new IllegalArgumentException("The width of the importance map has to be equal to the width of the input image");
            if (img.getHeight() != c.getHeight())
                throw new IllegalArgumentException("The height of the importance map has to be equal to the height of the input image");
            ImageIO.write(img, "PNG", output);
            return img;
        }
        return null;
    }
    
    protected BufferedImage loadImportanceMap(File input, Constraints c) throws IOException {
        if (input != null && input.exists()) {
            final BufferedImage img = ImageIO.read(input);
            if (img.getType() != BufferedImage.TYPE_BYTE_GRAY)
                throw new IllegalArgumentException("Importance map has to be a grayscale image: " + input);
            if (img.getWidth() != c.getWidth())
                throw new IllegalArgumentException("The width of the importance map has to be equal to the width of the input image");
            if (img.getHeight() != c.getHeight())
                throw new IllegalArgumentException("The height of the importance map has to be equal to the height of the input image");
            return img;
        }
        return null;
    }
    
    protected int[] extractImportanceMap(BufferedImage input, Constraints c) {
        if (input != null) {
            final WritableRaster raster = input.getRaster();
            final byte[] raw = (byte[]) raster.getDataElements(0, 0, input.getWidth(), input.getHeight(), null);
            final int[] map = new int[raw.length];
            for (int i = 0; i < raw.length; i++) {
                map[i] = raw[i] & 0xFF;
            }
            return map;
        } else {
            final int[] map = new int[c.getWidth() * c.getHeight()];
            for (int i = 0; i < map.length; i++) {
                map[i] = 0xFF;
            }
            return map;
        }
    }

    public SessionManager(Params params) throws IOException {
        Preconditions.checkNotNull(params, "The parameter 'params' must not be null");
        this.params = params;
        final BufferedImage inputImage, importanceMap;
        if (params.getSessionToResume() == null) {
            final String inputName = params.getInputFile().getName();
            this.sessionDirectory = checkDir(uniqueDir(params.getOutputFolder(), inputName), true).getAbsoluteFile();
            this.sessionName = this.sessionDirectory.getName();
            this.imagesDirectory = checkDir(new File(this.sessionDirectory, "images"), true).getAbsoluteFile();
            this.genomesDirectory = checkDir(new File(this.sessionDirectory, "genomes"), true).getAbsoluteFile();
            this.inputImageFile = new File(this.sessionDirectory, "input.png").getAbsoluteFile();
            this.importanceMapFile = new File(this.sessionDirectory, "importance-map.png").getAbsoluteFile();
            this.isSessionResumed = false;
            inputImage = importInputImage(params.getInputFile(), this.inputImageFile);
            this.constraints = new Constraints(inputImage.getWidth(), inputImage.getHeight());
            importanceMap = importImportanceMap(params.getImportanceMap(), this.importanceMapFile, this.constraints);
        } else {
            this.sessionDirectory = checkDir(params.getSessionToResume(), false).getAbsoluteFile();
            this.sessionName = this.sessionDirectory.getName();
            this.imagesDirectory = checkDir(new File(this.sessionDirectory, "images"), false).getAbsoluteFile();
            this.genomesDirectory = checkDir(new File(this.sessionDirectory, "genomes"), false).getAbsoluteFile();
            this.inputImageFile = new File(this.sessionDirectory, "input.png").getAbsoluteFile();
            this.importanceMapFile = new File(this.sessionDirectory, "importance-map.png").getAbsoluteFile();
            this.isSessionResumed = true;
            inputImage = Utils.readImage(this.inputImageFile);
            this.constraints = new Constraints(inputImage.getWidth(), inputImage.getHeight());
            importanceMap = loadImportanceMap(this.importanceMapFile, this.constraints);
        }
        this.inputPixelData = extractInputPixelData(inputImage);
        this.importanceMap = extractImportanceMap(importanceMap, this.constraints);
    }
    
    public boolean isSessionReady() {
        return (sessionDirectory != null && imagesDirectory != null && genomesDirectory != null) && (sessionDirectory.isDirectory() && imagesDirectory.isDirectory() && genomesDirectory.isDirectory());
    }
    
    public boolean isSessionResumed() {
        return isSessionResumed;
    }
    
    public Params getParams() {
        return params;
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
    
    public File getImportanceMapFile() {
        return importanceMapFile;
    }

    public int getWidth() {
        return constraints.getWidth();
    }
    
    public int getHeight() {
        return constraints.getHeight();
    }
    
    public Constraints getConstraints() {
        return constraints;
    }
    
    public int[] getInputPixelData() {
        return inputPixelData;
    }
    
    public int[] getImportanceMap() {
        return importanceMap;
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
            return Genome.fromJson(Utils.readTextFile(latestGenome));
        }
        return null;
    }
    
    public int listGenomeFiles(final List<File> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        final GenomesLister lister = new GenomesLister(output);
        genomesDirectory.listFiles(lister);
        return lister.getCount();
    }
    
    public int countGenomeFiles() {
        final GenomesCounter counter = new GenomesCounter();
        genomesDirectory.listFiles(counter);
        return counter.getCount();
    }
    
    public File storeGenome(Genome genome) throws IOException {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        
        final String number = pad(genome.selected, 6);
        final File genomeFile = new File(genomesDirectory, number + ".genome");
        
        if (genomeFile.exists())
            throw new IllegalArgumentException("File already exists: " + genomeFile);
        
        final PrintWriter writer = new PrintWriter(genomeFile);
        try {
            writer.print(Genome.toJson(genome));
        } finally {
            writer.close();
        }

        return genomeFile;
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
            if (!directory.isDirectory())
                throw new IOException("Not a directory: " + directory);
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
    
    private static class GenomesCounter implements FileFilter {
        
        private int count = 0;

        public int getCount() {
            return count;
        }
        
        @Override
        public boolean accept(File pathname) {
            if (pathname.isFile() && pathname.getName().endsWith(".genome")) {
                ++count;
            }
            return false;
        }
    }
    
    private static class GenomesLister implements FileFilter {
        
        private int count = 0;
        private final List<File> list;

        public GenomesLister(List<File> list) {
            Preconditions.checkNotNull(list, "The parameter 'list' must not be null");
            this.list = list;
        }
        
        public int getCount() {
            return count;
        }

        @Override
        public boolean accept(File pathname) {
            if (pathname.isFile() && pathname.getName().endsWith(".genome")) {
                ++count;
                list.add(pathname);
            }
            return false;
        }
    }
}
