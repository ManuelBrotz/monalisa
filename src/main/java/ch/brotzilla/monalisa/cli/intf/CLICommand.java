package ch.brotzilla.monalisa.cli.intf;

import ch.brotzilla.monalisa.Monalisa;

public interface CLICommand {

    void execute(Monalisa context) throws Exception;
    
}
