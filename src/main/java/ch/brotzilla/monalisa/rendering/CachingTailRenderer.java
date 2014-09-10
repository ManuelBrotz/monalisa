package ch.brotzilla.monalisa.rendering;

import java.awt.Graphics2D;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.images.ImageType;

public class CachingTailRenderer extends Renderer {

    protected final int numberOfGenesToRender;
    protected final Image cache;
    protected int numberOfGenesCached = 0;
    
    protected void renderCache(Genome genome) {
        final Gene[] genes = genome.genes;
        final int numberOfGenes = genes.length;
        final int numberOfGenesToCache = Math.max(numberOfGenes - numberOfGenesToRender, 0);
        if (numberOfGenesToCache > numberOfGenesCached) {
            final Graphics2D g = cache.getGraphics();
            numberOfGenesCached = numberOfGenesToCache;
            super.renderBackground(genome, g);
            for (int i = 0; i < numberOfGenesToCache; i++) {
                genes[i].render(g);
            }
        }
    }
    
    @Override 
    protected void renderBackground(Genome genome, Graphics2D g) {
        super.renderBackground(genome, g);
        renderCache(genome);
        g.drawImage(cache.getImage(), 0, 0, null);
    }
    
    @Override
    protected void renderGenome(Genome genome, Graphics2D g) {
        final Gene[] genes = genome.genes;
        final int numberOfGenes = genes.length;
        for (int i = numberOfGenesCached; i < numberOfGenes; i++) {
            genes[i].render(g);
        }
    }

    public CachingTailRenderer(int numberOfGenesToRender, int width, int height, boolean autoUpdateBuffer) {
        super(width, height, autoUpdateBuffer);
        Preconditions.checkArgument(numberOfGenesToRender > 0, "The parameter 'numberOfGenesToRender' has to be greater than zero");
        this.numberOfGenesToRender = numberOfGenesToRender;
        this.cache = new Image(ImageType.ARGB, width, height);
    }

    public CachingTailRenderer(int numberOfGenesToRender, Image image, boolean autoUpdateBuffer) {
        super(image, autoUpdateBuffer);
        Preconditions.checkArgument(numberOfGenesToRender > 0, "The parameter 'numberOfGenesToRender' has to be greater than zero");
        this.numberOfGenesToRender = numberOfGenesToRender;
        this.cache = new Image(ImageType.ARGB, image.getWidth(), image.getHeight());
    }
    
    public int getNumberOfGenesToRender() {
        return numberOfGenesToRender;
    }
}
