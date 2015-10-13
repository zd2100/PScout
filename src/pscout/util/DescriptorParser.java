package pscout.util;

import java.util.ArrayList;
import java.util.List;

import pscout.models.Descriptor;

public class DescriptorParser {
	public static List<Descriptor> parseDescriptorString(String descriptor){
		ArrayList<Descriptor> descriptors = new ArrayList<Descriptor>();
		if(descriptor != null){
			int arrayDim = 0;
			for(int i = 0; i < descriptor.length(); i++){
				char c = descriptor.charAt(i);
				
				if(c == '['){
					arrayDim++;
				}
				
				if(c == 'L'){
					int end = descriptor.indexOf(';',i);
					Descriptor item = new Descriptor();
					item.type = Descriptor.Types.Object;
					item.objectClass = descriptor.substring(i + 1, end);
					item.arrayDimension = arrayDim;
					
					descriptors.add(item);
					arrayDim = 0;
					i = end;
				}
				
				if(c == 'B'){
					Descriptor item = new Descriptor();
					item.type = Descriptor.Types.Byte;
					item.arrayDimension = arrayDim;
					
					descriptors.add(item);
					arrayDim = 0;
				}
				if(c == 'C'){
					Descriptor item = new Descriptor();
					item.type = Descriptor.Types.Char;
					item.arrayDimension = arrayDim;
					
					descriptors.add(item);
					arrayDim = 0;
				}
				if(c == 'D'){
					Descriptor item = new Descriptor();
					item.type = Descriptor.Types.Double;
					item.arrayDimension = arrayDim;
					
					descriptors.add(item);
					arrayDim = 0;
				}
				if(c == 'F'){
					Descriptor item = new Descriptor();
					item.type = Descriptor.Types.Float;
					item.arrayDimension = arrayDim;
					
					descriptors.add(item);
					arrayDim = 0;
				}
				if(c == 'I'){
					Descriptor item = new Descriptor();
					item.type = Descriptor.Types.Int;
					item.arrayDimension = arrayDim;
					
					descriptors.add(item);
					arrayDim = 0;
				}
				if(c == 'J'){
					Descriptor item = new Descriptor();
					item.type = Descriptor.Types.Long;
					item.arrayDimension = arrayDim;
					
					descriptors.add(item);
					arrayDim = 0;
				}
				if(c == 'S'){
					Descriptor item = new Descriptor();
					item.type = Descriptor.Types.Short;
					item.arrayDimension = arrayDim;
					
					descriptors.add(item);
					arrayDim = 0;
				}
				if(c == 'Z'){
					Descriptor item = new Descriptor();
					item.type = Descriptor.Types.Boolean;
					item.arrayDimension = arrayDim;
					
					descriptors.add(item);
					arrayDim = 0;
				}
			}
		}
		
		return descriptors;
	}
}
