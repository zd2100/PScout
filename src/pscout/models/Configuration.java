package pscout.models;

public class Configuration {
	// Configuration File Name
	public static String configFile = "config";
	
	// General Setting
	public String androidSourcePath;
	public String jarFilePath;
	public String classDumpPath;
	
	public int parallelJobs = 10;
	public String androidVersion;
	
	// Database Setting
	public String driverClass;
	public String connectionString;
	
	// Extract Jar Bash
	public String[] extractJarCommands;
	public String extractJarBash;
	
	
}
