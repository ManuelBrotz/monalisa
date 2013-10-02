package ch.brotzilla.monalisa.io;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.Params;
import ch.brotzilla.monalisa.utils.Utils;

import com.google.common.base.Preconditions;

public class SessionManager {

    protected final Params params;
    
    protected final boolean isSessionResumed;
    protected final String sessionName;

    protected final File sessionDirectory;
    protected final File genomesDirectory;

    protected final File inputImageFile;
    protected final File importanceMapFile;
    
    protected final Context context;
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
    
    protected BufferedImage importImportanceMap(File input, File output, Context c) throws IOException {
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
    
    protected BufferedImage loadImportanceMap(File input, Context c) throws IOException {
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
    
    protected int[] extractImportanceMap(BufferedImage input, int width, int height) {
        if (input != null) {
            Preconditions.checkArgument(input.getWidth() == width, "The width of the parameter 'input' does not match");
            Preconditions.checkArgument(input.getHeight() == height, "The height of the parameter 'input' does not match");
            final WritableRaster raster = input.getRaster();
            final byte[] raw = (byte[]) raster.getDataElements(0, 0, width, height, null);
            final int[] map = new int[raw.length];
            for (int i = 0; i < raw.length; i++) {
                map[i] = raw[i] & 0xFF;
            }
            return map;
        } else {
            final int[] map = new int[width * height];
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
            this.genomesDirectory = checkDir(new File(this.sessionDirectory, "genomes"), true).getAbsoluteFile();
            this.inputImageFile = new File(this.sessionDirectory, "input.png").getAbsoluteFile();
            this.importanceMapFile = new File(this.sessionDirectory, "importance-map.png").getAbsoluteFile();
            this.isSessionResumed = false;
            inputImage = importInputImage(params.getInputFile(), this.inputImageFile);
            importanceMap = importImportanceMap(params.getImportanceMap(), this.importanceMapFile, this.context);
        } else {
            this.sessionDirectory = checkDir(params.getSessionToResume(), false).getAbsoluteFile();
            this.sessionName = this.sessionDirectory.getName();
            this.genomesDirectory = checkDir(new File(this.sessionDirectory, "genomes"), false).getAbsoluteFile();
            this.inputImageFile = new File(this.sessionDirectory, "input.png").getAbsoluteFile();
            this.importanceMapFile = new File(this.sessionDirectory, "importance-map.png").getAbsoluteFile();
            this.isSessionResumed = true;
            inputImage = Utils.readImage(this.inputImageFile);
            importanceMap = loadImportanceMap(this.importanceMapFile, this.context);
        }
        this.inputPixelData = extractInputPixelData(inputImage);
        this.importanceMap = extractImportanceMap(importanceMap, inputImage.getWidth(), inputImage.getHeight());
        this.context = new Context(inputImage.getWidth(), inputImage.getHeight(), this.inputPixelData, this.importanceMap);
    }
    
    public boolean isSessionReady() {
        return (sessionDirectory != null && genomesDirectory != null) && (sessionDirectory.isDirectory() && genomesDirectory.isDirectory());
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

    @Deprecated
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
        return context.getWidth();
    }
    
    public int getHeight() {
        return context.getHeight();
    }
    
    public Context getContext() {
        return context;
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
    
    @Deprecated
    public int listGenomeFiles(final List<File> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        final GenomesLister lister = new GenomesLister(output);
        genomesDirectory.listFiles(lister);
        return lister.getCount();
    }
    
    public int loadGenomes(final List<Genome> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        final LinkedList<File> files = new LinkedList<File>();
        final GenomesLister lister = new GenomesLister(files);
        genomesDirectory.listFiles(lister);
        final TextReader reader = new TextReader(1024 * 100);
        int count = 0;
        for (final File file : files) {
            try {
                final Genome genome = Genome.fromJson(reader.readTextFile(file));
                output.add(genome);
                count++;
            } catch (IOException e) {
                System.out.println("Error loading genome: " + file);
                e.printStackTrace();
            }
        }
        return count;
    }
    
    @Deprecated
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
        writer.print(Genome.toJson(genome));
        writer.close();

        return genomeFile;
    }
    
    public File exportSVG(Genome genome, File folder) throws IOException {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        Preconditions.checkNotNull(folder, "The parameter 'folder' must not be null");
        Preconditions.checkArgument(folder.isDirectory(), "The parameter 'folder' has to be a directory");
        
        final File exportFile = new File(folder, sessionName + '-' + pad(genome.selected, 6) + ".svg");
        
        if (exportFile.exists())
            throw new IllegalArgumentException("File already exists: " + exportFile);
        
        final DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        final SVGDocument doc = (SVGDocument) impl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
        final SVGGraphics2D svg = new SVGGraphics2D(doc);

        genome.renderGenes(svg);

        svg.stream(exportFile.toString());
        
        return exportFile;
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
