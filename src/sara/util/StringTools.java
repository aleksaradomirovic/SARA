package sara.util;

import java.util.LinkedList;
import java.util.List;

public class StringTools {
	public static List<char[]> softWrap(char[] str, final int width) {
		LinkedList<char[]> r = new LinkedList<>();
		
		int len = 0, breakline = -1, idx = 0;
		for(;idx < str.length; idx++, len++) {
			if(len == width) { // shouldn't ever be greater
				breakline = breakline != -1 ? breakline : idx;
				r.add(sectionOf(str, idx-len, len-(idx-breakline)));
				len = idx-breakline;
				if(len > 0) len--;
				breakline = -1;
			}
			
			switch(str[idx]) {
				case ' ': {
					if(len <= 0) {
						len--;
					} else {
						breakline = idx;
					}
					
					break;
				}
				case '\n': {
					r.add(sectionOf(str, idx-len, len));
					len = -1;
					breakline = -1;
					
					break;
				}
			}
		}
		if(len > 0 || str.length == 0 || str[idx-1] == '\n') {
			r.add(sectionOf(str, idx-len, len));
		}
		
		return r;
	}
	
	public static List<char[]> split(char[] c, final char regex) {
		LinkedList<char[]> r = new LinkedList<>();
		boolean inQuotes = false;
		int last = 0, len = 0;
		for(int i = 0; i < c.length; i++, len++) {
			if(c[i] == '"') {
				inQuotes = !inQuotes;
			} else if(c[i] == regex && !inQuotes) {
				r.add(sectionOf(c, last, len));
				last=i+1;
				len = -1;
			}
		}
		r.add(sectionOf(c, last, len));
		return r;
	}
	
	public static String[] toArgs(char[] c) {
		List<char[]> raw = split(c, ' ');
		String[] r = new String[raw.size()];
		int i = 0;
		for(char[] str : raw) r[i++] = String.valueOf(str);
		return r;
	}
	
	public static char[] sectionOf(final char[] original, int offset, int length) {
		char[] r = new char[length];
		for(int i = 0; i < length; i++, offset++) {
			r[i] = original[offset];
		}
		return r;
	}
	
	public static char[] sectionOf(final char[] original, int offset) {
		return sectionOf(original,offset,original.length-offset);
	}
	
	public static char[] trim(char[] str) {
		int st = 0, len = str.length;
		for(;str[st] <= 0x20; st++, len--);
		for(;str[st+len-1] <= 0x20; len--);
//		if(len == str.length) return str; // maybe for optimization?
		return sectionOf(str, st, len);
	}
	
	public static int indexOf(char[] str, char regex) {
		for(int i = 0; i < str.length; i++) {
			if(str[i] == regex) return i;
		}
		return -1;
	}
	
	public static boolean isPrintableKey(int code) {
		return (code > 0x28 || code == 0x20);
	}
}
