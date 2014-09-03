package ch.brotzilla.monalisa.utils;

import com.google.common.base.Preconditions;

public class BoundingBox {

    private final int xmin, xmax, ymin, ymax, width, height;
    
    public BoundingBox(int xmin, int ymin, int xmax, int ymax) {
        Preconditions.checkArgument(xmin <= xmax, "The parameter 'xmin' has to be less than or equal to the paramter 'xmax'");
        Preconditions.checkArgument(ymin <= ymax, "The parameter 'ymin' has to be less than or equal to the paramter 'ymax'");
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.width = xmax - xmin;
        this.height = ymax - ymin;
    }

    public int getXMin() {
        return xmin;
    }
    
    public int getXMax() {
        return xmax;
    }
    
    public int getYMin() {
        return ymin;
    }
    
    public int getYMax() {
        return ymax;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("{xmin = ").append(xmin).append(", xmax = ").append(xmax)
         .append(", ymin = ").append(ymin).append(", ymax = ").append(ymax)
         .append(", width = ").append(width).append(", height = ").append(height).append("}");
        return b.toString();
    }
}
