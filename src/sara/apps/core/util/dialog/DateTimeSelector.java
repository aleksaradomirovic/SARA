package sara.apps.core.util.dialog;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import sara.Applet;
import sara.util.StringTools;

/*
 *    << MMM  YYYY >>
 *  M  T  W  T  F  S  S 
 *  		    	  1
 *             		  8
 *             		  15
 *                    22
 *                    29
 *  30 31    
 *           
 *         HH:mm
 */
class DateTimeSelector extends Dialog<ZonedDateTime> {
	private static final DateTimeFormatter monthyear = DateTimeFormatter.ofPattern("LLL yyyy"),
			timeForm = DateTimeFormatter.ofPattern("HH:mm");

	private ZonedDateTime time; private final ZonedDateTime original;
	public DateTimeSelector(Applet parent, ZonedDateTime time) {
		super(parent, 21, 10);
		this.time = time;
		original = time;
	}

	@Override
	protected void onInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUpdate() {}
	
	@Override
	protected void onClose() {
		returnValue(time);
	}
	
	private int mode = 0;

	@Override
	protected void keyPressed(int code, char c) {
		switch(code) {
			case KeyEvent.VK_PAGE_UP: {
				if(mode > 0) mode--;
				break;
			}
			case KeyEvent.VK_PAGE_DOWN: {
				if(mode < 2) mode++;
				break;
			}
			case KeyEvent.VK_ENTER: {
				returnValue(time);
				return;
			}
			case KeyEvent.VK_ESCAPE: {
//				System.out.println("esc");
				returnValue(original);
				return;
			}
		}
		
		switch(mode) {
			case 0: {
				switch(code) {
					case KeyEvent.VK_DOWN: {
						time = time.withDayOfMonth(1).plusYears(1);
						break;
					}
					case KeyEvent.VK_RIGHT: {
						time = time.withDayOfMonth(1).plusMonths(1);
						break;
					}
					case KeyEvent.VK_UP: {
						time = time.withDayOfMonth(1).plusYears(-1);
						break;
					}
					case KeyEvent.VK_LEFT: {
						time = time.withDayOfMonth(1).plusMonths(-1);
						break;
					}
				}
				break;
			}
			case 1: {
				switch(code) {
					case KeyEvent.VK_DOWN: {
						time = time.plusDays(7);
						break;
					}
					case KeyEvent.VK_RIGHT: {
						time = time.plusDays(1);
						break;
					}
					case KeyEvent.VK_UP: {
						time = time.plusDays(-7);
						break;
					}
					case KeyEvent.VK_LEFT: {
						time = time.plusDays(-1);
						break;
					}
				}
				break;
			}
			case 2: {
				switch(code) {
					case KeyEvent.VK_DOWN: {
						time = time.plusHours(1);
						break;
					}
					case KeyEvent.VK_RIGHT: {
						time = time.plusMinutes(1);
						break;
					}
					case KeyEvent.VK_UP: {
						time = time.plusHours(-1);
						break;
					}
					case KeyEvent.VK_LEFT: {
						time = time.plusMinutes(-1);
						break;
					}
				}
				break;
			}
		}
		
		refreshDisplay();
	}
	
	private static final char[] mtw = StringTools.cp("M  T  W  T  F  S  S");
	private static final Color[] wt = StringTools.stringOf(Color.WHITE, 21), blk = StringTools.stringOf(Color.BLACK, 21),
			gry = StringTools.stringOf(Color.GRAY, 21);
	
	@Override
	protected void refreshDisplay() {
		rootDisplay.clear();
		

		rootDisplay.writeln(StringTools.cp(time.format(monthyear)), blk, mode == 0 ? wt : gry, 0, 6);
		
		rootDisplay.writeln(mtw, 1, 1);
		int ord = 3*(time.withDayOfMonth(1).getDayOfWeek().getValue()) - 2, dom = time.getDayOfMonth(), len = time.getMonth().length(time.toLocalDate().isLeapYear()),
				line = 2;
		for(int d = 1; d <= len; d++, ord+=3) {
			if(ord > 21) {
				line++;
				ord = 1;
			}
			
			if(d != dom)
				rootDisplay.writeln(StringTools.cp(String.valueOf(d)), line, ord);
			else
				rootDisplay.writeln(StringTools.cp(String.valueOf(d)), blk, mode == 1 ? wt : gry, line, ord);
		}
		
		rootDisplay.writeln(StringTools.cp(time.format(timeForm)), blk, mode == 2 ? wt : gry, 9, 8);
		
		super.refreshDisplay();
	}

}
