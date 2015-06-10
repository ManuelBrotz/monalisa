package ch.brotzilla.monalisa.rendering;

import java.awt.Graphics2D;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.Image;

import com.google.common.base.Preconditions;

public class CachingRenderer extends GenomeRenderer {

    private final PolygonCache cache;
    
    @Override
    protected void renderGenome(Genome genome, Graphics2D g) {
        for (final Gene gene : genome.genes) {
            CacheEntry entry = cache.get(gene);
            if (entry != null) {
                g.drawImage(entry.getImage(), entry.getX(), entry.getY(), null);
            } else {
                gene.render(g);
            }
        }
    }

    public CachingRenderer(PolygonCache cache, int width, int height, boolean autoUpdateBuffer) {
        super(width, height, autoUpdateBuffer);
        Preconditions.checkNotNull(cache, "The parameter 'cache' must not be null");
        this.cache = cache;
    }
    
    public CachingRenderer(PolygonCache cache, Image image, boolean autoUpdateBuffer) {
        super(image, autoUpdateBuffer);
        Preconditions.checkNotNull(cache, "The parameter 'cache' must not be null");
        this.cache = cache;
    }
}
