package ch.brotzilla.monalisa.cli.exceptions;

@SuppressWarnings("serial")
public abstract class CLIException extends Exception {

    private final String commandName;
    
    protected CLIException(String commandName, String message, Throwable cause) {
        super(message, cause);
        this.commandName = commandName;
    }
    
    public String getCommandName() {
        return commandName;
    }

}
