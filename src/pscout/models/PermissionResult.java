package pscout.models;

import java.util.ArrayList;
import java.util.List;

public class PermissionResult {
	public final List<String> permissions;
	public int paramIndex;
	public Status status;
	
	public PermissionResult(){
		this.permissions = new ArrayList<String>();
		this.status = Status.Unknown;
	}

	public enum Status{
		Found, // permission found within method
		FromParent, // permission is passed in via parameter
		Unknown // permission cannot be determined
	}
}
