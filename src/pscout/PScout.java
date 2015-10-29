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

import pscout.asm.AsmClassAnalyzer;
import pscout.asm.AsmPermissionAnalyzer;
import pscout.db.DbProvider;
import pscout.models.Class;
import pscout.models.Configuration;
import pscout.models.Method;
import pscout.models.MethodInvocation;
import pscout.models.SearchScope;
import pscout.models.SearchTarget;
import pscout.util.BashRunner;

public class PScout {	

	private final Factory factory;
	public PScout(){
		this.factory = Factory.instance();
	}
	
	public void shutdown(){
		// close all connections
		this.factory.shutdown();
	}
	
	/* ---------------- Step 1: Extract Jar Files --------------------- */
	public void extractJars() throws Exception {
		Configuration config = this.factory.getConfiguration();
		if(config != null){
			System.out.println(Statistics.getTime() + " ========= Extracting *.jar files =========");
			
			// Execute bash file with format: ./bash jars output
			List<String> commands = new ArrayList<String>();
			commands.addAll(Arrays.asList(config.extractJarCommands));
			commands.add(String.format("%s %s %s",config.extractJarBash, config.jarFilePath, config.classDumpPath));
			BashRunner  bash = new BashRunner(commands, false);
			bash.run();
			
			System.out.println(Statistics.getTime() + " ========= Extraction Completed! =========");
			System.out.println();
		}
	}
	
	
	/* ---------------- Step 2: Parse Class Hierarchy and Call Graph ---------------- */
	public void analyzeClasses() throws Exception {
		Configuration config = this.factory.getConfiguration();
		if(config != null){
			System.out.println(Statistics.getTime() + " ========= Parsing Class Hierarchy and Call Graph =========");
			
			// clear out existing classes and methods for current android version
			this.factory.getDbProvider().deleteVersion(config.androidVersion);
			File classDirectory = new File(config.classDumpPath);
			
			// class dump directory not exist
			if(!classDirectory.exists() || !classDirectory.isDirectory())
				throw new Exception("Class Dump directory not exist!");
			
			// Parallel execution
			ExecutorService threadPool = Executors.newFixedThreadPool(config.parallelJobs);
			this.analyzeClassesRecursive(classDirectory, threadPool);

			// shutdown and wait for all thread to complete in 5 minutes
			threadPool.shutdown();
			threadPool.awaitTermination(5l,TimeUnit.MINUTES);

			System.out.println(Statistics.getTime() + " ========= Class Hierarchy and Call Graph Ready! =========");
			System.out.println();
		}
	}
	
	// Recursively search sub-directory and analyze class files
	private void analyzeClassesRecursive(File directory, ExecutorService threadPool){
		if(directory.exists() && directory.isDirectory()){
			// Loop through each file and directory
			for(File fileOrDirectory : directory.listFiles()){
				// Call recursively if current file is a directory
				if(fileOrDirectory.isDirectory()){
					analyzeClassesRecursive(fileOrDirectory, threadPool);
				}else{
					if(fileOrDirectory.getName().endsWith(".class")) 
						threadPool.submit(new AsmClassAnalyzer(fileOrDirectory));
				}
			} 
			// End of for loop
		}
	}
	
	
	/* ---------------- Step 3: Search for permission invocations ---------------- */
	private void deepScan() throws Exception {
		DbProvider provider = this.factory.getDbProvider();
		
		SearchTarget target = new SearchTarget("android/content/Context", "checkCallingOrSelfPermission", "(Ljava/lang/String;)I", 1);
		List<MethodInvocation> invocationList = provider.findAllInvocations(target.clsName, target.methodName, target.methodDesc, "4.4.1");
		
		for(MethodInvocation invocation : invocationList){
			System.out.println("--------------- " + invocation.callingClass + " " + invocation.callingMethod + " ------------------");
			SearchScope scope = new SearchScope(invocation.callingClass, invocation.callingMethod, invocation.callingMethodDescriptor);
			AsmPermissionAnalyzer analyzer = new AsmPermissionAnalyzer(scope, target);
			analyzer.run();
			System.out.println("---------------------------------------------------------------------------------------------------");
			System.out.println();
		}
	}
	
	
	private static void showUsage(){
		System.out.println("Usage");
	}

	public static void main(String[] args) {
		// display usage information
		if(args == null || args.length == 0){
			showUsage();
		}
		
		PScout pscout = new PScout();
		try{
		//	pscout.extractJars();
		//	pscout.analyzeClasses();
			pscout.deepScan();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			// safe shutdown
			pscout.shutdown();
		}
	}
}
