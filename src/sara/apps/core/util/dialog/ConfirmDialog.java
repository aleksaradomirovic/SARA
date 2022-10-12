package sara.apps.core.util.dialog;

import java.awt.Color;
import java.awt.event.KeyEvent;

import sara.Applet;
import sara.util.StringTools;

class ConfirmDialog extends Dialog<Integer> {
	
	/*
	 * ""
	 *  YES NO CANCEL 
	 */
	
	private static final Color[] grays = StringTools.stringOf(Color.GRAY, 6),
			whites = StringTools.stringOf(Color.WHITE, 6), blacks = StringTools.stringOf(Color.BLACK, 6);
	private static final char[] yes = "YES".toCharArray(), no = "NO".toCharArray(), cancel = "CANCEL".toCharArray();

	final char[] query;
	final boolean canCancel;
	public ConfirmDialog(Applet parent, String query, boolean canCancel) {
		super(parent, Math.max((query.length()+2), canCancel ? 15 : 7), 5);
		this.query = query.toCharArray();
		this.canCancel = canCancel;
		selector = canCancel ? 2 : 1;
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
		returnValue(canCancel ? 0 : -1);
	}
	
	int selector;
	@Override
	protected void refreshDisplay() {
		rootDisplay.writeln(query, 1, 1);
		
		if(selector == 0)
			rootDisplay.writeln(yes, blacks, whites, 3, 1);
		else 
			rootDisplay.writeln(yes, whites, grays, 3, 1);
		
		if(selector == 1)
			rootDisplay.writeln(no, blacks, whites, 3, canCancel ? rootDisplay.width()-10 : rootDisplay.width() - 4);
		else 
			rootDisplay.writeln(no, whites, grays, 3, canCancel ? rootDisplay.width()-10 : rootDisplay.width() - 4);
		
		if(canCancel) {
			if(selector == 2)
				rootDisplay.writeln(cancel, blacks, whites, 3, rootDisplay.width()-7);
			else 
				rootDisplay.writeln(cancel, whites, grays, 3, rootDisplay.width()-7);
		}
		
		super.refreshDisplay();
	}

	@Override
	protected void keyPressed(int code, char c) {
		switch(code) {
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_PAGE_DOWN:
			case KeyEvent.VK_RIGHT:
				if(selector < 1 || (selector < 2 && canCancel))
					selector++;
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_LEFT:
				if(selector > 0)
					selector--;
				break;
			case KeyEvent.VK_ESCAPE:
				selector = canCancel ? 2 : 1;
			case KeyEvent.VK_ENTER:
				switch(selector) {
					case 0: returnValue( 1); break;
					case 1: returnValue(-1); break;
					case 2: returnValue( 0); break;
				}
				return;
			
		}
		refreshDisplay();
	}

}
