package pl.graniec.gear.masterserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;
import pl.graniec.gear.masterserver.packets.FailedPacket;
import pl.graniec.gear.masterserver.packets.HelloPacket;
import pl.graniec.gear.masterserver.packets.KeepAlivePacket;
import pl.graniec.gear.masterserver.packets.RegisterPacket;
import pl.graniec.gear.masterserver.packets.SucceedPacket;
import pl.graniec.gear.masterserver.packets.UpdatePacket;
import pl.graniec.gear.masterserver.registry.Registry;
import pl.graniec.gear.masterserver.registry.RegistryEntry;

class SocketOperator implements Runnable{
	
	// static fields
	
	private static final Logger LOGGER =
		Logger.getLogger(SocketOperator.class.getName());
	
	private static final int SOCKET_TIMEOUT = 500;
	
	
	// non-static fields
	
	private final Socket clientSocket;
	
	private InputStream inStream;
	
	private OutputStream outStream;
	
	
	// non-static methods
	
	public SocketOperator(Socket clientSocket) {
		this.clientSocket = clientSocket;
		try {
			clientSocket.setSoTimeout(SOCKET_TIMEOUT);
			
			inStream = clientSocket.getInputStream();
			outStream = clientSocket.getOutputStream();
		} catch (final IOException e) {
			LOGGER.log(
					Level.WARNING,
					"error while initializing client socket",
					e
			);
			
			StreamHelper.closeStream(outStream);
			StreamHelper.closeStream(inStream);
			
			try {
				closeConnection();
			} catch(final IOException a) {
				// nothing to fear
			}
		}
	}
	
	private void closeConnection() throws IOException {
		clientSocket.close();
	}
	
	private void processEvent(NetGameEvent event)
	throws DataCorruptedException, IOException {
		final String eventName = event.getName();
		
		if (eventName.equals(HelloPacket.NAME)) {
			processHelloEvent(event);
		} else if (eventName.equals(RegisterPacket.NAME)) {
			processRegisterEvent(event);
		} else if (eventName.equals(KeepAlivePacket.NAME)) {
			processKeepAliveEvent(event);
		} else if (eventName.equals(UpdatePacket.NAME)) {
			processUpdateEvent(event);
		} else {
			throw new DataCorruptedException("unknown event: " + eventName);
		}
	}

	private void processHelloEvent(NetGameEvent event)
			throws DataCorruptedException, IOException {
		final HelloPacket helloPacket = new HelloPacket();
		helloPacket.parseEvent(event);
		
		if (helloPacket.getVersionMajor() == Protocol.VERSION_MAJOR) {
			sendSucceedEvent();
		} else {
			sendFailedEvent();
			closeConnection();
		}
	}

	private void processUpdateEvent(NetGameEvent event)
			throws DataCorruptedException, IOException {
		final UpdatePacket updatePacket = new UpdatePacket();
		updatePacket.parseEvent(event);
		
		final RegistryEntry registryEntry = new RegistryEntry(
				getClientIpAddr(),
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
		
		final int serverPort = keepAlivePacket.getServerPort();
		
		final RegistryEntry registryEntry = new RegistryEntry(
				getClientIpAddr(), serverPort
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
		
		
		final RegistryEntry registryEntry = new RegistryEntry(
				getClientIpAddr(),
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
			while (clientSocket.isConnected()) {
				
				final NetGameEvent event = NetGameEvent.fromStream(inStream);
				processEvent(event);
				
				disconnectIfThreadIsInterrupted();
			}
		} catch (final IOException e) {
			LOGGER.fine("client " + getClientIpAddr() + " disconnected");
		} catch (final DataCorruptedException e) {
			LOGGER.log(Level.WARNING, "client sent corrupted data", e);
		} finally {
			StreamHelper.closeStream(inStream);
			StreamHelper.closeStream(outStream);
			
			try {
				closeConnection();
			} catch (final IOException e) {
				// nothing to fear
			}
		}
	}
	
	private void disconnectIfThreadIsInterrupted() throws IOException {
		if (Thread.interrupted()) {
			closeConnection();
		}
	}

	private String getClientIpAddr() {
		return clientSocket.getInetAddress().getHostAddress();
	}
}
