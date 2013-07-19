package ch.brotzilla.monalisa;

import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;

public class MonaLisa {

    public static final int NUM_THREADS = 8;

    protected Params params;
    protected SessionManager session;

    protected int[] inputPixelData;

    protected ExecutorService storageThread;
    protected BlockingQueue<Genome> storageQueue;

    protected ExecutorService processingThreads;

    protected MersenneTwister random;

    protected Genome currentGenome;

    protected long generated, selected;

    public MonaLisa(String[] args) {
        this(new Params(Preconditions.checkNotNull(args, "The parameter 'args' must not be null")));
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

    public void setup() throws IOException {
        this.session = params.createSessionManager();
        this.inputPixelData = Utils.extractPixelData(session.getInputImage());

        final int imageWidth = session.getInputImage().getWidth(), imageHeight = session.getInputImage().getHeight();
        if (params.getInputFile() != null) {
            System.out.println("Loaded image: " + params.getInputFile().getAbsoluteFile());
        } else {
            System.out.println("Loaded image: " + session.getInputImageFile().getAbsoluteFile());
        }
        System.out.println("Image size  : " + imageWidth + "x" + imageHeight + ", " + inputPixelData.length + " pixels, " + (inputPixelData.length * 4) + " bytes");

        this.random = new MersenneTwister(params.getSeed());

        if (session.isSessionResumed()) {
            this.currentGenome = session.loadLatestGenome();
            if (this.currentGenome == null) {
                throw new IllegalStateException("Cannot load latest genome");
            }
            this.generated = this.currentGenome.generated;
            this.selected = this.currentGenome.selected;
        }

        this.storageQueue = Queues.newLinkedBlockingQueue();
        this.storageThread = Executors.newFixedThreadPool(1);
        storageThread.submit(new Runnable() {

            private final int width = imageWidth;
            private final int height = imageHeight;
            private long timeLastStored = 0;

            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                final Renderer renderer = new Renderer(width, height, false);
                while (!storageThread.isShutdown()) {
                    try {
                        final Genome genome = storageQueue.take();
                        if (System.currentTimeMillis() - timeLastStored >= 10000) {
                            renderer.render(genome);
                            session.storeGenome(genome, renderer.getImage());
                            timeLastStored = System.currentTimeMillis();
                        }
                    } catch (Exception e) {
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
                private final int width = session.getInputImage().getWidth();
                private final int height = session.getInputImage().getHeight();

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
        try {
            ml.setup();
            ml.start();
            ml.processingThreads.awaitTermination(100, TimeUnit.DAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
