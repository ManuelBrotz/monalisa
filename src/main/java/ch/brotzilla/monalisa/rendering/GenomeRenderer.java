package ch.brotzilla.monalisa.rendering;

import java.awt.Graphics2D;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.images.ImageType;

import com.google.common.base.Preconditions;

public abstract class GenomeRenderer extends Renderer {

    protected void renderBackground(Genome genome, Graphics2D g) {
        g.setBackground(TRANSPARENT);
        g.clearRect(0, 0, image.getWidth(), image.getHeight());
    }
    
    protected abstract void renderGenome(Genome genome, Graphics2D g);
    
    public GenomeRenderer(int width, int height, boolean autoUpdateBuffer) {
        super(new Image(ImageType.ARGB, width, height), autoUpdateBuffer);
    }
    
    public GenomeRenderer(Image image, boolean autoUpdateBuffer) {
        super(image, autoUpdateBuffer);
    }

    public final void render(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        final Graphics2D g = image.getGraphics();
        renderBackground(genome, g);
        renderGenome(genome, g);
        if (autoUpdateBuffer) {
            image.updateBuffer();
        }
    }

}
