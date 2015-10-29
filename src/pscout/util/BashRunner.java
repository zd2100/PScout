package pscout.util;

import java.lang.ProcessBuilder.Redirect;
import java.util.List;

public class BashRunner implements Runnable {
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
			e.printStackTrace();
		}
	}
}
