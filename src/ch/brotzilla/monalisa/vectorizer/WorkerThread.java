package ch.brotzilla.monalisa.vectorizer;

import java.util.concurrent.ExecutorService;

public class WorkerThread extends BasicThread {

    @Override
    protected void execute() {
        
    }

    public WorkerThread(Vectorizer owner, ExecutorService executor) {
        super(owner, executor);
    }

}
