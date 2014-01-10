package ch.brotzilla.monalisa.cli.exceptions;

@SuppressWarnings("serial")
public abstract class CLICommandException extends CLIException {

    private final String commandName;
    
    protected CLICommandException(String commandName, String message, Throwable cause) {
        super(message, cause);
        this.commandName = commandName;
    }
    
    public String getCommandName() {
        return commandName;
    }

}
