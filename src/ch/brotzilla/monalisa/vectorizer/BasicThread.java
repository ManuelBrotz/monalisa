package ch.brotzilla.monalisa.vectorizer;

import java.util.concurrent.ExecutorService;

import com.google.common.base.Preconditions;

public abstract class BasicThread implements Runnable {

    private final Vectorizer owner;
    private final ExecutorService executor;
    
    private boolean running = false;
    private boolean finished = false;
    
    protected abstract void execute() throws Exception;
    
    public BasicThread(Vectorizer owner, ExecutorService executor) {
        Preconditions.checkNotNull(owner, "The parameter 'owner' must not be null");
        Preconditions.checkNotNull(executor, "The parameter 'executor' must not be null");
        this.owner = owner;
        this.executor = executor;
    }
    
    public Vectorizer getOwner() {
        return owner;
    }
    
    public ExecutorService getExecutor() {
        return executor;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public boolean isFinished() {
        return finished;
    }

    @Override
    public final void run() {
        running = true;
        try {
            execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            running = false;
            finished = true;
        }
    }
}
