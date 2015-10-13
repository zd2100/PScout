package pscout.models;

public class Descriptor {
	public enum Types{
		Object,
		Byte,
		Short,
		Int,
		Long,
		Float,
		Double,
		Boolean,
		Char
	}
	
	public String objectClass;
	public Types type;
	public int arrayDimension;
	
	public boolean isArray(){
		return this.arrayDimension > 0;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		switch(this.type){
			case Object:
				builder.append(this.objectClass);
				break;
			case Byte:
				builder.append("byte");
				break;
			case Short:
				builder.append("short");
				break;
			case Int:
				builder.append("int");
				break;
			case Long:
				builder.append("long");
				break;
			case Float:
				builder.append("float");
				break;
			case Double:
				builder.append("double");
				break;
			case Boolean:
				builder.append("boolean");
				break;
			case Char:
				builder.append("char");
				break;
		}
		
		for(int i = 0; i < this.arrayDimension; i++){
			builder.append("[]");
		}
		
		return builder.toString();
	}
}
