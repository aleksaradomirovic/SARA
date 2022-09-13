package sara;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import sara.util.StringTools;

public abstract class ConsoleApplet extends ScrollableApplet {
	protected int inputWidth;
	public ConsoleApplet(Applet parent, int w, int h, String title) {
		super(parent, w, h, title);
	}
	
	public ConsoleApplet(Applet parent, String title) {
		this(parent,160,40,title);
	}
	
	@Override
	protected Dimension setSizeDiscrete(int lines, int cols) {
		if(lines < 2) throw new IllegalArgumentException("Must be taller than 1!");
		return super.setSizeDiscrete(lines-1,cols);
	}
	
	@Override
	protected Dimension setSize(int lines, int cols) {
		Dimension r = super.setSize(lines+1, cols);
		portHeight = lines;
		inputWidth = screen.LCD[0].length-3;
		return r;
	}
	
	char[] input;
	int lastChar = 0;
	
	@Override
	protected void repaint() {
		if(input != null) { // qa open
			clear(portHeight);
			write('\u25ba',portHeight,0);
//			if(lastChar > inputWidth - 2) write('+',portHeight, 1);
			
			int pos = 2;
			for(int i = (lastChar > inputWidth ? lastChar-inputWidth : 0); i < lastChar; i++, pos++) {
				write(input[i],portHeight,pos);
			}
			write('\u2588',portHeight,pos);
		}
		super.repaint();
	}
	
	public void openQA(int sz) {
		input = new char[sz];
		lastChar = 0;
		repaint();
	}
	
	public void openQA() {
		openQA(256);
	}
	
	public void closeQA() {
		input = null;
	}
	
	protected void writeErr(String str) {
		writeLine(str,Color.RED,null);
	}
	
	protected void writeWarn(String str) {
		writeLine(str,Color.YELLOW,null);
	}
	
	protected boolean echo = true;
//	private int pastCommand;
//	ArrayList<char[]> commandHistory = new ArrayList<>(); TODO
	@Override
	protected void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		
		if(input != null) {
			switch(e.getKeyChar()) {
				case KeyEvent.VK_DELETE:
				case KeyEvent.VK_BACK_SPACE: {
					if(lastChar > 0) input[--lastChar] = 0;
					break;
				}
				case 0x1A: {
					openQA(input.length);
					break;
				}
				case KeyEvent.VK_ENTER: {
					char[] command = StringTools.sectionOf(input, 0, lastChar);
					openQA(input.length);
					if(echo) writeLine("> "+String.valueOf(command));
					handleCommand(command);
					scrollTo(Math.min(0, scrollRange-portHeight));
					break;
				}
				default: {
					if(StringTools.isPrintableKey(e.getKeyCode()) && e.getKeyChar() != 0xffff) {
						input[lastChar++] = e.getKeyChar();
					}
					break;
				}
			}
			
			repaint();
		}
	}
	
	protected abstract void handleCommand(char[] command);
}
