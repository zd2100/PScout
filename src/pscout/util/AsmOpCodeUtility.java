package pscout.util;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;

public class AsmOpCodeUtility {
	private static int bitMask = 0x1;
	public static enum AccessTypes{
		Class,
		Method,
		Paramenter
	}
	
	public static boolean isPublic(int opcode){
		return ((opcode >> 0) & bitMask) == 1;
	}
	
	public static boolean isPrivate(int opcode){
		return ((opcode >> 1) & bitMask) == 1;
	}
	
	public static boolean isProtected(int opcode){
		return ((opcode >> 2) & bitMask) == 1;
	}
	
	public static boolean isStatic(int opcode){
		return ((opcode >> 3) & bitMask) == 1;
	}
	
	public static boolean isFinal(int opcode){
		return ((opcode >> 4) & bitMask) == 1;
	}
	
	public static boolean isSynchronized(int opcode){
		return ((opcode >> 5) & bitMask) == 1;
	}
	
	public static boolean isSuper(int opcode){
		return ((opcode >> 5) & bitMask) == 1;
	}
	
	public static boolean isVolatile(int opcode){
		return ((opcode >> 6) & bitMask) == 1;
	}
	
	public static boolean isTransient(int opcode){
		return ((opcode >> 7) & bitMask) == 1;
	}
	
	public static boolean isNative(int opcode){
		return ((opcode >> 8) & bitMask) == 1;
	}
	
	public static boolean isInterface(int opcode){
		return ((opcode >> 9) & bitMask) == 1;
	}
	
	public static boolean isAbstract(int opcode){
		return ((opcode >> 10) & bitMask) == 1;
	}
	
	public static boolean isStrict(int opcode){
		return ((opcode >> 11) & bitMask) == 1;
	}
	
	public static boolean isSynthetic(int opcode){
		return ((opcode >> 12) & bitMask) == 1;
	}
	
	public static boolean isAnnotation(int opcode){
		return ((opcode >> 13) & bitMask) == 1;
	}
	
	public static boolean isEnum(int opcode){
		return ((opcode >> 14) & bitMask) == 1;
	}
	
	public static boolean isMandated(int opcode){
		return ((opcode >> 15) & bitMask) == 1;
	}
	
	public static boolean isDeprecated(int opcode){
		return ((opcode >> 16) & bitMask) == 1;
	}

	public static boolean is(int opcode, int targetOpCode){
		int shift = (int)(Math.log(targetOpCode) / Math.log(2));
		return ((opcode >> shift) & bitMask) == 1;
	}
	
	public static List<String> buildAccessList(int access, AccessTypes type){
		List<String> list = new ArrayList<String>();
		
		if(isPublic(access)) list.add("public");
		if(isPrivate(access)) list.add("private");
		if(isProtected(access)) list.add("protected");
		if(isStatic(access)) list.add("static");
		if(isAbstract(access)) list.add("abstract");
		if(isFinal(access)) list.add("final");
		if(isDeprecated(access)) list.add("deprecated");
		
		if(type == AccessTypes.Class){
			if(isSuper(access)) list.add("super");
			if(isSynthetic(access)) list.add("synthetic");
		}
		
		if(type == AccessTypes.Method){
			if(isNative(access)) list.add("native");
			if(isSynchronized(access)) list.add("synchronized");
		}
		
		if(type == AccessTypes.Paramenter){
			if(isMandated(access)) list.add("mandated");
		}

		return list;
	}
	
	public static String MapInvokeType(int opcode){
		
		switch(opcode){
		case Opcodes.INVOKEDYNAMIC:
			return "dynamic";
		case Opcodes.INVOKEINTERFACE:
			return "interface";
		case Opcodes.INVOKESPECIAL:
			return "special";
		case Opcodes.INVOKESTATIC:
			return "static";
		case Opcodes.INVOKEVIRTUAL:
			return "virtual";
		}
		
		return null;
	}
	
}
