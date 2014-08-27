package ch.brotzilla.monalisa.io;

import java.awt.geom.AffineTransform;
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
import ch.brotzilla.monalisa.utils.BoundingBox;
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

    public SessionManager(Params params) throws IOException, SQLiteException {
        Preconditions.checkNotNull(params, "The parameter 'params' must not be null");
        
        this.params = params;
        this.sessionResumed = params.getSessionToResume() != null;
        
        final ImageData targetImage, importanceMap;
        final int numberOfGenomes;
        final Genome latestGenome;
        
        if (this.sessionResumed) {
            this.sessionName = extractSessionName(params.getSessionToResume());
            this.databaseFile = params.getSessionToResume().getAbsoluteFile();
            try (final Database db = Database.openDatabase(databaseFile)) {
                targetImage = db.queryImage("target-image");
                Preconditions.checkNotNull(targetImage, "Database contains no target image");
                Preconditions.checkState(targetImage.getType() == ImageType.ARGB, "Target image type is not supported (" + targetImage.getType() + ")");
                importanceMap = db.queryImage("importance-map");
                numberOfGenomes = db.queryNumberOfGenomes();
                latestGenome = db.queryLatestGenome();
            }
        } else {
            this.sessionName = extractSessionName(params.getTargetImageFile());
            this.databaseFile = new File(params.getSessionRootFolder(), sessionName + ".mldb");
            targetImage = ImageData.createFrom(ImageIO.read(params.getTargetImageFile()), ImageType.ARGB);
            if (params.getImportanceMapFile() != null) {
                importanceMap = ImageData.createFrom(ImageIO.read(params.getImportanceMapFile()), ImageType.Gray);
            } else {
                importanceMap = null;
            }
            numberOfGenomes = 0;
            latestGenome = null;
            try (final Database db = Database.createDatabase(databaseFile)) {
                try (final Transaction t = db.begin()) {
                    db.insertImage("target-image", params.getTargetImageFile().getAbsolutePath(), targetImage);
                    if (importanceMap != null) {
                        db.insertImage("importance-map", params.getImportanceMapFile().getAbsolutePath(), importanceMap);
                    }
                }
            }
        }
        
        this.vectorizerContext = new VectorizerContext(targetImage, importanceMap, numberOfGenomes, latestGenome);
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
    
    public VectorizerContext getVectorizerContext() {
        return vectorizerContext;
    }
    
    public Database connect() throws IOException, SQLiteException {
        return Database.openDatabase(databaseFile);
    }
    
    public File exportSVG(Genome genome, File target, boolean clipped, boolean autoName, boolean replaceIfExists) throws IOException {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        Preconditions.checkNotNull(target, "The parameter 'target' must not be null");
        if (autoName) {
            Preconditions.checkArgument(target.isDirectory(), "The parameter 'target' has to be a directory");
        }
        
        final File exportFile;
        if (autoName) {
            exportFile = new File(target, sessionName + '-' + Strings.padStart(genome.numberOfImprovements+"", 6, '0') + (clipped ? "-clipped" : "") + ".svg");
        } else {
            exportFile = target;
        }
        
        if (exportFile.exists() && !replaceIfExists)
            throw new IOException("File already exists: " + exportFile);
        
        final DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        final SVGDocument doc = (SVGDocument) impl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
        final SVGGraphics2D svg = new SVGGraphics2D(doc);

        if (clipped) {
            svg.setClip(0, 0, getWidth(), getHeight());
        } else {
            final BoundingBox b = genome.computeBoundingBox();
            final int tx = b.getXMin() < 0 ? -b.getXMin() : 0;
            final int ty = b.getYMin() < 0 ? -b.getYMin() : 0;
            svg.setTransform(AffineTransform.getTranslateInstance(tx, ty));
        }
        
        genome.renderGenes(svg);

        svg.stream(exportFile.toString());
        
        return exportFile;
    }
    
    public File exportTargetImage(File target, boolean autoName, boolean replaceIfExists) throws IOException {
        Preconditions.checkNotNull(target, "The parameter 'target' must not be null");
        if (autoName) {
            Preconditions.checkArgument(target.isDirectory(), "The parameter 'target' has to be a directory");
        }
        
        final File exportFile;
        if (autoName) {
            exportFile = new File(target, sessionName + ".png");
        } else {
            exportFile = target;
        }

        if (exportFile.exists() && !replaceIfExists)
            throw new IOException("File already exists: " + exportFile);
        
        ImageIO.write(ImageData.createBufferedImage(vectorizerContext.getTargetImage()), "PNG", exportFile);
        
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
