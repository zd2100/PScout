package pscout.asm;

import java.util.Arrays;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import pscout.Factory;
import pscout.Statistics;
import pscout.asm.AsmOpCodes.AccessTypes;
import pscout.db.DbProvider;
import pscout.models.Class;
import pscout.models.Configuration;
import pscout.models.Method;
import pscout.util.StringJoiner;

public class AsmClassVisitor extends ClassVisitor{
	private final Configuration config;
	private final DbProvider dbProvider;
	private final Class cls;
	
	public AsmClassVisitor(){
		super(Opcodes.ASM5);
		this.config = Factory.instance().getConfiguration();
		this.dbProvider = Factory.instance().getDbProvider();
		this.cls = new Class();
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
		// increment class count
		Statistics.classCount.incrementAndGet();

		this.cls.className = name;
		this.cls.access = StringJoiner.join(AsmOpCodes.buildAccessList(access, AccessTypes.Class), ";");
		this.cls.version = this.config.androidVersion;
		this.cls.superClass = superName;
		this.cls.signature = signature;
		this.cls.isAbstract = AsmOpCodes.isAbstract(access);
		this.cls.isEnum = AsmOpCodes.isEnum(access);
		this.cls.isInterface = AsmOpCodes.isInterface(access);
		if(interfaces != null) cls.interfaces.addAll(Arrays.asList(interfaces));
		
		// insert into database
		try{
			this.dbProvider.addClass(cls);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
		// increment method count
		Statistics.methodCount.incrementAndGet();
		
		Method method = new Method();
		method.className = this.cls.className;
		method.methodName = name;
		method.access = access + ";" + StringJoiner.join(AsmOpCodes.buildAccessList(access, AccessTypes.Method), ";");
		method.version = this.config.androidVersion;
		method.signature = signature;
		method.descriptor = desc;
		if(exceptions != null) method.exceptions.addAll(Arrays.asList(exceptions));
		method.isAbstract = AsmOpCodes.isAbstract(access);
		method.isNative = AsmOpCodes.isNative(access);

		// insert into database
		try{
			this.dbProvider.addMethod(method);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return new AsmMethodVisitor(method);
	}
	
	
	@Override
	public void visitEnd(){
	}
}
