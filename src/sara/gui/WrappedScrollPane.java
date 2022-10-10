package sara.gui;

import java.awt.Color;
import java.util.ArrayList;

import sara.util.StringTools;

public class WrappedScrollPane extends ScrollPane {
	public WrappedScrollPane(int w, int h) {
		super(w,h);
		wrapped = new ArrayList<>(500);
	}
	
	private WrappedScrollPane(int w, int h, ArrayList<Line> raws) { // for resizing
		this(w,h);
		for(Line in : raws) {
			addLine(in.text, in.fg, in.bg);
		}
		redraw();
	}
	
	public WrappedScrollPane getResizedPane(int w, int h) {
		return new WrappedScrollPane(w,h,raws);
	}

	protected final ArrayList<Line> wrapped;
	
	@Override
	public void jumpTo(int pos) {
		scrollPos = Math.max(0, Math.min(wrapped.size()-1, pos));
		redraw();
	}
	
	@Override
	protected void addLine(char[] lcd, Color[] fg, Color[] bg) {
		lcd = lcd.clone(); // safekeeping
		if(fg != null) fg = fg.clone();
		if(bg != null) bg = bg.clone();
		raws.add(new Line(lcd,fg,bg));
		
		int last = 0, lastSpace = -1, i;
		for(i = 0; i < lcd.length; i++) { // wrap
			if(lcd[i] == ' ') {
				if(last != 0) {
					if(last == i) last++;
					else lastSpace = i;
				}
			} else if(lcd[i] == '\n') {
				int l = i-last;
				wrapped.add(new Line(
						StringTools.sectionOf(lcd, last, l),
						fg != null ? StringTools.sectionOf(fg, last, l) : null,
						bg != null ? StringTools.sectionOf(bg, last, l) : null));
				lastSpace = -1;
				last = i+1;
//				continue;
			}
			if(i-last == w) {
				int sep = lastSpace != -1 ? lastSpace : i, l = sep-last;
				wrapped.add(new Line(
						StringTools.sectionOf(lcd, last, l),
						fg != null ? StringTools.sectionOf(fg, last, l) : null,
						bg != null ? StringTools.sectionOf(bg, last, l) : null));
				lastSpace = -1;
				last = i;
			}
		}
		if(i == 0 || lcd[i-1] != '\n') {
			int l = i-last;
			wrapped.add(new Line(
					StringTools.sectionOf(lcd, last, l),
					fg != null ? StringTools.sectionOf(fg, last, l) : null,
					bg != null ? StringTools.sectionOf(bg, last, l) : null));
		}
	}
	
	@Override
	public void setLine(int line, char[] in, Color[] fg, Color[] bg) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void redraw() {
		clear();
		Line i;
		for(int line = 0, pos = scrollPos; line < h && pos < wrapped.size(); line++, pos++) {
			i = wrapped.get(pos);
//			System.out.println(i.text);
			writeln(i.text, i.fg, i.bg, line, 0);
		}
	}
}
