package sara.apps.core.util.dialog;

import java.time.ZonedDateTime;

import sara.Applet;

public abstract class Dialog<E> extends Applet {
	public Dialog(Applet parent, int w, int h) {
		super(parent, w, h);
		greedy = true;
	}
	
	private E rv;
	
	protected synchronized final void returnValue(E value) {
		rv = value;
		notifyAll();
	}
	
	private static synchronized final <T> T getDialog(Dialog<T> dialog) {
		try {
			synchronized(dialog) {
				dialog.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		System.out.println("ret truly");
		T r = dialog.rv;
		dialog.close();
		return r;
	}
	
	@Override
	protected void onClose() {
		returnValue(null);
	}
	
	public static ZonedDateTime getDateTimeDialog(Applet parent, ZonedDateTime old) {
		return getDialog(new DateTimeSelector(parent, old));
	}
	
	public static char[] getMessageDialog(Applet parent, String query, int maxlen, char[] old) {
		return getDialog(new MessageDialog(parent, query, maxlen, old));
	}
	
	public static char[] getMessageDialog(Applet parent, String query, int maxlen) {
		return getDialog(new MessageDialog(parent, query, maxlen));
	}
	
	public static int getMenuDialog(Applet parent, String query, String[] opts) {
		return getDialog(new MenuSelector(parent, query, opts));
	}
}
