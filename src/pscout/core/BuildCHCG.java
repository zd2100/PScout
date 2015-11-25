package pscout.core;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import pscout.analyzers.CHCGAnalyzer;
import pscout.db.IDataProvider;
import pscout.models.Config;

public class BuildCHCG {
	
	private final Logger LOGGER = Logger.getLogger(BuildCHCG.class.getName());
	private final Config config;
	private final IDataProvider provider;
	private final ExecutorService threadPool;
	
	@Inject
	public BuildCHCG(IDataProvider provider, Config config){
		this.config = config;
		this.provider = provider;
		this.threadPool = Executors.newFixedThreadPool(config.threads);
	}

	public void execute() {
		try{
			File directory = new File(this.config.classDumpPath);
			// class dump directory not exist
			if(!directory.exists() || !directory.isDirectory())
				throw new Exception("Class Dump directory not exist!");
			
			// analyze class
			this.analyzeRecursive(directory);
			
			// shut down thread pool
			this.threadPool.shutdown();
			this.threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	// Recursively search sub-directory and analyze class files
	private void analyzeRecursive(File directory){
		if(directory.exists() && directory.isDirectory()){
			// Loop through each file and directory
			for(File fileOrDirectory : directory.listFiles()){
				// Call recursively if current file is a directory
				if(fileOrDirectory.isDirectory()){
					analyzeRecursive(fileOrDirectory);
				}else{
					if(fileOrDirectory.getName().endsWith(".class")) {
						CHCGAnalyzer analyzer = new CHCGAnalyzer(this.provider, this.config, fileOrDirectory);
						this.threadPool.submit(analyzer);
				//		analyzer.run();
					}	
				}
				// End of if-else
			} 
			// End of for loop
		}
	}
}
