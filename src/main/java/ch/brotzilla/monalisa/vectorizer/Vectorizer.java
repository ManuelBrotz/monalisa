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
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.evolution.intf.FitnessFunction;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.util.MersenneTwister;
import ch.brotzilla.util.TickRate;

public class Vectorizer {

    // created on construction
    private final List<VectorizerListener> listeners;
    private final TickRate tickrate;

    // supplied by the user
    private final SessionManager session;
    private final VectorizerConfig config;
    
    // created on startup
    private MersenneTwister seeds, rng;
    private ExecutorService workerThreads;
    private BlockingQueue<Genome> storageQueue;
    private ExecutorService storageThread;

    // internal state
    private State state = State.Stopped;
    private long lastUpdateFired;
    
    public enum State {
        Running, Stopping, Stopped
    }

    public Vectorizer(SessionManager session, VectorizerConfig config) {
        Preconditions.checkNotNull(session, "The parameter 'session' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkArgument(session.getVectorizerContext() == config.getVectorizerContext(), "The parameters 'session' and 'config' have to reference the same vectorizer context");
        this.listeners = Lists.newArrayList();
        this.tickrate = new TickRate(60);
        this.session = session;
        this.config = config;
    }
    
    public State getState() {
        return state;
    }

    public double getTickRate() {
        return tickrate.getTickRate();
    }

    public SessionManager getSession() {
        return session;
    }
    
    public VectorizerConfig getConfig() {
        return config;
    }
    
    public synchronized int nextSeed() {
        Preconditions.checkState(seeds != null, "No seeds available");
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

        seeds = new MersenneTwister(getSession().getParams().getSeed());
        rng = new MersenneTwister(nextSeed());

        state = State.Running;
        lastUpdateFired = -1;
        tickrate.reset();
        
        storageQueue = Queues.newLinkedBlockingQueue();
        storageThread = Executors.newFixedThreadPool(1);
        storageThread.submit(new StorageThread(this, storageThread, storageQueue));

        final int numThreads = getSession().getParams().getNumThreads();
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
        if (state != State.Running || genome == null) {
            return null;
        }
        
        final VectorizerConfig c = getConfig();
        final VectorizerContext vc = c.getVectorizerContext();
        final EvolutionStrategy es = c.getEvolutionStrategy();
        final FitnessFunction fc = c.getFitnessFunction();
        final Genome latest = vc.getLatestGenome();
        final int numberOfMutations = vc.incNumberOfMutations();
        
        if (es != null) {
            genome = es.apply(rng, c, genome, latest == null || fc.isImprovement(latest, genome));
        }

        if (genome != null && (latest == null || genome.overrideFitness || fc.isImprovement(latest, genome))) {
            genome.numberOfImprovements = vc.incNumberOfImprovements();
            genome.numberOfMutations = numberOfMutations;
            vc.setLatestGenome(genome);
            storageQueue.offer(genome);
            fireImproved(genome);
            lastUpdateFired = System.currentTimeMillis();
        }
        
        tickrate.tick();

        if (lastUpdateFired < 0 || System.currentTimeMillis() - lastUpdateFired >= 1000) {
            lastUpdateFired = System.currentTimeMillis();
            fireUpdate();
        }
        
        return vc.getLatestGenome();
    }

    public void addListener(VectorizerListener listener) {
        Preconditions.checkNotNull(listener, "The parameter 'listener' must not be null");
        if (state != State.Stopped) {
            throw new IllegalStateException("Listeners cannot be added while vectorizer is running");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(VectorizerListener listener) {
        if (state != State.Stopped) {
            throw new IllegalStateException("Listeners cannot be removed while vectorizer is running");
        }
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
    
}
