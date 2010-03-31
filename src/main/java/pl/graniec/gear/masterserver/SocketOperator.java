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
import pl.graniec.gear.masterserver.packets.ListRequestPacket;
import pl.graniec.gear.masterserver.packets.RegisterPacket;
import pl.graniec.gear.masterserver.packets.ServerListPacket;
import pl.graniec.gear.masterserver.packets.SucceedPacket;
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
			clientSocket.setTcpNoDelay(true);
			
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
				disconnect();
			} catch(final IOException a) {
				// nothing to fear
			}
		}
	}
	
	private void disconnect() throws IOException {
		if (!clientSocket.isClosed()) {
			LOGGER.finer("disconnecting client " + clientSocket);
			clientSocket.close();
		}
	}
	
	private void processEvent(NetGameEvent event)
	throws DataCorruptedException, IOException {
		LOGGER.finer("received event " + event + " from " + clientSocket);
		
		final String eventName = event.getName();
		
		if (eventName.equals(HelloPacket.NAME)) {
			processHelloEvent(event);
		} else if (eventName.equals(RegisterPacket.NAME)) {
			processRegisterEvent(event);
		} else if (eventName.equals(KeepAlivePacket.NAME)) {
			processKeepAliveEvent(event);
		} else if (eventName.equals(ListRequestPacket.NAME)) {
			processListRequestEvent(event);
		} else {
			throw new DataCorruptedException("unknown event: " + eventName);
		}
	}

	private void processListRequestEvent(NetGameEvent event)
			throws IOException {
		final ServerListPacket serverListPacket = new ServerListPacket();
		
		fillServerListPacketWithRegistryEntries(serverListPacket);
		sendServerListEvent(serverListPacket);
		
		disconnect();
	}

	private void sendServerListEvent(final ServerListPacket serverListPacket)
			throws IOException {
		final NetGameEvent serverListEvent = serverListPacket.buildEvent();
		serverListEvent.toStream(outStream);
	}

	private void fillServerListPacketWithRegistryEntries(
			final ServerListPacket serverListPacket) {
		final Registry registry = Registry.getInstance();
		for (final RegistryEntry entry : registry.getEntries()) {
			serverListPacket.addRegistryEntry(entry);
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
			disconnect();
		}
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
		
		disconnect();
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
		
		tryToRegister(registryEntry);
		disconnect();
	}

	private void tryToRegister(final RegistryEntry registryEntry)
			throws IOException {
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
		LOGGER.finer("sending FAILED to " + clientSocket);
		
		final FailedPacket failedPacket = new FailedPacket();
		final NetGameEvent failedEvent = failedPacket.buildEvent();
		failedEvent.toStream(outStream);
	}

	private void sendSucceedEvent() throws IOException {
		LOGGER.finer("sending SUCCEESS to " + clientSocket);
		
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
			LOGGER.fine("client disconnected: " + clientSocket);
		} catch (final DataCorruptedException e) {
			LOGGER.log(Level.WARNING, "client sent corrupted data", e);
		} finally {
			StreamHelper.closeStream(inStream);
			StreamHelper.closeStream(outStream);
			
			try {
				disconnect();
			} catch (final IOException e) {
				// nothing to fear
			}
		}
	}
	
	private void disconnectIfThreadIsInterrupted() throws IOException {
		if (Thread.interrupted()) {
			LOGGER.fine("connected too long - disconnecting " + clientSocket);
			disconnect();
		}
	}

	private String getClientIpAddr() {
		return clientSocket.getInetAddress().getHostAddress();
	}
}
