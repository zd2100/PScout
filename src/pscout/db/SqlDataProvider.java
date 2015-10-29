package pscout.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import pscout.models.Class;
import pscout.models.Method;
import pscout.models.MethodInvocation;

public class SqlDataProvider implements DbProvider {
	private final String connectionString;
	private final ComboPooledDataSource dataSource;
	
	long timecounter;
	
	public SqlDataProvider(String driverClass, String connectionString) throws Exception{
		this(driverClass, connectionString, 10);
	}
	
	public SqlDataProvider(String driverClass, String connectionString, int parallelJobs) throws Exception{
		this.connectionString = connectionString;
		this.dataSource = new ComboPooledDataSource();
		this.dataSource.setDriverClass(driverClass);
		this.dataSource.setJdbcUrl(this.connectionString);
		this.dataSource.setMaxPoolSize(2 * parallelJobs);
		this.dataSource.setMaxStatements(20 * parallelJobs);
	}
	
	// This method is used to clean up resources used by connection pool
	// Calling this method means to terminate this sql provider
	@Override
	public synchronized void shutdown(){
		if(this.dataSource != null){
			this.dataSource.close();
		}
	}
	
	@Override
	public synchronized void deleteVersion(String version) throws SQLException{
		final String[] deleteSql = new String[] {
				"DELETE FROM Classes WHERE Version = ?", 
				"DELETE FROM Methods WHERE Version = ?",
				"DELETE FROM MethodInvocations WHERE Version = ?"
			};
		
		Connection conn = null;
		PreparedStatement statement = null;
		
		try{
			conn =  this.getConnection();
			
			for(String sql : deleteSql){
				statement = conn.prepareStatement(sql);
				statement.setString(1, version);
				statement.executeUpdate();
				statement.close();
			}
			
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}
	}
	
