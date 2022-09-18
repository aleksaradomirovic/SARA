package sara;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import sara.util.StringTools;

public abstract class ScrollableApplet extends Applet {
	protected int lineWidth, portHeight;
	public ScrollableApplet(Applet parent, int w, int h, String title) {
		super(parent, w, h, title);
		lines = new ArrayList<>();
		clear();
	}
	private int scrollPos = 0;
	protected boolean fatlock = false;
	
	ArrayList<TextLine> lines;
	private class TextSeg {
		final char[] text;
		final Color fg, bg;
		TextSeg(char[] text, Color fg, Color bg) {
//			if(text.length > lineWidth) throw new RuntimeException("Text line '"+String.valueOf(text)+"' is too long!");
			this.text = text;
			this.fg = fg;
			this.bg = bg;
		}
	}
	
	private class TextLine {
		private int l;
		
		Collection<TextSeg> segments;
		
		TextLine() {
			l = 0;
			segments = new LinkedList<>();
			lines.add(this);
		}
		
		TextLine(TextSeg start) {
			this();
			append(start);
		}
		
		void append(TextSeg seg) {
			int i = 0;
			for(char[] c : StringTools.softWrap(seg.text, lineWidth, l)) {
				if(i == 0) segments.add(new TextSeg(c, seg.fg, seg.bg));
				else lines.add(new TextLine(new TextSeg(c, seg.fg, seg.bg)));
			}
		}
	}
	
	protected void clear() {
		lines.clear();
		new TextLine();
		
		repaint();
	}
	
	private TextLine lastLine() {
		return lines.get(lines.size()-1);
	}
	
	private void write(String str, Color fg, Color bg, boolean nl) {
		lastLine().append(new TextSeg(str.toCharArray(), fg, bg));
		if(nl) new TextLine();
		
		repaint();
	}
	
	protected final void write(String str, Color fg, Color bg) {
		write(str, fg, bg, false);
	}
	
	protected final void write(String str) {
		write(str, null, null, false);
	}
	
	protected final void writeln(String str, Color fg, Color bg) {
		write(str, fg, bg, true);
	}
	
	protected final void writeln(String str) {
		write(str, null, null, true);
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
		scrollRange = fatlock ? lines.size()-portHeight : lines.size()-1;
//		System.out.println(range);
//		System.out.println(scrollPos);
		for(int h = 0; h < portHeight; h++) {
			clear(h);
		}
		int pos;
		for(int l = scrollPos, h = 0; l < lines.size() && h < portHeight; l++, h++) {
			pos = 0;
			for(TextSeg seg : lines.get(l).segments) {
				write(seg.text,seg.fg, seg.bg, h, pos);
				pos += seg.text.length;
			}
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
