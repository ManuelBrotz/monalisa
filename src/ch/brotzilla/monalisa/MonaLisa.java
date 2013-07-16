package ch.brotzilla.monalisa;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;

public class MonaLisa {

	public static final int NUM_THREADS = 8;
	
	protected Params params;
	
	protected BufferedImage inputImage;
	protected int[] inputPixelData;
	protected byte[] inputChannelData;
	
	protected ExecutorService storageThread;
	protected BlockingQueue<Genome> storageQueue;
	
	protected ExecutorService processingThreads;

	protected MersenneTwister random;
	
	protected Genome currentGenome;
	
	protected long generated, selected;
	
	public MonaLisa(String[] args) {
		Preconditions.checkNotNull(args, "The parameter 'args' must not be null");
		this.params = new Params(args);
	}
	
	public MonaLisa(Params params) {
		Preconditions.checkNotNull(params, "The parameter 'params' must not be null");
		this.params = params;
	}
	
	public synchronized Genome submit(Genome genome) {
		if (genome != null && genome != currentGenome) {
			generated++;
			if (currentGenome == null || genome.fitness < currentGenome.fitness) {
				selected++;
				genome.generated = generated;
				genome.selected = selected;
				currentGenome = genome;
				storageQueue.offer(genome);
				System.out.println("Generated: " + generated + ", Selected: " + selected + ", Polygons: " + genome.genes.length + ", Points: " + genome.countPoints() + ", Fitness: " + genome.fitness);
			}
		}
		return currentGenome;
	}
	
	public void setup() {
		this.inputImage = Utils.readImage(params.getInputFile());
		this.inputPixelData = Utils.extractPixelData(inputImage);
		this.inputChannelData = Utils.decodePixelData(inputPixelData, null);
		System.out.println("Loaded image: " + params.getInputFile().getAbsoluteFile());
		System.out.println("Image size  : " + inputImage.getWidth() + "x" + inputImage.getHeight() + ", " + inputPixelData.length + " pixels, " + (inputPixelData.length * 4) + " bytes");
		 
		this.random = new MersenneTwister(params.getSeed());
		
		this.storageQueue = Queues.newLinkedBlockingQueue();
		this.storageThread = Executors.newFixedThreadPool(1);
		storageThread.submit(new Runnable() {
			
			private final File outputRoot = params.getOutputFolder();
			private final String inputName = params.getInputFile().getName();
			private final int width = inputImage.getWidth();
			private final int height = inputImage.getHeight();
			private long countStored = 0;
			private long timeLastStored = 0;

			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				final Renderer renderer = new Renderer(width, height, false);
				final File outputFolder = new File(outputRoot, inputName);
				while (!storageThread.isShutdown()) {
					try {
						final Genome genome = storageQueue.take();
						if (System.currentTimeMillis() - timeLastStored >= 10000) {
							renderer.render(genome);
							writeImage(renderer.getImage(), outputFolder, countStored);
							countStored++;
							timeLastStored = System.currentTimeMillis();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void start() {
		if (processingThreads != null) 
			throw new IllegalStateException("Already running");
		processingThreads = Executors.newFixedThreadPool(NUM_THREADS);
		for (int i = 0; i < NUM_THREADS; i++) {
			processingThreads.submit(new Runnable() {
				
				private final Color backgroundColor = params.getBackgroundColor();
				private final long seed = random.nextLong();
				private final int width = inputImage.getWidth();
				private final int height = inputImage.getHeight();
				
				@Override
				public void run() {
					final MersenneTwister rng = new MersenneTwister(seed);
					final Renderer renderer = new Renderer(width, height, true);
					
					Genome genome = currentGenome;
					while (!processingThreads.isShutdown()) {
						try {
							genome = submit(genome);
							if (genome == null) {
								genome = new Genome(backgroundColor, Utils.createRandomGenes(rng, 10, 20, width, height, 50, 50, inputPixelData));
							} else {
								genome = Utils.mutateGenome(rng, genome);
								switch (rng.nextInt(10)) {
								case 0:
									genome = Utils.addRandomGene(rng, genome, width, height, 50, 50, inputPixelData);
									break;
								case 1:
									genome = Utils.removeRandomGene(rng, genome);
									break;
								}
							}
							renderer.render(genome);
							genome.fitness = Utils.computeSimpleFitness(genome, inputPixelData, renderer.getData());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
	
	public void stop() {
		if (processingThreads != null) {
			processingThreads.shutdown();
			try {
				processingThreads.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			processingThreads = null;
		}
	}
	
	public static void main(String[] args) {
		final MonaLisa ml = new MonaLisa(args);
		ml.setup();
		ml.start();
		try {
			ml.processingThreads.awaitTermination(100, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

//		byte[] t = new byte[] {2, 5, 7, 9};
//		byte[] input = new byte[] {2, 4, 6, 8};
//		int[] target = new int[] {Utils.encodeColor(t)};
//		double fitness = Utils.computeSimpleFitness(input, target);
//		System.out.println(fitness);
	}

	private void runTest() {
		final File output = new File("output/tests/lips");
		long generation = 0, found = 0;
		Genome genome = new Genome(null, Utils.createRandomGenes(random, 10, 20, inputImage.getWidth(), inputImage.getHeight(), 50, 50, inputPixelData));
		
		final Renderer renderer = new Renderer(inputImage.getWidth(), inputImage.getHeight(), true);
		
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
