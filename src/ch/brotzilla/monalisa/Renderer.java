package ch.brotzilla.monalisa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import com.google.common.base.Preconditions;

public class Renderer {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    protected final int width, height;
    protected final BufferedImage image;
    protected final Graphics2D graphics;
    protected final boolean readData;
    protected final WritableRaster raster;
    protected final int[] data;

    public Renderer(int width, int height, boolean readData) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.graphics = image.createGraphics();
        this.readData = readData;
        if (readData) {
            this.raster = image.getRaster();
            this.data = new int[width * height];
        } else {
            this.raster = null;
            this.data = null;
        }
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int[] getData() {
        return data;
    }

    public void render(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        if (genome.background == null) {
            graphics.setBackground(TRANSPARENT);
        } else {
            graphics.setBackground(genome.background);
        }
        graphics.clearRect(0, 0, width, height);
        genome.renderGenes(graphics);
        if (readData) {
            raster.getDataElements(0, 0, width, height, data);
        }
    }

}
