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
    
    
    @Option(name = "-i", aliases = { "--input" }, metaVar = "File", usage = "the input image")
    private File inputFile;

    @Option(name = "-o", aliases = { "--output-folder" }, metaVar = "Folder", usage = "the output folder")
    private File outputFolder;

    @Option(name = "-r", aliases = { "--resume" }, metaVar = "Folder", usage = "the folder of the session to resume")
    private File sessionToResume;

    @Option(name = "-m", aliases = { "--importance-map" }, metaVar = "File", usage = "the importance map")
    private File importanceMap;
    
    @Option(name = "-b", aliases = { "--background-color" }, metaVar = "Color", usage = "the background color for the image")
    private String backgroundColorName;
    private Color backgroundColor;
    
    @Option(name = "-s", aliases = { "--seed" }, metaVar = "Number", usage = "the seed for the random number generator")
    private int seed = 0;

    @Option(name = "-t", aliases = { "--num-threads" }, metaVar = "Number", usage = "the number of threads to use")
    private int numThreads = 4;
    
    @Option(name = "-g", aliases = { "--show-gui" }, metaVar = "Yes/No", usage = "displays a simple graphical user interface")
    private boolean showGui = false;

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
    
    public File getInputFile() {
        return inputFile;
    }

    public File getOutputFolder() {
        return outputFolder;
    }
    
    public File getSessionToResume() {
        return sessionToResume;
    }
    
    public File getImportanceMap() {
        return importanceMap;
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

    public void validate() {
        if (sessionToResume != null) {
            if (inputFile != null)
                throw new IllegalArgumentException("--input cannot be used with --resume");
            if (outputFolder != null)
                throw new IllegalArgumentException("--output-folder cannot be used width --resume");
            if (!sessionToResume.isDirectory())
                throw new IllegalArgumentException("--resume has to be a directory");
        } else {
            if (inputFile == null || !inputFile.isFile())
                throw new IllegalArgumentException("--input has to be a file");
            if (outputFolder == null || (outputFolder.exists() && !outputFolder.isDirectory()))
                throw new IllegalArgumentException("--output-folder has to be a directory");
            if (!outputFolder.exists() && !outputFolder.mkdirs())
                throw new IllegalArgumentException("--output-folder cannot be created");
        }
        if (importanceMap != null && !importanceMap.isFile())
            throw new IllegalArgumentException("--importance-map has to be a file");
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