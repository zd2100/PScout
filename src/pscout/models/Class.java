package pscout.models;

public class Class {
	public long id;
	public String className;
	public String version;
	public String access;
	public String signature;
	public String superClass;
	public String interfaces;
	public boolean isAbstract;
	public boolean isInterface;
	public boolean isEnum;
	public boolean isPublic;
	
	@Override
	public String toString(){
		return "Class: " + id + "\t" + className + "\t" + access + "\t" + superClass + "\t" + interfaces;
	}
}
