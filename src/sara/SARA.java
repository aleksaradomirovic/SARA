package sara;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import sara.localization.EnglishLocalization;
import sara.localization.Localization;

public class SARA {
	static {
		loc = new EnglishLocalization();
		gmt = ZoneOffset.UTC;
		
		root = new SARAMain(null);
	}
	
	public static void main(String[] args) throws IOException {
//		System.out.println(" 000| 111  222  333  444  555  666  777  AAA".length());
//		System.out.println(DateTimeFormatter.ofPattern("dd MM yyyy HH mm").parse("09 10 2022 10 57"));
	}
	
	static Map<String,Constructor<Applet>> starts;
	static ArrayList<Constructor<Applet>> apps;
	
	public static final String version = "4\u03b1.0.0";
	
	@SuppressWarnings("unchecked")
	public static Collection<Constructor<Applet>> apps() {
		return (Collection<Constructor<Applet>>) apps.clone();
	}
	
	public static final Applet root;
	public static Localization loc;
	
	public static final ZoneId gmt;
	
	static void init(Applet e) {
		new Thread(new Initter(e)).start();
	}
	
	private static class Initter implements Runnable {
		Applet toInit;
		
		private Initter(Applet e) {
			toInit = e;
		}
		
		@Override
		public void run() {
			toInit.panel.frame.setVisible(true);
			
			try { Thread.sleep(10); } // just give it a mo
			catch(InterruptedException e) {}
			while(!toInit.panel.isDisplayable()) {
				try { Thread.sleep(10); }
				catch(InterruptedException e) {} // ignore? yes
			}
			
			toInit.panel.graphicsText = toInit.panel.createVolatileImage(1, 1).createGraphics();
//			toInit.panel.frame.pack();
			
			toInit.onInit();
			toInit.refreshDisplay();
		}
	}
	
	public static void exit(int status) {
		System.exit(status);
	}
}
