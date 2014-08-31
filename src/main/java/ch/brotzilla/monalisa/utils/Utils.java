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
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.rendering.SimpleRenderer;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;
import ch.brotzilla.util.TextReader;

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

    public static Gene createRandomGene(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        final int width = vectorizerContext.getWidth(), height = vectorizerContext.getHeight(), xborder = evolutionContext.getBorderX(), yborder = evolutionContext.getBorderY();
        final int bwidth = width + 2 * xborder, bheight = height + 2 * yborder;
        final int[] inputData = vectorizerContext.getTargetImage().getBuffer(), x = new int[3], y = new int[3];
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

    public static Gene[] createRandomGenes(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, int minGenes, int maxGenes) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(vectorizerContext, "The parameter 'vectorizerContext' must not be null");
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
            genes[i] = createRandomGene(rng, vectorizerContext, evolutionContext);
        }
        return genes;
    }
    
    public static Gene[][] copyGenes(Gene[][] genes) {
        Preconditions.checkNotNull(genes, "The parameter 'genes' must not be null");
        Preconditions.checkArgument(genes.length > 0, "The length of the parameter 'genes' has to be greater than zero");
        final int layers = genes.length;
        final Gene[][] result = new Gene[layers][];
        for (int i = 0; i < layers; i++) {
            Preconditions.checkNotNull(genes[i], "The parameter 'genes[" + i + "]' must not contain null");
            final int size = genes[i].length;
            Preconditions.checkArgument(size > 0, "The length of the parameter 'genes[" + i + "]' has to be greater than zero");
            result[i] = new Gene[size];
            System.arraycopy(genes[i], 0, result[i], 0, size);
        }
        return result;
    }

    public static Gene[][] copyGenesReplaceLastLayer(Gene[][] genes, Gene[] layer) {
        Preconditions.checkNotNull(genes, "The parameter 'genes' must not be null");
        Preconditions.checkArgument(genes.length > 0, "The length of the parameter 'genes' has to be greater than zero");
        Preconditions.checkNotNull(layer, "The parameter 'layer' must not be null");
        Preconditions.checkArgument(layer.length > 0, "The length of the parameter 'layer' has to be greater than zero");
        final int layers = genes.length;
        final Gene[][] result = new Gene[layers][];
        for (int i = 0; i < layers-1; i++) {
            Preconditions.checkNotNull(genes[i], "The parameter 'genes[" + i + "]' must not contain null");
            final int size = genes[i].length;
            Preconditions.checkArgument(size > 0, "The length of the parameter 'genes[" + i + "]' has to be greater than zero");
            result[i] = new Gene[size];
            System.arraycopy(genes[i], 0, result[i], 0, size);
        }
        result[layers-1] = layer;
        return result;
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
        sum = sum + (sum / 20000f) * genome.countPolygons() + (sum / 200000f) * genome.countPoints();
        return sum;
    }
    
    public static double computeSimpleFitness(Genome genome, int[] inputData, int[] importanceMap, int width, int height) {
        final SimpleRenderer renderer = new SimpleRenderer(width, height, true);
        renderer.render(genome);
        return computeSimpleFitness(genome, inputData, importanceMap, renderer.getBuffer());
    }
    
    public static Genome splitCurrentLayerIntoNewLayer(Genome genome, int sizeOfNewLayer) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        Preconditions.checkArgument(sizeOfNewLayer > 0, "The parameter 'sizeOfNewLayer' has to be greater than zero");
        
        final Gene[][] genes = genome.genes;
        final Gene[] currentLayer = genome.getCurrentLayer();
        final int currentSize = currentLayer.length;
        Preconditions.checkArgument(currentSize > sizeOfNewLayer, "The size of the current layer of the parameter 'genome' has to be greater than the parameter 'sizeOfNewLayer'");
        
        final Gene[] newLayer = new Gene[currentSize - sizeOfNewLayer];
        System.arraycopy(currentLayer, 0, newLayer, 0, currentSize - sizeOfNewLayer);
        
        final Gene[] newCurrentLayer = new Gene[sizeOfNewLayer];
        System.arraycopy(currentLayer, currentSize - sizeOfNewLayer, newCurrentLayer, 0, sizeOfNewLayer);
        
        final Gene[][] newGenes = new Gene[genes.length + 1][];
        for (int l = 0; l < genes.length - 1; l++) {
            newGenes[l] = new Gene[genes[l].length];
            System.arraycopy(genes[l], 0, newGenes[l], 0, genes[l].length);
        }
        
        newGenes[newGenes.length - 2] = newLayer;
        newGenes[newGenes.length - 1] = newCurrentLayer;
        
        return new Genome(genome.background, newGenes);
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
