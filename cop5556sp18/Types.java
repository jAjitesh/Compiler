package cop5556sp18;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;

public class Types {

	public static enum Type {
		INTEGER, BOOLEAN, IMAGE, FLOAT, FILE, NONE;
	}

	public static Type getType(Kind kind) {
		switch (kind) {
		case KW_int: {
			return Type.INTEGER;
		}
		case KW_boolean: {
			return Type.BOOLEAN;
		}
		case KW_image: {
			return Type.IMAGE;
		}
		case KW_filename: {
			return Type.FILE;
		}
		case KW_float: {
			return Type.FLOAT;
		}
		default:
			break;
		}
		// should not reach here
		assert false: "invoked getType with Kind that is not a type"; 
		return null;
	}
	
	public static String getJVMType(Type type){
		if(type.equals(Type.INTEGER)){
			return "I";
		}
		if(type.equals(Type.BOOLEAN)){
			return "Z";
		}
		if(type.equals(Type.FLOAT)){
			return "F";
		}
		if(type.equals(Type.IMAGE)){
			return "Ljava/awt/image/BufferedImage";
		}
		if(type.equals(Type.IMAGE)){
			return "Ljava/lang/String";
		}
		return null;
	}
}
