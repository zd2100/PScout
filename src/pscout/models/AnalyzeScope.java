package pscout.models;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeScope {
	
	public static int NoIndex = 0;
	
	public Invocation invocation;
	public int index;
	public List<String> permissions;
		
	public AnalyzeScope(Invocation invocation, int index){
		this(invocation, index, new ArrayList<String>());
	}
	
	public AnalyzeScope(Invocation invocation, int index, List<String> permissions){
		this.invocation = invocation;
		this.index = index;
		this.permissions = permissions;
	}
	
	public boolean IsPermissionAlreadyDetermined(){
		return this.permissions != null && !this.permissions.isEmpty();
	}
	
	
}
