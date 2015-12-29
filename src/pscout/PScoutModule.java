package pscout;

import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

import pscout.core.BuildCHCG;
import pscout.core.ExtractJarFile;
import pscout.db.IDataProvider;
import pscout.db.MySqlDataProvider;
import pscout.models.Config;

/**
 * Google Guice IoC loading module
 * @author Ding Zhu
 */
public class PScoutModule extends AbstractModule{
	private static Logger LOGGER = Logger.getLogger(PScoutModule.class.getName());

	@Override
	protected void configure() {
		this.bind(IDataProvider.class).to(MySqlDataProvider.class).in(Scopes.SINGLETON);
		this.bind(ExtractJarFile.class).in(Scopes.SINGLETON);
		this.bind(BuildCHCG.class);
	}
	
	@Provides @Singleton
	private Config provideConfig(){
		try{
			Gson gson = new Gson();
			return gson.fromJson(new FileReader(Config.configFile),Config.class);
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
		return null;
	}
}
