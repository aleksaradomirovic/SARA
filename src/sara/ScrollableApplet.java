package sara;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import sara.util.StringTools;

public abstract class ScrollableApplet extends Applet {
	protected int lineWidth, portHeight;
	public ScrollableApplet(Applet parent, int w, int h, String title) {
		super(parent, w, h, title);
		repaint();
	}
	private int scrollPos = 0;
	
	ArrayList<TextLine> lines = new ArrayList<>(),
			unwrapped = new ArrayList<>();
	private class TextLine {
		final char[] text;
		final Color fg, bg;
		TextLine(char[] text, Color fg, Color bg) {
			if(text.length > lineWidth) throw new RuntimeException("Text line '"+String.valueOf(text)+"' is too long!");
			this.text = text;
			this.fg = fg;
			this.bg = bg;
		}
	}
	
	protected void writeLine(String str, Color fg, Color bg) {
		char[] arr = str.toCharArray();
		unwrapped.add(new TextLine(arr, fg, bg));
		for(char[] line : StringTools.softWrap(arr, lineWidth)) {
			lines.add(new TextLine(line, fg, bg));
		}
		repaint();
	}
	
	protected void writeLine(String str) {
		writeLine(str,null,null);
	}
	
	protected Dimension setSizeDiscrete(int lines, int cols) {
		if(cols < 3) throw new IllegalArgumentException("Must be wider than 2!");
		return setSize(lines,cols-2);
	}
	
	@Override
	protected Dimension setSize(int lines, int cols) {
		lineWidth = cols;
		portHeight = lines;
		return super.setSize(lines, cols+2);
	}
	
	protected int scrollRange = -1;
	@Override
	protected void repaint() {
//		long ms = System.currentTimeMillis();
		scrollRange = lines.size()-1;
//		System.out.println(range);
		TextLine line;
//		System.out.println(scrollPos);
		for(int h = 0; h < portHeight; h++) {
			clear(h);
		}
		for(int l = scrollPos, h = 0; l <= scrollRange && h < portHeight; l++, h++) {
			line = lines.get(l);
			write(line.text,line.fg,line.bg,h,0);
		}
		
		final int x = lineWidth+1;
		write('\u25b2',scrollPos == 0 ? Color.GRAY : null,null,0,x);
		for(int i = 1; i < portHeight-1; i++) {
			write('\u2551',i,x);
		}
		write('\u25bc',scrollPos >= scrollRange ? Color.GRAY : null,null,portHeight-1,x);
		write('\u2593',1+(scrollPos*(portHeight-3))/(scrollRange < 1 ? 1 : scrollRange),x);
		
		refreshDisplay();
//		System.out.println(System.currentTimeMillis()-ms);
	}
	
	protected final void scrollTo(int pos) {
		int npos = Math.max(0, Math.min(scrollRange, pos));
		if(npos != scrollPos) {
			scrollPos = npos;
			repaint();
//			System.out.println(scrollPos+" -> "+npos+" ("+pos+")");
		}
	}
	
	@Override
	protected void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_PAGE_UP: {
				scrollTo(scrollPos-(e.isShiftDown() ? 10 : 1));
				break;
			}
			case KeyEvent.VK_PAGE_DOWN: {
				scrollTo(scrollPos+(e.isShiftDown() ? 10 : 1));
				break;
			}
		}
	}
}
