package ch.brotzilla.monalisa;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.google.common.base.Preconditions;

public class MonaLisa {

	protected Params params;
	
	protected BufferedImage inputImage;
	protected int[] inputPixelData;
	protected byte[] inputChannelData;
	
	protected MersenneTwister random;
	
	protected Renderer renderer;
	
	public MonaLisa(String[] args) {
		Preconditions.checkNotNull(args, "The parameter 'args' must not be null");
		this.params = new Params(args);
	}
	
	public MonaLisa(Params params) {
		Preconditions.checkNotNull(params, "The parameter 'params' must not be null");
		this.params = params;
	}

	protected void setup() {
		this.inputImage = Utils.readImage(params.getInputFile());
		this.inputPixelData = Utils.extractPixelData(inputImage);
		this.inputChannelData = Utils.decodePixelData(inputPixelData, null);
		System.out.println("Loaded image: " + params.getInputFile().getAbsoluteFile());
		System.out.println("Image size  : " + inputImage.getWidth() + "x" + inputImage.getHeight() + ", " + inputPixelData.length + " pixels, " + (inputPixelData.length * 4) + " bytes");
		this.random = new MersenneTwister();
		this.renderer = new Renderer(inputImage.getWidth(), inputImage.getHeight());
	}
	
	public static void main(String[] args) {
		final MonaLisa ml = new MonaLisa(args);
		ml.setup();
		ml.runTest();

//		byte[] t = new byte[] {2, 5, 7, 9};
//		byte[] input = new byte[] {2, 4, 6, 8};
//		int[] target = new int[] {Utils.encodeColor(t)};
//		double fitness = Utils.computeSimpleFitness(input, target);
//		System.out.println(fitness);
	}

	private void renderTest() {
		final Gene[] genes = Utils.createRandomGenes(random, 50, 100, inputImage.getWidth(), inputImage.getHeight(), 50, 50, inputPixelData);
		final Genome genome = new Genome(null, genes);
		renderer.render(genome);
		System.out.println("RenderTest: output/RenderTest.png");
		System.out.println(genome);
		try {
			ImageIO.write(renderer.getImage(), "PNG", new File("output/RenderTest.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void runTest() {
		final File output = new File("output/tests/lips");
		long generation = 0, found = 0;
		Genome genome = new Genome(null, Utils.createRandomGenes(random, 10, 20, inputImage.getWidth(), inputImage.getHeight(), 50, 50, inputPixelData));
		
		renderer.render(genome);
		writeImage(renderer.getImage(), output, 0);
		long lastWritten = System.currentTimeMillis();
		
		double fitness = Utils.computeSimpleFitness(genome, inputPixelData, renderer.getData());
		System.out.println("Initial fitness: " + fitness);
		
		while (true) {
			Genome mutated = Utils.mutateGenome(random, genome);
			switch (random.nextInt(20)) {
			case 0:
				mutated = Utils.addRandomGene(random, genome, inputImage.getWidth(), inputImage.getHeight(), 50, 50, inputPixelData);
				break;
			case 1:
				mutated = Utils.removeRandomGene(random, genome);
				break;
			}
			renderer.render(mutated);
			final double fit = Utils.computeSimpleFitness(genome, inputPixelData, renderer.getData());
			if (fit < fitness) {
				fitness = fit;
				genome = mutated;
				found++;
				if (System.currentTimeMillis() - lastWritten >= 10000) {
					writeImage(renderer.getImage(), output, found);
					lastWritten = System.currentTimeMillis();
				}
				System.out.println("Generation: " + generation + ", Found: " + found + ", Triangles: " + genome.genes.length +  ", Fitness: " + fitness);
			}
			generation++;
		}
	}
	
	private void writeImage(BufferedImage image, File folder, long number) {
		String name = "" + number;
		while (name.length() < 6)
			name = "0" + name;
		name = name + ".png";
		if (!folder.exists())
			folder.mkdirs();
		try {
			ImageIO.write(image, "PNG", new File(folder, name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
