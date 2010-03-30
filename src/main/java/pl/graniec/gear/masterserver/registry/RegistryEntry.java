package pl.graniec.gear.masterserver.registry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public class RegistryEntry {
	
	// static fields
	
	private static final int PORT_MAX = 65535;

	
	// non-static fields
	
	private final String serverAddr;
	
	private final int serverPort;
	
	private String serverName = "";
	
	private String currentMapName = "";

	
	// non-static methods
	
	public RegistryEntry(RegistryEntry other) {
		serverAddr = other.serverAddr;
		serverPort = other.serverPort;
		serverName = other.serverName;
		currentMapName = other.currentMapName;
	}
	
	
	public RegistryEntry(String serverAddr, int serverPort) {
		checkNotNull(serverAddr);
		checkArgument(serverPort > 0 && serverPort <= PORT_MAX);
		
		this.serverAddr = serverAddr;
		this.serverPort = serverPort;
	}
	
	public String getServerAddr() {
		return serverAddr;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public String getCurrentMapName() {
		return currentMapName;
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public void setCurrentMapName(String currentMapName) {
		this.currentMapName = currentMapName;
	}
	
}
