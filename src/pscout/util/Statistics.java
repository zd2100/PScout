package pscout.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {
	public static AtomicInteger classCount;
	public static AtomicInteger methodCount;
	public static AtomicInteger invocationCount;
	
	static{
		classCount = new AtomicInteger();
		methodCount = new AtomicInteger();
		invocationCount = new AtomicInteger();
	}
	
	public static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	public static String getTime(){
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}
	
}
