package ch.brotzilla.monalisa.utils;

import com.google.common.base.Preconditions;

public class Constraints {

    protected final int width, height;
    protected int borderX = 50, borderY = 50;
    
    public Constraints(int width, int height) {
        Preconditions.checkArgument(width > 0 && height > 0, "The parameters 'width' and 'height' have to be greater than zero");
        this.width = width;
        this.height = height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getBorderX() {
        return borderX;
    }
    
    public Constraints setBorderX(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.borderX = value;
        return this;
    }
    
    public int getBorderY() {
        return borderY;
    }
    
    public Constraints setBorderY(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.borderY = value;
        return this;
    }
    
    public Constraints setBorder(int borderX, int borderY) {
        Preconditions.checkArgument(borderX >= 0 && borderY >= 0, "The parameters 'borderX' and 'borderY' have to be greater than or equal to zero");
        this.borderX = borderX;
        this.borderY = borderY;
        return this;
    }

}
