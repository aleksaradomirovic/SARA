package sara.gui;

import java.awt.Color;

public class DecoratedInputTextField extends InputTextField {
	private final InputTextField embedded;
	public DecoratedInputTextField(int w) {
		super(w);
		add(embedded = new InputTextField(w-2), 2, 0);
	}
	
	public char decorator = '>';
	
	@Override
	public void keyInput(int code, char c) {
		embedded.keyInput(code, c);
	}
	
	@Override
	public void redraw() {
		embedded.redraw();
	}
	
	@Override
	public char[] getInput() {
		return embedded.getInput();
	}
	
	@Override
	public void open(int sz) {
		embedded.open(sz);
		write(decorator, Color.WHITE, Color.BLACK,0,0);
	}
	
	@Override
	public void close() {
		embedded.close();
		write('\0', Color.WHITE, Color.BLACK,0,0);
	}
}
