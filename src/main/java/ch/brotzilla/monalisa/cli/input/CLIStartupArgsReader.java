package ch.brotzilla.monalisa.cli.input;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.cli.intf.CLIReader;

public class CLIStartupArgsReader implements CLIReader {

    private String[] startupArgs;
    private final CLIReader reader;
    
    public CLIStartupArgsReader(String[] startupArgs, CLIReader reader) {
        this.startupArgs = startupArgs;
        this.reader = Preconditions.checkNotNull(reader, "The parameter 'reader' must not be null");
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }

    @Override
    public String[] nextLine(String prompt) throws Exception {
        if (startupArgs == null) {
            return reader.nextLine(prompt);
        } else {
            final String[] tmp = startupArgs;
            startupArgs = null;
            return tmp;
        }
    }

}
