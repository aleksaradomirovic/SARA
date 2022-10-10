package sara;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

import sara.SARAIO.FileExtensionFilter;
import sara.apps.core.Startup;

public class SARAAppLoader {
	public static final File moduleLocation;
	
	static {
		moduleLocation = new File(SARAIO.LOCALAPPDATA+"\\modules");
		moduleLocation.mkdirs();
	}
	
	static void init() {
		SARA.starts = new HashMap<>();
		SARA.apps = new ArrayList<>(200);
		
		getModule();
		
		File[] apps, jars;
		
		apps = moduleLocation.listFiles(new FileExtensionFilter("apps"));
		jars = moduleLocation.listFiles(new FileExtensionFilter("jar"));
		
//		System.out.println(Arrays.toString(apps));
//		System.out.println(Arrays.toString(jars));
		
		URL[] jarURLs = new URL[jars.length];
		for(int i = 0; i < jars.length; i++) {
			try {
				jarURLs[i] = jars[i].toURI().toURL();
//				System.out.println(jarURLs[i]);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		System.out.println(Arrays.toString(jarURLs));
		
		URLClassLoader loader = URLClassLoader.newInstance(jarURLs, SARAAppLoader.class.getClassLoader());
		
		for(File appInfo : apps) {
			try {
				getModule(loader, new FileInputStream(appInfo));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		try {
			loader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		System.out.println(SARA.apps);
	}
	
	private static void getModule(ClassLoader l, InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		for(String line : reader.lines().toList()) {
			validateClass(l,line);
		}
	}
	
	private static void getModule() {
		getModule(SARAAppLoader.class.getClassLoader(), SARAAppLoader.class.getResourceAsStream("/applist.txt"));
	}
	
	private static final Class<?> appletClass = Applet.class;
	
	private static void validateClass(ClassLoader l, String path) {
		try {
			Class<?> tst = Class.forName(path, true, l);
			
//			System.out.print(tst+" : ");
//			System.out.println(tst.getSuperclass());
//			System.out.println(Arrays.toString(tst.getConstructors()));
			
			if(!appletClass.isAssignableFrom(tst)) {
				System.err.println(tst+" is not a subclass of "+appletClass+", aborting assignment...");
				return;
			}
			
			@SuppressWarnings("unchecked")
			Class<Applet> cls = (Class<Applet>) tst;
			
			Constructor<Applet> constructor = cls.getConstructor(Applet.class, String[].class);
			SARA.apps.add(constructor);
			
			Startup commands = cls.getDeclaredAnnotation(Startup.class);
			
//			System.out.println(cls);
			if(commands == null) {
//				System.err.println(cls+" has no startup commands!");
				return;
			}
			
			for(String com : commands.commands()) {
				if(SARA.starts.put(com, constructor) != null) System.err.println("Warning - duplicate command '"+com+"'!");
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
