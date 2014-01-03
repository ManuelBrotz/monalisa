package ch.brotzilla.monalisa.vectorizer;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.PolygonCache;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.utils.TickRate;

public class Vectorizer {

    private State state = State.Stopped;

    // created on construction
    private final List<VectorizerListener> listeners;
    private final TickRate tickrate;

    // created on startup
    private MersenneTwister rng;
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
        this.listeners = Lists.newArrayList();
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
    
    public synchronized int nextSeed() {
        int seed = rng.nextInt();
        while (seed == 0) {
            seed = rng.nextInt();
        }
        return seed;
    }

    public void start() {
        if (state != State.Stopped) {
            throw new IllegalStateException("Unable to start vectorizer");
        }
        if (!isReady()) {
            throw new IllegalStateException("Vectorizer not ready");
        }

        state = State.Running;
        tickrate.reset();
        rng = new MersenneTwister(session.getParams().getSeed());
        polygonCache = new PolygonCache(getWidth(), getHeight());

        storageQueue = Queues.newLinkedBlockingQueue();
        storageThread = Executors.newFixedThreadPool(1);
        storageThread.submit(new StorageThread(this, storageThread, storageQueue));

        final int numThreads = session.getParams().getNumThreads();
        workerThreads = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            workerThreads.submit(new WorkerThread(this, workerThreads));
        }
        
        fireStarted(getSession().getVectorizerContext().getLatestGenome());
    }

    public void stop() {
        if (state != State.Running) {
            return;
        }
        state = State.Stopping;
        fireStopping();
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
            fireStopped();
        }
    }

    public synchronized Genome submit(Genome genome) {
        if (state != State.Running) {
            return null;
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
                polygonCache.submit(genome);
                fireImprovement(genome);
            }
            tickrate.tick();
        }
        return vc.getLatestGenome();
    }
    
    public void addListener(VectorizerListener listener) {
        Preconditions.checkNotNull(listener, "The parameter 'listener' must not be null");
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(VectorizerListener listener) {
        listeners.remove(listener);
    }

    private void fireStarted(Genome latest) {
        for (VectorizerListener l : listeners) {
            l.started(this, latest);
        }
    }
    
    private void fireImprovement(Genome latest) {
        for (VectorizerListener l : listeners) {
            l.improvement(this, latest);
        }
    }
    
    private void fireStopping() {
        for (VectorizerListener l : listeners) {
            l.stopping(this);
        }
    }
    
    private void fireStopped() {
        for (VectorizerListener l : listeners) {
            l.stopped(this);
        }
    }

    private void checkStopped(String property) {
        if (state != State.Stopped) {
            throw new IllegalStateException("Property '" + property + "' cannot be changed while vectorizer is running");
        }
    }
}
