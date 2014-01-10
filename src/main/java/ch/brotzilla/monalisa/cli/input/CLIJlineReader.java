package ch.brotzilla.monalisa.cli.input;

import com.google.common.base.Preconditions;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import ch.brotzilla.monalisa.cli.intf.CLIReader;
import ch.brotzilla.util.CmdLine;

public class CLIJlineReader implements CLIReader {

    private final ConsoleReader reader;
    private boolean closed = false;
    
    public CLIJlineReader(ConsoleReader reader) {
        this.reader = Preconditions.checkNotNull(reader, "The parameter 'reader' must not be null");
    }

    @Override
    public void close() throws Exception {
        closed = true;
        try {
            TerminalFactory.get().restore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] nextLine(String prompt) throws Exception {
        if (closed) {
            throw new IllegalStateException("Reader has already been closed");
        }
        final String line = reader.readLine(prompt);
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        return CmdLine.parse(line);
    }

}
