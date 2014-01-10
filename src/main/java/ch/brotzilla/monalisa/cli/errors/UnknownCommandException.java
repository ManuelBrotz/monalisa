package ch.brotzilla.monalisa.cli.errors;

@SuppressWarnings("serial")
public class UnknownCommandException extends CLIException {

    public UnknownCommandException(String commandName) {
        super(commandName, "Unknown command: " + commandName, null);
    }

}
