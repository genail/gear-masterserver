import java.util.logging.*;

public class Server {
	private String addr,name,map,mode;
	private int port,players,maxPlayers,identifier;
	private Logger logger;
	
	public Server(String addr, int port, String name, String map, String mode, int players, int maxPlayers, int identifier) throws ServerException{
		logger = Logger.getLogger("log");
		
		ServerException ex = null;
		if(!checkName(name))
			ex = new ServerException("Incorrect name!");
		if(!checkMap(map))
			ex = new ServerException("Incorrect map!");
		if(!checkMode(mode))
			ex = new ServerException("Incorrect mode!");
		if(ex!=null){
			logger.warning("Cannot create Server instance. Message: "+ex.getMessage());
			throw ex;
		}
			
		this.addr=addr;
		this.port=port;
		this.name=name;
		this.map=map;
		this.mode=mode;
		this.players=players;
		this.maxPlayers=maxPlayers;
		logger.finest("New server added: "+this.addr+":"+this.port);
	}
	public String getAddress(){
		return addr;
	}
	public int getPort(){
		return port;
	}
	public String getName(){
		return name;
	}
	public String getMap(){
		return map;
	}
	public String getMode(){
		return mode;
	}
	public int getPlayers(){
		return players;
	}
	public int getMaxPlayers(){
		return maxPlayers;
	}
	public boolean updateName(String name,int id) throws WrongIdentifier{
		if(identifier!=id){
			logger.warning("Couldn't update name for "+addr+":"+port+". Given identifier does not fit the initial one.");
			throw new WrongIdentifier();
		}
		if(!checkName(name)){
			logger.warning("Couldn't update name for "+addr+":"+port+". Incorrect name given ("+name+")");
			return false;
		}
		logger.fine("Updated name for "+addr+":"+port+". Old: "+this.name+" New: "+name);
		this.name=name;
		return true;
	}
	public boolean updateMap(String map, int id) throws WrongIdentifier{
		if(identifier!=id){
			logger.warning("Couldn't update map for "+addr+":"+port+". Given identifier does not fit the initial one.");
			throw new WrongIdentifier();
		}
		if(!checkMap(map)){
			logger.warning("Couldn't update map for "+addr+":"+port+". Incorrect map given ("+map+")");
			return false;
		}
		logger.fine("Updated map for "+addr+":"+port+". Old: "+this.map+" New: "+map);
		this.map=map;
		return true;
	}
	public boolean updateMode(String mode,int id) throws WrongIdentifier{
		if(identifier!=id){
			logger.warning("Couldn't update mode for "+addr+":"+port+". Given identifier does not fit the initial one.");
			throw new WrongIdentifier();
		}
		if(!checkMode(mode)){
			logger.warning("Couldn't update mode for "+addr+":"+port+". Incorrect name given ("+mode+")");
			return false;
		}
		logger.fine("Updated mode for "+addr+":"+port+". Old: "+this.mode+" New: "+mode);
		this.mode=mode;
		return true;
	}
	public boolean updatePlayers(int players, int id) throws WrongIdentifier{
		if(identifier!=id){
			logger.warning("Couldn't update players for "+addr+":"+port+". Given identifier does not fit the initial one.");
			throw new WrongIdentifier();
		}
		if(players<=maxPlayers && players>-1){
			logger.fine("Updated players for "+addr+":"+port+". Old: "+this.players+" New: "+players);
			this.players=players;
			return true;
		}
		logger.warning("Couldn't update players for "+addr+":"+port+". Number of players exceeded maximum or given number is less then 0 ("+players+")");
		return false;
	}
	public boolean updateMaxPlayers(int maxPlayers, int id) throws WrongIdentifier{
		if(identifier!=id){
			logger.warning("Couldn't update maxPlayers for "+addr+":"+port+". Given identifier does not fit the initial one.");
			throw new WrongIdentifier();
		}
		if(maxPlayers>0){
			logger.fine("Updated maxPlayers for "+addr+":"+port+". Old: "+this.maxPlayers+" New: "+maxPlayers);
			this.maxPlayers=maxPlayers;
			return true;
		}
		logger.warning("Couldn't update maxPlayers for "+addr+":"+port+". maxPlayers must be greater then 0!("+maxPlayers+")");
		return false;
	}
	public static boolean checkName(String name){
		return validateString(name,3,100);
	}
	public static boolean checkMode(String mode){
		return validateString(mode,3,100);
	}
	public static boolean checkMap(String map){
		return validateString(map,3,100);
	}
	private static boolean validateString(String string, int min, int max){
		return string.matches("[a-zA-Z0-9+-_\\.,;:!@#$%^&*\\(\\)|]{"+min+","+max+"}");
	}
	@SuppressWarnings("serial")
	public class ServerException extends Exception{
		public ServerException(String msg){
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class WrongIdentifier extends Exception{
		public WrongIdentifier(){
			super("Given identifier does not fit the initial one. Aborting.");
		}
	}
}