package pscout.util;

import java.util.List;

public class StringJoiner {
	public static String join(String[] strings, String delimiter){
		if(strings == null){
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		for(String string : strings){
			if(builder.length() != 0){
				builder.append(delimiter);
			}
			builder.append(string);
		}
		return builder.toString();
	}
	
	public static String join(List<String> strings, String delimiter){
		return join(strings.toArray(new String[strings.size()]), delimiter);
	}
}
