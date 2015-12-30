package pscout.models;

public class Method {
	public long id;
	public long classId;
	public String methodName;
	public String version;
	public String access;
	public String signature;
	public String descriptor;
	public String exceptions;
	public boolean isAbstract;
	public boolean isNative;
	public boolean isPublic;
	
	@Override
	public String toString(){
		return "Method: " + id + "\t" + methodName + "\t" + version + "\t" + access + "\t" + descriptor;
	}
}
