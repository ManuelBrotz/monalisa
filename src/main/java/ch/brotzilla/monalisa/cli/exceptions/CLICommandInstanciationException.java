package ch.brotzilla.monalisa.cli.exceptions;

@SuppressWarnings("serial")
public class CLICommandInstanciationException extends CLICommandException {

    public CLICommandInstanciationException(String commandName, Throwable cause) {
        super(commandName, "Cannot instanciate command: " + commandName, cause);
    }

}
