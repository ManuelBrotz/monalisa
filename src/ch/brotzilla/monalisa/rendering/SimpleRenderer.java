package ch.brotzilla.monalisa.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.images.ImageType;

import com.google.common.base.Preconditions;

public class SimpleRenderer {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private Image image;
    private boolean autoUpdateBuffer;
    
    public SimpleRenderer(int width, int height, boolean autoUpdateBuffer) {
        this.image = new Image(ImageType.ARGB, width, height);
        this.autoUpdateBuffer = autoUpdateBuffer;
    }
    
    public SimpleRenderer(Image image, boolean autoUpdateBuffer) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkArgument(image.getType() == ImageType.ARGB, "The parameter 'image' has to be of type ImageType.ARGB");
        this.image = image;
        this.autoUpdateBuffer = autoUpdateBuffer;
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
    
    public void render(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        final Graphics2D g = image.getGraphics();
        if (genome.background == null) {
            g.setBackground(TRANSPARENT);
        } else {
            g.setBackground(genome.background);
        }
        g.clearRect(0, 0, image.getWidth(), image.getHeight());
        genome.renderGenes(g);
        if (autoUpdateBuffer) {
            image.updateBuffer();
        }
    }
}
