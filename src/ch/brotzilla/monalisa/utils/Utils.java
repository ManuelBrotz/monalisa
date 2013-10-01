package ch.brotzilla.monalisa.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.brotzilla.monalisa.Renderer;
import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;

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
        final int[] inputData = context.getInputData(), x = new int[3], y = new int[3];
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

    public static Gene mutateGene(MersenneTwister rng, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Gene result = new Gene(input);
        if (rng.nextBoolean(0.9d)) {
            final int coord = rng.nextInt(result.x.length);
            final int dx = rng.nextInt(21) - 10, dy = rng.nextInt(21) - 10;
            result.x[coord] += dx;
            result.y[coord] += dy;
        } else {
            switch (rng.nextInt(4)) {
            case 0: {
                final int channel = rng.nextInt(4);
                final int delta = rng.nextInt(51) - 25;
                result.color[channel] = Math.abs((result.color[channel] + delta) % 256);
                break;
            }
            case 1: {
                final float factor = 1.01f + (0.49f * rng.nextFloat());
                float r = (result.color[1] + 1) * factor;
                float g = (result.color[2] + 1) * factor;
                float b = (result.color[3] + 1) * factor;
                if (r > 255) result.color[1] = 255; else result.color[1] = Math.round(r);
                if (g > 255) result.color[2] = 255; else result.color[2] = Math.round(g);
                if (b > 255) result.color[3] = 255; else result.color[3] = Math.round(b);
                break;
            }
            case 2: {
                final float factor = 0.5f + (0.49f * rng.nextFloat());
                float r = (result.color[1] + 1) * factor;
                float g = (result.color[2] + 1) * factor;
                float b = (result.color[3] + 1) * factor;
                if (r > 255) result.color[1] = 255; else result.color[1] = Math.round(r);
                if (g > 255) result.color[2] = 255; else result.color[2] = Math.round(g);
                if (b > 255) result.color[3] = 255; else result.color[3] = Math.round(b);
                break;
            }
            }
        }
        return result;
    }

    public static Genome mutateGenome(MersenneTwister rng, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Genome result = new Genome(input);
        final Gene[] genes = result.genes;
        final int count = rng.nextInt(6) + 1;
        for (int i = 0; i < count; i++) {
            final int index = rng.nextInt(genes.length);
            genes[index] = mutateGene(rng, genes[index]);
        }
        return result;
    }
    
    public static Genome swapRandomGenes(MersenneTwister rng, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        if (input.genes.length > 2) {
            final Genome result = new Genome(input);
            final Gene[] genes = result.genes;
            final int index1 = rng.nextInt(genes.length);
            int index2 = rng.nextInt(genes.length);
            while (index1 == index2) {
                index2 = rng.nextInt(genes.length);
            }
            Gene tmp = genes[index1];
            genes[index1] = genes[index2];
            genes[index2] = tmp;
            return result;
        }
        return input;
    }
    
    public static Genome addRandomPoint(MersenneTwister rng, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Genome result = new Genome(input);
        final Gene[] genes = result.genes;
        final int index = rng.nextInt(genes.length);
        genes[index] = addRandomPoint(rng, genes[index]);
        return result;
    }

    public static Gene addRandomPoint(MersenneTwister rng, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final int len = input.x.length + 1;
        final int index = rng.nextInt(len);
        final int other = index == 0 ? 1 : index == len - 1 ? len - 2 : index;
        final int[] x = new int[len], y = new int[len];
        if (index == 0) {
            System.arraycopy(input.x, 0, x, 1, len - 1);
            System.arraycopy(input.y, 0, y, 1, len - 1);
        } else if (index == len - 1) {
            System.arraycopy(input.x, 0, x, 0, len - 1);
            System.arraycopy(input.y, 0, y, 0, len - 1);
        } else {
            System.arraycopy(input.x, 0, x, 0, index);
            System.arraycopy(input.y, 0, y, 0, index);
            System.arraycopy(input.x, index, x, index + 1, len - index - 1);
            System.arraycopy(input.y, index, y, index + 1, len - index - 1);
        }
        x[index] = input.x[other] + (rng.nextInt(101) - 50);
        y[index] = input.y[other] + (rng.nextInt(101) - 50);
        return new Gene(x, y, input.color);
    }

    public static Genome removeRandomPoint(MersenneTwister rng, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        int index = rng.nextInt(input.genes.length);
        if (input.genes[index].x.length > 3) {
            final Genome result = new Genome(input);
            final Gene[] genes = result.genes;
            genes[index] = removeRandomPoint(rng, genes[index]);
            return result;
        }
        return input;
    }
    
    public static Gene removeRandomPoint(MersenneTwister rng, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        Preconditions.checkArgument(input.x.length > 3, "The parameter 'input' must contain more than 3 points");
        final int length = input.x.length;
        final int index = rng.nextInt(length);
        final int[] x = new int[length - 1], y = new int[length - 1];
        if (index == 0) {
            System.arraycopy(input.x, 1, x, 0, length - 1);
            System.arraycopy(input.y, 1, y, 0, length - 1);
        } else if (index == length - 1) {
            System.arraycopy(input.x, 0, x, 0, length - 1);
            System.arraycopy(input.y, 0, y, 0, length - 1);
        } else {
            System.arraycopy(input.x, 0, x, 0, index);
            System.arraycopy(input.x, index + 1, x, index, length - index - 1);
            System.arraycopy(input.y, 0, y, 0, index);
            System.arraycopy(input.y, index + 1, y, index, length - index - 1);
        }
        return new Gene(x, y, input.color);
    }

    public static Genome addRandomGene(MersenneTwister rng, Genome input, Context context) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        Preconditions.checkNotNull(context, "The parameter 'context' must not be null");
        final Gene gene = createRandomGene(rng, context);
        final Gene[] genes = new Gene[input.genes.length + 1];
        System.arraycopy(input.genes, 0, genes, 0, input.genes.length);
        genes[genes.length - 1] = gene;
        return new Genome(input.background, genes);
    }

    public static Genome removeRandomGene(MersenneTwister rng, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final int length = input.genes.length;
        if (length > 1) {
            final int index = rng.nextInt(length);
            final Gene[] genes = new Gene[length - 1];
            if (index == 0) {
                System.arraycopy(input.genes, 1, genes, 0, length - 1);
            } else if (index == length - 1) {
                System.arraycopy(input.genes, 0, genes, 0, length - 1);
            } else {
                System.arraycopy(input.genes, 0, genes, 0, index);
                System.arraycopy(input.genes, index + 1, genes, index, length - index - 1);
            }
            return new Genome(input.background, genes);
        }
        return input;
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
        return computeSimpleFitness(genome, inputData, importanceMap, renderer.getData());
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
}
