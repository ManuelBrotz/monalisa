package ch.brotzilla.monalisa.evolution.genes;

import com.google.common.base.Preconditions;

public class RectConstraint {

    private final int x, y, width, height;
    
    public RectConstraint(int x, int y, int width, int height) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zeo");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
}
