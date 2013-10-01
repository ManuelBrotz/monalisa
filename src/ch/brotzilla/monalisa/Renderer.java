package ch.brotzilla.monalisa;

import java.awt.Color;
//import java.awt.image.BufferedImage;

import java.awt.image.BufferedImage;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.ImageARGB;

import com.google.common.base.Preconditions;

public class Renderer {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public final int width, height;
    public final boolean readData;
    
    protected ImageARGB image;
    
    public Renderer(int width, int height, boolean readData) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        this.width = width;
        this.height = height;
        this.readData = readData;
        this.image = new ImageARGB(width, height, readData);
    }
    
    public Renderer(ImageARGB image) {
        this.image = Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        this.width = image.width;
        this.height = image.height;
        this.readData = image.readData;
    }

    public ImageARGB getImage() {
        return image;
    }
    
    public BufferedImage getBufferedImage() {
        return image.image;
    }

    public int[] getData() {
        return image.data;
    }
    
    public void render(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        if (genome.background == null) {
            image.graphics.setBackground(TRANSPARENT);
        } else {
            image.graphics.setBackground(genome.background);
        }
        image.graphics.clearRect(0, 0, width, height);
        genome.renderGenes(image.graphics);
        if (readData) {
            image.readData();
        }
    }

}
