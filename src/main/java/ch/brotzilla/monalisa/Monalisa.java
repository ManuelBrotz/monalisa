package ch.brotzilla.monalisa;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;

import ch.brotzilla.monalisa.evolution.constraints.ComplexMutationConstraints;
import ch.brotzilla.monalisa.evolution.constraints.GeneAlphaConstraint;
import ch.brotzilla.monalisa.evolution.constraints.GeneAngleConstraint;
import ch.brotzilla.monalisa.evolution.constraints.GeneSelfIntersectionConstraint;
import ch.brotzilla.monalisa.evolution.constraints.GeneStrictCoordinatesConstraint;
import ch.brotzilla.monalisa.evolution.constraints.GeneVertexToEdgeDistanceConstraint;
import ch.brotzilla.monalisa.evolution.constraints.MutationConstraints;
import ch.brotzilla.monalisa.evolution.fitness.BasicFitnessFunction;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.evolution.intf.FitnessFunction;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.evolution.intf.RendererFactory;
import ch.brotzilla.monalisa.evolution.mutations.GeneAddPointMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneAlphaChannelMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneColorBrighterMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneColorChannelMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneColorDarkerMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneColorHueMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneMoveMutation;
import ch.brotzilla.monalisa.evolution.mutations.GenePointMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneRemovePointMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneSwapPointsMutation;
import ch.brotzilla.monalisa.evolution.mutations.GenomeSwapGenesMutation;
import ch.brotzilla.monalisa.evolution.mutations.ProbabilityGeneMutationSelector;
import ch.brotzilla.monalisa.evolution.selectors.GaussianRangeSelector;
import ch.brotzilla.monalisa.evolution.selectors.TailIndexSelector;
import ch.brotzilla.monalisa.evolution.strategies.BasicGenomeFactory;
import ch.brotzilla.monalisa.evolution.strategies.BasicMutationStrategy;
import ch.brotzilla.monalisa.evolution.strategies.MutationConfig;
import ch.brotzilla.monalisa.evolution.strategies.ProgressiveAddPolygonStrategy;
import ch.brotzilla.monalisa.evolution.strategies.ProgressiveEvolutionStrategy;
import ch.brotzilla.monalisa.gui.MainWindow;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.CachingTailRenderer;
import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.utils.Params;
import ch.brotzilla.monalisa.utils.UI;
import ch.brotzilla.monalisa.vectorizer.Vectorizer;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.monalisa.vectorizer.VectorizerListener;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteException;
import com.google.common.base.Preconditions;

public class Monalisa {

    public final static boolean SetupLibraryPath = false;
    public final static String Version = "Monalisa v0.5";
    public final static String Author = "2015, by Manuel Brotz, manu.brotz@gmx.ch";
    
    protected Params params;
    protected SessionManager session;
    
    protected Vectorizer vectorizer;

    protected MainWindow mainWindow;

    protected final DecimalFormat rf = new DecimalFormat("#,##0.00");

    protected static void printVersionInfo() {
        System.out.println(Version);
        System.out.println(Author);
    }
    
    protected static void setupLibraryPath() {
        final Properties props = System.getProperties();
        final String libraryPath = new File("sqlite4java/").getAbsolutePath() + "/";
        System.out.println("System property '" + SQLite.LIBRARY_PATH_PROPERTY + "' has been set to:");
        System.out.println(libraryPath);
        System.out.println();
        props.setProperty(SQLite.LIBRARY_PATH_PROPERTY, libraryPath);
    }
    
    protected static MutationConfig setupMutationConfig(SessionManager session) {
        return new MutationConfig.Builder()
        .setOuterBorder(0, 0)
        .setInnerBorder(0, 0)
        .setGeneIndexSelector(new TailIndexSelector(15))
        .setPointMutationRange(new GaussianRangeSelector(15, false))
        .setColorChannelMutationRange(new GaussianRangeSelector(10, false))
        .setGeneVersusGenomeMutationProbability(0.99d)
        .setMinMutationsPerGenome(1)
        .setMaxMutationsPerGenome(2)
        .build();
    }

