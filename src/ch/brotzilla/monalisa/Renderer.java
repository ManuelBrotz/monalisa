package ch.brotzilla.monalisa;

import java.awt.Color;
//import java.awt.image.BufferedImage;

import ch.brotzilla.monalisa.genes.Genome;
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

    public ImageARGB getImage() {
        return image;
    }

    public int[] getData() {
        return image.data;
    }
    
//    public ImageARGB swapImage(BufferedImage image) {
//        final ImageARGB result = this.image;
//        if (image != null) {
//            Preconditions.checkArgument(image.getWidth() == width && image.getHeight() == height, "The size of the parameter 'image' has to be " + width + "x" + height);
//            this.image = new ImageARGB(image, readData);
//        } else {
//            this.image = new ImageARGB(width, height, readData);
//        }
//        return result;
//    }
//    
//    public ImageARGB swapImage(ImageARGB image) {
//        final ImageARGB result = this.image;
//        if (image != null) {
//            Preconditions.checkArgument(image.width == width && image.height == height, "The size of the parameter 'image' has to be " + width + "x" + height);
//            if (readData) 
//                Preconditions.checkArgument(image.readData, "The parameter 'image.readData' must be true");
//            this.image = image;
//        } else {
//            this.image = new ImageARGB(width, height, readData);
//        }
//        return result;
//    }

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
