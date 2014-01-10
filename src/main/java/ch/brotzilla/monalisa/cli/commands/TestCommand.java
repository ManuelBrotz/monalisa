package ch.brotzilla.monalisa.cli.commands;

import ch.brotzilla.monalisa.cli.CLIContext;
import ch.brotzilla.monalisa.cli.intf.CLICommand;
import ch.brotzilla.monalisa.cli.intf.CLICommandInfo;

@CLICommandInfo(name = "test", description = "this is a test command")
public class TestCommand implements CLICommand {

    public TestCommand() {
    }
    
    public void execute(CLIContext context) throws Exception {
        System.out.println("Hello World!");
    }

}
