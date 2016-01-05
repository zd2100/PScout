package pscout.db;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.google.inject.Inject;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import pscout.models.Class;
import pscout.models.Config;
import pscout.models.Invocation;
import pscout.models.Method;

public class MySqlDataProvider implements IDataProvider {
	
	private static final Logger LOGGER = Logger.getLogger(MySqlDataProvider.class.getName());
	
	private final ComboPooledDataSource dataSource;
	private final Sql2o sql2o;
	
	@Inject
	public MySqlDataProvider(Config config) throws Exception{
		if(config == null) throw new Exception("config is null");
		this.dataSource = new ComboPooledDataSource();
		this.dataSource.setDriverClass(config.driverClass);
		this.dataSource.setJdbcUrl(config.connectionString);
		this.dataSource.setMaxPoolSize(2 * config.threads);
		this.dataSource.setMaxStatements(20 * config.threads);
		this.sql2o = new Sql2o(this.dataSource);
	}

	@Override
	public void addClass(Class cls) {
		final String sql = "INSERT IGNORE INTO Classes (ClassName,Version,Access,Signature,SuperClass,Interfaces,IsAbstract,IsInterface,IsEnum,IsPublic)"
				+ " VALUES (:className, :version, :access, :signature, :superClass, :interfaces, :isAbstract, :isInterface, :isEnum, :isPublic)";
		
		try(Connection con = this.getConnection()){
			Object value = con.createQuery(sql, true)
						.bind(cls)
						.executeUpdate()
						.getKey();
			if(value != null){
				cls.id = (long) value;
			}
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void addMethod(Method method) {
		final String sql = "INSERT IGNORE INTO Methods (ClassId, MethodName,Version,Access,Signature,Descriptor,Exceptions,IsAbstract,IsNative,IsPublic)" 
				+ " VALUES (:classId, :methodName, :version, :access, :signature, :descriptor, :exceptions, :isAbstract, :isNative, :isPublic)";
		try(Connection con = this.getConnection()){
			Object value = con.createQuery(sql, true)
							.bind(method)
							.executeUpdate()
							.getKey();
			if(value != null){
				method.id = (long) value;
			}
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void addInvocation(Invocation invocation) {
		final String sql = "INSERT IGNORE INTO Invocations (InvokeType, CallerClass, CallerMethod, CallerMethodDesc, TargetClass, TargetMethod, TargetMethodDesc, Version)" 
				+ " VALUES (:invokeType, :callerClass, :callerMethod, :callerMethodDesc, :targetClass, :targetMethod, :targetMethodDesc, :version)";
		try(Connection con = this.getConnection()){
				Object value = con.createQuery(sql, true)
							.bind(invocation)
							.executeUpdate()
							.getKey();
				if(value != null){
					invocation.id = (long) value;
				}
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	@Override
	public void addPermissionInvocation(Invocation invocation, String permission) {
		final String sql = "INSERT IGNORE INTO PermissionInvocations (InvocationId, Permission) VALUES (:id, :permission)";
		try(Connection con = this.getConnection()){
				con.createQuery(sql)
				.addParameter("id", invocation.id)
				.addParameter("permission", permission)
				.executeUpdate();
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public Class getClassById(long id) {
		final String sql = "SELECT ID, ClassName, Version, Access, SuperClass, Signature, Interfaces, IsAbstract, IsInterface, IsEnum FROM Classes"
				+ " WHERE ID = :id";
		try(Connection con = this.getConnection()){
			return con.createQuery(sql)
					.addParameter("id", id)
					.executeAndFetchFirst(Class.class);	
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public Class getClass(String className, String version){
		final String sql = "SELECT ID, ClassName, Version, Access, SuperClass, Signature, Interfaces, IsAbstract, IsInterface, IsEnum FROM Classes"
				+ " WHERE ClassName = :className AND Version = :version";
		try(Connection con = this.getConnection()){
			return con.createQuery(sql)
					.addParameter("className", className)
					.addParameter("version", version)
					.executeAndFetchFirst(Class.class);	
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Method getMethodById(long id) {
		final String sql = "SELECT ID, ClassId, MethodName, Version, Access, Signature, Descriptor, Exceptions, IsAbstract, IsNative FROM Methods"
				+ " WHERE ID = :id";
		try(Connection con = this.getConnection()){
			return con.createQuery(sql)
					.addParameter("id", id)
					.executeAndFetchFirst(Method.class);
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public Method getMethod(String clsName, String methodName, String descriptor, String version) {
		final String sql = "SELECT Id, ClassId, MethodName,Version,Access,Signature,Descriptor,Exceptions,IsAbstract,IsNative FROM Methods"
				+ " WHERE ClassId = (SELECT Id FROM Classes WHERE ClassName = :clsName AND Version = :version) AND MethodName = :methodName AND Descriptor = :descriptor AND Version = :version";
		try(Connection con = this.getConnection()){
			return con.createQuery(sql)
					.addParameter("clsName", clsName)
					.addParameter("methodName", methodName)
					.addParameter("descriptor", descriptor)
					.addParameter("version", version)
					.executeAndFetchFirst(Method.class);
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Invocation getInvocationById(long id) {
		final String sql = "SELECT ID, InvokeType, CallerClass, CallerMethod, CallerMethodDesc, TargetClass, TargetMethod, TargetMethodDesc, Version"
				+ " FROM Invocations"
				+ " WHERE id = :id";
		try(Connection con = this.getConnection()){
			return con.createQuery(sql)
					.addParameter("id", id)
					.executeAndFetchFirst(Invocation.class);
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public List<Invocation> findInvocations(String className, String methodName, String descriptor, String version){
		final String sql = "SELECT ID, InvokeType, CallerClass, CallerMethod, CallerMethodDesc, TargetClass, TargetMethod, TargetMethodDesc, Version"
				+ " FROM Invocations"
				+ " WHERE TargetClass = :className AND targetMethod = :methodName AND TargetMethodDesc = :descriptor AND Version = :version";
		
		try(Connection con = this.getConnection()){
			return con.createQuery(sql)
					.addParameter("className", className)
					.addParameter("methodName", methodName)
					.addParameter("descriptor", descriptor)
					.addParameter("version", version)
					.executeAndFetch(Invocation.class);
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public void shutdown(){
		this.dataSource.close();
	}
	
	private Connection getConnection(){
		return this.sql2o.open();
	}
}
