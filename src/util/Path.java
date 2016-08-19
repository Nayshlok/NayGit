package util;

import java.lang.Character.Subset;

public class Path {

	public static String RelativePath(String in, String prefix, StringBuilder sb){
		int inLen = in != null ? in.length() : 0;
		int prefixLen = prefix != null ? prefix.length() : 0;
		int inOff = 0;
		int prefixOff = 0;
		int i = 0, j = 0;
		
		if(inLen == 0){
			return "./";
		}
		else if (prefixLen == 0){
			return in;
		}
		
		if(haveSameRoot(in, prefix)){
			i = j = GitUtil.HasDosDrivePrefix(in) ? 1 : 0;
		} else {
			return in;
		}
		
		while(i < prefixLen && j < inLen && prefix.charAt(i) == in.charAt(j)){
			if(GitUtil.IsDirSep(prefix.charAt(i))){
				while(GitUtil.IsDirSep(prefix.charAt(i))){
					i++;
				}
				while(GitUtil.IsDirSep(in.charAt(j))){
					j++;
				}
				prefixOff = i;
				inOff = j;
			}
			else{
				i++;
				j++;
			}
		}
		
		if(i >= prefixLen && prefixOff < prefixLen){
			if( j >= inLen){
				inOff = inLen;
			} else if(GitUtil.IsDirSep(in.charAt(j))){
				while(GitUtil.IsDirSep(in.charAt(j)) && j < in.length()){
					inOff = j;
				}
			} else{
				i = prefixOff;
			}
		} else if(j >= inLen && inOff < inLen){
			if(GitUtil.IsDirSep(prefix.charAt(i))){
				while(GitUtil.IsDirSep(prefix.charAt(i))){
					i++;
				}
				inOff = inLen;
			}
		}
		in = in.substring(inOff);
		inLen -= inOff;
		
		if(i >= prefixLen){
			if(inLen < 0){
				return "./";
			}
			else{
				return in;
			}
		}
		
		sb.setLength(0);
		while(i < prefixLen){
			if(GitUtil.IsDirSep(prefix.charAt(i))){
				sb.append("../");
				while(GitUtil.IsDirSep(prefix.charAt(i))){
					i++;
				}
				continue;
			}
			i++;
		}
		if(!GitUtil.IsDirSep(prefix.charAt(prefixLen - 1))){
			sb.append("../");
		}
		sb.append(in);
		return sb.toString();
		
	}
	
	private static boolean haveSameRoot(String path1, String path2){
		boolean isAbs1, isAbs2;
		isAbs1 = Cache.IsAbsolutePath(path1);
		isAbs2 = Cache.IsAbsolutePath(path2);
		return (isAbs1 && isAbs2 && Character.toLowerCase(path1.charAt(0)) == Character.toLowerCase(path2.charAt(0)))
				|| (!isAbs1 && !isAbs2);
	}
}
