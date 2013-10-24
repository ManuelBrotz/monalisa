package ch.brotzilla.monalisa.vectorizer;

import ch.brotzilla.monalisa.images.ImageData;

import com.google.common.base.Preconditions;

public class VectorizerContext {
    
    protected final ImageData targetImage, importanceMap;

    public VectorizerContext(ImageData targetImage, ImageData importanceMap) {
        Preconditions.checkNotNull(targetImage, "The parameter 'targetImage' must not be null");
        this.targetImage = targetImage;
        this.importanceMap = importanceMap;
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

}
