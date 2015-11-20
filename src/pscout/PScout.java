package pscout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;

import pscout.core.BuildCHCG;
import pscout.core.ExtractJarFile;
import pscout.db.IDataProvider;

import pscout.models.Class;
import pscout.models.Method;
import pscout.util.Statistics;

public class PScout {
	private static Logger LOGGER = Logger.getLogger(PScout.class.getName());
	
	private final Injector injector;
	
	public PScout(){
		this.injector = Guice.createInjector(new PScoutModule());
	}
	
	public void extractJars(){
		ExtractJarFile extractor = this.injector.getInstance(ExtractJarFile.class);
		extractor.execute();
	}
	
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

	public void shutdown(){
		IDataProvider dataProvider = this.injector.getInstance(IDataProvider.class);
		dataProvider.shutdown();
	}

	public static void main(String[] args) {
		PScout pscout = new PScout();
		/*
		LOGGER.log(Level.INFO, Statistics.getTime() + "\tExtracting Jars...");
		pscout.extractJars();
		LOGGER.log(Level.INFO, Statistics.getTime() + "\tDone!");

		LOGGER.log(Level.INFO, Statistics.getTime() + "\tCHCG Start...");
		pscout.buildClassHierarchyCallGraph();
		LOGGER.log(Level.INFO, Statistics.getTime() + "\tDone!");
		*/

		pscout.shutdown();
	}
}
