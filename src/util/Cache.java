package util;

public class Cache {

	public static boolean IsAbsolutePath(String path){
		boolean absPath = false;
		
		if(path.length() > 0){
			absPath = GitUtil.IsDirSep(path.charAt(0)) || GitUtil.HasDosDrivePrefix(path);
 		}
		
		return absPath;
	}
	
}
