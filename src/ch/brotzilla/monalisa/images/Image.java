package ch.brotzilla.monalisa.images;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import com.google.common.base.Preconditions;

public abstract class Image {

    public final int width, height, size, length;
    public final boolean readData;
    public final BufferedImage image;
    public final Graphics2D graphics;
    
    private final WritableRaster raster;
    
    protected abstract int calculateLengthInBytes(int size);
    
    protected abstract BufferedImage createImage(int width, int height);
    protected abstract BufferedImage checkImage(BufferedImage image);
    protected abstract BufferedImage convertImageData(ImageData image);
    
    protected abstract void internalReadData(WritableRaster raster);
    
    protected void setRenderingHints() {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    public Image(int width, int height, boolean readData) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        this.width = width;
        this.height = height;
        this.readData = readData;
        this.size = width * height;
        this.length = calculateLengthInBytes(size);
        this.image = Preconditions.checkNotNull(createImage(width, height), "The internal method 'createImage()' must not return null");
        this.graphics = image.createGraphics();
        setRenderingHints();
        if (readData) {
            this.raster = image.getRaster();
        } else {
            this.raster = null;
        }
    }
    
    public Image(BufferedImage image, boolean readData) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        this.image = Preconditions.checkNotNull(checkImage(image), "The internal method 'checkImage()' must not return null");
        this.readData = readData;
        this.width = this.image.getWidth();
        this.height = this.image.getHeight();
        this.size = this.width * this.height;
        this.length = calculateLengthInBytes(size);
        this.graphics = this.image.createGraphics();
        setRenderingHints();
        if (readData) {
            this.raster = this.image.getRaster();
        } else {
            this.raster = null;
        }
    }
    
    public Image(ImageData image, boolean readData) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        this.image = Preconditions.checkNotNull(convertImageData(image), "The internal method 'convertImageData()' must not return null");
        this.readData = readData;
        this.width = this.image.getWidth();
        this.height = this.image.getHeight();
        this.size = this.width * this.height;
        this.length = calculateLengthInBytes(size);
        this.graphics = this.image.createGraphics();
        setRenderingHints();
        if (readData) {
            this.raster = this.image.getRaster();
        } else {
            this.raster = null;
        }
    }
    
    public final void readData() {
        if (!readData)
            throw new IllegalStateException("Unable to read image data.");
        internalReadData(raster);
    }
}