    protected static RendererFactory setupRendererFactory() {
        return new RendererFactory() {
            @Override
            public Renderer createRenderer(VectorizerConfig config) {
                return new CachingTailRenderer(15, config.getWidth(), config.getHeight(), true);
            }
        };
    }
    
    protected static GenomeFactory setupGenomeFactory() {
        return new BasicGenomeFactory(5, 5);
    }
    
    protected static EvolutionStrategy setupEvolutionStrategy() {
        return new ProgressiveEvolutionStrategy(new ProgressiveAddPolygonStrategy());
    }
    
    protected static MutationStrategy setupMutationStrategy() {
        return new BasicMutationStrategy(
                new ProbabilityGeneMutationSelector.Builder()
                .add(0.75d, new GenePointMutation())
                .add(0.15d, new GeneColorHueMutation(), new GeneAlphaChannelMutation(), new GeneColorChannelMutation(), new GeneColorBrighterMutation(), new GeneColorDarkerMutation())
                .add(0.10d, new GeneMoveMutation(), new GeneAddPointMutation(), new GeneRemovePointMutation(), new GeneSwapPointsMutation())
                .build(),
                new GenomeSwapGenesMutation());
    }

    protected static MutationConstraints setupMutationConstraints() {
        return new ComplexMutationConstraints.Builder()
        .add(new GeneAlphaConstraint(10, 245))
        .add(new GeneAngleConstraint(15.0d))
        .add(new GeneStrictCoordinatesConstraint())
        .add(new GeneVertexToEdgeDistanceConstraint(5.0d))
        .add(new GeneSelfIntersectionConstraint())
        .build();
    }
    
    protected static FitnessFunction setupFitnessFunction() {
        return new BasicFitnessFunction(3.0, 1.0, 1.0, 1.0);
    }
    
    protected static Vectorizer setupVectorizer(SessionManager session) {
        return new Vectorizer(session, new VectorizerConfig.Builder()
        .setVectorizerContext(session.getVectorizerContext())
        .setMutationConfig(setupMutationConfig(session))
        .setMutationStrategy(setupMutationStrategy())
        .setEvolutionStrategy(setupEvolutionStrategy())
        .setRendererFactory(setupRendererFactory())
        .setGenomeFactory(setupGenomeFactory())
        .setConstraints(setupMutationConstraints())
        .setFitnessFunction(setupFitnessFunction())
        .build());
    }
    
    protected MainWindow setupMainWindow() throws IOException {
        final MainWindow result = new MainWindow(this);
        result.setBounds(UI.computeScreenCenteredWindowBounds(
                new Dimension(vectorizer.getConfig().getWidth() + 50, vectorizer.getConfig().getHeight() + 150),
                new Dimension(640, 480)));
        result.setVisible(true);
        return result;
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
    
    public Params getParams() {
        return params;
    }
    
    public SessionManager getSessionManager() {
        return session;
    }
    
    public Vectorizer getVectorizer() {
        return vectorizer;
    }
    
    public MainWindow getMainWindow() {
        return mainWindow;
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
        
        this.vectorizer = setupVectorizer(session);
        this.mainWindow = setupMainWindow();
        
        vectorizer.addListener(new VectorizerListener() {
            @Override
            public void started(Vectorizer v, Genome latest) {
                mainWindow.submit(v.getConfig(), latest);
            }
            @Override
            public void improved(Vectorizer v, Genome latest) {
                mainWindow.submit(v.getConfig(), latest);
                mainWindow.getStatusDisplay().update(v.getConfig(), v.getTickRate());
            }
            @Override
            public void update(Vectorizer v) {
                mainWindow.getStatusDisplay().update(v.getConfig(), v.getTickRate());
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

        vectorizer.start();
    }

    public void quit() {
        vectorizer.stop();
        System.exit(0);
    }

    public static void main(String[] args) {
        printVersionInfo();
        if (SetupLibraryPath) {
            setupLibraryPath();
        }
        final Monalisa ml = new Monalisa(args);
        try {
            if (ml.params.isReady()) {
                ml.setup();
                ml.start();
            } else {
                ml.printError();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
