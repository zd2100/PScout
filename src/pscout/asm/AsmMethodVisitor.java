package pscout.asm;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import pscout.Factory;
import pscout.Statistics;
import pscout.db.DbProvider;
import pscout.models.Class;
import pscout.models.Configuration;
import pscout.models.Method;
import pscout.models.MethodInvocation;

public class AsmMethodVisitor extends MethodVisitor{
	private final Configuration config;
	private final DbProvider dbProvider;
	private final Method method;
	
	public AsmMethodVisitor( Method method){
		super(Opcodes.ASM5);
		this.config = Factory.instance().getConfiguration();
		this.dbProvider = Factory.instance().getDbProvider();
		this.method = method;
	}
	
	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs){
		System.out.println("Dynamic:" + name + "\t" + desc);
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf){
		Statistics.methodInvocationCount.incrementAndGet();
		
		MethodInvocation invocation = new MethodInvocation();
		invocation.invokeType = AsmOpCodes.MapInvokeType(opcode);
		invocation.callingClass = this.method.className;
		invocation.callingMethod = this.method.methodName;
		invocation.callingMethodDescriptor = this.method.descriptor;
		invocation.targetClass = owner;
		invocation.targetMethod = name;
		invocation.targetMethodDescriptor = desc;
		invocation.version = this.config.androidVersion;

		try{
			this.dbProvider.addMethodInvoke(invocation);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}