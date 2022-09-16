package sara.apps.core.user;

import java.awt.Color;
import java.io.File;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import sara.Applet;
import sara.ConsoleApplet;
import sara.SARAIO;
import sara.annotations.Independent;
import sara.annotations.SaveDir;
import sara.util.CfgMap;
import sara.util.StringTools;

@Independent(names={"reminders","remind","reminder"})
@SaveDir(paths={"user/reminders.cfg"})
public class Reminders extends ConsoleApplet {
	private static final File save = SARAIO.makeAbsolute(new File("user/reminders.cfg"));
	final ZoneId systemZone;
	static final ZoneId GMT = ZoneId.of("GMT");
	private final CfgMap cfg;

	public Reminders(Applet parent, String[] args) {
		super(parent, "Reminders");
		fatlock = true;
		systemZone = ZoneId.systemDefault();
		cfg = new CfgMap(save);
		reminders = new HashMap<>();
		echo = false;
	}
	private HashMap<String,Reminder> reminders;

	@Override
	protected void init() {
		for(String s : cfg.propertySet()) {
			Reminder r = new Reminder(s);
			List<?> properties = cfg.getAsList(s);
			r.date = ZonedDateTime.of(LocalDateTime.parse((String)properties.get(0),Reminder.saveFormat), GMT).withZoneSameInstant(systemZone);
			r.alert = ((Double)properties.get(1)).intValue();
			r.description = (String)properties.get(2);
			
			if(properties.size() > 3) {
				ListIterator<?> iter = properties.listIterator(3);
				while(iter.hasNext()) {
					r.notes.add((String) iter.next());
				}
			}
			
			reminders.put(s, r);
		}
		
		display();
		openQA();
	}

	@Override
	protected void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onClose() {
		cfg.clear();
		
		LinkedList<Object> in;
		for(Reminder r : reminders.values()) {
			in = new LinkedList<>();
			in.add(r.date.withZoneSameInstant(GMT).format(Reminder.saveFormat));
			in.add(r.alert);
			in.add(r.description);
			for(String s : r.notes) {
				in.add(s);
			}
			cfg.put(r.name, in);
		}
		
		if(edit) cfg.save();
	}
	
	private void display() {
		clear();
		List<Reminder> rems = new LinkedList<>(reminders.values());
		Collections.sort(rems);
		
		for(Reminder r : rems) {
			writeLine(r.name+" - "+r.date.format(Reminder.format), Color.BLACK, ReminderHandler.alert(r.alert));
			writeLine(r.description);
			for(String s : r.notes) {
				writeLine(" - "+s);
			}
			writeLine("");
		}
	}
	
	boolean edit = false;

