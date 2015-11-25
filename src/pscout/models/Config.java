package pscout.models;

public class Config {
	public static final String configFile = "config";
	
	/* Common */
	public int threads;
	
	
	/* Database */
	public String driverClass;
	public String connectionString;
	
	/* Android */
	public String androidVersion;
	public String androidSourcePath;
	public String jarFilePath;
	public String classDumpPath;
	
	/* Extract */
	public String[] extractJarCommands;
	public String extractJarBash;
	
	public String getClassDumpPath(){
		return this.classDumpPath + (this.classDumpPath.endsWith("\\") ? "": "\\");
	}
}
