package ch.brotzilla.monalisa.vectorizer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import ch.brotzilla.monalisa.db.Database;
import ch.brotzilla.monalisa.evolution.genes.Genome;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.base.Preconditions;

public class StorageThread extends BasicThread {
    
    private final BlockingQueue<Genome> storageQueue;

    @Override
    protected void execute() throws IOException, SQLiteException {
        
        final Vectorizer v = getOwner();
        
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        long timeLastStored = 0;
        try (final Database db = v.getSession().connect()) {
            while (!getExecutor().isShutdown()) {
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

    public StorageThread(Vectorizer owner, ExecutorService executor, BlockingQueue<Genome> storageQueue) {
        super(owner, executor);
        Preconditions.checkNotNull(storageQueue, "The parameter 'storageQueue' must not be null");
        this.storageQueue = storageQueue;
    }
    
}
