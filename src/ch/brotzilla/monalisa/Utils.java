package ch.brotzilla.monalisa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.common.base.Preconditions;

public class Utils {

	public static BufferedImage readImage(File file) {
		try {
			final BufferedImage image = ImageIO.read(file);
			if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
				final BufferedImage image2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g = image2.createGraphics();
				g.drawImage(image, 0, 0, null);
				return image2;
			}
			return image;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to read image.");
		}
	}
	
	public static int[] extractPixelData(BufferedImage image) {
		final WritableRaster raster = image.getRaster();
		return (int[]) raster.getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
	}
	
	public static byte[] decodePixelData(int[] pixelData, byte[] output) {
		Preconditions.checkNotNull(pixelData, "The parameter 'pixelData' must not be null");
		final int length = pixelData.length;
		if (output == null  || output.length != length * 4) {
			output = new byte[length * 4];
		}
		for (int i = 0, j = -1; i < length; i++) {
			final int argb = pixelData[i];
			output[++j] = (byte) ((argb >> 24) & 0x000000FF);
			output[++j] = (byte) ((argb >> 16) & 0x000000FF);
			output[++j] = (byte) ((argb >>  8) & 0x000000FF);
			output[++j] = (byte) ( argb        & 0x000000FF);
		}
		return output;
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
	
	public static Gene createRandomGene(MersenneTwister rng, int width, int height, int xborder, int yborder, int[] inputData) {
		final int bwidth = width + 2 * xborder, bheight = height + 2 * yborder;
		final int[] x = new int[3], y = new int[3];
		final Point c = new Point();
		x[0] = rng.nextInt(bwidth) - xborder;
		x[1] = rng.nextInt(bwidth) - xborder;
		x[2] = rng.nextInt(bwidth) - xborder;
		y[0] = rng.nextInt(bheight) - yborder;
		y[1] = rng.nextInt(bheight) - yborder;
		y[2] = rng.nextInt(bheight) - yborder;
		if (inputData != null) {
			Utils.computeCentroid(x, y, c);
			if (c.x >= 0 && c.x < width && c.y >= 0 && c.y < height) {
				final int color = inputData[c.y * width + c.x];
				final int alpha = rng.nextInt(256) << 24;
				return new Gene(x, y, (color & 0x00FFFFFF) | alpha);
			}
		}
		final int a = rng.nextInt(256), r = rng.nextInt(256), g = rng.nextInt(256), b = rng.nextInt(256);
		return new Gene(x, y, new Color(r, g, b, a));
	}
	
	public static Gene[] createRandomGenes(MersenneTwister rng, int minGenes, int maxGenes, int width, int height, int xborder, int yborder, int[] inputData) {
		Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
		Preconditions.checkArgument(minGenes > 0, "The parameter 'minGenes' must be grather than zero");
		Preconditions.checkArgument(maxGenes >= minGenes, "The parameter 'maxGenes' must be greater than or equal to 'minGenes'");
		Preconditions.checkArgument(width > 0, "The parameter 'width' must be greater than zero");
		Preconditions.checkArgument(height > 0, "The parameter 'height' must be greater than zero");
		Preconditions.checkArgument(xborder >= 0, "The parameter 'xborder' must be greater than or equal to zero");
		Preconditions.checkArgument(yborder >= 0, "The parameter 'yborder' must be greater than or equal to zero");
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
			genes[i] = createRandomGene(rng, width, height, xborder, yborder, inputData);
		}
		return genes;
	}
	
	public static Gene mutateGene(MersenneTwister rng, Gene input) {
		Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
		Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
		final Gene result = new Gene(input);
		if (rng.nextInt(5) == 0) {
			final int channel = rng.nextInt(4);
			result.color[channel] = rng.nextInt(256);
		} else {
			final int coord = rng.nextInt(3);
			final int dx = rng.nextInt(101) - 50, dy = rng.nextInt(101) - 50;
			result.x[coord] += dx;
			result.y[coord] += dy;
		}
		return result;
	}
	
	public static Genome mutateGenome(MersenneTwister rng, Genome input) {
		Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
		Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
		final Genome result = new Genome(input);
		final Gene[] genes = result.genes;
		final int count = rng.nextInt(6)+1;
		for (int i = 0; i < count; i++) {
			if (rng.nextInt(20) == 0) {
				final int index1 = rng.nextInt(genes.length);
				final int index2 = rng.nextInt(genes.length);
				if (index1 == index2) {
					--i;
					continue;
				}
				Gene tmp = genes[index1];
				genes[index1] = genes[index2];
				genes[index2] = tmp;
			} else {
				final int index = rng.nextInt(genes.length);
				genes[index] = mutateGene(rng, genes[index]);
			}
		}
		return result;
	}
	
	public static Genome addRandomGene(MersenneTwister rng, Genome input, int width, int height, int xborder, int yborder, int[] inputData) {
		Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
		Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
		Preconditions.checkArgument(width > 0, "The parameter 'width' must be greater than zero");
		Preconditions.checkArgument(height > 0, "The parameter 'height' must be greater than zero");
		Preconditions.checkArgument(xborder >= 0, "The parameter 'xborder' must be greater than or equal to zero");
		Preconditions.checkArgument(yborder >= 0, "The parameter 'yborder' must be greater than or equal to zero");
		final Gene gene = createRandomGene(rng, width, height, xborder, yborder, inputData);
		final Gene[] genes = new Gene[input.genes.length+1];
		System.arraycopy(input.genes, 0, genes, 0, input.genes.length);
		genes[genes.length-1] = gene;
		return new Genome(input.background, genes);
	}
	
	public static Genome removeRandomGene(MersenneTwister rng, Genome input) {
		Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
		Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
		final int length = input.genes.length;
		final int index = rng.nextInt(length);
		final Gene[] genes = new Gene[length-1];
		if (index == 0) {
			System.arraycopy(input.genes, 1, genes, 0, length-1);
		} else if (index == length-1) {
			System.arraycopy(input.genes, 0, genes, 0, length-1);
		} else {
			System.arraycopy(input.genes, 0, genes, 0, index);
			System.arraycopy(input.genes, index+1, genes, index, length - index - 1);
		}
		return new Genome(input.background, genes);
	}
	
	public static double computeSimpleFitness(Genome genome, int[] inputData, int[] targetData) {
		Preconditions.checkNotNull(inputData, "The parameter 'inputData' must not be null");
		Preconditions.checkNotNull(targetData, "The parameter 'targetData' must not be null");
		Preconditions.checkArgument(inputData.length == targetData.length);
		double sum = 0;
		final int length = targetData.length;
		for (int i = 0; i < length; i++) {
			final int ic = inputData[i];
			final int ia = (ic >> 24) & 0x000000FF;
			final int ir = (ic >> 16) & 0x000000FF;
			final int ig = (ic >>  8) & 0x000000FF;
			final int ib =  ic        & 0x000000FF;
			final int tc = targetData[i];
			final int ta = (tc >> 24) & 0x000000FF;
			final int tr = (tc >> 16) & 0x000000FF;
			final int tg = (tc >>  8) & 0x000000FF;
			final int tb =  tc        & 0x000000FF;
			final int da = ia - ta;
			final int dr = ir - tr;
			final int dg = ig - tg; 
			final int db = ib - tb;
			sum += Math.sqrt((da * da) + (dr * dr) + (dg * dg) + (db * db));
		}
		sum = sum + (sum / 10000f) * genome.genes.length;
		return sum;
	}
	
	public static double computeSimpleFitness(byte[] inputData, int[] targetData) {
		Preconditions.checkNotNull(inputData, "The parameter 'inputData' must not be null");
		Preconditions.checkNotNull(targetData, "The parameter 'targetData' must not be null");
		Preconditions.checkArgument(inputData.length == targetData.length * 4);
		double sum = 0;
		final int length = targetData.length;
		for (int i = 0, j = -1; i < length; i++) {
			final int tc = targetData[i];
			final int ta = (tc >> 24) & 0x000000FF;
			final int tr = (tc >> 16) & 0x000000FF;
			final int tg = (tc >>  8) & 0x000000FF;
			final int tb =  tc        & 0x000000FF;
			final int da = inputData[++j] - ta;
			final int dr = inputData[++j] - tr;
			final int dg = inputData[++j] - tg; 
			final int db = inputData[++j] - tb;
			sum += Math.sqrt((da * da) + (dr * dr) + (dg * dg) + (db * db));
		}
		return sum;
	}

	public static int[] decodeColor(int argb, int[] output) {
		final int a = (argb >> 24) & 0x000000FF;
		final int r = (argb >> 16) & 0x000000FF;
		final int g = (argb >>  8) & 0x000000FF;
		final int b =  argb        & 0x000000FF;
		if (output == null || output.length != 4) {
			return new int[] {a, r, g, b};
		}
		output[0] = a;
		output[1] = r;
		output[2] = g;
		output[3] = b;
		return output;
	}

	public static byte[] decodeColor(int argb, byte[] output) {
		final byte a = (byte)((argb >> 24) & 0x000000FF);
		final byte r = (byte)((argb >> 16) & 0x000000FF);
		final byte g = (byte)((argb >>  8) & 0x000000FF);
		final byte b = (byte)( argb        & 0x000000FF);
		if (output == null || output.length != 4) {
			return new byte[] {a, r, g, b};
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
}
