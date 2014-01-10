package ch.brotzilla.monalisa.cli.intf;

public interface CLIReader extends AutoCloseable {

    String[] nextLine(String prompt) throws Exception;
    
}
