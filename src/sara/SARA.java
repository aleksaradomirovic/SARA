package sara;

import java.awt.Canvas;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import sara.annotations.Independent;
import sara.annotations.SaveDir;
import sara.localization.Localization;
import sara.util.CfgMap;

public class SARA {
	static final Canvas drawTester = new Canvas();
	static Applet root;
	static final Collection<Applet> allApps = new LinkedList<>();
	public static final String version = "3\u03b1.1.2";
	public static Localization loc;
	
	public static CfgMap userdata;
	public static List<String> names = new LinkedList<>();
	public static String username;
	
	public static void main(String[] args) throws IOException {
		loc = new sara.localization.EnglishLocalization();
		
		new SARAMain();
		
		loadAllApps();
	}
	
	private static HashMap<String, Constructor<?>> appShortcuts;
	private static HashMap<File, Class<?>> fileAssignments;
	private static void loadAllApps() {
		appShortcuts = new HashMap<>();
		fileAssignments = new HashMap<>();
		
		Independent assignment; SaveDir save;
		for(Class<?> c : SARAClassLoader.getAllApps()) {
//			System.out.println(c);
			if((assignment = c.getAnnotation(Independent.class)) != null) {
				try {
					Constructor<?> constructor = c.getConstructor(new Class<?>[] { Applet.class, String[].class });
					for(String name : assignment.names()) {
						if(appShortcuts.get(name) != null) System.err.println("Duplicate shortcuts for "+appShortcuts.get(name).getDeclaringClass()+" and "+c+"!");
						appShortcuts.put(name, constructor);
						System.out.println("Assigned shortcut '"+name+"' to "+c);
					}
				} catch(NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
			
			if((save = c.getAnnotation(SaveDir.class)) != null) {
				for(String s : save.paths()) {
					File f = SARAIO.makeAbsolute(new File(s));
					if(fileAssignments.get(f) != null) System.err.println("Both "+fileAssignments.get(f)+" and "+c+" are saving to '"+f+"'!");
					fileAssignments.put(f, c);
					System.out.println("Assigned file/directory '"+f+"' to "+c);
				}
			}
		}
	}
	
	static boolean runApplet(String name, String[] args) {
		Constructor<?> constructor = appShortcuts.get(name);
		if(constructor == null) return false;
		try {
			Object obj = constructor.newInstance(new Object[] { root, args });
			if(!(obj instanceof Applet)) throw new InstantiationException("Object "+obj+" is not an instance of sara.Applet!");
			return true;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static void init(Applet app) {
		if(app.initted) throw new RuntimeException("Applet was already initted!");
		
		new Thread(new Initter(app)).start();
	}
	
	private static class Initter implements Runnable {
		Applet app;
		
		Initter(Applet app) {
			this.app = app;
		}
		
		@Override
		public void run() {
			app.setVisible(true);
			
			while(!app.window.isDisplayable()) {
				try { Thread.sleep(10); } // wait until window is displayable
				catch(InterruptedException e) {}
			}
			
			app.init();
			allApps.add(app);
			app.initted = true;
		}
	}
	
	static void exit(int status) {
		try {
			root.close();
		} catch (IllegalStateException e) {}
		
		// SAVE USERCFG
		userdata.put("names", names);
		userdata.put("username", username);
		
		userdata.save();
		
		System.exit(status);
	}
}
