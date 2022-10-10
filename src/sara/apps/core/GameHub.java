package sara.apps.core;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import sara.Applet;
import sara.SARA;
import sara.Screen;
import sara.gui.ScrollingFieldPane;
import sara.util.StringTools;

@Startup(commands= {"gamehub","games","game"})
public class GameHub extends Applet {
	private final ScrollingFieldPane list;
	
	public GameHub(Applet parent, String[] args) {
		super(parent, 160, 40);
		
		rootDisplay.add(list = new ScrollingFieldPane(156, 38), 2, 2);
		
		for(Constructor<Applet> e : SARA.apps()) {
//			System.out.println(e);
			
			Game ann = e.getDeclaringClass().getDeclaredAnnotation(Game.class);
			if(ann == null || !ann.gameHub()) continue;
			
//			System.out.println(ann.name());
			list.add(new GameStart(e, ann.name(), new String[] { ann.description() }));
		}
		rootDisplay.redraw();
	}

	@Override
	protected void onInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onClose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void keyPressed(int code, char c) {
		switch(code) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_PAGE_UP: {
				list.moveDown(-1);
				break;
			}
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_PAGE_DOWN: {
				list.moveDown(1);
				break;
			}
			case KeyEvent.VK_ENTER: {
				if(list.isEmpty()) break;
				close();
				try {
					GameStart focus = (GameStart) list.focus();
					focus.game.newInstance(parent, new String[0]);
					return;
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		rootDisplay.redraw();
		refreshDisplay();
	}
	
	private class GameStart extends Screen {
		Constructor<Applet> game;
		
		char[] name;
		char[][] desc;
		
		public GameStart(Constructor<Applet> game, String name, String[] desc) {
			super(156, desc.length + 3);
			this.game = game;
			this.name = name.toCharArray();
			this.desc = new char[desc.length][];
			for(int i = 0; i < desc.length; i++) {
				this.desc[i] = desc[i].toCharArray();
			}
		}
		
		@Override
		public void redraw() {
//			clear();
			if(list.focus() != this)
				writeln(name, 1, 0);
			else
				writeln(name, StringTools.stringOf(Color.BLACK, name.length), StringTools.stringOf(Color.WHITE, name.length), 1, 0);
			
			for(int i = 0; i < desc.length; i++) {
				writeln(desc[i], 2+i, 0);
			}
		}
	}

}
