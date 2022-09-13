package sara;

import java.awt.Canvas;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import sara.apps.core.SARAMain;
import sara.localization.Localization;
import sara.util.CfgMap;

public class SARA {
	static final Canvas drawTester = new Canvas();
	static Applet root;
	static final Collection<Applet> allApps = new LinkedList<>();
	public static final String version = "3\u03b1.0.0";
	public static Localization loc;
	
	public static CfgMap userdata;
	public static List<String> names = new LinkedList<>();
	public static String username;
	
	public static void main(String[] args) throws IOException {
		loc = new sara.localization.EnglishLocalization();
		
//		byte[] bytes = SARAIO.toBE("bruhsdq 3142420 ][][]".toCharArray());
//		for(byte b : bytes) {
//			System.out.println(b);
//		}
//		for(char c : SARAIO.toCharField(bytes)) {
//			System.out.println(c);
//		}
		
		new SARAMain();
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
