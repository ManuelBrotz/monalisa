package ch.brotzilla.monalisa;

import java.awt.Color;
import java.io.File;
import java.util.Random;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

class Params {
	
	@Option(name = "-i", aliases = {"--input"}, metaVar = "File", required = true, usage = "the input image")
	private File inputFile;
	
	@Option(name = "-o", aliases = {"--output-folder"}, metaVar = "Folder", required = true, usage = "the output folder")
	private File outputFolder;
	
	@Option(name = "-s", aliases = {"--seed"}, metaVar = "Number", required = false, usage = "the seed for the random number generator")
	private int seed = 0;
	
	@Option(name = "-b", aliases = {"--background-color"}, metaVar = "Color", required = false, usage = "The background color for the image")
	private String backgroundColorName;
	private Color backgroundColor;
	
	public Params(String[] args) {
		final CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(80);
		try {
			parser.parseArgument(args);
			validate();
			init();
		} catch (Exception e) {
			System.out.println("Usage:");
			parser.printUsage(System.out);
			System.out.println();
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public File getInputFile() {
		return inputFile;
	}
	
	public File getOutputFolder() {
		return outputFolder;
	}
	
	public int getSeed() {
		return seed;
	}
	
	public String getBackgroundColorName() {
		return backgroundColorName;
	}
	
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	
	public void validate() {
		if (!inputFile.isFile())
			throw new IllegalArgumentException("--input is not a valid file");
		if (outputFolder.exists() && !outputFolder.isDirectory())
			throw new IllegalArgumentException("--output-folder is not a directory");
		if (!outputFolder.exists() && !outputFolder.mkdirs())
			throw new IllegalArgumentException("--output-folder cannot be created");
	}
	
	public void init() {
		if (seed == 0) {
			seed = (new Random()).nextInt();
		}
		if (backgroundColorName != null && !backgroundColorName.isEmpty()) {
			try {
				backgroundColor = Utils.decodeColor(backgroundColorName);
			} catch (Exception e) {
				throw new IllegalArgumentException("--background-color is not a valid color (" + backgroundColorName + ")");
			}
		}
	}
}