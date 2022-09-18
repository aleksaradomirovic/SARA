package sara;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.VolatileImage;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class Applet {
	protected final Screen screen;
	protected final JFrame window;
	protected final Applet parent;
	private final Collection<Applet> children = new LinkedList<>();
	
	protected boolean greedy = false;
	
	private Applet(Applet parent, int w, int h) {
		if(parent != null) {
			this.parent = parent;
			parent.children.add(this);
		}
		else {
			if(SARA.root != null) throw new RuntimeException("There is already a root Applet ("+SARA.root+")!");
			SARA.root = this;
			this.parent = null;
		}
		screen = new Screen(w,h);
		setSize(h,w);
		window = new JFrame();
		window.add(screen.display);
		WindowHandler wh = new WindowHandler();
		window.addWindowListener(wh);
		window.addKeyListener(wh);
		window.setResizable(false);
		window.pack();
		
		SARA.init(this);
	}
	
	public Applet(Applet parent, int w, int h, String title) {
		this(parent,w,h);
		window.setTitle(title);
	}
	
	public Applet(Applet parent, String title) {
		this(parent,160,40,title);
	}
	
	protected final Iterable<Applet> children() {
		return children;
	}
	
	public final void setVisible(boolean visible) {
		window.setVisible(visible);
	}
	
	protected final void refreshDisplay() {
		if(screen.graphics == null) return;
		screen.repaint();
		window.repaint();
	}
	
	protected final void write(char c, Color fg, Color bg, int line, int col) {
		screen.LCD[line][col] = c;
		screen.FG[line][col] = fg;
		screen.BG[line][col] = bg;
	}
	
	protected final void setColor(Color fg, Color bg, int line, int col) {
		screen.FG[line][col] = fg;
		screen.BG[line][col] = bg;
	}
	
	protected final void write(char c, int line, int col) {
		write(c,null,null,line,col);
	}
	
	protected final void write(char[] chars, Color fg, Color bg, int line, int offset) {
		for(int i = 0; i < chars.length && offset < screen.LCD[line].length; i++, offset++) {
			write(chars[i],fg,bg,line,offset);
		}
	}
	
	protected final void write(char[] chars, int line, int offset) {
		write(chars,null,null,line,offset);
	}
	
	protected final void clear(int line, int col) {
		screen.LCD[line][col] = 0;
		screen.FG[line][col] = null;
		screen.BG[line][col] = null;
	}
	
	protected final void clear(int line) {
		for(int col = 0; col < screen.LCD[line].length; col++) {
			screen.LCD[line][col] = 0;
			screen.FG[line][col] = null;
			screen.BG[line][col] = null;
		}
	}
	
	boolean closing = false;
	public void close() {
		if(closing) throw new IllegalStateException("Window was already set to close earlier");
		closing = true;
		for(Applet child : children) {
			child.close();
		}
		onClose();
		if(parent != null) parent.children.remove(this);
		SARA.allApps.remove(this);
		if(SARA.root == this) SARA.exit(0);
		window.dispose();
	}
	
	boolean initted = false;
	
	protected abstract void init();
	protected abstract void update();
	protected abstract void onClose();
	protected void repaint() { throw new UnsupportedOperationException(); }
	
	protected void keyPressed(KeyEvent e) {}
	
	protected Dimension setSize(int lines, int cols) {
//		System.out.println(lines+", "+cols);
		return screen.setSize(lines, cols);
	}
	
	public static class Screen {
		protected char[][] LCD;
		protected Color[][] FG, BG;
		private final JPanel display;
		private Font font;
		private VolatileImage buffer;
		private Graphics2D graphics;
		
		private class LCD extends JPanel {
			private static final long serialVersionUID = 3510243596227066479L;
			
			@Override
			protected void paintComponent(Graphics g) {
				if(buffer == null) {
					buffer = display.createVolatileImage(prefSize.width, prefSize.height);
					updateBufferGraphics();
					Screen.this.repaint();
				}
				g.drawImage(buffer, 0, 0, null);
			}
		}
		
		public static final Font defaultDisplayFont = new Font("Courier New",0,12);
//		public Screen() {
//			this(160,40,defaultDisplayFont);
//		}
		
		public Screen(int w, int h) {
			this(w,h,defaultDisplayFont);
		}
		
		public Screen(int w, int h, Font f) {
			display = new LCD();
			
			font = f;
			setSize(h, w);
		}
		
		public final JPanel getDisplay() {
			return display;
		}
		
		public final Font getFont() {
			return font;
		}
		
		public final void setFont(Font font) {
			this.font = font;
			tweakDisplay();
		}
		
		private Dimension setSize(int lines, int cols) {
			LCD = new char[lines][cols];
			FG = new Color[lines][cols];
			BG = new Color[lines][cols];
			
			return tweakDisplay();
		}
		
		private Dimension prefSize;
		protected final Dimension tweakDisplay() {
			updateFontMetrics();
			int w = LCD[0].length, h = LCD.length;
			
			prefSize = new Dimension(w*FONT_WIDTH + FONT_HEADING * 2, h*FONT_HEIGHT + FONT_HEADING * 2);
			display.setPreferredSize(prefSize);
			if(display.isDisplayable()) {
				buffer = display.createVolatileImage(prefSize.width, prefSize.height);
				updateBufferGraphics();
			}
			return prefSize;
		}
		
		protected final void updateBufferGraphics() {
			graphics = buffer.createGraphics();
//			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			graphics.setBackground(Color.BLACK);
			graphics.setFont(font);
		}
		
		protected int FONT_HEIGHT, FONT_WIDTH, FONT_ASCENT, FONT_HEADING;
		protected void updateFontMetrics() {
			java.awt.FontMetrics fm = SARA.drawTester.getFontMetrics(font);
			FONT_HEIGHT = fm.getHeight();
			FONT_WIDTH = fm.charWidth(' ');
			FONT_ASCENT = fm.getAscent();
			FONT_HEADING = FONT_WIDTH/2;
		}
		
		private boolean painting = false, revert = false;
		protected final void repaint() {
//			System.out.println(graphics);
			if(painting) {
				revert = true; // in case shit happens between different threads
				return;
			}
			painting = true;
			revert = true;
			while(revert) {
				graphics.clearRect(0, 0, prefSize.width, prefSize.height);
				revert = false;
//				long ms = System.currentTimeMillis();
				for(int l = 0, h = FONT_ASCENT + FONT_HEADING; l < LCD.length; l++, h+=FONT_HEIGHT) {
					if(revert) break; // if revert just go and repaint
					for(int c = 0, w = FONT_HEADING; c < LCD[l].length; c++, w+=FONT_WIDTH) {
						if(BG[l][c] != null) {
							graphics.setColor(BG[l][c]);
							graphics.fillRect(w, h-FONT_ASCENT, FONT_WIDTH, FONT_HEIGHT);
						}
						graphics.setColor(FG[l][c] != null ? FG[l][c] : Color.WHITE);
						graphics.drawString(String.valueOf(LCD[l][c] == 0 ? ' ' : LCD[l][c]), w, h);
					}
				}
//				System.out.println(System.currentTimeMillis()-ms);
//				System.out.println("r");
			}
			painting = false;
//			System.out.println(graphics);
		}
	}

	private class WindowHandler implements WindowListener, KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			Applet.this.keyPressed(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {}

		@Override
		public void windowOpened(WindowEvent e) {
			for(Applet c : children) {
				if(c.greedy) {
					c.window.setExtendedState(JFrame.NORMAL);
					c.window.toFront();
				}
			}
		}

		@Override
		public void windowClosing(WindowEvent e) {
			close();
		}

		@Override
		public void windowClosed(WindowEvent e) {}

		@Override
		public void windowIconified(WindowEvent e) {
			for(Applet c : children) {
				c.window.setExtendedState(JFrame.ICONIFIED);
			}
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			for(Applet c : children) {
				if(c.greedy) {
					c.window.setExtendedState(JFrame.NORMAL);
					c.window.toFront();
				}
			}
		}

		@Override
		public void windowActivated(WindowEvent e) {
			for(Applet c : children) {
				if(c.greedy) {
					c.window.setExtendedState(JFrame.NORMAL);
					c.window.toFront();
				}
			}
		}

		@Override
		public void windowDeactivated(WindowEvent e) {}
	}
}
