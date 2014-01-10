package ch.brotzilla.monalisa.cli.exceptions;

@SuppressWarnings("serial")
public abstract class CLIException extends Exception {

    public CLIException(String message, Throwable cause) {
        super(message, cause);
    }

}
