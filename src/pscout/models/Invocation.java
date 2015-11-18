package pscout.models;

public class Invocation {
	public long id;
	public String callerClass;
	public String callerMethod;
	public String callerMethodDesc;
	public String targetClass;
	public String targetMethod;
	public String targetMethodDesc;
	public String invokeType;
	public String version;
	
	@Override
	public String toString(){
		return "Invoke: " + invokeType + "\t" + "[" + callerClass + "]"
				+ "." + callerMethod + "(" + callerMethodDesc + ")" +"\t=>\t"
				+ "[" + targetClass + "]" + "." + targetMethod + "(" + targetMethodDesc + ")";
	}
}
