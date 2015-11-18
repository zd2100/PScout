package pscout.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import pscout.models.Config;
import pscout.util.BashRunner;

public class ExtractJarFile {
	private final Logger LOGGER = Logger.getLogger(ExtractJarFile.class.getName());
	
	private final Config config;

	@Inject
	public ExtractJarFile(Config config){
		this.config = config;
	}
	
	public void execute(){
		try{
			// Execute bash file with format: ./bash jars output
			List<String> commands = new ArrayList<String>();
			commands.addAll(Arrays.asList(config.extractJarCommands));
			commands.add(String.format("%s %s %s", config.extractJarBash, config.jarFilePath, config.classDumpPath));
			BashRunner  bash = new BashRunner(commands, false);
			bash.run();
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
