import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class MasterServer{
	private static Logger log = Logger.getLogger(MasterServer.class.getName());
	private int port;
	private ServerSocket ssocket;
	
	public MasterServer(int port) throws CannotInitializeServerSocket, IOException{
		this.port=port;
		try{
			ssocket = new ServerSocket(port);
		} catch(IOException e){
			log.severe("Cannot initiualize ServerSocket! Error: "+e.getMessage());
			throw new CannotInitializeServerSocket(e);
		}
		while(true){
			Socket client = ssocket.accept();
			log.finest("New client connected at "+client.getInetAddress().getHostAddress()+". Creating SocketOperator");
			new Thread(new SocketOperator(client)).run();
		}
	}	
}
class SocketOperator implements Runnable{
	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	private static Logger logger = Logger.getLogger(SocketOperator.class.getName());
	private boolean goOn=true;
	
	public SocketOperator(Socket client){
		this.client=client;
		try{
			in=new DataInputStream(client.getInputStream());
			out=new DataOutputStream(client.getOutputStream());
			logger.finest("New SocketOperator for client at "+client.getInetAddress().getHostAddress()+" has been created.");
		} catch(IOException e){
			logger.warning("Cannot get I/O for client at "+client.getInetAddress().getHostAddress()+". Error: "+e.getMessage());
			try{client.close();
			in.close();
			out.close();} catch(IOException a) {}//TODO: OH CRAP
			goOn=false;
		}
	}
	public void run() {
		try{
			while(goOn){
				int length = in.readUnsignedShort();
				byte[] data = new byte[length];
				in.read(data, 0, length);
				logger.finest("Data received from client at "+client.getInetAddress().getHostAddress()+". Processing");
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(data));
				Element root = doc.getDocumentElement();
				System.out.println(root.getTagName());
			}
		} catch(IOException e){
			System.out.println(client.isClosed());
			if(client.isClosed())
				logger.finest("Client at "+client.getInetAddress().getHostAddress()+" disconnected."); // TODO: Error occurse once outputstream is closed.
			else
				logger.warning("I/O Exception occured with client at "+client.getInetAddress().getHostAddress()+
					". Client will be dropped! Message: "+e.getMessage());
		} catch(ParserConfigurationException e){
			logger.warning("ParserConfigurationException occured for client at "+client.getInetAddress().getHostAddress()+
					". Probably corrupted message data. Client will be dropped. Message: "+e.getMessage());
		} catch(SAXException e){
			logger.warning("SAXException occured for client at "+client.getInetAddress().getHostAddress()+
					". Probably corrupted message data. Client will be dropped. Message: "+e.getMessage());
		}finally{
			try{ client.close(); in.close(); out.close(); }catch(IOException a){}//TODO: And again
		}
	}
}

@SuppressWarnings("serial")
class SocketOperatorException extends Exception{
	public SocketOperatorException(Throwable cause){
		super(cause);
	}
}
@SuppressWarnings("serial")
class CannotInitializeServerSocket extends Exception{
	public CannotInitializeServerSocket(Throwable cause){
		super(cause);
	}
}