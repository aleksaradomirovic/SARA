package sara.apps.core;

import java.awt.Color;
import java.util.Collection;

import sara.ConsoleApplet;
import sara.SARA;
import sara.util.CfgMap;
import sara.util.StringTools;

public class SARAMain extends ConsoleApplet {
	public SARAMain() {
		super(null,160,40,"SARA Main");
	}

	@Override
	protected void init() {
		writeLine("SARA [Supportive Analytic Reciprocal Assistant]",null,Color.BLUE);
		writeLine("Version: "+SARA.version);
		writeLine("User "+System.getProperty("user.name")+" on "+System.getProperty("os.name")+" ["+System.getProperty("os.arch")+"]");
		setup();
		writeLine("Welcome, "+SARA.username+"!", Color.CYAN, null);
		
		openQA();
	}
	
	private void setup() {
		writeLine("Loading user data...");
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
						writeLine("Echo is: "+echo);
						break;
					}
					
					int yn = SARA.loc.affirmative(args[1]);
					if(yn > 0) echo = true;
					if(yn < 0) echo = false;
					else {
						writeErr("Unknown argument '"+args[1]+"', needs 'yes' or 'no'");
						break;
					}
					writeLine("Echo is now: "+echo);
					
					break;
				}
				case "say":
				case "echo": {
					writeLine(argct >= 2 ? args[1] : "");
					break;
				}
				case "echo.": {
					writeLine("");
					break;
				}
				case "names":
				case "name": {
					if(argct < 2) {
						writeLine("Names: "+SARA.names);
						writeLine("Preferred: "+SARA.username);
						break;
					}
					
					switch(args[1]) {
						case "new":
						case "add": {
							if(SARA.names.contains(args[2])) writeErr(args[2]+" already exists as a name!");
							else {
								SARA.names.add(args[2]);
								writeLine("Added name '"+args[2]+"'");
							}
							
							break;
						}
						case "remove": {
							if(SARA.names.contains(args[2])) {
								if(SARA.username.equals(args[2])) writeErr("Can't remove preferred name! (change preferred name first)");
								else if(SARA.names.size() < 2) writeErr("Can't remove names if you have less than 2 left!");
								else {
									SARA.names.remove(args[2]);
									writeLine("Removed name '"+args[2]+"'");
								}
							} else writeErr("No name '"+args[2]+"' to remove!");
							break;
						}
						case "favorite":
						case "preferred": {
							if(!SARA.names.contains(args[2])) SARA.names.add(args[2]);
							SARA.username = args[2];
							
							writeLine("Preferred username set to '"+args[2]+"'");
							
							break;
						}
					}
					
					break;
				}
				default: {
					writeErr("Unknown argument '"+args[0]+"'!");
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			writeErr("Too few arguments provided!");
		}
		
		openQA();
	}
}
