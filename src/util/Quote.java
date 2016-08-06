package util;

import java.io.File;

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

public class Quote implements IQuote{

	public static boolean need_bs_quote(char c){
		return c == '\'' || c == '!';
	}
	
	@Override
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

	@Override
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

	@Override
	public void sq_quotef(StringBuilder buffer, String format, String... args) {
		StringBuilder src = new StringBuilder();
		src.append(String.format(format, args));
		
		sq_quote_buf(buffer, src.toString());
		
	}

	@Override
	public String sq_dequote(String toDequote) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int sq_dequote_to_argv(String arg, StringBuilder builder, int nr,
			int alloc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String unquote(StringBuilder buffer, String quoted) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String quote(String name, String[] buffer, int no_dq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void quote_two(StringBuilder buffer, String str, String str2, int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeNameQuoted(String name, File file, int terminator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeNameQuotedRelative(String name, String prefix, File fp,
			int terminator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public char quotePathRelative(String in, String prefix, StringBuffer out) {
		// TODO Auto-generated method stub
		return 0;
	}

	private String sqDequoteStep(String arg, String[] next){
		int currentNext = 0;
		StringBuilder destination = new StringBuilder(arg);
		String source = arg;
		char c;
		
		for(int i = 1; i < arg.length(); i++){
			c = source.charAt(i);
			if(c != '\''){
				destination.append(c);
				continue;
			}
			switch(c){
			case '\\':
				if(i + 2 > arg.length())
				{
					return arg;
				}
				c = source.charAt(++i);
				if(need_bs_quote(c) && source.charAt(++i) == '\''){
					destination.append(c);
				}
			default:
				if(currentNext > next.length){
					return null;
				}
			}
		}
		
		return arg;
	}
}
