package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import util.Usage;

/* Help to copy the thing properly quoted for the shell safety.
 * any single quote is replaced with '\'', any exclamation point
 * is replaced with '\!', and the whole thing is enclosed in a
 * single quote pair.
 *
 * For example, if you are passing the result to system() as an
 * argument:
 *
 * sprintf(cmd, "foobar %s %s", sq_quote(arg0), sq_quote(arg1))
 *
 * would be appropriate.  If the system() is going to call ssh to
 * run the command on the other side:
 *
 * sprintf(cmd, "git-diff-tree %s %s", sq_quote(arg0), sq_quote(arg1));
 * sprintf(rcmd, "ssh %s %s", sq_quote(host), sq_quote(cmd));
 *
 * Note that the above examples leak memory!  Remember to free result from
 * sq_quote() in a real application.
 *
 * sq_quote_buf() writes to an existing buffer of specified size; it
 * will return the number of characters that would have been written
 * excluding the final null regardless of the buffer size.
 *
 * sq_quotef() quotes the entire formatted string as a single result.
 */

public class Quote{

	private final int quote_path_fully = 1;
	
	public static boolean need_bs_quote(char c){
		return c == '\'' || c == '!';
	}
	
	public void sq_quote_buf(StringBuilder buffer, String src) {
		String toFree = null;
		
		if(buffer.toString().equals(src)){
			toFree = buffer.toString();
			buffer.delete(0, buffer.length());
		}
		
		while(!src.isEmpty()){
			int len = src.indexOf('!');
			if(len == -1){
				len = src.indexOf('\'');
			}
			buffer.append(src.substring(0, len));
			len++;
			while(need_bs_quote(src.charAt(len))){
				buffer.append('\'');
				buffer.append(src.charAt(len++));
				buffer.append('\'');
			}
		}
	}

	public void sq_quote_argv(StringBuilder buffer, String[] argv,
			int maxLen) {
		for(int i = 0; i < argv.length; ++i){
			buffer.append(' ');
			sq_quote_buf(buffer, argv[i]);
			if(buffer.length() > maxLen){
				Usage.die("Too many or long arguements");
			}
		}
	}

	public void sq_quotef(StringBuilder buffer, String format, String... args) {
		StringBuilder src = new StringBuilder();
		src.append(String.format(format, args));
		
		sq_quote_buf(buffer, src.toString());
		
	}

	public String sq_dequote(String toDequote) {
		return sqDequoteStep(toDequote);
	}
	
	public List<String> sq_dequote_to_argv(String arg, List<String> argv) {
		return sqDequoteToArgvInternal(arg, argv);
	}

	public String[] sqDequoteToArgvArray(String arg, String[] array){
		return sqDequoteToArrayInternal(arg, array);
	}
	
	public int quoteCStyle(String name, StringBuilder buffer, File fp, boolean nodq){
		return quoteCStyleCounted(name, -1, buffer, fp, nodq);
	}
	
	public void quoteTwoCStyle(StringBuilder sb, String prefix, String path, boolean nodq){
		if(quoteCStyle(prefix, null, null, false) > 0 ||
				quoteCStyle(path, null, null, false) > 0){
			if(!nodq){
				sb.append('"');
			}
			quoteCStyle(prefix, sb, null, true);
			quoteCStyle(path, sb, null, true);
			if(!nodq){
				sb.append('"');
			}
		}else{
			sb.append(prefix);
			sb.append(path);
		}
	}
	