	@Override
	public void addClass(Class cls) throws SQLException {
		final String insertSql = "INSERT INTO Classes (ClassName,Version,Access,Signature, SuperClass,Interfaces,IsAbstract,IsInterface,IsEnum) VALUES (?,?,?,?,?,?,?,?,?)";
		
		Connection conn = null;
		PreparedStatement statement = null;
		
		try{
			conn =  this.getConnection();

			// Insert new record
			statement = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, cls.className);
			statement.setString(2, cls.version);
			statement.setString(3, cls.access);
			statement.setString(4, cls.signature);
			statement.setString(5, cls.superClass);
			statement.setString(6, cls.getInterfaceString());
			statement.setBoolean(7, cls.isAbstract);
			statement.setBoolean(8, cls.isInterface);
			statement.setBoolean(9, cls.isEnum);
			
			statement.executeUpdate();
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}
	}

	@Override
	public Class getClass(String clsName, String version) throws SQLException {
		final String getSql = "SELECT ID,ClassName,Version,Access,SuperClass,Signature,Interfaces,IsAbstract,IsInterface,IsEnum FROM Classes WHERE ClassName = ? AND Version = ?";
		
		Connection conn = null;
		PreparedStatement statement= null;
		
		try{
			conn = this.getConnection();
			statement = conn.prepareStatement(getSql);
			statement.setString(1, clsName);
			statement.setString(2, version);

			ResultSet result = statement.executeQuery();

			if(result.next()){
				Class cls = this.extractClass(result);
				return cls;
			}
			
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}
		
		return null;
	}

	@Override
	public List<Class> findDirectSubClasses(String clsName, String version, boolean includeSelf) throws SQLException{
		final String sql = "SELECT ID,ClassName,Version,Access,SuperClass,Signature,Interfaces,IsAbstract,IsInterface,IsEnum FROM Classes WHERE SuperClass = ? AND Version = ?";

		Connection conn = null;
		PreparedStatement statement= null;
		List<Class> list = new ArrayList<Class>();
		
		if(includeSelf)	list.add(this.getClass(clsName, version));
		
		try{
			conn = this.getConnection();

			statement = conn.prepareStatement(sql);
			statement.setString(1, clsName);
			statement.setString(2, version);
			
			ResultSet result = statement.executeQuery();
			while(result.next()){
				Class cls = this.extractClass(result);
				list.add(cls);
			}
			
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}
		
		return list;
	}
	
	@Override
	public List<Class> findAllSubClasses(String clsName, String version, boolean includeSelf) throws SQLException{
		ArrayList<Class> list = new ArrayList<Class>();
		Deque<Class> queue = new ArrayDeque<Class>();
		
		Class origin = this.getClass(clsName, version);
		if(includeSelf) list.add(origin);
		queue.add(origin);
		
		while(!queue.isEmpty()){
			Class current = queue.removeFirst();
			List<Class> subClasses = this.findDirectSubClasses(current.className, current.version, false);
			queue.addAll(subClasses);
			list.addAll(subClasses);
		}
		
		return list;
	}
	
	// result includes both interfaces and classes
	@Override
	public List<Class> findDirectImplementations(String interfaceName, String version, boolean includeSelf) throws SQLException{
		final String sql = "SELECT ID,ClassName,Version,Access,SuperClass,Signature,Interfaces,IsAbstract,IsInterface,IsEnum FROM Classes WHERE Interfaces like ? AND Version = ?";
		
		Connection conn = null;
		PreparedStatement statement= null;
		ArrayList<Class> list = new ArrayList<Class>();
		
		if(includeSelf)	list.add(this.getClass(interfaceName, version));
		try{
			conn = this.getConnection();

			statement = conn.prepareStatement(sql);
			statement.setString(1, "%" + interfaceName + "%");
			statement.setString(2, version);

			ResultSet result = statement.executeQuery();

			while(result.next()){
				Class cls = this.extractClass(result);
				list.add(cls);
			}
			
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}
		
		return list;
	}
	
	// result includes both interfaces and classes
	@Override
	public List<Class> findAllImplementations(String interfaceName, String version, boolean includeSelf) throws SQLException{
		ArrayList<Class> list = new ArrayList<Class>();
		Deque<Class> queue = new ArrayDeque<Class>();
		
		// Add original interface to the queue to start
		Class origin = this.getClass(interfaceName, version);
		if(includeSelf) list.add(origin);
		queue.add(origin);
		
		while(!queue.isEmpty()){ 
			Class current = queue.removeFirst();
			
			
			List<Class> impls = current.isInterface 
					? this.findDirectImplementations(current.className, current.version, false)
					: this.findDirectSubClasses(current.className, current.version, false);	
			
			queue.addAll(impls);
			list.addAll(impls);
		}
		
		System.out.println("Total SQL Time: " + this.timecounter + "millis");
		
		return list;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void addMethod(Method method) throws SQLException {
		final String insertSql = "INSERT INTO Methods (ClassName,MethodName,Version,Access,Signature,Descriptor,Exceptions,IsAbstract,IsNative) VALUES (?,?,?,?,?,?,?,?,?)";
		
		Connection conn = null;
		PreparedStatement statement = null;
		
		try{
			conn =  this.getConnection();

			// Insert new record
			statement = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, method.className);
			statement.setString(2, method.methodName);
			statement.setString(3, method.version);
			statement.setString(4, method.access);
			statement.setString(5, method.signature);
			statement.setString(6, method.descriptor);
			statement.setString(7, method.getExceptionString());
			statement.setBoolean(8, method.isAbstract);
			statement.setBoolean(9, method.isNative);
			
			statement.executeUpdate();
			
		}catch(SQLException e){
			System.err.println(method.className + "." + method.methodName);
		}
		finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}
	}

	@Override
	public Method getMethod(String className, String methodName, String descriptor, String version) throws SQLException {
		final String getSql = "SELECT ID,ClassName,MethodName,Version,Access,Signature,Descriptor,Exceptions,IsAbstract,IsNative FROM Methods WHERE ClassName = ? AND MethodName = ? AND Descriptor = ? AND Version = ?";
		
		Connection conn = null;
		PreparedStatement statement= null;
		
		try{
			conn = this.getConnection();
			statement = conn.prepareStatement(getSql);
			statement.setString(1, className);
			statement.setString(2, methodName);
			statement.setString(3, descriptor);
			statement.setString(4, version);

			ResultSet result = statement.executeQuery();

			if(result.next()){
				Method method = this.extractMethod(result);
				return method;
			}
			
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}
		
		return null;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void addMethodInvoke(MethodInvocation methodInvocation) throws SQLException {
		final String insertSql = "INSERT INTO MethodInvocations (InvokeType,CallingClass,CallingMethod,CallingMethodDescriptor,TargetClass,TargetMethod,TargetMethodDescriptor,Version) VALUES (?,?,?,?,?,?,?,?)";
		
		Connection conn = null;
		PreparedStatement statement = null;
		
		try{
			conn =  this.getConnection();

			// Insert new record
			statement = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, methodInvocation.invokeType);
			statement.setString(2, methodInvocation.callingClass);
			statement.setString(3, methodInvocation.callingMethod);
			statement.setString(4, methodInvocation.callingMethodDescriptor);
			statement.setString(5, methodInvocation.targetClass);
			statement.setString(6, methodInvocation.targetMethod);
			statement.setString(7, methodInvocation.targetMethodDescriptor);
			statement.setString(8, methodInvocation.version);
			
			statement.executeUpdate();
			
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}

	}

	@Override
	public List<MethodInvocation> findDirectInvocations(String className, String methodName, String descriptor, String version) throws SQLException {
		final String sql = "SELECT ID,InvokeType,CallingClass,CallingMethod,CallingMethodDescriptor,TargetClass,TargetMethod,TargetMethodDescriptor,Version FROM MethodInvocations WHERE TargetClass = ? AND TargetMethod = ? AND TargetMethodDescriptor = ? AND Version = ?";
		
		ArrayList<MethodInvocation> list = new ArrayList<MethodInvocation>();
		Connection conn = null;
		PreparedStatement statement= null;
		
		try{
			conn = this.getConnection();

			statement = conn.prepareStatement(sql);
			statement.setString(1, className);
			statement.setString(2, methodName);
			statement.setString(3, descriptor);
			statement.setString(4, version);
			
			ResultSet result = statement.executeQuery();
			while(result.next()){
				MethodInvocation invocation = this.extractMethodInvocation(result);
				list.add(invocation);
			}
			
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}
		
		return list;
	}

	// indirect invocation is invocation of interface/super class/abstract class method
	public List<MethodInvocation> findIndirectInvocations(String className, String methodName, String descriptor, String version) throws SQLException {
		 Class cls = this.getClass(className, version);
		 Class superClass = this.getClass(cls.superClass, version);
		 ArrayList<String> potentials = new ArrayList<String>();
		 ArrayList<MethodInvocation> invocations = new ArrayList<MethodInvocation>();
		 
		 // if the method is from super class
		 //TODO: need to check if superclass is native java class. e.g. java/lang/Object
		 if(!superClass.className.equals("java/lang/Object")){
			 if(this.getMethod(superClass.className, methodName, descriptor, version) != null){
				 potentials.add(superClass.className);
			 }
		 }
		 // if the method is from interface
		 //TODO: need to check if the interface should be used for further checking
		 for(String interfaceName : cls.interfaces){
			 if(this.getMethod(interfaceName, methodName, descriptor, version) != null){
				 potentials.add(interfaceName);
			 }
		 }
		 
		 for(String targetClass : potentials){
			 invocations.addAll(this.findDirectInvocations(targetClass, methodName, descriptor, version));
		 }
		 
		 return invocations;
	}
	
	public List<MethodInvocation> findAllInvocations(String className, String methodName, String descriptor, String version) throws SQLException {
		ArrayList<MethodInvocation> invocations = new ArrayList<MethodInvocation>();
		invocations.addAll(this.findDirectInvocations(className, methodName, descriptor, version));
		invocations.addAll(this.findIndirectInvocations(className, methodName, descriptor, version));
		return invocations;
	}
	
	public int findAllInvocationsRecursive(String className, String methodName, String descriptor, String version) throws SQLException {
		// ArrayList<MethodInvocation> allInvocations = new ArrayList<MethodInvocation>();
		Deque<Method> queue = new ArrayDeque<Method>();
		
		Method origin = new Method();
		origin.className = className;
		origin.methodName = methodName;
		origin.descriptor = descriptor;
		origin.version = version;
		queue.add(origin);
		
		int level = 0;
		
		while(!queue.isEmpty()){
			Method method = queue.removeFirst();
			
			List<MethodInvocation> invocations = this.findAllInvocations(method.className, method.methodName, method.descriptor, method.version);
			// allInvocations.addAll(invocations);
			for(MethodInvocation invocation : invocations){
				if(!this.hasInvocation(invocation.id)){
				
					addToTemp(invocation.id);
					
					Method current  = new Method();
					current.className = invocation.callingClass;
					current.methodName = invocation.callingMethod;
					current.descriptor = invocation.callingMethodDescriptor;
					current.version = invocation.version;
					
					
					if(level < 30){
						queue.add(current);
					}
				}
			}
			level++;
		}
		
		return level;
		
	}
	
	private boolean hasInvocation(int id) throws SQLException{
		final String getSql = "SELECT ID FROM invocationtemp WHERE ID = ?";
		
		Connection conn = null;
		PreparedStatement statement= null;
		
		try{
			conn = this.getConnection();
			statement = conn.prepareStatement(getSql);
			statement.setInt(1, id);

			ResultSet result = statement.executeQuery();

			if(result.next()){
				int rid = result.getInt("ID");
				return rid != 0;
			}
			
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}
		
		return false;
	}
	
	private void addToTemp(int id) throws SQLException{
		String sql = "INSERT INTO InvocationTemp (ID) VALUES (?)";
		Connection conn = null;
		PreparedStatement statement = null;
		
		try{
			conn =  this.getConnection();

			// Insert new record
			statement = conn.prepareStatement(sql);
			statement.setInt(1, id);
			statement.executeUpdate();
			
		}finally{
			if(statement != null) statement.close();
			if(conn != null) conn.close();
		}

	}
	
	
	private Connection getConnection() throws SQLException{
		return this.dataSource.getConnection();
	}
	
	private Class extractClass(ResultSet result) throws SQLException{
		Class cls = new Class();
		cls.id = result.getInt("ID");
		cls.className = result.getString("ClassName");
		cls.version = result.getString("Version");
		cls.access = result.getString("Access");
		cls.superClass = result.getString("SuperClass");
		cls.signature = result.getString("Signature");
		cls.setInterfaceString(result.getString("Interfaces"));
		cls.isAbstract = result.getBoolean("IsAbstract");
		cls.isInterface = result.getBoolean("IsInterface");
		cls.isEnum = result.getBoolean("IsEnum");
		return cls;
	}
	
	private Method extractMethod(ResultSet result) throws SQLException{
		Method method = new Method();
		method.id = result.getInt("ID");
		method.className = result.getString("ClassName");
		method.methodName = result.getString("MethodName");
		method.version = result.getString("Version");
		method.access = result.getString("Access");
		method.signature = result.getString("Signature");
		method.descriptor = result.getString("Descriptor");
		method.setExceptionList(result.getString("Exceptions"));
		method.isAbstract = result.getBoolean("IsAbstract");
		method.isNative = result.getBoolean("IsNative");
		return method;
	}

	private MethodInvocation extractMethodInvocation(ResultSet result) throws SQLException{
		MethodInvocation invocation = new MethodInvocation();
		invocation.id = result.getInt("ID");
		invocation.invokeType = result.getString("InvokeType");
		invocation.callingClass = result.getString("CallingClass");
		invocation.callingMethod = result.getString("CallingMethod");
		invocation.callingMethodDescriptor = result.getString("CallingMethodDescriptor");
		invocation.targetClass = result.getString("TargetClass");
		invocation.targetMethod = result.getString("TargetMethod");
		invocation.targetMethodDescriptor = result.getString("TargetMethodDescriptor");
		invocation.version = result.getString("Version");
		
		return invocation;
	}
	
}
