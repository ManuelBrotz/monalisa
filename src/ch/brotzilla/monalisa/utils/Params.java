package ch.brotzilla.monalisa.utils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public class Params {

    private final CmdLineParser parser;
    private final String arguments;
    private int numArguments = 0;
    private boolean isValid = false, isInitialized = false;
    private Exception error = null;
    
    @Option(name = "--image", metaVar = "File", usage = "the input image file")
    private File targetImageFile;

    @Option(name = "--map", metaVar = "File", usage = "the importance map file")
    private File importanceMap;
    
    @Option(name = "--root", metaVar = "Folder", usage = "the session root folder where the new session file will be created")
    private File sessionRoot;

    @Option(name = "--resume", metaVar = "Session", usage = "the *.mldb file to resume")
    private File sessionToResume;

    @Option(name = "--background-color", metaVar = "Color", usage = "the background color to use")
    private String backgroundColorName;
    private Color backgroundColor;
    
    @Option(name = "--seed", metaVar = "Number", usage = "the seed for the random number generator")
    private int seed = 0;

    @Option(name = "--num-threads", metaVar = "Number", usage = "the number of threads to use")
    private int numThreads = 4;
    
    @Option(name = "--show-gui", metaVar = "Switch", usage = "displays a simple graphical user interface")
    private boolean showGui = false;

    @Option(name = "--export-latest", metaVar = "File", usage = "exports the latest genome file as an svg document to the specified directory")
    private File exportLatest;
    
    public Params(String[] args) {
        Preconditions.checkNotNull(args, "The parameter 'args' must not be null");
        parser = new CmdLineParser(this);
        parser.setUsageWidth(80);
        arguments = Joiner.on(" ").join(args);
        try {
            parser.parseArgument(args);
            numArguments = parser.getArguments().size();
            validate();
            init();
        } catch (Exception e) {
            error = e;
        }
    }

    public CmdLineParser getParser() {
        return parser;
    }
    
    public String getArguments() {
        return arguments;
    }
    
    public int getNumArguments() {
        return numArguments;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public boolean isReady() {
        return isValid && isInitialized && error == null;
    }
    
    public Exception getError() {
        return error;
    }
    
    public File getTargetImageFile() {
        return targetImageFile;
    }

    public File getImportanceMapFile() {
        return importanceMap;
    }

    public File getSessionRootFolder() {
        return sessionRoot;
    }
    
    public File getSessionToResume() {
        return sessionToResume;
    }
    
    public int getSeed() {
        return seed;
    }
    
    public int getNumThreads() {
        return numThreads;
    }
    
    public boolean getShowGui() {
        return showGui;
    }

    public String getBackgroundColorName() {
        return backgroundColorName;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }
    
    public File getExportLatest() {
        return exportLatest;
    }

    public void validate() {
        if (sessionToResume != null) {
            if (targetImageFile != null)
                throw new IllegalArgumentException("--image cannot be used with --resume");
            if (sessionRoot != null)
                throw new IllegalArgumentException("--root cannot be used width --resume");
            if (!sessionToResume.isFile())
                throw new IllegalArgumentException("--resume has to be a file");
        } else {
            if (targetImageFile == null || !targetImageFile.isFile())
                throw new IllegalArgumentException("--image has to be a file");
            if (sessionRoot == null || !sessionRoot.isDirectory())
                throw new IllegalArgumentException("--root has to be a directory");
        }
        if (importanceMap != null && !importanceMap.isFile())
            throw new IllegalArgumentException("--map has to be a file");
        if (exportLatest != null && !exportLatest.isDirectory()) 
            throw new IllegalArgumentException("--export-latest has to be a directory");
        if (numThreads < 1) 
            throw new IllegalArgumentException("--num-threads must be greater than or equal to 1");
        isValid = true;
    }

    public void init() throws IOException {
        if (seed == 0) {
            seed = (new Random()).nextInt();
        }
        if (backgroundColorName != null && !backgroundColorName.isEmpty()) {
            try {
                backgroundColor = Utils.decodeColor(backgroundColorName);
            } catch (Exception e) {
                throw new IllegalArgumentException("--background-color is not a valid color (" + backgroundColorName + ")");
            }
        }
        isInitialized = true;
    }
}