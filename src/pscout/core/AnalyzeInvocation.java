package pscout.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import pscout.analyzers.PermissionAnalyzer;
import pscout.db.IDataProvider;
import pscout.models.AnalyzeScope;
import pscout.models.Class;
import pscout.models.Config;
import pscout.models.Invocation;
import pscout.models.Method;
import pscout.models.PermissionResult;

public class AnalyzeInvocation {
	private static Logger LOGGER = Logger.getLogger(AnalyzeInvocation.class.getName());
	
	private final IDataProvider dataProvider;
	private final Config config;
	private final ExecutorService threadPool;

	private final String initialClass = "android/app/IActivityManager";
	private final String initialMethod = "checkPermission";
	private final String initialDesc = "(Ljava/lang/String;II)I";
	private final int initialPermissionIndex = 0;
	
	@Inject
	public AnalyzeInvocation(IDataProvider dataProvider, Config config){
		this.dataProvider = dataProvider;
		this.config = config;
		this.threadPool = Executors.newFixedThreadPool(config.threads);
	}
	
	public void analyze(){
		try{
			Queue<AnalyzeScope> queue = new ConcurrentLinkedQueue<AnalyzeScope>();
		
			// Setup initial scope
			Class cls = this.dataProvider.getClassByName(this.initialClass, config.androidVersion);
			Method method = this.dataProvider.getMethod(cls.className, this.initialMethod, this.initialDesc, config.androidVersion);
			List<Invocation> initalInvocations = this.findInvocations(cls, method);
			for(Invocation invocation : initalInvocations){
				queue.add(new AnalyzeScope(invocation, this.initialPermissionIndex));
			}
	
			while(!queue.isEmpty()){
				AnalyzeScope scope = queue.remove();
				AnalyzeTask task = new AnalyzeTask(scope, queue);
				task.run();
			}
			
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
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
						Class c = this.dataProvider.getClassByName(itf, method.version);
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
		private final Queue<AnalyzeScope> queue;
		
		public AnalyzeTask(AnalyzeScope scope, Queue<AnalyzeScope> queue){
			this.scope = scope;
			this.queue = queue;
		}

		@Override
		public void run() {
			try{
				// analyze permission string
				PermissionAnalyzer analyzer = new PermissionAnalyzer(dataProvider);
				PermissionResult result = analyzer.analyze(this.scope);
				
				if(result.status == PermissionResult.Status.Internal){
					
				}else if(result.status == PermissionResult.Status.External){
					
				}else{	// Status Unknown
				}
				
			}catch(Exception e){
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}
}
