package sara.gui;

import java.awt.Color;
import java.awt.event.KeyEvent;

import sara.Screen;
import sara.util.StringTools;

public class InputTextField extends Screen {
	public InputTextField(int w) {
		super(w, 1);
	}
	
	private int writePos, lastChar;
	char[] input;
	
	public void open() {
		open(256);
	}
	
	public void open(int sz) {
		input = new char[sz];
		writePos = 0;
		lastChar = 0;
		redraw();
	}
	
	public void open(int sz, char[] old) {
		input = new char[sz];
		for(int i = 0; i < old.length; i++) input[i] = old[i];
		writePos = old.length;
		lastChar = old.length;
		redraw();
	}
	
	public void close() {
		if(input == null) throw new IllegalStateException("Already closed!");
		for(int i = 0; i < w; i++) {
			write('\0',null,null,0,i);
		}
		input = null;
	}

	public void keyInput(int code, char c) {
		if(input == null) return;
		switch(code) {
			case KeyEvent.VK_BACK_SPACE: {
				if(writePos > 0) {
					input[--lastChar] = 0;
					writePos--;
				}
				break;
			}
			default: {
				if(c >= 0x20 && c != 65535 && lastChar < input.length) {
					input[lastChar++] = c;
					writePos++;
				}
			}
		}
		redraw();
	}
	
	public char[] getInput() {
		if(input == null) throw new IllegalStateException("Requested input while text field is not open!");
		return StringTools.sectionOf(input, 0, lastChar);
	}
	
	@Override
	public void redraw() {
		writeln(input, 0, Math.max(0, lastChar-w), w);
		color(Color.BLACK, Color.WHITE, 0, writePos);
	}
}
