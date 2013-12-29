package ch.brotzilla.monalisa.utils;

public class BoundingBox {

    public final int xmin, xmax, ymin, ymax;
    
    public BoundingBox(int xmin, int ymin, int xmax, int ymax) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
    }

}
