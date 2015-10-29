package pscout.asm;

import java.io.FileInputStream;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import pscout.Factory;
import pscout.models.Configuration;
import pscout.models.SearchScope;
import pscout.models.SearchTarget;

public class AsmPermissionAnalyzer implements Runnable {
	private final SearchScope scope;
	private final SearchTarget target;
	private ClassNode clsNode;
	
	public AsmPermissionAnalyzer(SearchScope scope, SearchTarget target){
		this.scope = scope;
		this.target = target;
	}
	
	@SuppressWarnings("unchecked")
	public void run(){
		try{
			// load class file
			loadClass();

			for(MethodNode method : (List<MethodNode>)clsNode.methods){
				// looking for target method
				if(method.name.equals(scope.searchMethod) && method.desc.equals(scope.searchMethodDesc)){
					analyzeMethod(method, this.target); // perform analysis
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadClass() throws Exception {
		Configuration config = Factory.instance().getConfiguration();
		String filePath = config.classDumpPath + (config.classDumpPath.endsWith("\\") ? "": "\\") + scope.searchClass + ".class";
		
		ClassReader reader = new ClassReader(new FileInputStream(filePath));
		this.clsNode = new ClassNode();
		reader.accept(this.clsNode, ClassReader.EXPAND_FRAMES);
	}
	
	public void analyzeMethod(MethodNode method, SearchTarget target) throws Exception{
		int index = 0;
		while((index = this.indexOfMethodInvocation(method.instructions, target, index)) != -1){
			// calculate instruction offset as parameters are loaded in reverse order
			int offset = target.paramCount() - target.paramIndex + 1; 
			
			String permission = this.findPermissionString(method.instructions, index - offset);
			if(permission == null){
				// permission not found
				System.out.println("<No Permission Found>");
			}else{
				System.out.println("Permission Found: " + permission);
			}
			
			index++; // move to next instruction
		}
	}
	
	public int indexOfMethodInvocation(InsnList instructions, SearchTarget target, int start){
		// loop through each instruction
		for(int i = start; i < instructions.size(); i++){
			AbstractInsnNode insn = instructions.get(i);
			if(insn instanceof MethodInsnNode){
				MethodInsnNode m = (MethodInsnNode) insn;
				if(m.owner.equals(target.clsName) && m.name.equals(target.methodName) && m.desc.equals(target.methodDesc)){
					return i; // find matching invocation
				}
			}
		}
		
		return -1; // not found
	}
	
	public String findPermissionString(InsnList instructions, int index) throws Exception {
		AbstractInsnNode insn = instructions.get(index);
		
		if(insn instanceof LdcInsnNode){ // load insn: ldc "permission"
			LdcInsnNode node = (LdcInsnNode)insn;
			return node.cst.toString();
		} else if(insn instanceof FieldInsnNode){ // field insn: getstatic field
			FieldInsnNode node = (FieldInsnNode) insn;
			Object value = null;
			if(node.owner.equals(this.clsNode.name)){ 
				// field is in current class
				value = getField(node.name);
				return value != null ? value.toString() : null;
			}else{ 
				// load field from another class
				SearchScope newScope = new SearchScope(node.owner, node.name, node.desc);
				AsmPermissionAnalyzer analyzer = new AsmPermissionAnalyzer(newScope, this.target);
				analyzer.loadClass();
				value = analyzer.getField(node.name);
			}
			
			return value != null ? value.toString() : null;
			
		} else{
			System.out.println("Unexpected Insn: " + insn);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Object getField(String name){
		for(FieldNode field : (List<FieldNode>)this.clsNode.fields){
			if(field.name.equals(name)){
				return field.value;
			}
		}
		return null;
	}
}
