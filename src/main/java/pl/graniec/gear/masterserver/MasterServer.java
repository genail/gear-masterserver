package pl.graniec.gear.masterserver;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.graniec.gear.masterserver.registry.RegistryCleaner;


public class MasterServer implements Runnable {
	
	// static fields
	
	private static final Logger LOGGER =
		Logger.getLogger(MasterServer.class.getName());
	
	private static final int DEFAULT_PORT = 37005;
	
	private static final int CLEAN_PERIOD_MS = 10000;
	
	private static final int OLD_CONNECTION_TIME_MS = 10000;
	
	
	// non-static fields
	
	private int port = DEFAULT_PORT;
	
	private ServerSocket serverSocket;
	
	private final Map<Thread, Long> clientThreadsTimeMap =
		new HashMap<Thread, Long>();
	
	private final ThreadFactory threadFactory =
		Executors.defaultThreadFactory();
	
	private final Timer cleanTimer = new Timer();
	
	// non-static methods
	
	public MasterServer() {
		startConnectionCleaner();
		startRegistryCleaner();
	}

	private void startRegistryCleaner() {
		RegistryCleaner.getInstance().start();
	}

	private void startConnectionCleaner() {
		final TimerTask cleanerTask = new TimerTask() {
			@Override
			public void run() {
				cleanOldConnections();
			}
		};
		
		cleanTimer.schedule(cleanerTask, CLEAN_PERIOD_MS, CLEAN_PERIOD_MS);
	}
	
	private void cleanOldConnections() {
		final List<Thread> threadsToClean = retreiveThreadsToClean();
		sendInterrupted(threadsToClean);
		removeThreadsFromTimeMap(threadsToClean);
	}

	private void removeThreadsFromTimeMap(final List<Thread> threadsToClean) {
		synchronized (clientThreadsTimeMap) {
			for (final Thread t : threadsToClean) {
				clientThreadsTimeMap.remove(t);
			}
		}
	}

	private void sendInterrupted(final List<Thread> threadsToClean) {
		for (final Thread t : threadsToClean) {
			t.interrupt();
		}
	}
	
	private List<Thread> retreiveThreadsToClean() {
		final List<Thread> threadsToClean = new LinkedList<Thread>();
		final long now = System.currentTimeMillis();
		
		synchronized (clientThreadsTimeMap) {
			for (final Entry<Thread, Long> entry : clientThreadsTimeMap.entrySet()) {
				final long delta = now - entry.getValue();
				
				if (delta >= OLD_CONNECTION_TIME_MS) {
					threadsToClean.add(entry.getKey());
				}
			}
		}
		
		return threadsToClean;
	}
	
	public final void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public final void run() {
		checkState(serverSocket == null);
		
		try {
			startServer();
			listenForClients();
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "i/o error while running server: " + e);
		}
	}

	private void listenForClients() throws IOException {
		while (!serverSocket.isClosed()) {
			final Socket client = serverSocket.accept();
			processNewClient(client);
		}
	}

	private void processNewClient(final Socket client) {
		LOGGER.fine("new connection: " + client);
		
		final SocketOperator socketOperator = new SocketOperator(client);
		final Thread clientThread = threadFactory.newThread(socketOperator);
		final long creationTime = System.currentTimeMillis();

		synchronized (clientThreadsTimeMap) {
			clientThreadsTimeMap.put(clientThread, creationTime);
		}
		
		clientThread.start();
	}

	private void startServer() throws IOException {
		LOGGER.info("server is starting on port " + port);
		serverSocket = new ServerSocket(port);
	}
}

