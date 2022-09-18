package sara;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

import sara.util.CfgMap;
import sara.util.StringTools;

public class SARAMain extends ConsoleApplet {
	public SARAMain() {
		super(null,160,40,"SARA Main");
	}

	@Override
	protected void init() {
		writeln("SARA [Supportive Analytic Reciprocal Assistant]",null,Color.BLUE);
		writeln("Version: "+SARA.version);
		writeln("User "+System.getProperty("user.name")+" on "+System.getProperty("os.name")+" ["+System.getProperty("os.arch")+"]");
		setup();
		writeln("Welcome, "+SARA.username+"!", Color.CYAN, null);
		
		openQA();
	}
	
	private void setup() {
		writeln("Loading user data...");
		CfgMap cfg = new CfgMap(new java.io.File("userdata.cfg"));
		SARA.userdata = cfg;
		Collection<?> list;
		
		if((list = cfg.getAsList("names")) == null) {
			writeWarn("No names found, adding system username...");
			SARA.names.add(System.getProperty("user.name"));
		} else {
			for(Object o : list) {
				if(o instanceof String) SARA.names.add((String)o);
			}
		}
		
		if((SARA.username = (String)cfg.get("username")) == null) {
			SARA.username = SARA.names.get(0);
		}
	}

	@Override
	protected void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onClose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleCommand(char[] command) {
		final String[] args = StringTools.toArgs(command);
		final int argct = args.length;
		
		try {
			switch(args[0]) {
				case "@echo": {
					if(argct < 2) {
						writeln("Echo is: "+echo);
						break;
					}
					
					int yn = SARA.loc.affirmative(args[1]);
					if(yn > 0) echo = true;
					if(yn < 0) echo = false;
					else {
						writeErr("Unknown argument '"+args[1]+"', needs 'yes' or 'no'");
						break;
					}
					writeln("Echo is now: "+echo);
					
					break;
				}
				case "say":
				case "echo": {
					writeln(argct >= 2 ? args[1] : "");
					break;
				}
				case "echo.": {
					writeln("");
					break;
				}
				case "names":
				case "name": {
					if(argct < 2) {
						writeln("Names: "+SARA.names);
						writeln("Preferred: "+SARA.username);
						break;
					}
					
					switch(args[1]) {
						case "new":
						case "add": {
							if(SARA.names.contains(args[2])) writeErr(args[2]+" already exists as a name!");
							else {
								SARA.names.add(args[2]);
								writeln("Added name '"+args[2]+"'");
							}
							
							break;
						}
						case "remove": {
							if(SARA.names.contains(args[2])) {
								if(SARA.username.equals(args[2])) writeErr("Can't remove preferred name! (change preferred name first)");
								else if(SARA.names.size() < 2) writeErr("Can't remove names if you have less than 2 left!");
								else {
									SARA.names.remove(args[2]);
									writeln("Removed name '"+args[2]+"'");
								}
							} else writeErr("No name '"+args[2]+"' to remove!");
							break;
						}
						case "favorite":
						case "preferred": {
							if(!SARA.names.contains(args[2])) SARA.names.add(args[2]);
							SARA.username = args[2];
							
							writeln("Preferred username set to '"+args[2]+"'");
							
							break;
						}
					}
					
					break;
				}
				case "run": {
					if(SARA.runApplet(args[1], Arrays.copyOfRange(args, 2, argct))) writeln("Opening app '"+args[1]+"'...");
					else writeErr("No such app '"+args[1]+"'!");
					break;
				}
				default: {
					if(SARA.runApplet(args[0], Arrays.copyOfRange(args, 1, argct))) writeln("Opening app '"+args[0]+"'...");
					else writeErr("Unknown argument '"+args[0]+"'!");
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			writeErr("Too few arguments provided!");
		}
		
		openQA();
	}
}
