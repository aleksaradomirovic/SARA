package sara.apps.core.util.dialog;

import java.awt.event.KeyEvent;

import sara.Applet;
import sara.gui.InputTextField;
import sara.util.StringTools;

class MessageDialog extends Dialog<char[]> {
	private final InputTextField input;
//	private final int len;
	private final char[] old;

	public MessageDialog(Applet parent, String query, int maxlen) {
		super(parent, Math.min(80, maxlen+2), 5);
		rootDisplay.writeln(StringTools.cp(query), 1, 1, rootDisplay.width()-2);
		rootDisplay.add(input = new InputTextField(rootDisplay.width()-2), 1, 3);
		input.open(maxlen);
		old = null;
	}
	
	public MessageDialog(Applet parent, String query, int maxlen, char[] old) {
		super(parent, Math.min(80, maxlen+2), 5);
		rootDisplay.writeln(StringTools.cp(query), 1, 1, rootDisplay.width()-2);
		rootDisplay.add(input = new InputTextField(rootDisplay.width()-2), 1, 3);
		input.open(maxlen, old);
		this.old = old;
	}

	@Override
	protected void onInit() {
	}

	@Override
	protected void onUpdate() {}
	
	@Override
	protected void onClose() {
		returnValue(old);
	}

	@Override
	protected void keyPressed(int code, char c) {
		switch(code) {
			case KeyEvent.VK_ENTER:
//				System.out.println(input.getInput());
				returnValue(input.getInput());
				return;
			case KeyEvent.VK_ESCAPE:
				returnValue(old);
				return;
			default: input.keyInput(code, c);
		}
		refreshDisplay();
	}

}
