package ch.brotzilla.monalisa.vectorizer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Queues;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.PolygonCache;
import ch.brotzilla.monalisa.utils.TickRate;

public class Vectorizer {

    private State state = State.Stopped;

    // created on construction
    private final TickRate tickrate;

    // created on startup
    private PolygonCache polygonCache;

    // supplied by the user
    private SessionManager session;
    private GenomeFactory genomeFactory;
    private EvolutionContext evolutionContext;
    private MutationStrategy mutationStrategy;

    // created on startup
    private ExecutorService workerThreads;
    private BlockingQueue<Genome> storageQueue;
    private ExecutorService storageThread;

    public enum State {
        Running, Stopping, Stopped
    }

    public Vectorizer() {
        this.tickrate = new TickRate(60);
    }

    public boolean isReady() {
        return session != null && genomeFactory != null && evolutionContext != null && mutationStrategy != null;
    }

    public State getState() {
        return state;
    }

    public TickRate getTickRate() {
        return tickrate;
    }

    public PolygonCache getPolygonCache() {
        return polygonCache;
    }

    public int getWidth() {
        return session == null ? 0 : session.getWidth();
    }

    public int getHeight() {
        return session == null ? 0 : session.getHeight();
    }

    public VectorizerContext getVectorizerContext() {
        return session == null ? null : session.getVectorizerContext();
    }

    public SessionManager getSession() {
        return session;
    }
    
    public void setSession(SessionManager value) {
        checkStopped("Session");
        this.session = value;
    }
    
    public GenomeFactory getGenomeFactory() {
        return genomeFactory;
    }
    
    public void setGenomeFactory(GenomeFactory value) {
        checkStopped("GenomeFactory");
        this.genomeFactory = value;
    }
    
    public EvolutionContext getEvolutionContext() {
        return evolutionContext;
    }

    public void setEvolutionContext(EvolutionContext value) {
        checkStopped("EvolutionContext");
        this.evolutionContext = value;
    }

    public MutationStrategy getMutationStrategy() {
        return mutationStrategy;
    }

    public void setMutationStrategy(MutationStrategy value) {
        checkStopped("MutationStrategy");
        this.mutationStrategy = value;
    }

    public synchronized void start() {
        if (state != State.Stopped) {
            throw new IllegalStateException("Unable to start vectorizer");
        }
        if (!isReady()) {
            throw new IllegalStateException("Vectorizer not ready");
        }

        state = State.Running;
        tickrate.reset();
        polygonCache = new PolygonCache(getWidth(), getHeight());

        storageQueue = Queues.newLinkedBlockingQueue();
        storageThread = Executors.newFixedThreadPool(1);
        storageThread.submit(new StorageThread(this, storageThread, storageQueue));

        final int numThreads = session.getParams().getNumThreads();
        workerThreads = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            workerThreads.submit(new WorkerThread(this, workerThreads));
        }
    }

    public synchronized void stop() {
        if (state != State.Running) {
            return;
        }
        state = State.Stopping;
        try {
            storageThread.shutdown();
            workerThreads.shutdown();
            try {
                storageThread.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                workerThreads.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            polygonCache.shutdown();
        } finally {
            state = State.Stopped;
            storageThread = null;
            workerThreads = null;
            storageQueue = null;
        }
    }

    synchronized public Genome submit(Genome genome) {
        if (state != State.Running) {
            throw new IllegalStateException("Vectorizer is not running");
        }
        final VectorizerContext vc = getVectorizerContext();
        final Genome latest = vc.getLatestGenome();
        if (genome != null && genome != latest) {
            final int numberOfMutations = vc.incNumberOfMutations();
            if (latest == null || genome.fitness < latest.fitness) {
                genome.numberOfImprovements = vc.incNumberOfImprovements();
                genome.numberOfMutations = numberOfMutations;
                vc.setLatestGenome(genome);
                storageQueue.offer(genome);
            }
            tickrate.tick();
        }
        return vc.getLatestGenome();
    }

    private void checkStopped(String property) {
        if (state != State.Stopped) {
            throw new IllegalStateException("Property '" + property + "' cannot be changed while vectorizer is running");
        }
    }
}
