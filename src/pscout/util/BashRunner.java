package pscout.util;

import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BashRunner implements Runnable {
	private final Logger LOGGER = Logger.getLogger(BashRunner.class.getName());
	private final List<String> bashCommands;
	private final boolean async;
	
	public BashRunner(List<String> bashCommands, boolean async){
		this.bashCommands = bashCommands;
		this.async = async;
	}
	
	public void run(){
		try {
			ProcessBuilder builder = new ProcessBuilder(this.bashCommands);
			builder.redirectOutput(Redirect.INHERIT);
			builder.redirectError(Redirect.INHERIT);
			Process process = builder.start();
			// wait for thread if not asynchronous
			if(!async) process.waitFor();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}
}
