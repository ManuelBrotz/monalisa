package ch.brotzilla.monalisa.db;

import org.tmatesoft.sqljet.core.SqlJetException;

import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class TransactionException extends SqlJetException  {
    public TransactionException(Exception cause) {
        super(Preconditions.checkNotNull(cause, "The parameter 'cause' must not be null").getMessage(), cause);
    }
}