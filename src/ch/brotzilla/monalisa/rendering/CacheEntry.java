package ch.brotzilla.monalisa.rendering;

import java.awt.image.BufferedImage;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;

public class CacheEntry extends TempEntry {
    
    private final Gene gene;
    private final BufferedImage image;
    private final int x, y;

    public CacheEntry(Gene gene, BufferedImage image, int x, int y) {
        super();
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkArgument(x >= 0, "The parameter 'x' has to be greater than or equal to zero");
        Preconditions.checkArgument(y >= 0, "The parameter 'y' has to be greater than or equal to zero");
        this.gene = gene;
        this.image = image;
        this.x = x;
        this.y = y;
    }
    
    public Gene getGene() {
        return gene;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
}
