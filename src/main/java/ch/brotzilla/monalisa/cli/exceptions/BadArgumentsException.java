package ch.brotzilla.monalisa.cli.exceptions;

@SuppressWarnings("serial")
public class BadArgumentsException extends CLICommandException {

    public BadArgumentsException(String commandName, Throwable cause) {
        super(commandName, "Error parsing arguments" + (cause == null ? "" : ": " + cause.getMessage()), cause);
    }

}
