package pscout.analyzers;

import java.util.logging.Logger;

import com.google.inject.Inject;

import pscout.db.IDataProvider;
import pscout.models.AnalyzeScope;
import pscout.models.PermissionResult;

public class PermissionAnalyzer {
	private static final Logger LOGGER = Logger.getLogger(PermissionAnalyzer.class.getName());
	
	private final IDataProvider dataProvider;

	public PermissionAnalyzer(IDataProvider dataProvider){
		this.dataProvider = dataProvider;
	}
	
	public PermissionResult analyze(AnalyzeScope scope){
		PermissionResult result = new PermissionResult();
		
		return result;
	}

}
