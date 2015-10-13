package pscout.models;

import java.util.ArrayList;
import java.util.List;

import pscout.util.StringJoiner;

public class Method {
	public int id;
	public String className;
	public String methodName;
	public String version;
	public String access;
	public String signature;
	public String descriptor;
	public final List<String> exceptions;
	public boolean isAbstract;
	public boolean isNative;
	
	public Method(){
		this.exceptions = new ArrayList<String>();
	}
	
	public String getExceptionString(){
		return StringJoiner.join(this.exceptions, ";");
	}
	
	public void setExceptionList(String exceptionString){
		this.exceptions.clear();
		if(exceptionString != null){
			for(String i : exceptionString.split(";")){
				if(!i.isEmpty()) this.exceptions.add(i);
			}
		}
	}
}
