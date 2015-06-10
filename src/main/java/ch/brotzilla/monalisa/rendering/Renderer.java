package ch.brotzilla.monalisa.rendering;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.images.ImageType;

public abstract class Renderer {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    protected Image image;
    protected boolean autoUpdateBuffer;

    public Renderer(Image image, boolean autoUpdateBuffer) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkArgument(image.getType() == ImageType.ARGB, "The parameter 'image' has to be of type ImageType.ARGB");
        this.image = image;
        this.autoUpdateBuffer = autoUpdateBuffer;
    }
    
    public Renderer(int width, int height, boolean autoUpdateBuffer) {
        this(new Image(ImageType.ARGB, width, height), autoUpdateBuffer);
    }
    
    public boolean getAutoUpdateBuffer() {
        return autoUpdateBuffer;
    }
    
    public Image getImage() {
        return image;
    }
    
    public int getWidth() {
        return image.getWidth();
    }
    
    public int getHeight() {
        return image.getHeight();
    }
    
    public BufferedImage getBufferedImage() {
        return image.getImage();
    }

    public int[] getBuffer() {
        return image.getBuffer();
    }
    
    public int[] readData() {
        return image.readData();
    }

}
