package ch.brotzilla.monalisa.rendering;

import java.awt.image.BufferedImage;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;

public class CacheEntry {
    
    private final Gene gene;
    private long created;
    private long touched;

    private BufferedImage image;
    private int x, y;

    public CacheEntry(Gene gene) {
        super();
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        this.gene = gene;
        this.created = System.currentTimeMillis();
        this.touched = created;
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
    
    public void setImage(BufferedImage image, int x, int y) {
        Preconditions.checkState(this.image == null, "The property 'Image' has already been set");
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkArgument(x >= 0, "The parameter 'x' has to be greater than or equal to zero");
        Preconditions.checkArgument(y >= 0, "The parameter 'y' has to be greater than or equal to zero");
        this.image = image;
        this.x = x;
        this.y = y;
    }
    
    public long getCreated() {
        return created;
    }
    
    public long getCreatedSince() {
        return (int) (System.currentTimeMillis() - created);
    }

    public long getTouched() {
        return touched;
    }
    
    public long getTouchedSince() {
        return (int) (System.currentTimeMillis() - touched);
    }
    
    public void touch() {
        touched = System.currentTimeMillis();
    }
}
