package sara;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

public class Screen {
	final char[][] LCD;
	final Color[][] FG, BG;
	
	protected final int w, h;
	
	public Screen(int w, int h) {
		children = new ArrayList<>();
		LCD = new char[h][w];
		FG = new Color[h][w];
		BG = new Color[h][w];
		
		this.w = w;
		this.h = h;
	}
	
	public int width() {
		return w;
	}
	
	public int height() {
		return h;
	}
	
	protected final Collection<Screen> children;
	
	public void redraw() {
		for(Screen c : children) {
			c.redraw();
		}
	}
	
	protected static class Line {
		public final char[] text;
		public Color[] fg, bg;
		
		public Line(char[] a, Color[] f, Color[] b) {
			text = a;
			fg = f;
			bg = b;
		}
	}
	
	public Line getLine(int line) {
		return new Line(LCD[line], FG[line], BG[line]);
	}
	
	private Screen parent;
	private int offX, offY;
	
	public void add(Screen child, int x, int y) {
		if(child.w + x > w || child.h + y > h) throw new IllegalArgumentException("Child would be out of screen bounds!");
		child.parent = this;
		child.offX = x;
		child.offY = y;
		children.add(child);
		child.drawOnParent();
	}
	
	public void remove(Screen child) {
		if(child.parent != this) throw new IllegalArgumentException("Screen provided is not child of me!");
		child.clearParentZone();
		children.remove(child);
		child.parent = null;
	}
	
	public void clearAllChildren() {
		for(Screen child : children) {
			child.clearParentZone();
//			children.remove(child);
			child.parent = null;
		}
		children.clear();
	}
	
	public final void color(Color fg, Color bg, int line, int col) {
		FG[line][col] = fg;
		BG[line][col] = bg;
		if(parent != null) parent.color(fg, bg, line+offY, col+offX);
	}
	
	public final void color(Color fg, Color bg, int line, int col, int len) {
		len+=col;
		for(int i = col; i < len; i++) {
			FG[line][i] = fg;
			BG[line][i] = bg;
		}
		if(parent != null) parent.color(fg, bg, line+offY, col+offX, len);
	}
	
	public final void write(char c, int line, int col) {
		write(c,null,null,line,col);
	}
	
	public final void write(char c, Color fg, Color bg, int line, int col) {
		LCD[line][col] = c;
		FG[line][col] = fg;
		BG[line][col] = bg;
		if(parent != null) parent.write(c, fg, bg, line+offY, col+offX);
	}
	
	public final void writeln(Line l, int line) {
		writeln(l.text,l.fg,l.bg,line,0);
	}
	public final void writeln(char[] c, final int line, final int offset) {
		writeln(c,line,offset,c.length);
	}
	public final void writeln(char[] c, Color[] fg, Color[] bg, final int line, final int offset) {
		writeln(c,fg,bg,line,offset,c.length);
	}
	
	public final void writeln(char[] c, final int line, final int offset, final int lim) {
		writeln(c,new Color[lim], new Color[lim],line, offset,lim);
	}
	
	public final void writeln(char[] c, Color[] fg, Color[] bg, final int line, final int offset, int lim) {
		lim = Math.min(c.length, lim);
//		if(fg == null || bg == null) {
//			writeln(c,line,offset, lim);
//			return;
//		}
		for(int i = 0, o = offset; i < lim; i++, o++) {
			LCD[line][o] = c[i];
			FG[line][o] = fg != null ? fg[i] : null;
			BG[line][o] = bg != null ? bg[i] : null;
		}
		if(parent != null) parent.writeln(c, fg, bg, line+offY, offset+offX, lim);
	}
	
	public final void drawOn(Screen other, int offX, int offY) {
		for(int i = 0, y = offY; i < LCD.length; i++, y++) {
			for(int j = 0, x = offX; j < LCD[i].length; j++, x++) {
				other.LCD[y][x] = LCD[i][j];
				other.FG[y][x] = FG[i][j];
				other.BG[y][x] = BG[i][j];
			}
		}
	}
	
	public final void drawOnParent() {
		if(parent == null) throw new IllegalStateException("No parent to draw on!");
		drawOn(parent,offX,offY);
	}
	
	public final void clear() {
		if(parent != null) {
			for(int i = 0, y = offY; i < LCD.length; i++, y++) {
				for(int j = 0, x = offX; j < LCD[i].length; j++, x++) {
					parent.LCD[y][x] = LCD[i][j] = 0;
					parent.FG[y][x] = FG[i][j] = null;
					parent.BG[y][x] = BG[i][j] = null;
				}
			}
		} else {
			for(int i = 0; i < LCD.length; i++) {
				for(int j = 0; j < LCD[i].length; j++) {
					LCD[i][j] = 0;
					FG[i][j] = null;
					BG[i][j] = null;
				}
			}
		}
	}
	
	protected final void clearParentZone() {
		for(int i = 0, y = offY; i < LCD.length; i++, y++) {
			for(int j = 0, x = offX; j < LCD[i].length; j++, x++) {
				parent.LCD[y][x] = 0;
				parent.FG[y][x] = null;
				parent.BG[y][x] = null;
			}
		}
	}
	
	@Override
	protected Screen clone() {
		Screen r = new Screen(LCD[0].length, LCD.length);
		drawOn(r,0,0);
		return r;
	}
}
