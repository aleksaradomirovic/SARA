package sara.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import sara.Screen;

public class ScrollingFieldPane extends Screen implements Iterable<Screen> {
	public ScrollingFieldPane(int w, int h) {
		super(w, h);
		elements = new ArrayList<>();
	}
	
	public final List<Screen> elements;
	
	@Override
	@Deprecated
	public void add(Screen child, int x, int y) {
		add(child);
	}
	
	public void add(Screen child) {
		if(child.width() > w || child.height() > h) throw new IllegalArgumentException("Child would be out of screen bounds!");
		elements.add(child);
	}
	
	@Override
	public void remove(Screen child) {
		elements.remove(child);
		redraw();
	}
	
	public void add(Screen child, int pos) {
		if(child.width() > w || child.height() > h) throw new IllegalArgumentException("Child would be out of screen bounds!");
		elements.add(pos, child);
	}
	
	public void sort(Comparator<? super Screen> c) {
		elements.sort(c);
//		scrollPos = 0;
		redraw();
	}
	
	public Screen remove(int pos) {
		Screen r = elements.remove(pos);
		redraw();
		return r;
	}
	
	public Screen at(int pos) {
		return elements.get(pos);
	}
	
	public Screen focus() {
		return elements.get(scrollPos);
	}
	
	public boolean isEmpty() {
		return elements.isEmpty();
	}
	
	protected int scrollPos = 0;
	
	public void jumpTo(int pos) {
		scrollPos = Math.max(0, Math.min(elements.size()-1, pos));
//		System.out.println(scrollPos);
		redraw();
	}
	
	public void moveDown(int change) {
		jumpTo(scrollPos+change);
	}
	
	@Override
	public void redraw() {
		clear();
		int pos = scrollPos;
		Screen subj;
		for(int i = 0; i < h && pos < elements.size(); pos++) {
			subj = elements.get(pos);
			subj.redraw();
			for(int j = 0; j < subj.height() && i < h; j++, i++) {
				writeln(subj.getLine(j), i);
			}
		}
	}

	@Override
	public Iterator<Screen> iterator() {
		return elements.iterator();
	}
}
