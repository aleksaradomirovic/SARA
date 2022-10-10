package sara.gui;

import java.awt.Color;
import java.util.ArrayList;

import sara.Screen;
import sara.util.StringTools;

public class ScrollPane extends Screen {
	protected final ArrayList<Line> raws;

	public ScrollPane(int w, int h) {
		super(w, h);
		raws = new ArrayList<>(250);
	}
	
	protected int scrollPos = 0;
	
	public void jumpTo(int pos) {
		scrollPos = Math.max(0, Math.min(raws.size()-1, pos));
		redraw();
	}
	
	public void moveDown(int change) {
		jumpTo(scrollPos+change);
	}
	
	protected void addLine(char[] lcd, Color[] fg, Color[] bg) {
		lcd = lcd.clone(); // safekeeping
		if(fg != null) fg = fg.clone();
		if(bg != null) bg = bg.clone();
		raws.add(new Line(lcd,fg,bg));
	}
	
	public void writeln(char[] in) {
		addLine(in, null, null);
		redraw();
	}
	
	public void writeln(char[] in, Color[] fg, Color[] bg) {
		addLine(in, fg, bg);
		redraw();
	}
	
	public void setLine(int line, char[] in, Color fg, Color bg) {
		setLine(line, in, StringTools.stringOf(fg, in.length), StringTools.stringOf(bg, in.length));
	}
	
	public void setLine(int line, char[] in, Color[] fg, Color[] bg) {
		raws.set(line, new Line(in != null ? in : raws.get(line).text, fg, bg));
	}
	
	@Override
	public void redraw() {
		clear();
		Line i;
		for(int line = 0, pos = scrollPos; line < h && pos < raws.size(); line++, pos++) {
			i = raws.get(pos);
			writeln(i.text, i.fg, i.bg, line, 0, w);
		}
	}

}
