package ch.brotzilla.monalisa.cli.exceptions;

@SuppressWarnings("serial")
public class CLIUnknownCommandException extends CLICommandException {

    public CLIUnknownCommandException(String commandName) {
        super(commandName, "Unknown command: " + commandName, null);
    }

}
