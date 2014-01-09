package ch.brotzilla.monalisa.vectorizer;

import java.util.Arrays;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.ImageData;

import com.google.common.base.Preconditions;

public class VectorizerContext {
    
    // image data
    private final ImageData targetImage, importanceMap;
    private final int[] targetImageData, importanceMapData;

    // vectorization state
    private Genome latestGenome;
    private int numberOfGenomes, numberOfMutations, numberOfImprovements;

    public VectorizerContext(ImageData targetImage, ImageData importanceMap, int numberOfGenomes, Genome latestGenome) {
        Preconditions.checkNotNull(targetImage, "The parameter 'targetImage' must not be null");
        this.targetImage = targetImage;
        this.importanceMap = importanceMap;
        this.targetImageData = targetImage.getBuffer();
        if (importanceMap != null) {
            this.importanceMapData = importanceMap.getBuffer();
        } else {
            this.importanceMapData = new int[targetImage.getWidth() * targetImage.getHeight()];
            Arrays.fill(importanceMapData, 255);
        }
        this.numberOfGenomes = numberOfGenomes;
        this.latestGenome = latestGenome;
        if (latestGenome != null) {
            this.numberOfMutations = latestGenome.numberOfMutations;
            this.numberOfImprovements = latestGenome.numberOfImprovements;
        } else {
            this.numberOfMutations = 0;
            this.numberOfImprovements = 0;
        }
    }
    
    public int getWidth() {
        return targetImage.getWidth();
    }
    
    public int getHeight() {
        return targetImage.getHeight();
    }
    
    public ImageData getTargetImage() {
        return targetImage;
    }
    
    public ImageData getImportanceMap() {
        return importanceMap;
    }
    
    public int[] getTargetImageData() {
        return targetImageData;
    }
    
    public int[] getImportanceMapData() {
        return importanceMapData;
    }
    
    public Genome getLatestGenome() {
        return latestGenome;
    }
    
    public synchronized void setLatestGenome(Genome value) {
        this.latestGenome = value;
    }
    
    public int getNumberOfGenomes() {
        return numberOfGenomes;
    }
    
    public synchronized void setNumberOfGenomes(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        numberOfGenomes = value;
    }
    
    public synchronized int incNumberOfGenomes() {
        return ++numberOfGenomes;
    }

    public int getNumberOfMutations() {
        return numberOfMutations;
    }
    
    public synchronized void setNumberOfMutations(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        numberOfMutations = value;
    }
    
    public synchronized int incNumberOfMutations() {
        return ++numberOfMutations;
    }

    public int getNumberOfImprovements() {
        return numberOfImprovements;
    }
    
    public synchronized void setNumberOfImprovements(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.numberOfImprovements = value;
    }
    
    public synchronized int incNumberOfImprovements() {
        return ++numberOfImprovements;
    }
}
