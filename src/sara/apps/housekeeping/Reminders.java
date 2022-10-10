package sara.apps.housekeeping;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import sara.Applet;
import sara.SARA;
import sara.SARAIO;
import sara.Screen;
import sara.apps.core.Startup;
import sara.apps.core.util.dialog.Dialog;
import sara.gui.ScrollingFieldPane;
import sara.util.StringTools;

@Startup(commands={"reminders","remind","calendar"})
public final class Reminders extends Applet {
	private final ScrollingFieldPane list;
	private static final int w = 160, h = 40;
	public static final DateTimeFormatter dispFormat = DateTimeFormatter.ofPattern("dd LLL yyyy (EEEE) hh:mm a"),
			saveFormat = DateTimeFormatter.ofPattern("dd MM yyyy HH mm");
	private static final char[] instructions = ("[N] new event  [T] edit title  [D] edit date/time"
			+ "  [A] edit address  [S] edit description  [enter] toggle completion  [pgup/dn] navigate").toCharArray();
	private static final File save = new File(SARAIO.LOCALAPPDATA+"\\apps\\Reminders\\calendar.dat");
	
	private final ZoneId gmt = SARA.gmt, local = ZoneId.systemDefault();

	public Reminders(Applet parent, String[] args) {
		super(parent, 160, 40);
		
		rootDisplay.writeln(instructions, 0, 0);
		rootDisplay.add(list = new ScrollingFieldPane(w, h-2), 0, 2);
		load();
	}

	@Override
	protected void onInit() {
		
	}

	@Override
	protected void onUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onClose() {
		save();
	}

//	boolean editMode = true;
	@Override
	protected void keyPressed(int code, char c) {
//		if(editMode != -1) return;
		Entry focus = list.isEmpty() ? null : ((Entry)list.focus());
		switch(code) {
			case KeyEvent.VK_PAGE_UP: {
				list.moveDown(-1);
				break;
			}
			case KeyEvent.VK_PAGE_DOWN: {
				list.moveDown(1);
				break;
			}
			case KeyEvent.VK_N: {
				list.add(new Entry());
				list.sort(new EventComp());
				break;
			}
			case KeyEvent.VK_ENTER: {
				if(focus != null) focus.complete = !focus.complete;
				break;
			}
			case KeyEvent.VK_D: {
				if(focus != null) {
					focus.dateTime = Dialog.getDateTimeDialog(parent, focus.dateTime.withZoneSameInstant(local)).withZoneSameInstant(gmt);
				}
				break;
			}
			case KeyEvent.VK_T: {
				if(focus != null) {
					focus.title = Dialog.getMessageDialog(this, "Enter title:", Entry.tw-4, focus.title);
				}
				break;
			}
			case KeyEvent.VK_A: {
				if(focus != null) {
					focus.addy = Dialog.getMessageDialog(this, "Enter address:", Entry.tw, focus.addy);
				}
				break;
			}
			case KeyEvent.VK_S: {
				if(focus != null) {
					focus.desc = Dialog.getMessageDialog(this, "Enter description:", Entry.tw, focus.desc);
				}
				break;
			}
		}
		refreshDisplay();
	}
	
	@Override
	protected void refreshDisplay() {
		list.redraw();
		if(!list.isEmpty()) list.write('>', 0, 0);
		super.refreshDisplay();
	}
	
	void save() {
		StringBuilder sb = new StringBuilder();
		for(Screen i : list) {
			Entry e = (Entry)i;
			
			sb.append(e.title);
			sb.append('\u0001');
			sb.append(e.dateTime.format(saveFormat));
			sb.append('\u0001');
			sb.append(e.addy);
			sb.append('\u0001');
			sb.append(e.desc);
			sb.append('\u0001');
			sb.append(e.complete ? 1 : 0);
			sb.append('\u0001');
			sb.append(e.alert);
			sb.append('\u0001');
			sb.append('\u0000');
		}
		
		try {
			File tmp = SARAIO.getTempFile(save);
			FileOutputStream writer = new FileOutputStream(tmp);
			writer.write(SARAIO.toByteStream(sb.toString().toCharArray(), SARAIO.UTF_16_BE));
			writer.close();
			SARAIO.finalizeTempFile(tmp, save);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void load() {
		try {
			int[] in = SARAIO.asText(SARAIO.loadFile(save));
			Entry n = new Entry();
			for(int i = 0, last = 0, mode = 0; i < in.length; i++) {
				switch(in[i]) {
					case 1: {
						char[] sect = SARAIO.getChars(in, last, i-last);
						switch(mode) {
							case 0:
								n.title = sect;
								break;
							case 1:
//								System.out.println(sect);
//								System.out.println(sect.length);
//								TemporalAccessor acc = saveFormat.parse(String.valueOf(sect));
								n.dateTime = ZonedDateTime.of(LocalDateTime.parse(String.valueOf(sect), saveFormat), gmt);
								break;
							case 2:
								n.addy = sect;
								break;
							case 3:
								n.desc = sect;
								break;
							case 4:
								n.complete = sect[0] == '1';
								break;
							case 5:
								n.alert = Integer.parseInt(String.valueOf(sect));
								break;
							default: throw new IOException();
						}
						
						mode++;
						last = i+1;
						break;
					}
					case 0: {
						list.add(n);
						n = new Entry();
						mode = 0;
						last = i+1;
						break;
					}
				}
			}
			
		} catch (IOException e) {
			if(!(e instanceof FileNotFoundException)) {
				e.printStackTrace();
				save.delete();
			}
		}
	}
	
	private class Entry extends Screen {
		private int h = 0;
		
		private static final char[] newTitle = StringTools.cp("New Event"), empty = new char[0];
		private static final int tw = Reminders.w - 2;
		
		private char[] title, addy, desc;
		private ZonedDateTime dateTime;
		private boolean complete;
		private int alert;
		
		public Entry() {
			super(Reminders.w, 5);
			/*
			 * [X] title
			 * date
			 * loc
			 * desc
			 * // spacer
			 */
			
			complete = false;
			alert = 0;
			dateTime = safeAhead();
			title = newTitle;
			addy = empty;
			desc = empty;
		}
		
		private ZonedDateTime safeAhead() {
			return ZonedDateTime.now(gmt).plusDays(1);
		}
		
		private char[] dtText() {
			return StringTools.cp(dateTime.withZoneSameInstant(local).format(dispFormat));
		}
		
		@Override
		public int height() {
			return h;
		}
		
		@Override
		public void redraw() {
			clear();
			h = 3;
			
			write('[', 0, 2);
			write(complete ? 'X' : ' ', 0, 3);
			write(']', 0, 4);
			writeln(title, 0, 6, tw-4);
			writeln(dtText(), 1, 2, tw);
			if(addy.length > 0) {
				writeln(addy, h-1, 2, tw);
				h++;
			}
			if(desc.length > 0) {
				writeln(desc, h-1, 2, tw);
				h++;
			}
		}
	}
	
	public static class EventComp implements Comparator<Screen> {
		@Override
		public int compare(Screen o1, Screen o2) {
			return compare((Entry)o1,(Entry)o2);
		}
		
		public int compare(Entry o1, Entry o2) {
			return o1.dateTime.compareTo(o2.dateTime);
		}
	}

}
