package ch.brotzilla.monalisa.cli.exceptions;

import com.beust.jcommander.JCommander;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public abstract class CLIArgumentsException extends CLICommandException {

    private final JCommander commander;
    
    public CLIArgumentsException(String commandName, JCommander commander, Throwable cause) {
        super(commandName, "Error parsing arguments" + (cause == null ? "" : ": " + cause.getMessage()), cause);
        Preconditions.checkNotNull(commander, "The parameter 'commander' must not be null");
        this.commander = commander;
    }
    
    public void printUsage() {
        commander.usage();
    }

}
