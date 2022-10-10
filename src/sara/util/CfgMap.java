package sara.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import sara.SARAIO;

public class CfgMap {
	public File saveFile;
	private final Map<String,Object> props;
	
	public CfgMap(File f) {
		saveFile = f;
		props = new HashMap<>();
		reload();
	}
	
	public void reload() {
		props.clear();
		
		int[] file;
		try { file = SARAIO.asText(SARAIO.loadFile(saveFile)); }
		catch(IOException e) { return; }
		
		int mode = 0;
		int last = 0;
		char[] key = null;
		for(int i = 0; i < file.length; i++) {
			switch(mode) {
				case 0:
					if(file[i] == '=') {
						mode = 1;
						last = i+1;
						key = SARAIO.getChars(file, last, i-last);
					}
					break;
				case 1:
					if(file[i] == 0) {
						props.put(String.valueOf(key).toLowerCase(), strToCfgObj(SARAIO.getChars(file, last, i)));
						mode = 0;
						last = i+1;
					}
					break;
			}
		}
		
		System.out.println(props);
	}
	
	private static Object strToCfgObj(char[] x) {
		switch(x[0]) {
			case '[':
				LinkedList<Object> r = new LinkedList<>();
				if(x.length <= 1) return r;
				int last = 0, i;
				for(i = 1; i < x.length; i++) {
					if(x[i] == 1) {
						r.add(strToCfgObj(StringTools.sectionOf(x, last, i-last)));
						last = i+1;
					}
				}
				r.add(strToCfgObj(StringTools.sectionOf(x, last, i-last)));
				return r;
			case '#':
				return Double.parseDouble(String.valueOf(x,1,x.length-1));
			case '"':
				return String.valueOf(x,1,x.length-1);
		}
		
		return String.valueOf(x);
	}
	
	private static String cfgObjToString(Object o) {
		if(o instanceof Number) {
			return "#"+((Number)o).doubleValue();
		} else if(o instanceof java.util.Collection) {
			StringBuilder sb = new StringBuilder("[");
			for(Object obj : (java.util.Collection<?>)o) {
				sb.append(cfgObjToString(obj)+'\u0001');
			}
			if(sb.length() > 1) return sb.substring(0, sb.length()-1);
			return sb.toString();
		}
		
		return "\""+o.toString();
	}
	
	public void save() throws IOException {
		StringBuilder sb = new StringBuilder();
		for(Entry<String,Object> e : props.entrySet()) {
			sb.append(e.getKey());
			sb.append('=');
			sb.append(cfgObjToString(e.getValue()));
			sb.append('\u0000');
		}
		SARAIO.saveToFile(SARAIO.toByteStream(sb.toString().toCharArray(), SARAIO.UTF_16_BE), saveFile);
	}
}
