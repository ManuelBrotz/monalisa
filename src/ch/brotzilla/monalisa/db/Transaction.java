package ch.brotzilla.monalisa.db;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.google.common.base.Preconditions;

public abstract class Transaction<T extends Object> {
    
    private final SqlJetDb db;
    private final SqlJetTransactionMode mode;
    private final T defaultResult;

    private T result;
    
    public Transaction(final SqlJetDb db, final SqlJetTransactionMode mode, T defaultResult) {
        Preconditions.checkNotNull(db, "The parameter 'db' must not be null");
        Preconditions.checkNotNull(mode, "The parameter 'mode' must not be null");
        this.db = db;
        this.mode = mode;
        this.defaultResult = defaultResult;
        this.result = defaultResult;
    }
    
    public Transaction(final SqlJetDb db, final SqlJetTransactionMode mode) {
        this(db, mode, null);
    }
    
    public final SqlJetDb getDb() {
        return db;
    }
    
    public final SqlJetTransactionMode getMode() {
        return mode;
    }
    
    public final T getDefaultResult() {
        return defaultResult;
    }
    
    public final T getResult() {
        return result;
    }
    
    public final T execute() throws SqlJetException {
        try {
            db.beginTransaction(mode);
            try {
                result = transaction();
                db.commit();
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        } catch (SqlJetException e) {
            throw e;
        } catch (Exception e) {
            throw new TransactionException(e);
        }
        return result;
    }
    
    public abstract T transaction() throws Exception;
}