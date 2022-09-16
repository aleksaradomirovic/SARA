package sara;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class SARANET {
	private static final HashMap<InetAddress,LinkedList<SocketHandler>> sockets = new HashMap<>();
	
	private static SocketHandler connect(InetAddress pubAddress, int pubPort, InetAddress locAddress, int locPort) throws IOException {
		LinkedList<SocketHandler> bucket = sockets.get(pubAddress);
		if(bucket == null) {
			SocketHandler h = new SocketHandler(new Socket(pubAddress, pubPort, locAddress, locPort >= 0 ? locPort : 0));
			bucket = new LinkedList<>();
			bucket.add(h);
			sockets.put(pubAddress, bucket);
			return h;
		}
		
		ListIterator<SocketHandler> iter = bucket.listIterator();
		SocketHandler h;
		while(iter.hasNext()) {
			if(!(h = iter.next()).active) {
				iter.remove();
				continue;
			}
			Socket s = h.address;
			if(s.getPort() == pubPort &&
					(locAddress == null || (locAddress.equals(h.address.getLocalAddress()) &&
					locPort == h.address.getLocalPort())))
				return h;
		}

		h = new SocketHandler(new Socket(pubAddress, pubPort, locAddress, locPort >= 0 ? locPort : 0));
		bucket.add(h);
		return h;
	}
	
	public static void link(SARANETInputHandler output, InetAddress pubAddress, int pubPort,
			InetAddress locAddress, int locPort) {
		try {
			connect(pubAddress, pubPort, locAddress, locPort).inputs.add(output);
		} catch (IOException e) {
			output.report(e.getMessage());
		}
	}
	
	public static void link(SARANETInputHandler output, InetAddress pubAddress, int pubPort) {
		link(output, pubAddress, pubPort, null, -1);
	}
	
	private static int delay = 50;
	private static class SocketHandler implements Runnable {
		private final Socket address;
		private final CopyOnWriteArrayList<SARANETInputHandler> inputs;
		private boolean active = true;
		
		SocketHandler(Socket s) {
			address = s;
			inputs = new CopyOnWriteArrayList<>();
		}

		@Override
		public void run() {
			int attempts = 0;
			while(!address.isConnected()) {
				if(attempts >= 10) {
					report("Failed after "+attempts+" attempts of trying to connect!");
					active = false;
					return;
				}
				try { Thread.sleep(100); }
				catch (InterruptedException e) {}
				attempts++;
			}
			
			try {
				InputStream is = address.getInputStream();
				while(!address.isClosed()) {
					while(is.available() <= 0) {
						try { Thread.sleep(delay); }
						catch(InterruptedException e) {}
					}
					
					byte[] in = is.readAllBytes();
					for(SARANETInputHandler i : inputs) {
						i.input(in.clone());
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
				report(e.getMessage());
			}
			active = false;
		}
		
		private void report(String msg) {
			for(SARANETInputHandler i : inputs) {
				i.report(msg);
			}
		}
	}
	
	public static interface SARANETInputHandler {
		default void report(String msg) {}
		void input(byte[] in);
		void flush();
	}
}
