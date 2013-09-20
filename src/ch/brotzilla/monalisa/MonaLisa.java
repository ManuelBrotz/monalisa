package ch.brotzilla.monalisa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.gui.MainWindow;
import ch.brotzilla.monalisa.mutations.GeneAddPointMutation;
import ch.brotzilla.monalisa.mutations.GeneAlphaChannelMutation;
import ch.brotzilla.monalisa.mutations.GeneColorBrighterMutation;
import ch.brotzilla.monalisa.mutations.GeneColorChannelMutation;
import ch.brotzilla.monalisa.mutations.GeneColorDarkerMutation;
import ch.brotzilla.monalisa.mutations.GenePointMutation;
import ch.brotzilla.monalisa.mutations.GeneRemovePointMutation;
import ch.brotzilla.monalisa.mutations.GeneSwapPointsMutation;
import ch.brotzilla.monalisa.mutations.GenomeAddGeneMutation;
import ch.brotzilla.monalisa.mutations.GenomeRemoveGeneMutation;
import ch.brotzilla.monalisa.mutations.GenomeSwapGenesMutation;
import ch.brotzilla.monalisa.mutations.Mutations;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.utils.Params;
import ch.brotzilla.monalisa.utils.SessionManager;
import ch.brotzilla.monalisa.utils.Utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;

public class MonaLisa {

    protected Params params;
    protected SessionManager session;

    protected MainWindow mainWindow;
    
    protected int[] inputPixelData;
    protected int[] importanceMap;

    protected ExecutorService storageThread;
    protected BlockingQueue<Genome> storageQueue;

    protected ExecutorService processingThreads;

    protected MersenneTwister random;

    protected Genome currentGenome;

    protected int generated, selected;
    
    protected final DecimalFormat ff = new DecimalFormat( "#,###,###,###,##0.######" );
    
    protected Mutations setupMutations() {
        final Mutations m = new Mutations();
        m.setMaxMutations(3);
        
        m.add(new GeneAlphaChannelMutation(0.1d));
        m.add(new GeneAlphaChannelMutation(0.01d));
        m.add(new GeneAlphaChannelMutation(0.005d));
        
        m.add(new GeneColorChannelMutation(0.1d));
        m.add(new GeneColorChannelMutation(0.01d));
        m.add(new GeneColorChannelMutation(0.005d));
        
        m.add(new GeneColorBrighterMutation(0.1d));
        m.add(new GeneColorBrighterMutation(0.01d));
        m.add(new GeneColorBrighterMutation(0.005d));
        
        m.add(new GeneColorDarkerMutation(0.1d));
        m.add(new GeneColorDarkerMutation(0.01d));
        m.add(new GeneColorDarkerMutation(0.005d));
        
        m.add(new GenePointMutation(0.9d));
        m.add(new GenePointMutation(0.5d));
        m.add(new GenePointMutation(0.1d));
        m.add(new GenePointMutation(0.01d));
        m.add(new GenePointMutation(0.001d));
        
        m.add(new GeneAddPointMutation(0.1d));
        m.add(new GeneRemovePointMutation(0.1d));
        m.add(new GeneSwapPointsMutation(0.1d));

        m.add(new GenomeAddGeneMutation(inputPixelData, 0.1d));
        m.add(new GenomeRemoveGeneMutation(0.1d));
        m.add(new GenomeSwapGenesMutation(0.1d));

        return m;
    }
    
    protected Genome oldMutationProcedure(MersenneTwister rng, Constraints constraints, Genome genome) {
        if (rng.nextBoolean(0.95d)) {
            genome = Utils.mutateGenome(rng, genome);
        } else {
            final Genome orig = genome;
            while (orig == genome) {
                switch (rng.nextInt(5)) {
                case 0:
                    genome = Utils.addRandomGene(rng, genome, constraints, inputPixelData);
                    break;
                case 1:
                    genome = Utils.addRandomPoint(rng, genome);
                    break;
                case 2:
                    genome = Utils.removeRandomGene(rng, genome);
                    break;
                case 3:
                    genome = Utils.removeRandomPoint(rng, genome);
                    break;
                case 4:
                    genome = Utils.swapRandomGenes(rng, genome);
                    break;
                }
            }
        }
        return genome;
    }
    
    protected void printError() {
        System.out.println("Usage:");
        params.getParser().printUsage(System.out);
        System.out.println();
        System.out.println("Parameters:");
        System.out.println(params.getArguments());
        if (params.getError() != null) {
            System.out.println();
            System.out.println("Error: " + params.getError().getMessage());
            System.out.println();
            params.getError().printStackTrace();
        }
    }
    
