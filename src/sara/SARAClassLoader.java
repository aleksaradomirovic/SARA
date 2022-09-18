package sara;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;

class SARAClassLoader {
	
	static Collection<Class<?>> getAllApps() {
		Collection<Class<?>> r;
		getApps(r = new LinkedList<>(), "/sara/apps");
		
		return r;
	}
	
	private static void getApps(Collection<Class<?>> list, String rsrc) {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(SARAClassLoader.class.getResourceAsStream("/appletinfo.dat")));
		
		for(String line : reader.lines().toList()) {
			try {
				Class<?> nc = Class.forName(line);
				list.add(nc);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
