package ch.brotzilla.monalisa.rendering;

import java.awt.Graphics2D;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.images.ImageType;

public class LayeredRenderer extends Renderer {

    protected final Image cache;
    protected int cachedLayers = 0;
    
    protected void renderCache(Genome genome) {
        final int layers = genome.genes.length;
        if (layers > 1 && layers - 1 != cachedLayers) {
            final Graphics2D g = cache.getGraphics();
            cachedLayers = layers - 1;
            super.renderBackground(genome, g);
            for (int l = 0; l < layers - 1; l++) {
                final Gene[] layer = genome.genes[l];
                for (final Gene gene : layer) {
                    gene.render(g);
                }
            }
        }
    }
    
    @Override 
    protected void renderBackground(Genome genome, Graphics2D g) {
        super.renderBackground(genome, g);
        final int layers = genome.genes.length;
        if (layers > 1) {
            renderCache(genome);
            g.drawImage(cache.getImage(), 0, 0, null);
        }
    }
    
    @Override
    protected void renderGenome(Genome genome, Graphics2D g) {
        final int layers = genome.genes.length;
        if (layers > 1) {
            for (final Gene gene: genome.getCurrentLayer()) {
                gene.render(g);
            }
        } else {
            genome.renderGenes(g);
        }
    }

    public LayeredRenderer(int width, int height, boolean autoUpdateBuffer) {
        super(width, height, autoUpdateBuffer);
        cache = new Image(ImageType.ARGB, width, height);
    }

    public LayeredRenderer(Image image, boolean autoUpdateBuffer) {
        super(image, autoUpdateBuffer);
        cache = new Image(ImageType.ARGB, image.getWidth(), image.getHeight());
    }
}
