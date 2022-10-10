package sara.util;

import java.awt.Color;
import java.util.LinkedList;

public class StringTools {
	public static char[] sectionOf(char[] c, int offset, int len) {
		char[] r = new char[len];
		for(int i = 0; i < len; i++, offset++) r[i] = c[offset];
		return r;
	}
	
	public static int[] sectionOf(int[] c, int offset, int len) {
		int[] r = new int[len];
		for(int i = 0; i < len; i++, offset++) r[i] = c[offset];
		return r;
	}
	
	public static Color[] sectionOf(Color[] c, int offset, int len) {
		Color[] r = new Color[len];
		for(int i = 0; i < len; i++, offset++) r[i] = c[offset];
		return r;
	}
	
	public static Color[] stringOf(Color c, int len) {
		Color[] r = new Color[len];
		for(int i = 0; i < len; i++) r[i] = c;
		return r;
	}
	
	public static String[] split(char[] str, char regex) {
		LinkedList<String> out = new LinkedList<>();
		int last = 0, i;
		boolean inQuotes = false;
		for(i = 0; i < str.length; i++) {
			if(str[i] == '"') inQuotes = !inQuotes;
			else if(str[i] == regex && !inQuotes) {
				if(i-last >= 2 && str[last] == '"' && str[i-1] == '"') out.add(String.valueOf(str, last+1, i-last-2));
				else out.add(String.valueOf(str, last, i-last));
				last = i+1;
			}
		}
		if(i-last >= 2 && str[last] == '"' && str[i-1] == '"') out.add(String.valueOf(str, last+1, i-last-2));
		else out.add(String.valueOf(str, last, i-last));
//		System.out.println(out);
		return out.toArray(new String[out.size()]);
	}
	
	public static char[] toChars(int[] cps) {
		char[] r = new char[cps.length];
		for(int i = 0; i < cps.length; i++) r[i] = (char)cps[i];
		return r;
	}
	
	public static char[] cp(String s) {
		return s.toCharArray();
	}
}
