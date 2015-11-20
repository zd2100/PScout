package pscout.analyzers;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.google.inject.Inject;

import pscout.db.IDataProvider;
import pscout.models.Class;
import pscout.models.Config;
import pscout.models.Invocation;
import pscout.models.Method;
import pscout.util.AsmUtility;
import pscout.util.Statistics;
import pscout.util.AsmUtility.AccessTypes;
import pscout.util.StringUtility;

public class CHCGAnalyzer implements Runnable {
	private final Logger LOGGER = Logger.getLogger(CHCGAnalyzer.class.getName());
	private final IDataProvider dataProvider;
	private final Config config;
	private File classFile;

	public CHCGAnalyzer(IDataProvider dataProvider, Config config){
		this.dataProvider = dataProvider;
		this.config = config;
	}
	
	public CHCGAnalyzer(IDataProvider dataProvider, Config config, File file){
		this.dataProvider = dataProvider;
		this.config = config;
		this.classFile = file;
	}
	
	public void setClassFile(File file){
		this.classFile = file;
	}

	@Override
	public void run() {
		try{
			ClassReader reader = new ClassReader(new FileInputStream(this.classFile));
			AsmClassVisitor visitor = new AsmClassVisitor(this.dataProvider, this.config);
			reader.accept(visitor, ClassReader.SKIP_DEBUG);
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	

	/* ---------------- Class Visitor ----------------- */
	private class AsmClassVisitor extends ClassVisitor{
		private final IDataProvider dataProvider;
		private final Class cls;
		private final Config config;
		
		public AsmClassVisitor(IDataProvider provider, Config config) {
			super(Opcodes.ASM5);
			this.dataProvider = provider;
			this.config = config;
			this.cls = new Class();
		}
		
		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
			this.cls.className = name;
			this.cls.access = StringUtility.join(AsmUtility.buildAccessList(access, AccessTypes.Class), ";");
			this.cls.version = this.config.androidVersion;
			this.cls.superClass = superName;
			this.cls.signature = signature;
			this.cls.isAbstract = AsmUtility.isAbstract(access);
			this.cls.isEnum = AsmUtility.isEnum(access);
			this.cls.isInterface = AsmUtility.isInterface(access);
			this.cls.interfaces = StringUtility.join(interfaces, ";");
			 
			this.dataProvider.addClass(this.cls);
			Statistics.classCount.incrementAndGet();
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
			Method method = new Method();
			method.classId = this.cls.id;
			method.methodName = name;
			method.access = StringUtility.join(AsmUtility.buildAccessList(access, AccessTypes.Method), ";");
			method.version = this.config.androidVersion;
			method.signature = signature;
			method.descriptor = desc;
			if(exceptions != null) method.exceptions = StringUtility.join(exceptions, ";");
			method.isAbstract = AsmUtility.isAbstract(access);
			method.isNative = AsmUtility.isNative(access);

			this.dataProvider.addMethod(method);
			Statistics.methodCount.incrementAndGet();
			
			return new AsmMethodVisitor(this.dataProvider, this.config, this.cls, method);
		}
	}
	
	
	
	
	/* -------------- Method Visitor -------------- */
	private class AsmMethodVisitor extends MethodVisitor{
		private final Config config;
		private final IDataProvider dbProvider;
		private final Class caller;
		private final Method callerMethod;
		
		public AsmMethodVisitor(IDataProvider provider, Config config, Class caller, Method callerMethod){
			super(Opcodes.ASM5);
			this.config = config;
			this.dbProvider = provider;
			this.caller = caller;
			this.callerMethod = callerMethod;
		}
		
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf){
			Invocation invocation = new Invocation();
			invocation.callerClass = this.caller.className;
			invocation.callerMethod = this.callerMethod.methodName;
			invocation.callerMethodDesc = this.callerMethod.descriptor;
			invocation.targetClass = owner;
			invocation.targetMethod = name;
			invocation.targetMethodDesc = desc;
			invocation.invokeType = AsmUtility.MapInvokeType(opcode);
			invocation.version = this.config.androidVersion;

			this.dbProvider.addInvocation(invocation);
			Statistics.invocationCount.incrementAndGet();
		}

	}
}
