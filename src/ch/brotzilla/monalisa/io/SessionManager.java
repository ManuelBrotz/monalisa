package ch.brotzilla.monalisa.io;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

import ch.brotzilla.monalisa.db.Database;
import ch.brotzilla.monalisa.db.Database.Transaction;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.ImageData;
import ch.brotzilla.monalisa.images.ImageType;
import ch.brotzilla.monalisa.utils.Params;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class SessionManager {

    protected final Params params;
    
    protected final String sessionName;
    protected final boolean sessionResumed;
    
    protected final File databaseFile;

    protected final VectorizerContext vectorizerContext;
    protected final ImageData targetImage;
    protected final ImageData importanceMap;
    
    protected final int numberOfGenomes;
    protected final Genome latestGenome;

    public SessionManager(Params params) throws IOException, SQLiteException {
        Preconditions.checkNotNull(params, "The parameter 'params' must not be null");
        this.params = params;
        this.sessionResumed = params.getSessionToResume() != null;
        if (this.sessionResumed) {
            this.sessionName = extractSessionName(params.getSessionToResume());
            this.databaseFile = params.getSessionToResume().getAbsoluteFile();
            try (final Database db = Database.openDatabase(databaseFile)) {
                this.targetImage = db.queryImage("target-image");
                Preconditions.checkNotNull(targetImage, "Database contains no target image");
                Preconditions.checkState(targetImage.getType() == ImageType.ARGB, "Target image type is not supported (" + targetImage.getType() + ")");
                this.importanceMap = db.queryImage("importance-map");
                this.numberOfGenomes = db.queryNumberOfGenomes();
                this.latestGenome = db.queryLatestGenome();
            }
        } else {
            this.sessionName = extractSessionName(params.getTargetImageFile());
            this.databaseFile = new File(params.getSessionRootFolder(), sessionName + ".mldb");
            this.targetImage = ImageData.createFrom(ImageIO.read(params.getTargetImageFile()), ImageType.ARGB);
            if (params.getImportanceMapFile() != null) {
                this.importanceMap = ImageData.createFrom(ImageIO.read(params.getImportanceMapFile()), ImageType.Gray);
            } else {
                this.importanceMap = null;
            }
            this.numberOfGenomes = 0;
            this.latestGenome = null;
            try (final Database db = Database.createDatabase(databaseFile)) {
                try (final Transaction t = db.begin()) {
                    db.insertImage("target-image", params.getTargetImageFile().getAbsolutePath(), targetImage);
                    if (importanceMap != null) {
                        db.insertImage("importance-map", params.getImportanceMapFile().getAbsolutePath(), importanceMap);
                    }
                }
            }
        }
        this.vectorizerContext = new VectorizerContext(targetImage, importanceMap);
    }
    
    public boolean isSessionResumed() {
        return sessionResumed;
    }
    
    public Params getParams() {
        return params;
    }

    public String getSessionName() {
        return sessionName;
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    public int getWidth() {
        return vectorizerContext.getWidth();
    }
    
    public int getHeight() {
        return vectorizerContext.getHeight();
    }
    
    public ImageData getTargetImage() {
        return targetImage;
    }
    
    public ImageData getImportanceMap() {
        return importanceMap;
    }
    
    public int getNumberOfGenomes() {
        return numberOfGenomes;
    }
    
    public Genome getLatestGenome() {
        return latestGenome;
    }
    
    public VectorizerContext getVectorizerContext() {
        return vectorizerContext;
    }
    
    public Database connect() throws IOException, SQLiteException {
        return Database.openDatabase(databaseFile);
    }
    
    public File exportSVG(Genome genome, File folder, boolean clipped) throws IOException {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        Preconditions.checkNotNull(folder, "The parameter 'folder' must not be null");
        Preconditions.checkArgument(folder.isDirectory(), "The parameter 'folder' has to be a directory");
        
        final File exportFile = new File(folder, sessionName + '-' + Strings.padStart(genome.selected+"", 6, '0') + (clipped ? "-clipped" : "") + ".svg");
        
        if (exportFile.exists())
            throw new IllegalArgumentException("File already exists: " + exportFile);
        
        final DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        final SVGDocument doc = (SVGDocument) impl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
        final SVGGraphics2D svg = new SVGGraphics2D(doc);

        if (clipped) {
            svg.setClip(0, 0, getWidth(), getHeight());
        }
        
        genome.renderGenes(svg);

        svg.stream(exportFile.toString());
        
        return exportFile;
    }
    
    private String extractSessionName(File input) {
        final String filename = input.getName();
        final int index = filename.lastIndexOf(".");
        if (index > -1) {
            return filename.substring(0, index);
        }
        return filename;
    }
}
