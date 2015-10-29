package pscout.asm;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import pscout.Factory;
import pscout.asm.AsmOpCodes.AccessTypes;
import pscout.db.DbProvider;
import pscout.models.Class;
import pscout.models.Configuration;
import pscout.models.Method;
import pscout.util.StringJoiner;

public class AsmClassAnalyzer implements Runnable {

	// Local variables
	private final File classFile;
	
	public AsmClassAnalyzer(File classFile){
		this.classFile = classFile;
	}
	
	@Override
	public void run() {
		try {
			ClassReader reader = new ClassReader(new FileInputStream(this.classFile));
			ClassVisitor classVisitor = new AsmClassVisitor();
			reader.accept(classVisitor, ClassReader.SKIP_DEBUG);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
