import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.*;

public class Registry{
	private static Logger log = Logger.getLogger("Log");
	private HashMap<String,Server> serverList;
	private HashMap<String,Date> banList;
	
	public Registry(){
		serverList = new HashMap<String,Server>();
		banList = new HashMap<String,Date>();
		log.info("New registry created.");
	}
	public boolean updateEntry(String addr, int port, int id, int players, int maxPlayers, String mode, String map){
		if(!serverList.containsKey(addr+":"+port)){
			log.warning("Attempt to update unregistered entry.");
			return false;
		}
		Server server = serverList.get(addr+":"+port);
		if(server.checkMap(map) && server.checkMaxPlayers(maxPlayers) && server.checkMode(mode)
				&& server.checkPlayers(players, maxPlayers) && server.checkId(id)){
			try{
				server.updateMaxPlayers(maxPlayers, id);
				server.updatePlayers(players, id);
				server.updateMode(mode,id);
				server.updateMap(map, id);
			} catch(WrongIdentifier e){
				log.severe("Unexpected Server.WrongIdentifier! Some records might have been already changed! Message:"+ e.getMessage());
				return false;
			} catch(ServerException e){
				log.severe("Unexpected Server.ServerException! Some records might have been already changed! Message:"+ e.getMessage());
				return false;
			}
			log.fine("Updated server "+addr+":"+port);
			return true;
		} else {
			log.warning("Could not update "+addr+":"+port);
			return false;
		}
	}
	public LinkedList<Server> getServerList(){
		return new LinkedList<Server>(serverList.values());
	}
	public boolean isBanned(String ip){
		return banList.containsKey(ip) && banList.get(ip).after(new Date());
	}
	public void registerEntry(Server server) throws EntryAlreadyExists, ClientBanned{
		if(serverList.containsValue(server) || serverList.containsKey(server.getAddress()+":"+server.getPort()))
			throw new EntryAlreadyExists();
		if(banList.containsKey(server.getAddress()) && banList.get(server.getAddress()).after(new Date()))
			throw new ClientBanned();
		serverList.put(server.getAddress()+":"+server.getPort(), server);
	}
}
@SuppressWarnings("serial")
class EntryAlreadyExists extends Exception{
	public EntryAlreadyExists(){
		super("Entry already exists in the registry. Registration failed.");
	}
}
@SuppressWarnings("serial")
class ClientBanned extends Exception{
	public ClientBanned(){
		super("Client is banned. Registration failed.");
	}
}