	public void WriteNameQuoted(String name, File fp, int terminator){
		if(terminator > 0){
			quoteCStyle(name, null, fp, false);
		}
		try(FileWriter fw = new FileWriter(fp);
				BufferedWriter bw = new BufferedWriter(fw)){
			if(terminator == 0){
				bw.append(name);
			}
			bw.append((char)terminator);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void WriteNameQuotedRelative(String name, String prefix, File fp, int terminator){
		StringBuilder sb = new StringBuilder();
		
		name = Path.RelativePath(name, prefix, sb);
		WriteNameQuoted(name, fp, terminator);
	}
	
	public String quotePathRelative(String in, String prefix, StringBuilder out){
		StringBuilder sb = new StringBuilder();
		String rel = Path.RelativePath(in, prefix, sb);
		out.setLength(0);
		quoteCStyleCounted(rel, rel.length(), out, null, false);
		
		return out.toString();
	}
	
	public boolean unquoteCStyle(StringBuilder sb, String quoted){
		int oldLen = sb.length(), len;
		int ch, ac;
		
		if(quoted.charAt(0) != '"'){
			return false;
		}
		
		for(;;){
			len = GitUtil.StrCSpn(quoted, "\"\\");
			sb.append(quoted.substring(1, len));
			String subQuoted = quoted.substring(len + 1);
			int i = 0;
			switch(subQuoted.charAt(i++)){
			case '"':
				return true;
			case '\\':
				break;
			default:
				throw new RuntimeException();
			}
			
			switch(ch = subQuoted.charAt(i++)){
			case 'b': ch = '\b'; break;
			case 'f': ch = '\f'; break;
			case 'n': ch = '\n'; break;
			case 'r': ch = '\r'; break;
			case 't': ch = '\t'; break;
			case '\\':
			case '"':
				break;
				
			case '0':
			case '1':
			case '2':
			case '3':
				ac = ((ch - '0') << 6);
				if(((ch = subQuoted.charAt(i++)) < '0' || '7' < ch)){
					sb.setLength(oldLen);
					return false;
				}
				ac |= (ch - '0') << 3;
				if(((ch = subQuoted.charAt(i++)) < '0' || '7' < ch)){
					sb.setLength(oldLen);
					return false;
				}
				ac |= (ch - '0');
				ch = ac;
				break;
			}
			sb.append((char)ch);
		}
		
	}
	
	public void PerlQuoteBuf(StringBuilder sb, String src){
		final char sq = '\'';
		final char bq = '\\';
		char c;
		
		sb.append(sq);
		int i = 0;
		while((c = src.charAt(i++)) > 0 && i < src.length()){
			if(c == sq || c == bq){
				sb.append(bq);
			}
			sb.append(c);
		}
		sb.append(sq);
	}
	
	public void PythonQuoteBuf(StringBuilder sb, String src){
		final char sq = '\'';
		final char bq = '\\';
		final char nl = '\n';
		char c;
		sb.append(sq);
		int i = 0;
		while((c = src.charAt(i++)) > 0 && i < src.length()){
			if(c == nl){
				sb.append(bq);
				sb.append('n');
				continue;
			}
			if(c == sq || c == bq){
				sb.append(bq);
			}
			sb.append(c);
		}
		sb.append(sq);
	}
	
	public void TclQuoteBuf(StringBuilder sb, String src){
		char c;
		sb.append('"');
		int i = 0;
		while((c = src.charAt(i++)) > 0 && i < src.length()){
			switch (c) {
			case '[': case ']':
			case '{': case '}':
			case '$': case '\\': case '"':
				sb.append('\\');
			default:
				sb.append(c);
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\t':
				\sb.append("\\t");
				break;
			}
		}
	}
	
	private String sqDequoteStep(String arg){
		return arg.replace("'", "");
	}
	
	private char[] sq_lookup;
	
	
	/* 1 means: quote as octal
	 * 0 means: quote as octal if (quote_path_fully)
	 * 255 means: never quote
	 * c: quote as "\\c"
	 */
	public char[] GetSqLookup(){
		if(sq_lookup == null){
			sq_lookup = new char[]{
					/*           0    1    2    3    4    5    6    7 */
					/* 0x00 */   1,   1,   1,   1,   1,   1,   1, 'a',
					/* 0x08 */ 'b', 't', 'n', 'v', 'f', 'r',   1,   1,
					/* 0x10 */ 	 1,	  1,   1,   1,   1,   1,   1,   1,
								 1,   1,   1,   1,   1,   1,   1,   1,
					/* 0x20 */  255,  255, '"',  255,  255,  255,  255,  255,
					/* 0x28 */ 255, 255, 255, 255, 255, 255, 255, 255,
								255, 255, 255, 255, 255, 255, 255, 255,
								255, 255, 255, 255, 255, 255, 255, 255,
								255, 255, 255, 255, 255, 255, 255, 255,
								255, 255, 255, 255, 255, 255, 255, 255, 
								255, 255, 255, 255, 255, 255, 255, 255,
					/* 0x58 */  255, 255, 255, 255,'\\', 255, 255, 255,
					/* 0x60 */  255, 255, 255, 255, 255, 255, 255, 255,
								255, 255, 255, 255, 255, 255, 255, 255,
								255, 255, 255, 255, 255, 255, 255, 255,
					/* 0x78 */  255, 255, 255, 255, 255, 255, 255, 255
					/* 0x80 */ /* set to 0 */
			};
			sq_lookup = Arrays.copyOf(sq_lookup, 256);
		}
		return sq_lookup;
	}
	
	private boolean sqMustQuote(char c){
		return (sq_lookup[(int)c] + quote_path_fully) > 0;
	}
	
	private int nextQuotePos(String s, int maxLen){
		int len;
		if(maxLen < 0){
			for(len = 0; len < s.length() && sqMustQuote(s.charAt(len)); len++);
		}
		else{
			for(len = 0; len < maxLen && !sqMustQuote(s.charAt(len)); len++);
		}
		return len;
	}
	
	private int quoteCStyleCounted(String name, int maxLen, StringBuilder buffer, File fp, boolean no_dq){
		int count = 0;
		if(fp == null){
			
		}
		else{
			count = quoteCStyleFile(name, maxLen, fp, no_dq);
		}
		
		return count;
	}
	
	private int quoteCStyleFile(String name, int maxLen, File fp, boolean no_dq){
		int count = 0;
		try(FileWriter fw = new FileWriter(fp);
				BufferedWriter bw = new BufferedWriter(fw)){
			int len = 0;
			String p = name;
			
			for(;;){
				int ch;
				len = nextQuotePos(p, maxLen);
				if(len == maxLen || maxLen < 0 && len >= p.length()){
					break;
				}
				
				if(!no_dq && p == name){
					bw.append('"');
					count++;
				}
				
				CharSequence sequence = p.subSequence(0, len);
				bw.append(sequence);
				count += sequence.length();
				bw.append('\\');
				count++;
				
				if(len < p.length() -1 ){
					ch = p.charAt(len);
					p = p.substring(len + 1);
					if(maxLen >= 0){
						maxLen -= len + 1;
					}
					if(sq_lookup[ch] >= ' '){
						bw.append(sq_lookup[ch]);
						count++;
					}
					else{
						bw.append((char)(((ch >> 6) & 03) + '0'));
						bw.append((char)(((ch >> 3) & 07) + '0'));
						bw.append((char)(((ch >> 0) & 07) + '0'));
						count += 3;
					}
				}
			}
			
			bw.append(p);
			count += p.length();
			if(p == name){
				return 0;
			}
			if(!no_dq){
				bw.append('"');
				count++;
			}
			
			bw.flush();
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}
	
	private int quoteCStyleBuffer(String name, int maxLen, StringBuilder buffer, boolean no_dq){
		int count = 0;
			int len = 0;
			String p = name;
			
			for(;;){
				int ch;
				len = nextQuotePos(p, maxLen);
				if(len == maxLen || maxLen < 0 && len >= p.length()){
					break;
				}
				
				if(!no_dq && p == name){
					buffer.append('"');
					count++;
				}
				
				CharSequence sequence = p.subSequence(0, len);
				buffer.append(sequence);
				count += sequence.length();
				buffer.append('\\');
				count++;
				
				if(len < p.length() -1 ){
					ch = p.charAt(len);
					p = p.substring(len + 1);
					if(maxLen >= 0){
						maxLen -= len + 1;
					}
					if(sq_lookup[ch] >= ' '){
						buffer.append(sq_lookup[ch]);
						count++;
					}
					else{
						buffer.append((char)(((ch >> 6) & 03) + '0'));
						buffer.append((char)(((ch >> 3) & 07) + '0'));
						buffer.append((char)(((ch >> 0) & 07) + '0'));
						count += 3;
					}
				}
			}
			
			buffer.append(p);
			count += p.length();
			if(p == name){
				return 0;
			}
			if(!no_dq){
				buffer.append('"');
				count++;
			}
			
		return count;
	}
	
	private List<String> sqDequoteToArgvInternal(String arg, List<String> argv){
		if(arg == null){
			return null;
		}
		argv.add(sqDequoteStep(arg));
		return argv;
	}
	
	private String[] sqDequoteToArrayInternal(String arg, String[] array){
		if(arg == null){
			return null;
		}
		String[] newArray = Arrays.copyOf(array, array.length + 1);
		newArray[array.length] = sqDequoteStep(arg);
		return newArray;
	}
	
}
