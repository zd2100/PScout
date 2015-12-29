package pscout.analyzers;

import java.io.FileInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import pscout.models.AnalyzeScope;
import pscout.models.Config;
import pscout.models.Invocation;
import pscout.models.PermissionResult;

public class PermissionAnalyzer {
	private static final Logger LOGGER = Logger.getLogger(PermissionAnalyzer.class.getName());
	
	private final Config config;

	public PermissionAnalyzer(Config config){
		this.config = config;
	}
	
	public PermissionResult analyze(AnalyzeScope scope){
		
		PermissionResult result = new PermissionResult();

		try{
			ClassNode cls = loadClass(scope.invocation.callerClass, this.config);
			MethodNode method = getMethod(cls, scope.invocation.callerMethod, scope.invocation.callerMethodDesc);
			
			// analyze
			this.analyzeMethod(cls, method, scope, result);
		
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
		return result;
	}

	private void analyzeMethod(ClassNode cls, MethodNode method, AnalyzeScope scope, PermissionResult result) throws Exception{
		// calculate instruction offset as parameters are loaded in reverse order
		int offset = paramCount(scope.invocation.targetMethodDesc) - scope.index + 1; 
		Invocation invocation = scope.invocation;
		
		for(int i = 0; i < method.instructions.size(); i++){
			AbstractInsnNode insn = method.instructions.get(i);
			if(insn instanceof MethodInsnNode){
				MethodInsnNode m = (MethodInsnNode) insn;
				// if method invocation instruction matches target method signature
				if(m.owner.equals(invocation.targetClass) && m.name.equals(invocation.targetMethod)	&& m.desc.equals(invocation.targetMethodDesc)){
					// find permission string
					Object value = this.findPermissionString(cls, method, i - offset);
					
					if(value instanceof VarInsnNode){
						VarInsnNode node = (VarInsnNode) value;
						result.status = PermissionResult.Status.FromParent;
						result.paramIndex = node.var;
						System.out.println("Permission Pass via variable " + node.var);
					}else if(value instanceof String){
						result.status = PermissionResult.Status.Found;
						result.permissions.add(value.toString());
					}else{
						LOGGER.log(Level.WARNING, "Permission Unknown");
						result.status = PermissionResult.Status.Unknown;
					}
				}
				// END IF
			}
			// END IF
		}
		// END FOR LOOP
	}

	
	private Object findPermissionString(ClassNode cls, MethodNode method, int insnIndex) throws Exception {
		AbstractInsnNode insn = method.instructions.get(insnIndex);
		
		if(insn instanceof LdcInsnNode){ 
			// load instruction: ldc "permission"
			LdcInsnNode node = (LdcInsnNode)insn;
			return node.cst.toString();
			
		} else if(insn instanceof FieldInsnNode){ 
			// field instruction: getstatic field
			FieldInsnNode node = (FieldInsnNode) insn;
			Object value = null;
			
			if(node.owner.equals(cls.name)){ 
				// field is in current class
				value = getField(cls, node.name);
			}else{ 
				// load field from another class
				ClassNode otherClass = loadClass(node.owner, this.config);
				value = getField(otherClass, node.name);
			}
			return value != null ? value.toString() : null;
		} else if(insn instanceof VarInsnNode){
			VarInsnNode node = (VarInsnNode) insn;
			// variable is the parameter of the current method
			if(node.var > 0 && node.var <= paramCount(method.desc)){
				return node;
			}
		} else if(insn instanceof MethodInsnNode){
			MethodInsnNode node = (MethodInsnNode) insn;
			LOGGER.log(Level.WARNING, "Method Insn: " + node.owner + node.name + node.desc);
		} else{
			LOGGER.log(Level.WARNING, "Unhandled Instruction: " + insn);
		}
		return null;
	}

	private Object traceVariable(ClassNode cls, InsnList instructions){
		return null;
	}

	
	
	/* Static Helpers */
	/**
	 * Find and return the class of the given name
	 * @param name ClassName
	 * @param config Configuration
	 * @return ClassNode
	 * @throws Exception Class does not exist
	 */
	private static ClassNode loadClass(String name, Config config) throws Exception {
		String filePath = config.getClassDumpPath() + name.replace('.', '/') + ".class";
		
		ClassReader reader = new ClassReader(new FileInputStream(filePath));
		ClassNode node = new ClassNode();
		reader.accept(node, ClassReader.EXPAND_FRAMES);
		return node;
	}
	
	/**
	 * Return a MethodNode of the matching name and descriptor from the class
	 * @param cls Class Node
	 * @param methodName Method Name
	 * @param methodDesc Method Descriptor
	 * @return MethodNode
	 */
	@SuppressWarnings("unchecked")
	private static MethodNode getMethod(ClassNode cls, String methodName, String methodDesc){
		for(MethodNode method : (List<MethodNode>)cls.methods){
			// looking for target method
			if(method.name.equals(methodName) && method.desc.equals(methodDesc)){
				return method;
			}
		}
		return null;
	}
	
	/**
	 * Return the value of the static field or null if the field does not exist
	 * @param cls Class Node
	 * @param name Field Name
	 * @return value of static field or null
	 */
	@SuppressWarnings("unchecked")
	private static Object getField(ClassNode cls, String name){
		for(FieldNode field : (List<FieldNode>)cls.fields){
			if(field.name.equals(name)){
				return field.value;
			}
		}
		return null;
	}

	/**
	 * Count the number of parameters of a method
	 * @param desc Method Descriptor
	 * @return number of parameters of the given method
	 */
	private static int paramCount(String desc){
		String baseTypes = "BCDFIJSZ";
		int count = 0;
		
		for(int i=0; i < desc.length(); i++){
			char c = desc.charAt(i);
			if(baseTypes.indexOf(c) != -1){ // base types
				count++;
			}else if(c == 'L'){ // object type
				count++;
				i = desc.indexOf(';', i);
			}else if(c == ')'){ // end of parameters
				break;
			}
		}
		
		return count;
	}
}
