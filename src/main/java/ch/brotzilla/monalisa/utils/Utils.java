package ch.brotzilla.monalisa.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.Geometry;
import ch.brotzilla.util.MersenneTwister;

import com.google.common.base.Preconditions;

public class Utils {

    public static BufferedImage readImage(File file) throws IOException {
        final BufferedImage image = ImageIO.read(file);
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            final BufferedImage image2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = image2.createGraphics();
            g.drawImage(image, 0, 0, null);
            return image2;
        }
        return image;
    }
    
    public static Point computeCentroid(Gene gene, Point output) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        return computeCentroid(gene.x, gene.y, output);
    }

    public static Point computeCentroid(int[] x, int[] y, Point output) {
        Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
        Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
        Preconditions.checkArgument(x.length == 3 && y.length == 3, "The length of the parameters 'x' and 'y' has to be equal to 3");
        final int cx = Math.round((x[0] + x[1] + x[2]) / 3f);
        final int cy = Math.round((y[0] + y[1] + y[2]) / 3f);
        if (output != null) {
            output.x = cx;
            output.y = cy;
            return output;
        }
        return new Point(cx, cy);
    }
    
    public static BoundingBox computeBoundingBox(Gene gene, int xEnlargement, int yEnlargement) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        return computeBoundingBox(gene.x, gene.y, xEnlargement, yEnlargement);
    }

    public static BoundingBox computeBoundingBox(int[] x, int[] y, int xEnlargement, int yEnlargement) {
        Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
        Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
        Preconditions.checkArgument(x.length == y.length, "The length of the parameters 'x' and 'y' has to be equal");
        final int len = x.length;
        int xmin = Integer.MAX_VALUE, xmax = Integer.MIN_VALUE, ymin = Integer.MAX_VALUE, ymax = Integer.MIN_VALUE;
        for (int i = 0; i < len; i++) {
            final int px = x[i], py = y[i];
            if (px < xmin) xmin = px;
            if (px > xmax) xmax = px;
            if (py < ymin) ymin = py;
            if (py > ymax) ymax = py;
        }
        xmin -= xEnlargement;
        xmax += xEnlargement;
        ymin -= yEnlargement;
        ymax += yEnlargement;
        return new BoundingBox(xmin, ymin, xmax, ymax);
    }

    public static BoundingBox computeBoundingBox(Gene gene, double xEnlargementFactor, double yEnlargementFactor) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        return computeBoundingBox(gene.x, gene.y, xEnlargementFactor, yEnlargementFactor);
    }

    public static BoundingBox computeBoundingBox(int[] x, int[] y, double xEnlargementFactor, double yEnlargementFactor) {
        Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
        Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
        Preconditions.checkArgument(x.length == y.length, "The length of the parameters 'x' and 'y' has to be equal");
        final int len = x.length;
        int xmin = Integer.MAX_VALUE, xmax = Integer.MIN_VALUE, ymin = Integer.MAX_VALUE, ymax = Integer.MIN_VALUE;
        for (int i = 0; i < len; i++) {
            final int px = x[i], py = y[i];
            if (px < xmin) xmin = px;
            if (px > xmax) xmax = px;
            if (py < ymin) ymin = py;
            if (py > ymax) ymax = py;
        }
        final int w = xmax - xmin, h = ymax - ymin;
        final double nw = (xmax - xmin) * xEnlargementFactor, nh = (ymax - ymin) * yEnlargementFactor;
        final double dw = (nw - w) / 2d, dh = (nh - h) / 2d;
        xmin -= dw;
        xmax += dw;
        ymin -= dh;
        ymax += dh;
        return new BoundingBox(xmin, ymin, xmax, ymax);
    }
    
    public static Gene createRandomGene(MersenneTwister rng, VectorizerConfig config) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        final EvolutionContext evolutionContext = config.getEvolutionContext();
        final VectorizerContext vectorizerContext = config.getVectorizerContext();
        final int width = config.getWidth(), height = config.getHeight(), xborder = evolutionContext.getOuterBorderX(), yborder = evolutionContext.getOuterBorderY();
        final int bwidth = width + 2 * xborder, bheight = height + 2 * yborder;
        final int[] inputData = vectorizerContext.getTargetImage().getBuffer(), x = new int[3], y = new int[3];
        x[0] = rng.nextInt(bwidth) - xborder;
        x[1] = rng.nextInt(bwidth) - xborder;
        x[2] = rng.nextInt(bwidth) - xborder;
        y[0] = rng.nextInt(bheight) - yborder;
        y[1] = rng.nextInt(bheight) - yborder;
        y[2] = rng.nextInt(bheight) - yborder;
        final Point c = Utils.computeCentroid(x, y, null);
        if (c.x >= 0 && c.x < width && c.y >= 0 && c.y < height) {
            final int color = inputData[c.y * width + c.x];
            final int alpha = rng.nextInt(256) << 24;
            return new Gene(x, y, (color & 0x00FFFFFF) | alpha);
        }
        return createRandomGene(rng, config);
    }

    public static Gene[] createRandomGenes(MersenneTwister rng, VectorizerConfig config, int minGenes, int maxGenes, GenomeFactory genomeFactory) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkArgument(minGenes > 0, "The parameter 'minGenes' must be grather than zero");
        Preconditions.checkArgument(maxGenes >= minGenes, "The parameter 'maxGenes' must be greater than or equal to 'minGenes'");
        Preconditions.checkNotNull(genomeFactory, "The parameter 'genomeFactory' must not be null");
        final int length;
        if (minGenes == maxGenes) {
            length = minGenes;
        } else {
            length = minGenes + rng.nextInt(maxGenes - minGenes + 1);
        }
        Preconditions.checkState(length >= minGenes);
        Preconditions.checkState(length <= maxGenes);
        final Gene[] genes = new Gene[length];
        for (int i = 0; i < length; i++) {
            genes[i] = genomeFactory.createGene(rng, config);
        }
        return genes;
    }
    
    public static Genome appendGene(Genome genome, MersenneTwister rng, VectorizerConfig config) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        final Gene gene = config.getGenomeFactory().createGene(rng, config);
        final Gene[] inputGenes = genome.genes;
        final Gene[] newGenes = new Gene[inputGenes.length + 1];
        System.arraycopy(inputGenes, 0, newGenes, 0, inputGenes.length);
        newGenes[newGenes.length - 1] = gene;
        return new Genome(newGenes, false);
    }

    public static Gene[] copyGenes(Gene[] genes) {
        Preconditions.checkNotNull(genes, "The parameter 'genes' must not be null");
        Preconditions.checkArgument(genes.length > 0, "The length of the parameter 'genes' has to be greater than zero");
        final int size = genes.length;
        final Gene[] result = new Gene[size];
        for (int i = 0; i < size; i++) {
            Preconditions.checkNotNull(genes[i], "The parameter 'genes[" + i + "]' must not contain null");
            result[i] = genes[i];
        }
        return result;
    }

    public static boolean isSelfIntersecting(Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        final int len = gene.x.length;
        if (len < 4) {
            return false;
        }
        final int last = len - 1;
        final int[] x = gene.x, y = gene.y;
        for (int lineA = 0; lineA < len - 2; lineA++) {
            for (int lineB = lineA + 2; lineB < len; lineB++) {
                if (lineA == 0 && lineB == last) {
                    break;
                }
                final int ax1 = x[lineA], ay1 = y[lineA];
                final int ax2 = x[lineA + 1], ay2 = y[lineA + 1];
                final int bx1 = x[lineB], by1 = y[lineB];
                final int bx2 = x[lineB == last ? 0 : lineB + 1];
                final int by2 = y[lineB == last ? 0 : lineB + 1];
                if (Geometry.isLineIntersectingLine(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int[] decodeColor(int argb, int[] output) {
        final int a = (argb >> 24) & 0x000000FF;
        final int r = (argb >> 16) & 0x000000FF;
        final int g = (argb >> 8) & 0x000000FF;
        final int b = argb & 0x000000FF;
        if (output == null || output.length != 4) {
            return new int[] { a, r, g, b };
        }
        output[0] = a;
        output[1] = r;
        output[2] = g;
        output[3] = b;
        return output;
    }

    public static byte[] decodeColor(int argb, byte[] output) {
        final byte a = (byte) ((argb >> 24) & 0x000000FF);
        final byte r = (byte) ((argb >> 16) & 0x000000FF);
        final byte g = (byte) ((argb >> 8) & 0x000000FF);
        final byte b = (byte) (argb & 0x000000FF);
        if (output == null || output.length != 4) {
            return new byte[] { a, r, g, b };
        }
        output[0] = a;
        output[1] = r;
        output[2] = g;
        output[3] = b;
        return output;
    }

    public static int encodeColor(int[] color) {
        return (color[0] << 24) | (color[1] << 16) | (color[2] << 8) | color[3];
    }

    public static int encodeColor(byte[] color) {
        return (color[0] << 24) | (color[1] << 16) | (color[2] << 8) | color[3];
    }

    public static Color decodeColor(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid color value. (" + value + ")");
        } else {
            value = value.trim();
            if (value.startsWith("#")) {
                final String tmp = value.substring(1);
                final int len = tmp.length();
                if (len == 8) {
                    final int a = Integer.parseInt(tmp.substring(0, 1), 16);
                    final int r = Integer.parseInt(tmp.substring(2, 3), 16);
                    final int g = Integer.parseInt(tmp.substring(4, 5), 16);
                    final int b = Integer.parseInt(tmp.substring(6, 7), 16);
                    return new Color(r, g, b, a);
                } else if (len == 6) {
                    final int r = Integer.parseInt(tmp.substring(0, 1), 16);
                    final int g = Integer.parseInt(tmp.substring(2, 3), 16);
                    final int b = Integer.parseInt(tmp.substring(4, 5), 16);
                    return new Color(r, g, b);
                } else if (len == 4) {
                    final int a = Integer.parseInt(tmp.substring(0, 1), 16);
                    final int v = Integer.parseInt(tmp.substring(2, 3), 16);
                    return new Color(v, v, v, a);
                } else if (len == 2) {
                    final int v = Integer.parseInt(tmp, 16);
                    return new Color(v, v, v);
                } else {
                    throw new IllegalArgumentException("Invalid color value, must be of length 8, 4, 6 or 2. (" + len + ")");
                }
            } else {
                throw new IllegalArgumentException("Invalid color value. (" + value + ")");
            }
        }
    }
    
    public static boolean equals(int[] a, int[] b) {
        if (a == null && b == null)
            return true;
        if (a == null ^ b == null)
            return false;
        if (a.length != b.length)
            return false;
        final int length = a.length;
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }
    
    public static <T> boolean equals(T[] a, T[] b) {
        if (a == null && b == null)
            return true;
        if (a == null ^ b == null)
            return false;
        if (a.length != b.length)
            return false;
        final int length = a.length;
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i] && !equals(a[i], b[i]))
                return false;
        }
        return true;
    }
    
    public static <T> boolean equals(T a, T b) {
        if (a == null && b == null)
            return true;
        if (a == null ^ b == null)
            return false;
        return a.equals(b);
    }
}
