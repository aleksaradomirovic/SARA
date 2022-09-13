package sara.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import sara.SARAIO;

public class CfgMap {
	private final HashMap<String,Object> map;
	public final File file;
	public CfgMap(File f) {
		file = f;
		List<char[]> lines;
		map = new HashMap<>();
		try {
			lines = StringTools.split(SARAIO.loadFileAsText(f), '\n');
		} catch (IOException e) {
			return;
		}
		
		for(char[] line : lines) {
			int eq = StringTools.indexOf(line, '=');
			if(eq == -1) continue;
			
			String key = String.valueOf(line,0,eq).trim();
			if(map.put(key, cfgToObject(StringTools.sectionOf(line, eq+1))) != null) System.err.println("Property '"+key+"' in '"+file+"' is double-declared!");
		}
//		System.out.println(map);
	}
	
	public Object get(String key) {
		return map.get(key);
	}
	
	public Object put(String key, Object o) {
		return map.put(key, o);
	}
	
	public Collection<?> getAsList(String key) {
		Object obj = map.get(key);
		if(obj == null || !(obj instanceof Collection)) return null;
		return (Collection<?>)obj;
	}
	
	public boolean save() {
		char[][] out = new char[map.size()][];
		int i = 0;
		for(Entry<String, Object> entry : map.entrySet()) {
			String str = entry.getKey()+"="+objectToCfg(entry.getValue());
			out[i] = str.toCharArray();
			i++;
		}
		try {
			SARAIO.saveText(file, out);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static Object cfgToObject(char[] cfg) {
		cfg = StringTools.trim(cfg);
		
		if(cfg.length >= 2) {
			if(cfg[0] == '"' && cfg[cfg.length-1] == '"') return String.valueOf(cfg,1,cfg.length-2);
			if(cfg[0] == '[' && cfg[cfg.length-1] == ']') {
				LinkedList<Object> r = new LinkedList<>();
				if(cfg.length == 2) return r;
				
				for(char[] c : StringTools.split(StringTools.sectionOf(cfg, 1, cfg.length-2), ',')) {
					c = StringTools.trim(c);
					r.add(cfgToObject(c));
				}
				return r;
			}
		}
		
		String str = String.valueOf(cfg);
		try { return Double.parseDouble(str); } catch(NumberFormatException e) {}
		return str;
	}
	
	private static String objectToCfg(Object obj) {
		String out;
		if(obj instanceof Collection) {
			StringBuilder sb = new StringBuilder("[");
			for(Object o : (Collection<?>)obj) {
				sb.append(objectToCfg(o));
				sb.append(',');
			}
			sb.setCharAt(sb.length()-1, ']');
			out = sb.toString();
		} else if(obj instanceof String) {
			out = '"'+((String)obj)+'"';
		} else out = obj.toString();
		return out;
	}
}
