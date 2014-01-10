package ch.brotzilla.monalisa.cli.commands;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.cli.CLIContext;
import ch.brotzilla.monalisa.cli.intf.CLICommand;
import ch.brotzilla.monalisa.cli.intf.CLICommandInfo;

@CLICommandInfo(name = "test", description = "this is a test command")
public class TestCommand implements CLICommand {

    @Parameter(description = "the message to print")
    private final List<String> messages = Lists.newArrayList();
    
    public TestCommand() {
    }
    
    public void execute(CLIContext context) throws Exception {
        for (final String msg : messages) {
            System.out.println(msg);
        }
    }

}
