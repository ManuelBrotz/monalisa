package ch.brotzilla.monalisa.vectorizer;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.PolygonCache;
import ch.brotzilla.monalisa.utils.TickRate;

public class Vectorizer {

    private final SessionManager session;
    
    private final TickRate tickrate;
    private final PolygonCache polygonCache;
    
    private final BlockingQueue<Genome> storageQueue;

    private boolean running;
    
    private ExecutorService workerThreads;
    private ExecutorService storageThread;

    private Genome currentGenome;
    private int generated, selected;
    
    void submit(Genome genome) {
        
    }

    public Vectorizer(SessionManager session) {
        Preconditions.checkNotNull(session, "The parameter 'session' must not be null");
        this.session = session;
        this.tickrate = new TickRate(60);
        this.polygonCache = new PolygonCache(session.getWidth(), session.getHeight());
        this.storageQueue = Queues.newLinkedBlockingQueue();
    }
    
    public SessionManager getSessionManager() {
        return session;
    }
    
    public TickRate getTickRate() {
        return tickrate;
    }

    public synchronized void start() {
        if (running) {
            throw new IllegalStateException("Vectorizer is already running");
        }
        running = true;
        
        storageThread = Executors.newFixedThreadPool(1);
        storageThread.submit(new StorageThread(this, storageQueue));
        
        final int numThreads = session.getParams().getNumThreads();
        workerThreads = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            workerThreads.submit(new WorkerThread(this));
        }
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
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
    }
}
