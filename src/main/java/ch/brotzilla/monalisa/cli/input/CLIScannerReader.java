package ch.brotzilla.monalisa.cli.input;

import java.util.Scanner;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.cli.intf.CLIReader;
import ch.brotzilla.util.CmdLine;

public class CLIScannerReader implements CLIReader {

    private final Scanner scanner;
    
    public CLIScannerReader(Scanner scanner) {
        this.scanner = Preconditions.checkNotNull(scanner, "The parameter 'scanner' must not be null");
    }

    @Override
    public void close() throws Exception {
        scanner.close();
    }

    @Override
    public String[] nextLine(String prompt) throws Exception {
        System.out.print(prompt);
        final String line = scanner.nextLine();
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        return CmdLine.parse(line);
    }

}
