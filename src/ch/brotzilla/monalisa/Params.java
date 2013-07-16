package ch.brotzilla.monalisa;

import java.io.File;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

class Params {
	
	@Option(name = "-i", aliases = {"--input"}, required = true, usage = "the input image")
	private File inputFile;
	
	public Params(String[] args) {
		final CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(80);
		try {
			parser.parseArgument(args);
			validate();
		} catch (Exception e) {
			System.out.println("Usage:");
			parser.printUsage(System.out);
			System.out.println();
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}
	
	public File getInputFile() {
		return inputFile;
	}
	
	public void validate() {
		if (!inputFile.isFile())
			throw new IllegalArgumentException("--input is not a valid file");
	}
	
}