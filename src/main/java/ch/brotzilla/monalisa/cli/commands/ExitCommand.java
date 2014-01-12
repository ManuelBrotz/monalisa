package ch.brotzilla.monalisa.cli.commands;

import ch.brotzilla.monalisa.Monalisa;
import ch.brotzilla.monalisa.cli.exceptions.CLIExitException;
import ch.brotzilla.monalisa.cli.intf.CLICommand;
import ch.brotzilla.monalisa.cli.intf.CLICommandInfo;

@CLICommandInfo(name = "exit", description = "exits the program")
public class ExitCommand implements CLICommand {

    public ExitCommand() {
    }

    @Override
    public void execute(Monalisa context) throws Exception {
        try {
            context.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new CLIExitException();
    }

}