    public MonaLisa(String[] args) {
        this(new Params(args));
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
                if (mainWindow != null) {
                    mainWindow.submit(genome);
                }
            }
        }
        return currentGenome;
    }

    public void setup() throws IOException {
        if (!params.isReady()) 
            throw new IllegalStateException("Not ready");
        
        session = new SessionManager(params);
        inputPixelData = session.getInputPixelData();
        importanceMap = session.getImportanceMap();
        if (importanceMap != null && importanceMap.length != inputPixelData.length)
            throw new IllegalArgumentException("Importance map has to be of size " + inputPixelData.length + " but is " + importanceMap.length);

        final int imageWidth = session.getWidth(), imageHeight = session.getHeight();
        if (session.isSessionResumed()) {
            System.out.println("Resumed session '" + session.getSessionName() + "': " + session.getSessionDirectory().getAbsolutePath());
        } else {
            System.out.println("Started new session '" + session.getSessionName() + "': " + session.getSessionDirectory().getAbsolutePath());
            System.out.println("Using image file: " + params.getInputFile().getAbsolutePath());
        }
        System.out.println("Image size: " + imageWidth + "x" + imageHeight + ", " + inputPixelData.length + " pixels, " + (inputPixelData.length * 4) + " bytes");
        if (importanceMap != null) {
            System.out.println("Using importance map of length: " + importanceMap.length + " bytes");
        }

        random = new MersenneTwister(params.getSeed());

        if (session.isSessionResumed()) {
            currentGenome = session.loadLatestGenome();
            if (currentGenome == null) {
                throw new IllegalStateException("Unable to load latest genome");
            }
            final double oldFitness = currentGenome.fitness;
            currentGenome.fitness = Utils.computeSimpleFitness(currentGenome, inputPixelData, importanceMap, imageWidth, imageHeight);
            if (oldFitness != currentGenome.fitness) {
                System.out.println("Original fitness: " + ff.format(oldFitness));
                System.out.println("New fitness     : " + ff.format(currentGenome.fitness));
            }
            generated = currentGenome.generated;
            selected = currentGenome.selected;
        }
        
        storageQueue = Queues.newLinkedBlockingQueue();
        storageThread = Executors.newFixedThreadPool(1);
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
                        final Genome genome = storageQueue.poll(250, TimeUnit.MILLISECONDS);
                        if (genome != null && System.currentTimeMillis() - timeLastStored >= 10000) {
                            System.out.println("Generated: " + generated + ", Selected: " + selected + ", Mutations: " + genome.mutations + ", Polygons: " + genome.genes.length + ", Points: " + genome.countPoints() + ", Fitness: " + ff.format(genome.fitness));
                            renderer.render(genome);
                            session.storeGenome(genome, renderer.getImage().image);
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
        if (!params.isReady()) 
            throw new IllegalStateException("Not ready");
        if (processingThreads != null)
            throw new IllegalStateException("Already running");
        
        if (params.getShowGui()) {
            showGui();
            if (currentGenome != null) {
                mainWindow.submit(currentGenome);
            }
        }
        
        final int numThreads = params.getNumThreads();
        processingThreads = Executors.newFixedThreadPool(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            processingThreads.submit(new Runnable() {

                private final Color backgroundColor = params.getBackgroundColor();
                private final long seed = random.nextLong();
                private final Constraints constraints = session.getConstraints();
                private final Mutations mutations = setupMutations();

                @Override
                public void run() {
                    final MersenneTwister rng = new MersenneTwister(seed);
                    final Renderer renderer = new Renderer(constraints.getWidth(), constraints.getHeight(), true);

                    Genome genome = currentGenome;
                    while (!processingThreads.isShutdown()) {
                        try {
                            genome = submit(genome);
                            if (genome == null) {
                                genome = new Genome(backgroundColor, Utils.createRandomGenes(rng, constraints, 10, 20, inputPixelData));
                            } else {
                                genome = mutations.apply(rng, constraints, genome);
                            }
                            renderer.render(genome);
                            genome.fitness = Utils.computeSimpleFitness(genome, inputPixelData, importanceMap, renderer.getData());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void shutdown() {
        if (processingThreads != null) {
            storageThread.shutdown();
            processingThreads.shutdown();
            try {
                storageThread.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                processingThreads.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void showGui() {
        if (mainWindow == null) {
            try {
                mainWindow = new MainWindow(session, currentGenome);
                final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                final int width = 640, height = 480;
                mainWindow.setBounds(screen.width / 2 - width / 2, screen.height / 2 - height / 2, width, height);
            } catch (Exception e) {
                System.out.println("Unable to instanciate the gui");
                e.printStackTrace();
                return;
            }
        }
        mainWindow.setVisible(true);
    }
    
    public void initCommandLine() {
        @SuppressWarnings("resource")
        final Scanner cmd = new Scanner(System.in);
        while (true) {
            final String input = cmd.nextLine();
            if (input == null || input.trim().isEmpty()) {
                continue;
            }
            if (input.equals("shutdown") || input.equals("exit")) {
                System.out.println("Shutting down...");
                shutdown();
                System.out.println("Goodbye");
                System.exit(0);
            } if (input.equals("show-gui")) {
                showGui();
            } else {
                System.out.println("Unknown command: " + input);
            }
        }
    }

    public static void main(String[] args) {
        final MonaLisa ml = new MonaLisa(args);
        try {
            if (ml.params.isReady()) {
                ml.setup();
                ml.start();
                ml.initCommandLine();
            } else {
                ml.printError();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
