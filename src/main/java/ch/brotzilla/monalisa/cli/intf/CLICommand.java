package ch.brotzilla.monalisa.cli.intf;

import ch.brotzilla.monalisa.cli.CLIContext;

public interface CLICommand {

    void execute(CLIContext context) throws Exception;
    
}
