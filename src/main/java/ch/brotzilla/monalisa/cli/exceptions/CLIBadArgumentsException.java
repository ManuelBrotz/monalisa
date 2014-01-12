package ch.brotzilla.monalisa.cli.exceptions;

import com.beust.jcommander.JCommander;

@SuppressWarnings("serial")
public class CLIBadArgumentsException extends CLIArgumentsException {

    public CLIBadArgumentsException(String commandName, JCommander commander, Throwable cause) {
        super(commandName, commander, cause);
    }
    
}
