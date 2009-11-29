import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Registry used to store all servers that that are currently available.
 * @author Norfavrell
 * @version 1.0
 */
public class Registry {
	private HashMap<String,Server> serverList;
	/**
	 * Main registry constructor.
	 */
	public Registry(){
		serverList=new HashMap<String,Server>();
	}
	/**
	 * Attempt to restore registry from file.
	 * Current registry will not be cleared.
	 * Any entries will not be overwritten.
	 * @param file File from which registry should be restored.
	 * @return Number of restored entries or -1 on error.
	 */
	public int restore(File file){
		return restore(file,false,false);
	}
	/**
	 * Attempt to restore registry from file.
	 * Current registry will not be cleared.
	 * @param file File from which registry should be restored.
	 * @param overwrite Indicates weather entries should be overwritten if already in registry.
	 * @return Number of restored entries or -1 on error.
	 */
	public int restore(File file, boolean overwrite){
		return restore(file,false,overwrite);
	}
	/**
	 * Attempt to restore registry from file.
	 * @param file File from which registry should be restored.
	 * @param clear Indicates weather current registry should be cleared before attepting restoration.
	 * @param overwrite Indicates weather entries should be overwritten if already in registry.
	 * @return Number of restored entries or -1 on error.
	 */
	public int restore(File file, boolean clear, boolean overwrite){
		BufferedReader in = null;
		try{
			in = new BufferedReader(new FileReader(file));
			if(clear)
				serverList.clear();
			int loaded=0;
			while(in.ready()){
				String data=in.readLine();
				String[] info = data.split(""+(char)16);
				if(info.length!=7){
					continue;
				}
				if(serverList.containsKey(info[0]+":"+info[1]))
					if(!overwrite)
						continue;
					else
						serverList.remove(info[0]+":"+info[1]);
				serverList.put(info[0]+":"+info[1], new Server(info[0],Integer.parseInt(info[1]),info[2],info[3],Integer.parseInt(info[4]),Integer.parseInt(info[5]),info[6]));
				loaded++;
			}
			in.close();
			return loaded;
		} catch(Exception a){	
			System.out.print("\n\n--Exception in Registry.restore("+file+","+clear+","+overwrite+")\n"+a.getMessage());
			System.out.print("\n--\n");
			return -1;
		} finally {
			try{in.close();}catch(IOException e){
				System.out.print("\n\n--Exception in Registry.restore("+file+","+clear+","+overwrite+")\n"+a.getMessage());
				System.out.print("\n--\n");
				return -1;
			}
		}
	}
	/**
	 * Backups entries to file for later restoration.
	 * File will be cleared before processing.
	 * @param file File to which entries should be written to.
	 * @return Number of written entries or -1 on error.
	 */
	public int backup(File file){
		return backup(file,false);
	}
	/**
	 * Backups entries to file for later restoration.<br/><b>WARNING</b> If clear is set to true, overwrite parameter parameter for restore should also be true.
	 * @param file File to which entries should be written to.
	 * @param clear Indicates weather clear file before backupping.
	 * @return Number of written entries or -1 on error.
	 */
	public int backup(File file, boolean clear){
		DataOutputStream out;
		try{
			out = new DataOutputStream(new FileOutputStream(file,clear));
			int saved=0;
			for(Server server: new LinkedList<Server>(serverList.values())){
				try{
					out.writeBytes(server.getAddress()+(char)16+""+server.getPort()+
							(char)16+server.getName()+(char)16+server.getMode()+(char)16+
							server.getPlayers()+(char)16+""+server.getMaxPlayers()+
							(char)16+server.getMap()+"\n");
				} catch(IOException e){}//TODO: This is to allow SOME records to be saved in case of error
				saved++;
			}
			out.close();
			return saved;
		} catch(IOException e){
			System.out.print("\n\n--Exception in Registry.backup("+file+","+clear+")\n"+e.getMessage());
			System.out.print("\n--\n");
			return -1;
		} finally {
			out.close();
		}
	}
	/**
	 * Add server to server list. Record will not be overwritten!
	 * @param server Server that has to be added. 
	 * @return true if server was added successfully or false in other case.
	 */
	public boolean add(Server server){
		return add(server,false);
	}
	/**
	 * Add server to server list.
	 * @param server Server that has to be added. 
	 * @param overwrite Indicates weather record should be overwritten if it already exists in server list.
	 * @return true if server was added successfully or false in other case.
	 */
	public boolean add(Server server, boolean overwrite){
		if(serverList.containsKey(server.getAddress()+":"+server.getPort()))
			if(overwrite)
				serverList.remove(server.getAddress()+":"+server.getPort());
			else
				return false;
		serverList.put(server.getAddress()+":"+server.getPort(), server);
		return true;
	}
	/**
	 * Get server by it's IP address and port it is running on.
	 * @param address Server's IP address.
	 * @param port Port on which server is running on.
	 * @return Server object.
	 */
	public Server get(String address, int port){
		return serverList.get(address+":"+port);
	}
}

/**
 * Keeps all informations about game server.
 * @author Norfavrell
 * @version 1.0
 */
class Server{
	private String address,name,mode,map;
	private int port,players,maxPlayers;
	
	/**
	 * Main constructor.
	 * @param address Server's IP address.
	 * @param port Port on which server accept connections.
	 * @param name Name of the server.
	 * @param mode Server's mode.
	 * @param players Number of players currently on the server.
	 * @param maxPlayers Maximum number of players this server can handle.
	 * @param map Name of map currently playing on the server.
	 */
	public Server(String address, int port, String name, String mode, int players, int maxPlayers, String map){
		this.address=address;
		this.port=port;
		this.name=name;
		this.mode=mode;
		this.players=players;
		this.maxPlayers=maxPlayers;
		this.map=map;
	}
	/**
	 * Returns IP address of the server.
	 * @return IP address of the server.
	 */
	public String getAddress(){
		return address;
	}
	/**
	 * Returns port on which server is accepting connections.
	 * @return Port on which server is accepting connections. 
	 */
	public int getPort(){
		return port;
	}
	/**
	 * Returns name of the server.
	 * @return Name of the server.
	 */
	public String getName(){
		return name;
	}
	/**
	 * Return server's mode.
	 * @return Server's mode.
	 */
	public String getMode(){
		return mode;
	}
	/**
	 * Return number of players currently playing on the server.
	 * @return Number of players currently playing on the server.
	 */
	public int getPlayers(){
		return players;
	}
	/**
	 * Return maximum number of players this server can handle.
	 * @return Maximum number of players this server can handle.
	 */
	public int getMaxPlayers(){
		return maxPlayers;
	}
	/**
	 * Return name of map that is currently playing on this server.
	 * @return Name of map that is currently playing on this server.
	 */
	public String getMap(){
		return map;
	}
	/**
	 * Updates server's name.
	 * @param name New name for server.
	 */
	public void setName(String name){
		this.name=name;
	}
	/**
	 * Updates server's mode.
	 * @param mode New mode for server.
	 */
	public void setMode(String mode){
		this.mode=mode;
	}
	/**
	 * Updates players number.
	 * @param players New players number.
	 */
	public void setPlayers(int players){
		this.players=players;
	}
	/**
	 * Updates maximum number of players this server can handle.
	 * @param maxPlayers Maximum number of players this server can handle.
	 */
	public void setMaxPlayers(int maxPlayers){
		this.maxPlayers=maxPlayers;
	}
	/**
	 * Updates name of map that is currently playing on this server.
	 * @param map Name of map that is currently playing on this server.
	 */
	public void setMap(String map){
		this.map=map;
	}
}