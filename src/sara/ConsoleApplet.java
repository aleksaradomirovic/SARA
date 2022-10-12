package sara;

import java.awt.Color;
import java.awt.event.KeyEvent;

import sara.gui.DecoratedInputTextField;
import sara.gui.WrappedScrollPane;
import sara.util.StringTools;

public abstract class ConsoleApplet extends Applet {
	protected final WrappedScrollPane printStream;
	protected DecoratedInputTextField input;
	public ConsoleApplet(Applet parent, int w, int h) {
		super(parent,w,h);
		rootDisplay.add(printStream = new WrappedScrollPane(w, h-1), 0, 0);
		rootDisplay.add(input = new DecoratedInputTextField(w), 0, h-1);
	}
	protected boolean echo = true;
	
	@Override
	protected final void keyPressed(int code, char c) {
		switch(code) {
			case KeyEvent.VK_PAGE_UP: {
				printStream.moveDown(-1);
//				System.out.println(code);
				break;
			}
			case KeyEvent.VK_PAGE_DOWN: {
				printStream.moveDown(1);
//				System.out.println(code);
				break;
			}
			case KeyEvent.VK_ENTER: {
				char[] str = input.getInput();
				input.close();
				if(echo) writeln("> "+String.valueOf(str));
				handleInput(str);
				break;
			}
			default: input.keyInput(code, c);
		}
		refreshDisplay();
	}

	public void writeln(char[] x) {
		printStream.writeln(x);
		refreshDisplay();
	}
	
	public void writeln(String x) {
		printStream.writeln(StringTools.cp(x));
		refreshDisplay();
	}
	
	public void writeln(char[] x, Color[] fg, Color[] bg) {
		printStream.writeln(x, fg, bg);
		refreshDisplay();
	}
	
	public void writeln(String in, Color fg, Color bg) {
		writeln(StringTools.cp(in),StringTools.stringOf(fg, in.length()),StringTools.stringOf(bg, in.length()));
	}
	
	protected abstract void handleInput(char[] str);
}
