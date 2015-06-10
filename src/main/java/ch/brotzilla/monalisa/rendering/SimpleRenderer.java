package ch.brotzilla.monalisa.rendering;

import java.awt.Graphics2D;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.Image;

public class SimpleRenderer extends GenomeRenderer {

    @Override
    protected void renderGenome(Genome genome, Graphics2D g) {
        genome.renderGenes(g);
    }

    public SimpleRenderer(int width, int height, boolean autoUpdateBuffer) {
        super(width, height, autoUpdateBuffer);
    }
    
    public SimpleRenderer(Image image, boolean autoUpdateBuffer) {
        super(image, autoUpdateBuffer);
    }

}
