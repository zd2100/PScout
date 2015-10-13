package pscout;

import java.util.concurrent.atomic.AtomicLong;

public class Statistics {
	// Variables for concurrency
	public final static Object lock = new Object();
	public final static AtomicLong classCount = new AtomicLong();
	public final static AtomicLong methodCount = new AtomicLong();
	public final static AtomicLong methodInvocationCount = new AtomicLong();
}
