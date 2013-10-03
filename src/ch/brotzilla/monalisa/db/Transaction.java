package ch.brotzilla.monalisa.db;

import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.google.common.base.Preconditions;

public abstract class Transaction<T extends Object> {
    
    protected final SqlJetDb db;
    
    private T result = null;
    
    public Transaction(final SqlJetDb db, final SqlJetTransactionMode mode) {
        Preconditions.checkNotNull(db, "The parameter 'db' must not be null");
        this.db = db;
        try {
            db.beginTransaction(mode);
            try {
                result = run();
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
            db.commit();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
    
    public T getResult() {
        return result;
    }
    
    public abstract T run() throws Exception;
}