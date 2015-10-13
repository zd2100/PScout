package pscout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pscout.analyzer.AsmClassAnalyzer;
import pscout.analyzer.TestAnalyzer;
import pscout.db.DbProvider;
import pscout.models.Class;
import pscout.models.Configuration;
import pscout.models.Method;
import pscout.models.MethodInvocation;
import pscout.util.BashRunner;

public class PScout {	

	private final Factory factory;
	private final Configuration config;
	private final SimpleDateFormat timeFormat;
	private final DbProvider dbProvider;
	
	public PScout(){
		this.factory = Factory.instance();
		this.config = this.factory.getConfiguration();
		this.timeFormat = new SimpleDateFormat("HH:mm:ss");
		this.dbProvider = this.factory.getDbProvider();
	}
	
	public void shutdown(){
		this.factory.shutdown();
	}
	
	
	public void extractJars(){
		if(this.config != null){
			System.out.println("Extracting *.jar files, please wait...");
			
			List<String> commands = new ArrayList<String>();
			commands.addAll(Arrays.asList(this.config.extractJarCommands));
			commands.add(String.format("%s %s %s", this.config.extractJarBash, this.config.jarFilePath, this.config.classDumpPath));
			BashRunner  bash = new BashRunner(commands);
			bash.run();
			System.out.println("Extraction Completed!");
			System.out.println();
		}
	}
	
	
	/*------------------- Scan Classes for hierarchy ----------------------*/
	public void analyzeClasses() throws Exception{
		if(config != null){
			
			System.out.println("========= Starting Level 1 Analysis... " + " ==========");
			long time = System.currentTimeMillis();
			
			// clear out existing classes and methods for current android version
			this.factory.getDbProvider().deleteVersion(this.config.androidVersion);
			
			File classDirectory = new File(this.config.classDumpPath);
			// class dump directory not exist
			if(!classDirectory.exists() || !classDirectory.isDirectory()){
				throw new Exception("Class Dump directory not exist!");
			}
			
			// Parallel execution
			ExecutorService threadPool = Executors.newFixedThreadPool(this.config.parallelJobs);
			
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask(){
				@Override
				public void run() {
					System.out.println("Class: " + Statistics.classCount.get() + "\t Methods: " + 
							Statistics.methodCount.get() + "\t Invocations: " + Statistics.methodInvocationCount.get());
				}
				
			}, 1000,1000);
			
			// start scanning classes
			this.analyzeClassesRecursive(classDirectory, threadPool);
			
			System.out.println("Task submitted, prepare to shutdown");
			// shutdown and wait for all thread to complete in 1 minute
			threadPool.shutdown();
			threadPool.awaitTermination(10l,TimeUnit.MINUTES);
			
			
			System.out.println("Analyzed " + Statistics.classCount.get() + " classes, " + Statistics.methodCount.get() + 
					" methods and " + Statistics.methodInvocationCount.get() + " invocations");
			
			System.out.println("========== Level 1 Analysis Completed! ============");
			System.out.println("Analyze Time: " + ((System.currentTimeMillis() - time) / 1000) + " seconds");
			
			timer.cancel();
		}
	}
	
	private void analyzeClassesRecursive(File directory, ExecutorService threadPool){
		if(directory.exists() && directory.isDirectory()){
			// Loop through each file and directory
			for(File fileOrDirectory : directory.listFiles()){
				// Call recursively if current file is a directory
				if(fileOrDirectory.isDirectory()){
					analyzeClassesRecursive(fileOrDirectory, threadPool);
				}else{
					if(fileOrDirectory.getName().endsWith(".class")){
						threadPool.submit(new AsmClassAnalyzer(fileOrDirectory));
					}
				}
			} 
			// End of for loop
		}
	}
	
	
	
	
	
	
	private static void showUsage(){
		System.out.println("Usage");
	}

	public static void main(String[] args) {
		if(args == null || args.length == 0){
			showUsage();
		}
		
		PScout pscout = new PScout();
		try{
		//	pscout.extractJars();
		//	pscout.analyzeClasses();
			/*
			long time = System.currentTimeMillis();
			int level = pscout.dbProvider.findAllInvocationsRecursive("android/content/Context", "checkPermission", "(Ljava/lang/String;II)I", "4.4.1");
			
			for(MethodInvocation method : methods){
				System.out.println(method.callingClass + "\t" + method.callingMethod + "\t" + method.callingMethodDescriptor + "\t" + method.invokeType);
			}
			
			System.out.println("Total: " + level + " levels");	
			System.out.println("Time: " + (System.currentTimeMillis() - time) + " millis");
			*/
			
			
			TestAnalyzer analyzer = new TestAnalyzer();
			analyzer.run();
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			pscout.shutdown();
		}
	}
}
