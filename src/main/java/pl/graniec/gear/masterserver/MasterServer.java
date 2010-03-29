package pl.graniec.gear.masterserver;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MasterServer{
	
	private static final Logger LOGGER =
		Logger.getLogger(MasterServer.class.getName());
	
//	private int port;
	
	private ServerSocket serverSocket;
	
	public MasterServer(int port) throws IOException {
		
//		this.port=port;
		
		try {
			serverSocket = new ServerSocket(port);
		} catch(IOException e){
			LOGGER.log(Level.SEVERE, "Cannot initialize socket", e);
		}
		
		while (true) {
			Socket client = serverSocket.accept();
			LOGGER.finest(
					"New client connected at "
					+  client.getInetAddress().getHostAddress()
					+ ". Creating SocketOperator");
			
			new Thread(new SocketOperator(client)).start();
		}
	}	
}

