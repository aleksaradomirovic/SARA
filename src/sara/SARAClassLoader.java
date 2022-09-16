package sara;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;

class SARAClassLoader {
	static Collection<Class<?>> getClassesInHierarchy(String pkg) {
		pkg.replace('.', '/');
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				ClassLoader.getSystemClassLoader().getResourceAsStream(pkg.replace('.', '/'))));
		
		LinkedList<Class<?>> classes = new LinkedList<>();
		for(String line : reader.lines().toList()) {
			if(line.indexOf('.') != -1) {
				if(line.endsWith(".class")) {
					classes.add(getClass(pkg, line));
				}
			} else {
				classes.addAll(getClassesInHierarchy(pkg+'.'+line));
			}
		}
		return classes;
	}
	
	private static Class<?> getClass(String pkg, String cls) {
		try {
			return Class.forName(pkg+'.'+cls.substring(0, cls.lastIndexOf('.')));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static Collection<Class<?>> getAllApps() {
		LinkedList<Class<?>> classes = new LinkedList<>();
		
		for(Package pkg : ClassLoader.getSystemClassLoader().getDefinedPackages()) {
			if(pkg.getName().indexOf('.') == -1) {
				for(Class<?> cls : getClassesInHierarchy(pkg.getName()+".apps")) {
					if(Applet.class.isAssignableFrom(cls)) classes.add(cls);
				}
			}
		}
		
		return classes;
	}
}
