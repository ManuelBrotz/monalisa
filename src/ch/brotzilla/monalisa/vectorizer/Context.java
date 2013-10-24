package ch.brotzilla.monalisa.vectorizer;

import ch.brotzilla.monalisa.images.ImageData;

import com.google.common.base.Preconditions;

public class Context {
    
    protected final int width, height;
    protected final ImageData targetImage, importanceMap;

    public Context(int width, int height, ImageData targetImage, ImageData importanceMap) {
        Preconditions.checkArgument(width > 0 && height > 0, "The parameters 'width' and 'height' have to be greater than zero");
        this.width = width;
        this.height = height;
        this.targetImage = Preconditions.checkNotNull(targetImage, "The parameter 'targetImage' must not be null");
        this.importanceMap = importanceMap;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public ImageData getTargetImage() {
        return targetImage;
    }
    
    public ImageData getImportanceMap() {
        return importanceMap;
    }

}
