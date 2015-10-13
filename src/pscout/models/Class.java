package pscout.models;

import java.util.ArrayList;
import java.util.List;

import pscout.util.StringJoiner;

public class Class {
	public int	id;
	public String className;
	public String version;
	public String access;
	public String signature;
	public String superClass;
	public final List<String> interfaces;
	public boolean isAbstract;
	public boolean isInterface;
	public boolean isEnum;
	
	public Class(){
		this.interfaces = new ArrayList<String>();
	}
	
	public String getInterfaceString(){
		return StringJoiner.join(interfaces, ";");
	}
	
	public void setInterfaceString(String interfaces){
		this.interfaces.clear();
		if(interfaces != null){
			for(String i : interfaces.split(";")){
				if(!i.isEmpty()) this.interfaces.add(i);
			}
		}
	}
}
