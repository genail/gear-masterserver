package pl.graniec.gear.masterserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;

class SocketOperator implements Runnable{
	
	private static final Logger LOGGER =
		Logger.getLogger(SocketOperator.class.getName());
	
	private final Socket clientSocket;
	
	private InputStream inStream;
	
	private OutputStream outStream;
	
	private boolean goOn=true;
	
	private final int pvminor=-1, pvmajor=-1;
	
	public SocketOperator(Socket clientSocket) {
		this.clientSocket = clientSocket;
		
		try {
			inStream = clientSocket.getInputStream();
			outStream = clientSocket.getOutputStream();
			
			LOGGER.finest(
					"New SocketOperator for client at "
					+ clientSocket.getInetAddress().getHostAddress()
					+ " has been created."
			);
		} catch (final IOException e) {
			LOGGER.log(
					Level.WARNING,
					"Cannot get I/O for client at "
					+ clientSocket.getInetAddress().getHostAddress(),
					e
			);
			
			StreamHelper.closeStream(outStream);
			StreamHelper.closeStream(inStream);
			
			try {
				clientSocket.close();
			} catch(final IOException a) {
				// nothing to fear
			}
			
			goOn = false;
		}
	}
	
	private void processEvent(NetGameEvent event) {
		final String eventName = event.getName();
		
		if (eventName.equals("REGISTER")) {
			processRegisterEvent(event);
		} else if (eventName.equals("KEEPALIVE")) {
			processKeepAliveEvent(event);
		} else if (eventName.equals("UPDATE")) {
			processUpdateEvent(event);
		} else if (eventName.equals("UNREGISTER")) {
			processUnregisterEvent(event);
		}
	}
	
	private void processUnregisterEvent(NetGameEvent event) {
		
	}

	private void processUpdateEvent(NetGameEvent event) {
		
	}

	private void processKeepAliveEvent(NetGameEvent event) {
		
	}

	private void processRegisterEvent(NetGameEvent event) {
		
	}

	public void run() {
		try{
			while (goOn) {
				final NetGameEvent event = NetGameEvent.fromStream(inStream);
				System.out.println(event);
				
				processEvent(event);
			}
		} catch(final IOException e) {
			if(e.getMessage()==null) {
				LOGGER.finest("Client at "+clientSocket.getInetAddress().getHostAddress()+" disconnected."); // TODO: Error occurse once outputstream is closed.
			} else {
				LOGGER.warning("I/O Exception occured with client at "+clientSocket.getInetAddress().getHostAddress()+
					". Client will be dropped! Message: "+e.getMessage());
			}
		} catch (final DataCorruptedException e) {
			LOGGER.log(Level.WARNING, "client sent corrupted data", e);
		} finally {
			StreamHelper.closeStream(inStream);
			StreamHelper.closeStream(outStream);
			
			try {
				clientSocket.close();
			} catch (final IOException e) {
				// nothing to fear
			}
		}
	}
}
