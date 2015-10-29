package pscout.models;

import java.util.ArrayList;
import java.util.List;

public class SearchTarget {
	public String clsName;
	public String methodName;
	public String methodDesc;
	public int paramIndex;
	
	public SearchTarget(String cls, String method, String desc, int index){
		this.clsName = cls;
		this.methodName = method;
		this.methodDesc = desc;
		this.paramIndex = index;
	}
	
	public int paramCount(){
		String baseTypes = "BCDFIJSZ";
		int count = 0;
		
		for(int i=0; i < this.methodDesc.length(); i++){
			char c = this.methodDesc.charAt(i);
			if(baseTypes.indexOf(c) != -1){ // base types
				count++;
			}else if(c == 'L'){ // object type
				count++;
				i = this.methodDesc.indexOf(';', i);
			}else if(c == ')'){ // end of parameters
				break;
			}
		}
		
		return count;
	}
}
