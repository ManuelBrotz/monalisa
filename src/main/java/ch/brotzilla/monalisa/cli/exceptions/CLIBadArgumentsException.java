package ch.brotzilla.monalisa.cli.exceptions;

@SuppressWarnings("serial")
public class CLIBadArgumentsException extends CLICommandException {

    public CLIBadArgumentsException(String commandName, Throwable cause) {
        super(commandName, "Error parsing arguments" + (cause == null ? "" : ": " + cause.getMessage()), cause);
    }

}
