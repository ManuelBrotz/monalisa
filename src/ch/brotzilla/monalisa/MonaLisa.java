package ch.brotzilla.monalisa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ch.brotzilla.monalisa.db.Database;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.evolution.strategies.SimpleMutationStrategy;
import ch.brotzilla.monalisa.gui.MainWindow;
import ch.brotzilla.monalisa.images.ImageData;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.utils.OldFormatConverter;
import ch.brotzilla.monalisa.utils.Params;
import ch.brotzilla.monalisa.utils.Utils;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;

public class MonaLisa {

    protected Params params;
    protected SessionManager session;

    protected int[] targetImage, importanceMap;

    protected MainWindow mainWindow;
    
    protected ExecutorService storageThread;
    protected BlockingQueue<Genome> storageQueue;
    protected ExecutorService processingThreads;

    protected MersenneTwister random;

    protected Genome currentGenome;
    protected int generated, selected;
    
    protected final DecimalFormat ff = new DecimalFormat( "#,###,###,###,##0.######" );
    
    protected MutationStrategy setupMutationStrategy() {
        return new SimpleMutationStrategy();
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

    public void setup() throws IOException, SQLiteException {
        if (!params.isReady()) 
            throw new IllegalStateException("Not ready");
        
        this.session = new SessionManager(params);

        final int imageWidth = session.getWidth(), imageHeight = session.getHeight();
        
        this.targetImage = session.getTargetImage().getData();
        if (session.getImportanceMap() != null) {
            this.importanceMap = session.getImportanceMap().getData();
        } else {
            this.importanceMap = new int[session.getTargetImage().getLength()];
            Arrays.fill(importanceMap, 255);
        }

        if (session.isSessionResumed()) {
            System.out.println("Resumed session '" + session.getSessionName() + "': " + session.getDatabaseFile());
        } else {
            System.out.println("Started new session '" + session.getSessionName() + "': " + session.getDatabaseFile());
            System.out.println("Using image file: " + params.getTargetImageFile());
        }
        System.out.println("Image size: " + imageWidth + "x" + imageHeight + ", " + session.getTargetImage().getLength() + " pixels");
        
        if (session.isSessionResumed()) {
            int genomes = session.getNumberOfGenomes();
            System.out.println("Counted " + genomes + " genomes in database.");
        }

        random = new MersenneTwister(params.getSeed());

        if (session.isSessionResumed()) {
            currentGenome = session.getLatestGenome();
            if (currentGenome == null) {
                System.out.println("No latest genome found in database.");
            }
            final double oldFitness = currentGenome.fitness;
            currentGenome.fitness = Utils.computeSimpleFitness(currentGenome, targetImage, importanceMap, imageWidth, imageHeight);
            if (oldFitness != currentGenome.fitness) {
                System.out.println("Original fitness: " + ff.format(oldFitness));
                System.out.println("New fitness     : " + ff.format(currentGenome.fitness));
            }
            generated = currentGenome.generated;
            selected = currentGenome.selected;
        }
        
        if (params.getExportLatest() != null) {
            try {
                session.exportSVG(currentGenome, params.getExportLatest());
            } catch (Exception e) {
                System.out.println("Failed exporting latest genome as svg document.");
                e.printStackTrace();
            }
        }
        
        storageQueue = Queues.newLinkedBlockingQueue();
        storageThread = Executors.newFixedThreadPool(1);
        storageThread.submit(new Runnable() {

            private final SessionManager session = MonaLisa.this.session;
            private long timeLastStored = 0;

            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                try (final Database db = session.connect()) {
                    while (!storageThread.isShutdown()) {
                        try {
                            final Genome genome = storageQueue.poll(250, TimeUnit.MILLISECONDS);
                            if (genome != null && System.currentTimeMillis() - timeLastStored >= 10000) {
                                db.insertGenome(genome);
                                timeLastStored = System.currentTimeMillis();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                private final Context context = session.getContext();
                private final MutationStrategy strategy = setupMutationStrategy();

                @Override
                public void run() {
                    final MersenneTwister rng = new MersenneTwister(seed);
                    final Renderer renderer = new Renderer(context.getWidth(), context.getHeight(), true);

                    Genome genome = currentGenome;
                    while (!processingThreads.isShutdown()) {
                        try {
                            genome = submit(genome);
                            if (genome == null) {
                                genome = new Genome(backgroundColor, Utils.createRandomGenes(rng, context, 10, 20));
                            } else {
                                genome = strategy.apply(rng, context, genome);
                            }
                            renderer.render(genome);
                            genome.fitness = Utils.computeSimpleFitness(genome, targetImage, importanceMap, renderer.getData());
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
            System.out.print("> ");
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
            } if (input.equals("status")) {
                final Genome genome = currentGenome;
                System.out.println("Generated: " + generated + ", Selected: " + selected + ", Mutations: " + genome.mutations + ", Polygons: " + genome.genes.length + ", Points: " + genome.countPoints() + ", Fitness: " + ff.format(genome.fitness));
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
