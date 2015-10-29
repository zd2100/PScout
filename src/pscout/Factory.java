package pscout;

import java.io.FileReader;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import com.google.gson.Gson;
import pscout.db.DbProvider;
import pscout.db.SqlDataProvider;
import pscout.models.Configuration;
import pscout.models.Method;
import pscout.models.Class;

public class Factory {
	private static Factory factory;
	
	private Configuration config;
	private DbProvider dbProvider;
	private Gson gson;
	
	private Factory() {
		this.gson = new Gson();
	}

	public synchronized static Factory instance() {
		if(factory == null){
			factory = new Factory();
		}
		return factory;
	}
	
	public synchronized Configuration getConfiguration(){
		if(this.config == null){
			try{
				this.config = this.gson.fromJson(new FileReader(Configuration.configFile), Configuration.class);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return this.config;
	}
	
	public synchronized DbProvider getDbProvider(){
		if(this.dbProvider == null){
			try{
				getConfiguration();
				this.dbProvider = new SqlDataProvider(this.config.driverClass, this.config.connectionString, this.config.parallelJobs);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return this.dbProvider;
	}
	
	public void shutdown(){
		if(this.dbProvider != null){
			this.dbProvider.shutdown();
		}
	}
}
