package pscout.analyzer;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.objectweb.asm.tree.analysis.Value;

import pscout.models.Descriptor;
import pscout.util.DescriptorParser;

public class TestAnalyzer {
	public void run(){
		try{
			String path = "D:\\Workspace\\Test\\bin\\Test.class";
			ClassReader reader = new ClassReader(new FileInputStream(path));
			ClassNode cn = new ClassNode();
			reader.accept(cn, ClassReader.EXPAND_FRAMES);
			
			for(MethodNode method : (List<MethodNode>) cn.methods){
				if(method.name.equals("main")){
					for(LocalVariableNode node : (List<LocalVariableNode>)method.localVariables){
						List<Descriptor> parameters = DescriptorParser.parseDescriptorString(node.desc);
						System.out.print(node.index + "\t");
						for(Descriptor param : parameters){
							System.out.print(param);
						}
						System.out.println();
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
