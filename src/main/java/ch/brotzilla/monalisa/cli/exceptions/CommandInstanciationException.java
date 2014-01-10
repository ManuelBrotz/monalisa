package ch.brotzilla.monalisa.cli.exceptions;

@SuppressWarnings("serial")
public class CommandInstanciationException extends CLICommandException {

    public CommandInstanciationException(String commandName, Throwable cause) {
        super(commandName, "Cannot instanciate command: " + commandName, cause);
    }

}
