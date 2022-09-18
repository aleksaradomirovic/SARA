package sara.apps.core;

import java.util.Collection;
import java.util.LinkedList;

import sara.Applet;
import sara.ConsoleApplet;
import sara.SARANET;
import sara.annotations.Independent;

@Independent(names={"comms"})
public final class Comms extends ConsoleApplet {
	public Comms(Applet parent, String[] args) {
		super(parent, "SARA Comms");
	}
	private static int exists = 0;

	@Override
	protected void init() {
		exists++;
		if(exists > 1) {
			System.err.println("Multiple instances of Comms exist! Closing...");
			close();
			return;
		}
		
		inputs = new LinkedList<>();
	}

	@Override
	protected void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onClose() {
		exists--;
	}
	
	Collection<CommsInputListener> inputs;
	
	private class CommsInputListener implements SARANET.SARANETInputHandler {
		@Override
		public void report(String msg) {
			writeln(msg);
		}

		@Override
		public void input(byte[] in) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}
	}
	
	@Override
	protected void handleCommand(char[] command) {
		// TODO Auto-generated method stub
		
	}
}
