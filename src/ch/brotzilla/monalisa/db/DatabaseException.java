package ch.brotzilla.monalisa.db;

import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class DatabaseException extends RuntimeException  {
    public DatabaseException(Exception cause) {
        super(Preconditions.checkNotNull(cause, "The parameter 'cause' must not be null").getMessage(), cause);
    }
}