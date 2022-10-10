package sara;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

import sara.util.StringTools;

public class SARAMain extends ConsoleApplet {
	public SARAMain(Applet parent) {
		super(parent,160,40);
	}

	@Override
	protected void onInit() {
		writeln("SARA [Supportive Analytic Reciprocal Analog]", null, Color.BLUE);
		writeln("v"+SARA.version, null, Color.BLUE);
		
		SARAAppLoader.init();
		printSysInfo();
//		System.out.println(SARA.starts);
		
		input.open();
	}
	
	private void printSysInfo() {
		writeln("Environment:");
		writeln("Operating System  = "+System.getProperty("os.name")+" v"+System.getProperty("os.version")+" ["+System.getProperty("os.arch")+"]");
		writeln("Local user        = \""+System.getProperty("user.name")+"\"");
		Runtime rt = Runtime.getRuntime();
		writeln("Max memory        = "+(rt.totalMemory()/1048576)+" MiB");
		writeln("Free memory       = "+(rt.freeMemory()/1048576)+" MiB");
		writeln("Available threads = "+rt.availableProcessors()+"");
	}
	
	private void printAppInfo() {
		writeln("SARA info:");
		writeln("Open app count = "+countTree());
	}

	@Override
	protected void onUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onClose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleInput(char[] str) {
		String[] args = StringTools.split(str, ' ');
		int argct = args.length;
		
		try {
			switch(args[0]) {
				case "say":
				case "echo": {
					writeln(argct > 1 ? args[1] : "");
					break;
				}
				case "@echo": {
					int ans = SARA.loc.affirmative(args[1]);
					if(ans == 0) {
						writeln("'"+args[1]+"' is neither an affirmative nor negative command!",Color.red, null);
						break;
					}
					echo = ans > 0;
					writeln("Echo is now "+echo+"!");
					break;
				}
				case "info": {
					switch(args[1]) {
						case "system":
							printSysInfo();
							break;
						case "app":
						case "sara":
							printAppInfo();
							break;
					}
					break;
				}
				default: {
					if(SARA.starts.containsKey(args[0])) {
						writeln("Starting '"+args[0]+"'...");
						try {
							SARA.starts.get(args[0]).newInstance(this, args);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException e) {
							writeln("Failed to start '"+args[0]+"'!",Color.red, null);
							e.printStackTrace();
						}
					} else writeln("Unknown command '"+args[0]+"'!",Color.red, null);
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			writeln("Too few arguments provided!",Color.red, null);
		}
		
		input.open();
	}
}
