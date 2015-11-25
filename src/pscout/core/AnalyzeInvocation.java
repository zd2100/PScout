package pscout.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.inject.Inject;

import pscout.analyzers.PermissionAnalyzer;
import pscout.db.IDataProvider;
import pscout.models.AnalyzeScope;
import pscout.models.Class;
import pscout.models.Config;
import pscout.models.Invocation;
import pscout.models.Method;
import pscout.models.PermissionResult;
import pscout.util.StringUtility;

public class AnalyzeInvocation {
	private static Logger LOGGER = Logger.getLogger(AnalyzeInvocation.class.getName());
	
	private final IDataProvider dataProvider;
	private final Config config;
	private final ExecutorService threadPool;

	private final String initialClass = "android/app/IActivityManager";
	private final String initialMethod = "checkPermission";
	private final String initialDesc = "(Ljava/lang/String;II)I";
	private final int initialPermissionIndex = 1;
	
	@Inject
	public AnalyzeInvocation(IDataProvider dataProvider, Config config){
		this.dataProvider = dataProvider;
		this.config = config;
		this.threadPool = Executors.newFixedThreadPool(config.threads);
		
		try {
			FileHandler fh = new FileHandler("C:\\log\\log%g.log", 1024 * 1024 * 10, 1000);
			fh.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fh);
			LOGGER.setUseParentHandlers(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void analyze(){
		try{
			BlockingQueue<AnalyzeScope> queue = new LinkedBlockingQueue<AnalyzeScope>();
			Set<String> hashSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		
			// Setup initial scope
			List<Invocation> initalInvocations = this.findInvocations(initialClass, initialMethod, initialDesc, config.androidVersion);
			for(Invocation invocation : initalInvocations){
				queue.add(new AnalyzeScope(invocation, this.initialPermissionIndex));
			}
	
			//while(!queue.isEmpty() || !this.threadPool.isTerminated()){
			while(!queue.isEmpty()){	
				AnalyzeScope scope = queue.take();
				//this.threadPool.submit(new AnalyzeTask(scope, queue, hashSet));
				AnalyzeTask task = new AnalyzeTask(scope, queue, hashSet);
				task.run();
			}
			
			// shut down thread pool
			this.threadPool.shutdown();
			this.threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			
			LOGGER.log(Level.INFO, "Analyze Finished");
			
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private List<Invocation> findInvocations(String className, String methodName, String methodDesc, String version){
		Class cls = this.dataProvider.getClass(className, version);
		Method method = this.dataProvider.getMethod(cls.className, methodName, methodDesc, version);
		return this.findInvocations(cls, method);
	}

	private List<Invocation> findInvocations(Class cls, Method method){
		List<Invocation> invocations = new ArrayList<Invocation>();
		List<Class> classes = new ArrayList<Class>();
		classes.add(cls); // for direct invocation
		classes.addAll(this.findSuperClassesWithMethod(cls, method)); // for invoking parent method
		classes.addAll(this.findInterfacesWithMethod(cls, method)); // for invoking interface method

		for(Class c: classes){
			List<Invocation> result = this.dataProvider.findInvocations(c.className, method.methodName, method.descriptor, method.version);
			invocations.addAll(result);
		}
		
		return invocations;
	}
	
	private List<Class> findSuperClassesWithMethod(Class cls, Method method){
		List<Class> list = new ArrayList<Class>();
		Queue<Class> queue = new LinkedList<Class>();
		queue.add(cls);

		while(!queue.isEmpty()){
			Class current = queue.remove();
			if(current.superClass != null){
				Method m = this.dataProvider.getMethod(current.superClass, method.methodName, method.descriptor, method.version);
				if(m != null){
					Class c = this.dataProvider.getClassById(m.classId);
					list.add(c);
					// continue to check super class
					queue.add(c);
				}
			}
		}
		
		return list;
	}
	
	private List<Class> findInterfacesWithMethod(Class cls, Method method){
		List<Class> list = new ArrayList<Class>();
		Queue<Class> queue = new LinkedList<Class>();
		queue.add(cls);
		
		while(!queue.isEmpty()){
			Class current = queue.remove();
			if(current.interfaces != null){
				String[] interfaces = current.interfaces.split(";");
				for(String itf: interfaces){
					itf = itf.trim(); // trim possible white space
					Method m = this.dataProvider.getMethod(itf, method.methodName, method.descriptor, method.version);
					if(m != null){
						Class c = this.dataProvider.getClass(itf, method.version);
						list.add(c);
						// continue to check interface
						queue.add(c);
					}
				}
				// END of for loop
			}
		}
		// End of while loop
		
		return list;
	}

	private class AnalyzeTask implements Runnable{
		private final AnalyzeScope scope;
		private final BlockingQueue<AnalyzeScope> queue;
		private final Set<String> hashSet;
		
		public AnalyzeTask(AnalyzeScope scope, BlockingQueue<AnalyzeScope> queue, Set<String> set){
			this.scope = scope;
			this.queue = queue;
			this.hashSet = set;
		}

		@Override
		public void run() {
			try{
				// skip analyzed invocation by other threads
				if(this.hashSet.contains(this.scope.invocation.callerClass + this.scope.invocation.callerMethod + this.scope.invocation.callerMethodDesc)){
					return;
				}
				
				LOGGER.log(Level.INFO, "Analyzing: " + this.scope.invocation);
				
				// mark this invocation as analyzed
				this.hashSet.add(this.scope.invocation.callerClass + this.scope.invocation.callerMethod + this.scope.invocation.callerMethodDesc);
				
				PermissionResult result = null;
				
				// Analyze permission string if necessary
				if(!this.scope.IsPermissionAlreadyDetermined()){
					LOGGER.log(Level.INFO, "Scanning Permssion");
					PermissionAnalyzer analyzer = new PermissionAnalyzer(config);
					result = analyzer.analyze(this.scope);
					LOGGER.log(Level.INFO, "Permission Done: " + result.status + "\t" + StringUtility.join(result.permissions, ","));
				}
				
				// Find parent invocations
				List<Invocation> invocations = findInvocations(this.scope.invocation.callerClass, 
															   this.scope.invocation.callerMethod, 
															   this.scope.invocation.callerMethodDesc, 
															   this.scope.invocation.version);
				
				// filter out invocation that is originate from java library
				// or invocation which have already been analyzed
				invocations = filterInvocation(invocations);
				
				// based on permission status, add parent invocation to the processing queue with corresponding scope
				if(this.scope.IsPermissionAlreadyDetermined()){
					// if permission is already determined by previous method
					// do need to perform permission analysis, simple report to database
					for(String permission: this.scope.permissions){
						dataProvider.addPermissionInvocation(this.scope.invocation, permission);
					}
					for(Invocation invocation : invocations){
						// permission string already found, no need to do permission analysis for caller method
						this.queue.add(new AnalyzeScope(invocation, AnalyzeScope.NoIndex, this.scope.permissions));
					}
				}else if(result.status == PermissionResult.Status.Found){
					// permission string is found within the method body
					for(String permission: result.permissions){
						dataProvider.addPermissionInvocation(this.scope.invocation, permission);
					}
					for(Invocation invocation : invocations){
						// permission string already found, no need to do permission analysis for caller method
						this.queue.add(new AnalyzeScope(invocation, AnalyzeScope.NoIndex, result.permissions));
					}
				}else if(result.status == PermissionResult.Status.FromParent){
					// permission is passed by caller class via parameter
					dataProvider.addPermissionInvocation(this.scope.invocation, "Parent");
					for(Invocation invocation : invocations){
						// perform permission analysis for caller method on index
						this.queue.add(new AnalyzeScope(invocation, result.paramIndex));
					}
				}else{
					// Status Unknown
					dataProvider.addPermissionInvocation(this.scope.invocation, "Unknown");
					for(Invocation invocation : invocations){
						// permission string cannot be determined, no analysis for parent method
						this.queue.add(new AnalyzeScope(invocation, AnalyzeScope.NoIndex));
					}
				}
				
				LOGGER.log(Level.INFO, "Analyze Done");

			}catch(Exception e){
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	
		// filter out invocations that is originated from java native library
		private List<Invocation> filterInvocation(List<Invocation> inputs){
			List<Invocation> result = new ArrayList<Invocation>();
			for(Invocation invocation : inputs){
				//  skip native java library classes
				if(invocation.callerClass.startsWith("java") 
				|| invocation.targetClass.startsWith("java")){
					continue;
				}

				result.add(invocation);
			}
			
			return result;
		}
	}
}
