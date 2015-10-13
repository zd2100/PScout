package pscout.db;

import java.sql.SQLException;
import java.util.List;

import pscout.models.Class;
import pscout.models.Method;
import pscout.models.MethodInvocation;

public interface DbProvider {
	public void addClass(Class cls) throws Exception;
	public Class getClass(String clsName, String version) throws Exception;
	public List<Class> findDirectSubClasses(String clsName, String version, boolean includeSelf) throws Exception;
	public List<Class> findAllSubClasses(String clsName, String version, boolean includeSelf) throws Exception;
	public List<Class> findDirectImplementations(String clsName, String version, boolean includeSelf) throws Exception;
	public List<Class> findAllImplementations(String interfaceName, String version, boolean includeSelf) throws Exception;
	
	public void addMethod(Method method) throws Exception;
	public Method getMethod(String className, String methodName, String descriptor, String version) throws Exception;
	
	public void addMethodInvoke(MethodInvocation methodInvocation) throws Exception;
	public List<MethodInvocation> findDirectInvocations(String className, String methodName, String descriptor, String version) throws Exception;
	public List<MethodInvocation> findIndirectInvocations(String className, String methodName, String descriptor, String version) throws SQLException;
	public List<MethodInvocation> findAllInvocations(String className, String methodName, String descriptor, String version) throws SQLException;
	public int findAllInvocationsRecursive(String className, String methodName, String descriptor, String version) throws SQLException;
	
	public void shutdown();
	public void deleteVersion(String version) throws Exception;
}
