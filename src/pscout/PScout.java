package pscout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;

import pscout.core.AnalyzeInvocation;
import pscout.core.BuildCHCG;
import pscout.core.ExtractJarFile;
import pscout.db.IDataProvider;

import pscout.util.Statistics;

/**
 * The Main entry point for PScout. This class provide main routine calls
 * @author Ding Zhu
 */
public class PScout {
	private static Logger LOGGER = Logger.getLogger(PScout.class.getName());
	
	private final Injector injector;
	
	public PScout(){
		this.injector = Guice.createInjector(new PScoutModule());
	}
	
	/**
	 * Step 1: Extract jar files to class files
	 */
	public void extractJars(){
		ExtractJarFile extractor = this.injector.getInstance(ExtractJarFile.class);
		extractor.execute();
	}
	
	/**
	 * Step 2: Build ClassHierarchy and Call Graph
	 * A timer is setup to repeatedly report number of class/method/invocation being processed
	 * Current report rate is 5 seconds
	 */
	public void buildClassHierarchyCallGraph(){
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				LOGGER.log(Level.INFO, Statistics.getTime() + "\tClass: " + Statistics.classCount.get() + 
						"\tMethods: " + Statistics.methodCount.get() + "\tInvocations: " + Statistics.invocationCount.get() );
			}
		}, 0, 5000);
		BuildCHCG chcg = this.injector.getInstance(BuildCHCG.class);
		chcg.execute();
		timer.cancel();
		LOGGER.log(Level.INFO, "Done !");
		LOGGER.log(Level.INFO, Statistics.getTime() + "\tClass: " + Statistics.classCount.get() + 
				"\tMethods: " + Statistics.methodCount.get() + "\tInvocations: " + Statistics.invocationCount.get() );
	}
	
	/**
	 * Step 3: Analyze Permission Invocation. 
	 * This is the main method to extract permission strings and permission invocations
	 */
	public void analyzeInvocations(){
		AnalyzeInvocation analyzer = this.injector.getInstance(AnalyzeInvocation.class);
		analyzer.analyze();
	}

	/**
	 * Final Step: Cleanly shutdown all services and release all resources.
	 */
	public void shutdown(){
		IDataProvider dataProvider = this.injector.getInstance(IDataProvider.class);
		dataProvider.shutdown();
	}

	/**
	 * Main entry point
	 * @param args option parameters 
	 */
	public static void main(String[] args) {
		
		if(args.length == 1){
			int step = Integer.parseInt(args[0]);
			
			PScout pscout = new PScout();
			
			switch(step){
				case 1:
					LOGGER.log(Level.INFO, Statistics.getTime() + "\tExtracting Jars...");
					pscout.extractJars();
					LOGGER.log(Level.INFO, Statistics.getTime() + "\tDone!");
					break;
				case 2:
					LOGGER.log(Level.INFO, Statistics.getTime() + "\tCHCG Start...");
					pscout.buildClassHierarchyCallGraph();
					LOGGER.log(Level.INFO, Statistics.getTime() + "\tDone!");
					break;
				case 3:
					LOGGER.log(Level.INFO, Statistics.getTime() + "\tAnalyze Permission Start...");
					pscout.analyzeInvocations();
					LOGGER.log(Level.INFO, Statistics.getTime() + "\tDone!");
					break;
					default:
						break;
			}
			
			pscout.shutdown();
		}
	}
}
