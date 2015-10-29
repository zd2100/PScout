package pscout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {
	// Variables for concurrency
	public final static Object lock = new Object();
	public final static AtomicLong classCount = new AtomicLong();
	public final static AtomicLong methodCount = new AtomicLong();
	public final static AtomicLong methodInvocationCount = new AtomicLong();
	
	public final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	public static String getTime(){
		Calendar calendar = Calendar.getInstance();
		return timeFormat.format(calendar.getTime());
	}
}
