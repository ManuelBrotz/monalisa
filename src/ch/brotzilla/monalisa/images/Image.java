package ch.brotzilla.monalisa.images;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import com.google.common.base.Preconditions;

public class Image {

    private final ImageType type;
    private final int width, height;
    private final BufferedImage image;
    private final Object buffer;

    private final Graphics2D graphics;
    private final WritableRaster raster;
    
    private void setRenderingHints() {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    
    public Image(ImageType type, int width, int height) {
        Preconditions.checkNotNull(type, "The parameter 'type' must not be null");
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        this.type = type;
        this.width = width;
        this.height = height;
        this.image = type.createCompatibleImage(width, height);
        this.buffer = type.createCompatibleArray(width * height);
        this.graphics = image.createGraphics();
        this.raster = image.getRaster();
        setRenderingHints();
    }
    
    public Image(BufferedImage image) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        final int t = image.getType();
        ImageType.check(t);
        this.type = ImageType.from(t);
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.image = image;
        this.buffer = type.createCompatibleArray(width * height);
        this.graphics = image.createGraphics();
        this.raster = image.getRaster();
    }
    
    public final ImageType getType() {
        return type;
    }
    
    public final int getWidth() {
        return width;
    }
    
    public final int getHeight() {
        return height;
    }
    
    public final Object getBufferObject() {
        return buffer;
    }
    
    @SuppressWarnings("unchecked")
    public final <T> T getBuffer() {
        return (T) buffer;
    }

    public final BufferedImage getImage() {
        return image;
    }
    
    public final Graphics2D getGraphics() {
        return graphics;
    }

    public final WritableRaster getRaster() {
        return raster;
    }
    
    public final void updateBuffer() {
        raster.getDataElements(0, 0, width, height, buffer);
    }
    
    @SuppressWarnings("unchecked")
    public final <T> T readData() {
        return (T) raster.getDataElements(0, 0, width, height, buffer);
    }
}
