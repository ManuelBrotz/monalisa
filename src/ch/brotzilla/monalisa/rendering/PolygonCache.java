package ch.brotzilla.monalisa.rendering;

import com.google.common.base.Preconditions;

public class PolygonCache {

    private final int width, height;
    
    public PolygonCache(int width, int height) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        this.width = width;
        this.height = height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }

}
