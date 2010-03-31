package pl.graniec.gear.masterserver.registry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public class RegistryEntry {
	
	// static fields
	
	private static final int PORT_MAX = 65535;

	
	// non-static fields
	
	private final String serverAddr;
	
	private final int serverPort;
	
	
	// non-static methods
	
	public RegistryEntry(RegistryEntry other) {
		serverAddr = other.serverAddr;
		serverPort = other.serverPort;
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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((serverAddr == null) ? 0 : serverAddr.hashCode());
		result = prime * result + serverPort;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RegistryEntry other = (RegistryEntry) obj;
		if (serverAddr == null) {
			if (other.serverAddr != null) {
				return false;
			}
		} else if (!serverAddr.equals(other.serverAddr)) {
			return false;
		}
		if (serverPort != other.serverPort) {
			return false;
		}
		return true;
	}
	
}
