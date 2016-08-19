package util;

public class GitUtil {

	public static final int FILE_OFFSET_BITS = 64;
	
	
	public static boolean IsDirSep(char c){
		return c == '/';
	}
	
	public static boolean Offset1stComponent(String path){
		boolean offset = false;
		if(path.length() > 1){
			offset = IsDirSep(path.charAt(0));
		}
		return offset;
	}
	
	public static boolean HasDosDrivePrefix(String path){
		return false;
	}
	
	public static int StrCSpn(String str1, String str2){
		int count = 0;
		for(char c : str1.toCharArray()){
			if(!str2.contains(c + "")){
				count++;
			}
		}
		return count;
	}
}
