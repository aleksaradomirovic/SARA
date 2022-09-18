package sara;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;

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
	int lastChar = 0, writePos = 0;
	
	@Override
	protected void repaint() {
		if(input != null) { // qa open
			clear(portHeight);
			write('\u25ba',portHeight,0);
//			if(lastChar > inputWidth - 2) write('+',portHeight, 1);
			
			int pos = 2;
			for(int i = (lastChar > inputWidth ? lastChar-inputWidth : 0); i < lastChar; i++, pos++) {
				write(input[i],portHeight,pos);
				if(i == writePos) setColor(Color.BLACK, insert ? Color.RED : Color.WHITE, portHeight, pos);
			}
			if(writePos == lastChar) setColor(Color.BLACK, insert ? Color.RED : Color.WHITE, portHeight, pos);
		}
		super.repaint();
	}
	
	public void openQA(int sz) {
		input = new char[sz];
		lastChar = 0;
		writePos = 0;
		repaint();
	}
	
	public void openQA() {
		openQA(256);
	}
	
	public void closeQA() {
		input = null;
	}
	
	protected void writeErr(String str) {
		writeln(str,Color.RED,null);
	}
	
	protected void writeWarn(String str) {
		writeln(str,Color.YELLOW,null);
	}
	
	private boolean insert = false;
	protected boolean echo = true;
//	private int pastCommand;
//	ArrayList<char[]> commandHistory = new ArrayList<>(); TODO
	@Override
	protected void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		
		if(input != null) {
			switch(e.getKeyCode()) {
				case KeyEvent.VK_LEFT: {
					if(writePos > 0) writePos--;
					break;
				}
				case KeyEvent.VK_RIGHT: {
					if(writePos < lastChar) writePos++;
					break;
				}
			}
			
			switch(e.getKeyChar()) {
				case KeyEvent.VK_DELETE: {
					if(writePos < lastChar) {
						for(int i = writePos; i < lastChar;) {
							input[i] = input[++i];
						}
						lastChar--;
					}
					break;
				}
				case KeyEvent.VK_BACK_SPACE: {
					if(writePos > 0 && lastChar > 0) {
						for(int i = writePos-1; i < lastChar;) {
							input[i] = input[++i];
						}
						lastChar--;
						writePos--;
					}
					break;
				}
				case 0x16: {
					try {
						char[] clipboard = ((String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor)).toCharArray();
						if(lastChar + clipboard.length <= input.length) {
							for(int i = writePos; writePos < lastChar;) {
								input[i+clipboard.length] = input[i++];
							}
							for(int j = 0; j < clipboard.length;) {
								input[writePos++] = clipboard[j++];
							}
							lastChar += clipboard.length;
						}
					} catch (HeadlessException | UnsupportedFlavorException | IOException e1) {
						e1.printStackTrace();
					}
					break;
				}
				case 0x1A: {
					openQA(input.length);
					break;
				}
				case KeyEvent.VK_ENTER: {
					char[] command = StringTools.sectionOf(input, 0, lastChar);
					openQA(input.length);
					if(echo) writeln("> "+String.valueOf(command));
					handleCommand(command);
					scrollTo(Math.min(0, scrollRange-portHeight));
					break;
				}
				case KeyEvent.VK_INSERT: {
					insert = !insert;
					break;
				}
				default: {
					if(lastChar < input.length && StringTools.isPrintableKey(e.getKeyChar()) && e.getKeyChar() != 0xffff) {
						if(!insert) {
							for(int i = lastChar; i > writePos;) {
								input[i] = input[--i];
							}
							lastChar++;
						} else if(writePos == lastChar) lastChar++;
						input[writePos++] = e.getKeyChar();
					}// else System.out.println((int)e.getKeyCode());
					break;
				}
			}
			
			repaint();
		}
	}
	
	protected abstract void handleCommand(char[] command);
}
