package ch.brotzilla.monalisa.cli.exceptions;

@SuppressWarnings("serial")
public class UnknownCommandException extends CLICommandException {

    public UnknownCommandException(String commandName) {
        super(commandName, "Unknown command: " + commandName, null);
    }

}
