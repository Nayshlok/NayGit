package util;

import java.io.File;

public interface IQuote {

	public void sq_quote_buf(StringBuilder buffer, String src);
	public void sq_quote_argv(StringBuilder buffer, String[] argv, int maxLen);
	public void sq_quotef(StringBuilder buffer, String format, String... args);
	
	public String sq_dequote(String toDequote);
	public int sq_dequote_to_argv(String arg, StringBuilder builder, int nr, int alloc);
	
	public String unquote(StringBuilder buffer, String quoted);
	public String quote(String name, String[] buffer, int no_dq);
	public void quote_two(StringBuilder buffer, String str, String str2, int i);
	
	public void writeNameQuoted(String name, File file, int terminator);
	public void writeNameQuotedRelative(String name, String prefix, File fp, int terminator);
	
	public char quotePathRelative(String in, String prefix, StringBuffer out);
	
	
}
