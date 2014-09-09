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
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.evolution.intf.RendererFactory;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.util.MersenneTwister;
import ch.brotzilla.util.TickRate;

public class Vectorizer {

    private State state = State.Stopped;
    private long lastUpdateFired;

    // created on construction
    private final List<VectorizerListener> listeners;
    private final TickRate tickrate;

    // created on startup
    private MersenneTwister seeds, rng;

    // supplied by the user
    private SessionManager session;
    private EvolutionContext evolutionContext;
    private EvolutionStrategy evolutionStrategy;
    private MutationStrategy mutationStrategy;
    private GenomeFactory genomeFactory;
    private RendererFactory rendererFactory;

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
        return session != null 
                && evolutionContext != null 
                && mutationStrategy != null 
                && evolutionStrategy != null
                && genomeFactory != null
                && rendererFactory != null;
    }

    public State getState() {
        return state;
    }

    public TickRate getTickRate() {
        return tickrate;
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

    public EvolutionContext getEvolutionContext() {
        return evolutionContext;
    }

    public void setEvolutionContext(EvolutionContext value) {
        checkStopped("EvolutionContext");
        this.evolutionContext = value;
    }

    public EvolutionStrategy getEvolutionStrategy() {
        return evolutionStrategy;
    }

    public void setEvolutionStrategy(EvolutionStrategy value) {
        checkStopped("EvolutionStrategy");
        this.evolutionStrategy = value;
    }

    public MutationStrategy getMutationStrategy() {
        return mutationStrategy;
    }

    public void setMutationStrategy(MutationStrategy value) {
        checkStopped("MutationStrategy");
        this.mutationStrategy = value;
    }

    public GenomeFactory getGenomeFactory() {
        return genomeFactory;
    }

    public void setGenomeFactory(GenomeFactory value) {
        checkStopped("GenomeFactory");
        this.genomeFactory = value;
    }

    public RendererFactory getRendererFactory() {
        return rendererFactory;
    }

    public void setRendererFactory(RendererFactory value) {
        checkStopped("RendererFactory");
        this.rendererFactory = value;
    }

    public synchronized int nextSeed() {
        Preconditions.checkNotNull(seeds, "No seeds available");
        int seed = seeds.nextInt();
        while (seed == 0) {
            seed = seeds.nextInt();
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
        lastUpdateFired = -1;
        tickrate.reset();
        seeds = new MersenneTwister(session.getParams().getSeed());
        rng = new MersenneTwister(nextSeed());

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
        try {
            try {
                fireStopping();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        final EvolutionContext ev = getEvolutionContext();
        final EvolutionStrategy es = getEvolutionStrategy();
        final Genome latest = vc.getLatestGenome();
        if (es != null && genome != null) {
            genome = es.apply(rng, vc, ev, genome);
        }
        if (genome != null) {
            final int numberOfMutations = vc.incNumberOfMutations();
            if (latest == null || genome.fitness < latest.fitness || genome.overrideFitnessFlag) {
                genome.numberOfImprovements = vc.incNumberOfImprovements();
                genome.numberOfMutations = numberOfMutations;
                vc.setLatestGenome(genome);
                storageQueue.offer(genome);
                fireImproved(genome);
                lastUpdateFired = System.currentTimeMillis();
            }
            tickrate.tick();
        }
        if (lastUpdateFired < 0 || System.currentTimeMillis() - lastUpdateFired >= 1000) {
            lastUpdateFired = System.currentTimeMillis();
            fireUpdate();
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

    private void fireImproved(Genome latest) {
        for (VectorizerListener l : listeners) {
            l.improved(this, latest);
        }
    }

    private void fireUpdate() {
        for (VectorizerListener l : listeners) {
            l.update(this);
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