	@Override
	protected void handleCommand(char[] command) {
		final String[] args = StringTools.toArgs(command);
		final int argct = args.length;
		
		try {
			switch(args[0]) {
				case "add":
				case "edit":
				case "new": {
					String text = String.join(" ", Arrays.copyOfRange(args, 1, argct));
					new ReminderHandler(this, getReminder(text));
					edit = true;
					
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
	
	private Reminder getReminder(String name) {
		Reminder r = reminders.get(name);
		return r != null ? r : new Reminder(name);
	}
	
	private class ReminderHandler extends ConsoleApplet {
		Reminder reminder;
		final String oldName;
		public ReminderHandler(Reminders parent, Reminder edit) {
			super(parent,80,30, "Editing Reminder: "+edit.name);
			greedy = true;
			fatlock = true;
			echo = false;
			oldName = edit.name;
			err = "";
			this.reminder = edit;
		}
		
		private void display() {
			clear();
			writeLine("Name:        "+reminder.name);
			writeLine("Date/Time:   "+reminder.date.format(Reminder.format));
			writeLine("\t            ["+reminder.date.withZoneSameInstant(GMT).format(Reminder.formatRigid)+"]");
			writeLine("Description: "+reminder.description);
			writeLine("Alert:       "+reminder.alert, Color.BLACK, alert(reminder.alert));
			writeLine("Notes:    "+(!reminder.notes.isEmpty() ? "1. "+reminder.notes.get(0) : ""));
			if(reminder.notes.size() > 1) {
				ListIterator<String> iter = reminder.notes.listIterator(1);
				int i = 2;
				while(iter.hasNext()) {
					writeLine("\t         "+(i++)+". "+iter.next());
				}
			}
			
			writeLine(err, Color.RED, null);
		}
		
		private static Color alert(int i) {
			switch(i) {
				case 0: return Color.GREEN;
				case 1: return Color.YELLOW;
				case 2: return Color.ORANGE;
				case 3: return Color.RED;
				default: throw new IllegalArgumentException();
			}
		}

		@Override
		protected void init() {
			display();
			openQA(1024);
		}

		@Override
		protected void update() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onClose() {
			reminders.remove(oldName);
			reminders.put(reminder.name, reminder);
			
			Reminders.this.display();
		}
		
		private String err;

		@Override
		protected void handleCommand(char[] command) {
			final String[] args = StringTools.toArgs(command);
			final int argct = args.length;
			err = "";
			
			try {
				switch(args[0]) {
					case "name": {
						String text = String.join(" ", Arrays.copyOfRange(args, 1, argct));
						if(!reminders.containsKey(text)) {
							if(args[1].indexOf('=') == -1) {
								if(!reminders.keySet().contains(text))
									reminder.name = text;
								else
									err = "Reminder of this name already exists!";
							} else {
								err = "Can't use equals sign '=' in a name field!"; 
							}
						} else {
							err = "Name '"+text+"' already exists!"; 
						}
						break;
					}
					case "desc":
					case "description":
					case "text": {
						String text = String.join(" ", Arrays.copyOfRange(args, 1, argct));
						if(text.indexOf('"') == -1) {
							reminder.description = text;
						} else {
							err = "Can't use double quotes '\"' in a text field!"; 
						}
						break;
					}
					case "date": {
						int year, month, day, hour = 0, min = 0;
						
						try {
							if(argct == 3) {
								reminder.date = ZonedDateTime.of(LocalDateTime.parse(args[1]+' '+args[2], Reminder.commandFormat), systemZone);
							} else {
								year = Integer.parseInt(args[1]);
								month = Integer.parseInt(args[2]);
								day = Integer.parseInt(args[3]);
								if(argct > 4) hour = Integer.parseInt(args[4]);
								if(argct > 5) min = Integer.parseInt(args[5]);
								reminder.date = ZonedDateTime.of(LocalDateTime.of(year, month, day, hour, min), systemZone);
							}
							if(reminder.date.isBefore(ZonedDateTime.now())) {
								reminder.date = reminder.safeTimeAhead();
								err = "Date/time has already occurred!";
							}
						} catch(NumberFormatException | DateTimeException e) {
							err = "Date formatting error! Command should be given as:\n- "+args[0]+" [year] [month] [day] [hour?] [minute?], or\n- YYYY-MM-DD hh:mm";
						}
						break;
					}
					case "notes":
					case "note": {
						switch(args[1]) {
							case "clear": {
								reminder.notes.clear();
								break;
							}
							
							case "new":
							case "add": {
								reminder.notes.add(String.join(" ", Arrays.copyOfRange(args, 2, argct)));
								break;
							}
							
							case "remove": {
								try {
									reminder.notes.remove(Integer.parseInt(args[2])-1);
								} catch(NumberFormatException e) {
									err = "Not a number!";
								} catch(IndexOutOfBoundsException e) {}
								
								break;
							}
							
							case "insert": {
								try {
									reminder.notes.add(Integer.parseInt(args[2])-1, String.join(" ", Arrays.copyOfRange(args, 3, argct)));
								} catch(NumberFormatException e) {
									err = "Not a number!";
								} catch(IndexOutOfBoundsException e) {}
								
								break;
							}
							
							case "set": {
								try {
									reminder.notes.set(Integer.parseInt(args[2])-1, String.join(" ", Arrays.copyOfRange(args, 3, argct)));
								} catch(NumberFormatException e) {
									err = "Not a number!";
								} catch(IndexOutOfBoundsException e) {}
								
								break;
							}
						}
						
						break;
					}
					case "alert": {
						switch(args[1].toLowerCase()) {
							case "0":
							case "none": {
								reminder.alert = 0;
								break;
							}
							case "1":
							case "low": {
								reminder.alert = 1;
								break;
							}
							case "2":
							case "medium": {
								reminder.alert = 2;
								break;
							}
							case "3":
							case "hi":
							case "high": {
								reminder.alert = 3;
								break;
							}
							default: {
								err = "No such alert argument!";
								break;
							}
						}
						break;
					}
					default: {
						err = "Unknown argument '"+args[0]+"'!";
					}
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				err = "Too few arguments provided!";
			}
			
			display();
			openQA();
		}
	}
	
	private class Reminder implements Comparable<Reminder> {
		private static final DateTimeFormatter 
				format = DateTimeFormatter.ofPattern("uuuu-MM-dd (EEEE) hh:mma v"),
				formatRigid = DateTimeFormatter.ofPattern("uuuu-MM-dd (EEEE) HH:mm v"),
				saveFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd-HH-mm"),
				commandFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd [HH:mm]");
		
		String name, description = "";
		ZonedDateTime date;
		int alert = 0; // 0-3
		LinkedList<String> notes = new LinkedList<>();
		
		Reminder(String n) {
			name = n;
			date = safeTimeAhead();
		}
		
		private ZonedDateTime safeTimeAhead() {
			return ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0), systemZone);
		}
		
		String toSave() {
			return String.join("\",\"", new String[] { name, date.format(format) });
		}

		@Override
		public int compareTo(Reminder o) {
			return date.compareTo(o.date);
		}
	}

}