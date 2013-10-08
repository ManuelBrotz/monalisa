package ch.brotzilla.monalisa.utils;

import ch.brotzilla.monalisa.images.ImageData;

import com.google.common.base.Preconditions;

public class Context {
    
    protected final int width, height;
    protected final ImageData targetImage, importanceMap;

    protected int borderX = 50, borderY = 50;
    
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

    public int getBorderX() {
        return borderX;
    }
    
    public Context setBorderX(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.borderX = value;
        return this;
    }
    
    public int getBorderY() {
        return borderY;
    }
    
    public Context setBorderY(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.borderY = value;
        return this;
    }
    
    public Context setBorder(int borderX, int borderY) {
        Preconditions.checkArgument(borderX >= 0 && borderY >= 0, "The parameters 'borderX' and 'borderY' have to be greater than or equal to zero");
        this.borderX = borderX;
        this.borderY = borderY;
        return this;
    }

}
