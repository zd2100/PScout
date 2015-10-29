package pscout.models;

import java.util.ArrayList;
import java.util.List;

public class SearchScope {
	public final String searchClass;
	public final String searchMethod;
	public final String searchMethodDesc;
	public final List<String> permissions;
	
	public SearchScope(String cls, String method, String desc){
		this.searchClass = cls;
		this.searchMethod = method;
		this.searchMethodDesc = desc;
		this.permissions = new ArrayList<String>();
	}
}
