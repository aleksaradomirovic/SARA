package sara.apps.core.util.dialog;

import java.awt.Color;
import java.awt.event.KeyEvent;

import sara.Applet;

class MenuSelector extends Dialog<Integer> {
	private final char[][] options;
	private final char[] title;
	private final Color[] grays;
	
	private int selector = 0;
	
	public MenuSelector(Applet parent, String query, String[] opts) {
		super(parent, calcWidth(query, opts)+2, opts.length+4);
		title = query.toCharArray();
		options = new char[opts.length][];
		for(int i = 0; i < opts.length; i++) {
			options[i] = opts[i].toCharArray();
		}
		grays = new Color[rootDisplay.width()];
		for(int i = 0; i < grays.length; i++) grays[i] = Color.GRAY;
	}
	
	private static int calcWidth(String title, String[] opts) {
		int max = title.length();
		for(String x : opts) {
			max = Math.max(max, x.length());
		}
		return max;
	}
	
	@Override
	protected void refreshDisplay() {
		rootDisplay.clear();
		
		rootDisplay.writeln(title, 1, (rootDisplay.width()-title.length)/2);
		
		for(int i = 0; i < options.length; i++) {
			rootDisplay.writeln(options[i], i == selector ? null : grays, null, i+3, (rootDisplay.width()-options[i].length)/2);
		}
		
		super.refreshDisplay();
	}
	
	@Override
	protected void onInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUpdate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onClose() {
		returnValue(-1);
	}

	@Override
	protected void keyPressed(int code, char c) {
		switch(code) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_PAGE_UP: {
				if(selector > 0) selector--;
				break;
			}
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_PAGE_DOWN: {
				if(selector < options.length-1) selector++;
				break;
			}
			case KeyEvent.VK_ESCAPE: {
				close();
				return;
			}
			case KeyEvent.VK_ENTER: {
				returnValue(selector);
				close();
				return;
			}
		}
		
		refreshDisplay();
	}

}
