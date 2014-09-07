package ch.brotzilla.monalisa;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;


import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.evolution.selectors.BasicIndexSelector;
import ch.brotzilla.monalisa.evolution.selectors.GaussianRangeSelector;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.evolution.strategies.LayeredEvolutionStrategy;
import ch.brotzilla.monalisa.gui.MainWindow;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.utils.Params;
import ch.brotzilla.monalisa.vectorizer.Vectorizer;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.monalisa.vectorizer.VectorizerListener;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.base.Preconditions;

public class Monalisa {

    protected Params params;
    protected SessionManager session;
    
    protected Vectorizer vectorizer;

    protected MainWindow mainWindow;

    protected final DecimalFormat ff = new DecimalFormat("#,###,###,###,##0.######");
    protected final DecimalFormat rf = new DecimalFormat("#,##0.00");

    protected EvolutionStrategy setupEvolutionStrategy() {
        return new LayeredEvolutionStrategy();
    }

    protected EvolutionContext setupEvolutionContext() {
        final EvolutionContext c = new EvolutionContext();
        c.setOuterBorder(session.getWidth() / 2, session.getHeight() / 2);
        c.setInnerBorder(session.getWidth() / 10, session.getHeight() / 10);
        c.setGeneIndexSelector(new BasicIndexSelector());
        c.setPointMutationRange(new GaussianRangeSelector(15));
        c.setColorChannelMutationRange(new GaussianRangeSelector(10));
        return c;
    }

    protected void printError() {
        System.out.println("Usage:");
        params.getParser().printUsage(System.out);
        if (params.getNumArguments() > 0) {
            System.out.println();
            System.out.println("Parameters:");
            System.out.println(params.getArgumentsLine());
        }
        if (params.getError() != null) {
            System.out.println();
            System.out.println("Error: " + params.getError().getMessage());
            System.out.println();
            params.getError().printStackTrace();
        }
    }

    public Monalisa(String[] args) {
        this(new Params(args));
    }

    public Monalisa(Params params) {
        Preconditions.checkNotNull(params, "The parameter 'params' must not be null");
        this.params = params;
    }

    public void setup() throws IOException, SQLiteException {
        if (!params.isReady())
            throw new IllegalStateException("Not ready");

        this.session = new SessionManager(params);

        final int imageWidth = session.getWidth(), imageHeight = session.getHeight();

        if (session.isSessionResumed()) {
            System.out.println("Resumed session '" + session.getSessionName() + "': " + session.getDatabaseFile());
        } else {
            System.out.println("Started new session '" + session.getSessionName() + "': " + session.getDatabaseFile());
            System.out.println("Using image file: " + params.getTargetImageFile());
        }
        System.out.println("Image size: " + imageWidth + "x" + imageHeight + ", " + session.getVectorizerContext().getTargetImage().getLength() + " pixels");

        if (session.isSessionResumed()) {
            int genomes = session.getVectorizerContext().getNumberOfGenomes();
            System.out.println("Counted " + genomes + " genomes in database.");
        }

        final Genome latestGenome = session.getVectorizerContext().getLatestGenome();
        
        if (session.isSessionResumed() && latestGenome == null) {
            System.out.println("No latest genome found in database.");
        }

        if (params.getExportLatest() != null) {
            try {
                session.exportSVG(latestGenome, params.getExportLatest(), false, true, true);
                session.exportSVG(latestGenome, params.getExportLatest(), true, true, true);
            } catch (Exception e) {
                System.out.println("Failed exporting latest genome as svg document.");
                e.printStackTrace();
            }
        }
        
        this.vectorizer = new Vectorizer();
        vectorizer.setSession(this.session);
        vectorizer.setEvolutionContext(setupEvolutionContext());
        vectorizer.setEvolutionStrategy(setupEvolutionStrategy());
        
        vectorizer.addListener(new VectorizerListener() {
            @Override
            public void started(Vectorizer v, Genome latest) {
                if (mainWindow != null) {
                    mainWindow.submit(latest);
                }
            }
            @Override
            public void improved(Vectorizer v, Genome latest) {
                if (mainWindow != null) {
                    mainWindow.submit(latest);
                    mainWindow.getStatusDisplay().update(v);
                }
            }
            @Override
            public void update(Vectorizer v) {
                if (mainWindow != null) {
                    mainWindow.getStatusDisplay().update(v);
                }
            }
            @Override
            public void stopping(Vectorizer v) {
                System.out.println("Stopping...");
            }
            @Override
            public void stopped(Vectorizer v) {
                System.out.println("Stopped!");
            }
        });
    }

    public void start() {
        if (!params.isReady())
            throw new IllegalStateException("Not ready");

        if (params.getShowGui()) {
            showGui();
        }
        
        vectorizer.start();
    }

    public void quit() {
        vectorizer.stop();
        System.exit(0);
    }

    public void showGui() {
        if (mainWindow == null) {
            try {
                mainWindow = new MainWindow(this, session, vectorizer.getVectorizerContext().getLatestGenome());
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
                quit();
            } else if (input.equals("show-gui")) {
                showGui();
            } else if (input.equals("status")) {
                final VectorizerContext vc = vectorizer.getVectorizerContext();
                final Genome genome = vc.getLatestGenome();
                System.out.println("Mutations: " + vc.getNumberOfMutations() + ", Improvements: " + vc.getNumberOfImprovements() + ", Polygons: " + genome.countPolygons() + ", Points: "
                        + genome.countPoints() + ", Fitness: " + ff.format(genome.fitness));
            } else if (input.equals("rate")) {
                System.out.println(rf.format(vectorizer.getTickRate().getTickRate()) + " images/sec");
            } else if (input.equals("cache")) {
                System.out.println("Number of cached polygons: <not available>"/* + vectorizer.getPolygonCache().getSize()*/);
            } else {
                System.out.println("Unknown command: " + input);
            }
        }
    }

    public static void main(String[] args) {
        final Monalisa ml = new Monalisa(args);
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
