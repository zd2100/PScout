package pscout.analyzers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import com.google.inject.Inject;

import pscout.db.IDataProvider;
import pscout.models.Class;
import pscout.models.Invocation;
import pscout.models.Method;

public class BackTracer {
	private static final Logger LOGGER = Logger.getLogger(BackTracer.class.getName());
	
	private final IDataProvider dataProvider;
	
	@Inject
	public BackTracer(IDataProvider dataProvider){
		this.dataProvider = dataProvider;
	}
	
	public Object analyze(Class cls, Method method, int paramIndex){
		this.findInvocations(cls, method);
		return null;
	}
	
	private List<Invocation> findInvocations(Class cls, Method method){
		List<Class> list = new ArrayList<Class>();
		list.add(cls);
		list.addAll(this.findSuperClassesWithMethod(cls, method));
		list.addAll(this.findInterfacesWithMethod(cls, method));
		
		// TODO: make unique
		
		for(Class c: list){
			System.out.println(c);
		}
		
		return null;
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
}
