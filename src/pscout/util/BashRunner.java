package pscout.util;

import java.lang.ProcessBuilder.Redirect;
import java.util.List;

public class BashRunner implements Runnable {
	private final List<String> bashCommands;
	
	public BashRunner(List<String> bashCommands){
		this.bashCommands = bashCommands;
	}
	
	public void run(){
		try {
			ProcessBuilder builder = new ProcessBuilder(this.bashCommands);
			builder.redirectOutput(Redirect.INHERIT);
			builder.redirectError(Redirect.INHERIT);
			Process process = builder.start();
			process.waitFor();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
