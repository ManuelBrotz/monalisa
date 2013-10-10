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
import ch.brotzilla.monalisa.io.TextReader;
import ch.brotzilla.monalisa.rendering.Renderer;

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
    
    public static String readTextFile(File file) throws IOException {
        final TextReader reader = new TextReader(1024 * 10);
        return reader.readTextFile(file);
    }

    public static Point computeCentroid(Gene gene, Point output) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        return computeCentroid(gene.x, gene.y, output);
    }

    public static Point computeCentroid(int[] x, int[] y, Point output) {
        Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
        Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
        final int cx = Math.round((x[0] + x[1] + x[2]) / 3f);
        final int cy = Math.round((y[0] + y[1] + y[2]) / 3f);
        if (output != null) {
            output.x = cx;
            output.y = cy;
            return output;
        }
        return new Point(cx, cy);
    }

    public static Gene createRandomGene(MersenneTwister rng, Context context) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        final int width = context.getWidth(), height = context.getHeight(), xborder = context.getBorderX(), yborder = context.getBorderY();
        final int bwidth = width + 2 * xborder, bheight = height + 2 * yborder;
        final int[] inputData = context.getTargetImage().getData(), x = new int[3], y = new int[3];
        final Point c = new Point();
        x[0] = rng.nextInt(bwidth) - xborder;
        x[1] = rng.nextInt(bwidth) - xborder;
        x[2] = rng.nextInt(bwidth) - xborder;
        y[0] = rng.nextInt(bheight) - yborder;
        y[1] = rng.nextInt(bheight) - yborder;
        y[2] = rng.nextInt(bheight) - yborder;
        Utils.computeCentroid(x, y, c);
        if (c.x >= 0 && c.x < width && c.y >= 0 && c.y < height) {
            final int color = inputData[c.y * width + c.x];
            final int alpha = rng.nextInt(256) << 24;
            return new Gene(x, y, (color & 0x00FFFFFF) | alpha);
        }
        final int a = rng.nextInt(256), r = rng.nextInt(256), g = rng.nextInt(256), b = rng.nextInt(256);
        return new Gene(x, y, new Color(r, g, b, a));
    }

    public static Gene[] createRandomGenes(MersenneTwister rng, Context context, int minGenes, int maxGenes) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(context, "The parameter 'context' must not be null");
        Preconditions.checkArgument(minGenes > 0, "The parameter 'minGenes' must be grather than zero");
        Preconditions.checkArgument(maxGenes >= minGenes, "The parameter 'maxGenes' must be greater than or equal to 'minGenes'");
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
            genes[i] = createRandomGene(rng, context);
        }
        return genes;
    }

    public static double computeSimpleFitness(Genome genome, int[] inputData, int[] importanceMap, int[] targetData) {
        Preconditions.checkNotNull(inputData, "The parameter 'inputData' must not be null");
        Preconditions.checkNotNull(importanceMap, "The parameter 'importanceMap' must not be null");
        Preconditions.checkNotNull(targetData, "The parameter 'targetData' must not be null");
        Preconditions.checkArgument(inputData.length == importanceMap.length, "The parameters 'inputData' and 'importanceMap' must be of equal length");
        Preconditions.checkArgument(inputData.length == targetData.length, "The parameters 'inputData' and 'targetData' must be of equal length");
        double sum = 0;
        final int length = targetData.length;
        for (int i = 0; i < length; i++) {
            final int ic = inputData[i];
            final int ia = (ic >> 24) & 0x000000FF;
            final int ir = (ic >> 16) & 0x000000FF;
            final int ig = (ic >> 8) & 0x000000FF;
            final int ib = ic & 0x000000FF;
            final int tc = targetData[i];
            final int ta = (tc >> 24) & 0x000000FF;
            final int tr = (tc >> 16) & 0x000000FF;
            final int tg = (tc >> 8) & 0x000000FF;
            final int tb = tc & 0x000000FF;
            final int da = ia - ta;
            final int dr = ir - tr;
            final int dg = ig - tg;
            final int db = ib - tb;
            sum += ((da * da) + (dr * dr) + (dg * dg) + (db * db)) * (256 - importanceMap[i]);
        }
        sum = sum + (sum / 20000f) * genome.genes.length + (sum / 200000f) * genome.countPoints();
        return sum;
    }
    
    public static double computeSimpleFitness(Genome genome, int[] inputData, int[] importanceMap, int width, int height) {
        final Renderer renderer = new Renderer(width, height, true);
        renderer.render(genome);
        return computeSimpleFitness(genome, inputData, importanceMap, renderer.getBuffer());
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
    
    public static boolean equals(double a, double b) {
        return (Double.compare(a, b) == 0) || (Double.isInfinite(a) && Double.isInfinite(b)) || (Double.isNaN(a) && Double.isNaN(b));
    }
    
    public static boolean equals(int[] a, int[] b) {
        if (a == null && b == null)
            return true;
        if (a == null && b != null || a != null && b == null)
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
    
    public static <T> boolean equals(T a, T b) {
        if (a == null && b == null)
            return true;
        if (a == null && b != null || a != null && b == null)
            return false;
        return a.equals(b);
    }
}
