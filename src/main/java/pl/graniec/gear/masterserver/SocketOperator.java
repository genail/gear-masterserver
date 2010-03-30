package pl.graniec.gear.masterserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;
import pl.graniec.gear.masterserver.packets.FailedPacket;
import pl.graniec.gear.masterserver.packets.KeepAlivePacket;
import pl.graniec.gear.masterserver.packets.RegisterPacket;
import pl.graniec.gear.masterserver.packets.SucceedPacket;
import pl.graniec.gear.masterserver.packets.UpdatePacket;
import pl.graniec.gear.masterserver.registry.Registry;
import pl.graniec.gear.masterserver.registry.RegistryEntry;

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
	
	private void processEvent(NetGameEvent event)
	throws DataCorruptedException, IOException {
		final String eventName = event.getName();
		
		if (eventName.equals(RegisterPacket.NAME)) {
			processRegisterEvent(event);
		} else if (eventName.equals(KeepAlivePacket.NAME)) {
			processKeepAliveEvent(event);
		} else if (eventName.equals(UpdatePacket.NAME)) {
			processUpdateEvent(event);
		} else {
			throw new DataCorruptedException("unknown event: " + eventName);
		}
	}

	private void processUpdateEvent(NetGameEvent event)
			throws DataCorruptedException, IOException {
		final UpdatePacket updatePacket = new UpdatePacket();
		updatePacket.parseEvent(event);
		
		final String serverAddr =
			clientSocket.getInetAddress().getHostAddress();
		
		final RegistryEntry registryEntry = new RegistryEntry(
				serverAddr,
				updatePacket.getServerPort()
		);
		
		registryEntry.setServerName(updatePacket.getServerName());
		registryEntry.setCurrentMapName(updatePacket.getCurrentMapName());
		
		if (updateEntry(registryEntry)) {
			sendSucceedEvent();
		} else {
			sendFailedEvent();
		}
	}

	private boolean updateEntry(RegistryEntry entry) {
		final Registry registry = Registry.getInstance();
		return registry.updateEntry(entry);
	}

	private void processKeepAliveEvent(NetGameEvent event)
			throws DataCorruptedException, IOException {
		final KeepAlivePacket keepAlivePacket = new KeepAlivePacket();
		keepAlivePacket.parseEvent(event);
		
		final String serverAddr =
			clientSocket.getInetAddress().getHostAddress();
		final int serverPort = keepAlivePacket.getServerPort();
		
		final RegistryEntry registryEntry = new RegistryEntry(
				serverAddr, serverPort
		);
		
		if (keepAliveEntry(registryEntry)) {
			sendSucceedEvent();
		} else {
			sendFailedEvent();
		}
	}

	private boolean keepAliveEntry(RegistryEntry entry) {
		final Registry registry = Registry.getInstance();
		return registry.keepAlive(entry);
	}

	private void processRegisterEvent(NetGameEvent event)
			throws DataCorruptedException, IOException {
		final RegisterPacket registerPacket = new RegisterPacket();
		registerPacket.parseEvent(event);
		
		
		final String serverAddr =
			clientSocket.getInetAddress().getHostAddress();
		
		final RegistryEntry registryEntry = new RegistryEntry(
				serverAddr,
				registerPacket.getServerPort()
		);
		
		registryEntry.setServerName(registerPacket.getServerName());
		registryEntry.setCurrentMapName(registerPacket.getCurrentMapName());
		
		
		if (registerEntry(registryEntry)) {
			sendSucceedEvent();
		} else {
			sendFailedEvent();
		}
	}
	
	private boolean registerEntry(RegistryEntry entry) {
		final Registry registry = Registry.getInstance();
		return registry.addEntry(entry);
	}

	private void sendFailedEvent() throws IOException {
		final FailedPacket failedPacket = new FailedPacket();
		final NetGameEvent failedEvent = failedPacket.buildEvent();
		failedEvent.toStream(outStream);
	}

	private void sendSucceedEvent() throws IOException {
		final SucceedPacket succeedPacket = new SucceedPacket();
		final NetGameEvent succeedEvent = succeedPacket.buildEvent();
		succeedEvent.toStream(outStream);
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
