package sara;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.VolatileImage;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class Applet {
	protected Screen rootDisplay;
	Display panel;
	
	private final Collection<Applet> children;
	protected final Applet parent;
	
	protected boolean greedy = false, independent = true;
	
	public Applet(Applet parent) {
//		if(parent == null && !(this instanceof SARAMain)) System.err.println("No parent provided!");
		children = new CopyOnWriteArrayList<>();
		this.parent = parent;
		if(parent != null) {
			parent.children.add(this);
		}
		
		panel = new Display();
		if(parent != null) panel.frame.setLocation(parent.panel.frame.getX()+20, parent.panel.frame.getY()+20);
		
		SARA.init(this);
	}
	
	public Applet(Applet parent, int w, int h) {
		this(parent);
		rootDisplay = new Screen(w,h);
	}
	
	protected abstract void onInit();
	protected abstract void onUpdate();
	protected abstract void onClose();
	
	protected abstract void keyPressed(int code, char c);
	
	private boolean closing = false;
	public final void close() {
		close(null);
	}
	
	protected final void close(Applet from) {
		if(closing) return; // might happen idk
		closing = true;
		for(Applet e : children) {
			e.close(this);
		}
		onClose();
		if(parent == null) SARA.exit(0);
		if(parent != from) parent.children.remove(this);
		panel.frame.dispose();
	}
	
	protected void refreshDisplay() {
		panel.draw(rootDisplay);
		panel.repaint();
	}
	
	public final int countTree() {
		int sum = 1;
		for(Applet c : children) {
			sum += c.countTree();
		}
		return sum;
	}
	
	public void setFont(Font f) {
		panel.f = f;
		if(panel.graphicsText != null) panel.fontMetrics(rootDisplay);
	}
	
	public Font getFont() {
		return panel.f;
	}
	
	public boolean LCDMode = false;
	
	class Display extends JPanel {
		private static final long serialVersionUID = -683143580223986063L;
		final JFrame frame;
		Graphics2D graphicsBG, graphicsText, graphics;
		VolatileImage bufText, bufBG, frontBuf;
		Font f = defaultDisplayFont;
		private static final Font defaultDisplayFont = new Font("Monospaced",0,12);
		
		private Display() {
			frame = new JFrame();
			frame.add(this);
			frame.setResizable(false);
			WindowHandler h = new WindowHandler();
			frame.addWindowListener(h);
			frame.addKeyListener(h);
		}
		
		int LINEHT, ASCENT, FONTWD, OFFSET;
		
		private void fontMetrics(Screen disp) {
			FontMetrics fm = graphicsText.getFontMetrics();
			LINEHT = fm.getHeight();
			ASCENT = fm.getAscent();
			FONTWD = fm.charWidth(' ');
			OFFSET = FONTWD / 2;
			
			Dimension nw = new Dimension(FONTWD*(disp.LCD[0].length+1),FONTWD + (LINEHT*disp.LCD.length));
			if(!nw.equals(getPreferredSize())) {
				setPreferredSize(nw);
				frame.pack();
				bufText = getGraphicsConfiguration().createCompatibleVolatileImage(nw.width, nw.height, VolatileImage.TRANSLUCENT);
				bufBG = getGraphicsConfiguration().createCompatibleVolatileImage(nw.width, nw.height, VolatileImage.OPAQUE);
				frontBuf = createVolatileImage(nw.width, nw.height);
				restartGraphics();
			}

			
//			System.out.println("FM "+nw);
		}
		
		private static final Color TP = new Color(0, 0, 0, 0);
		private void restartGraphics() {
			graphicsText = bufText.createGraphics();
			graphicsBG = bufBG.createGraphics();
			graphicsText.setBackground(TP);
			graphicsText.clearRect(0, 0, getWidth(), getHeight());
			graphicsBG.setBackground(Color.BLACK);
			graphicsBG.clearRect(0, 0, getWidth(), getHeight());
			graphicsText.setFont(f);
			
			graphics = frontBuf.createGraphics();
			if(LCDMode) graphicsText.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		private boolean drawing = false;
		private void draw(Screen disp) {
			if(drawing) {
				drawing = false;
				return;
			}
			drawing = true;
			if(graphicsText == null || disp == null) return;
			graphicsText.setFont(f);
			
			fontMetrics(disp);
			graphicsText.clearRect(0, 0, getWidth(), getHeight());
			
			for(int i = 0, h = OFFSET, ch = h+ASCENT; i < disp.LCD.length; i++, h+=LINEHT, ch+=LINEHT) {
				for(int j = 0, w = OFFSET; j < disp.LCD[i].length; j++, w+=FONTWD) {
					graphicsBG.setColor(disp.BG[i][j] != null ? disp.BG[i][j] : Color.BLACK);
					graphicsBG.fillRect(w, h, FONTWD, LINEHT);
					graphicsText.setColor(disp.FG[i][j] != null ? disp.FG[i][j] : Color.WHITE);
					graphicsText.drawString(String.valueOf(disp.LCD[i][j]), w, ch);
				}
			}
			
			if(!drawing) {
				draw(disp);
				return;
			}
			drawing = false;

			graphics.drawImage(bufBG, 0, 0, null);
			graphics.drawImage(bufText, 0, 0, null);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			g.drawImage(frontBuf, 0, 0, null);
		}
		
		private class KeyEventThread implements Runnable {
			final int code;
			final char c;
			KeyEventThread(int code, char c) {
				this.code = code;
				this.c = c;
			}
			
			@Override
			public void run() {
				Applet.this.keyPressed(code, c);
			}
		}
		
		private class WindowHandler implements WindowListener, KeyListener {

			@Override
			public void keyTyped(KeyEvent e) {
				//TODO typed?
			}

			@Override
			public void keyPressed(KeyEvent e) {
//				Applet.this.keyPressed(e.getKeyCode(), e.getKeyChar());
				new Thread(new KeyEventThread(e.getKeyCode(), e.getKeyChar())).start();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent e) {}

			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}

			@Override
			public void windowClosed(WindowEvent e) {}

			@Override
			public void windowIconified(WindowEvent e) {
				for(Applet a : children) {
					if(!a.independent) a.panel.frame.setExtendedState(JFrame.ICONIFIED);
				}
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				for(Applet a : children) {
					if(a.greedy) a.panel.frame.toFront();
				}
			}

			@Override
			public void windowActivated(WindowEvent e) {
				for(Applet a : children) {
					if(a.greedy) a.panel.frame.toFront();
				}
			}

			@Override
			public void windowDeactivated(WindowEvent e) {}
		}
	}
}
