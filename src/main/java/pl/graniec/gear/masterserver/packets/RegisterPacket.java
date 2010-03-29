package pl.graniec.gear.masterserver.packets;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import pl.graniec.gear.masterserver.NetGameEvent;
import pl.graniec.gear.masterserver.NetGameEventValue;
import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;

public final class RegisterPacket implements Packet {

	// static fields
	
	private static final int ARGS_COUNT = 3;
	
	public static final String NAME = "REGISTER";
	
	
	// non-static fields
	
	private int serverPort;
	
	private String serverName;
	
	private String currentMapName;
	
	
	// non-static methods
	
	public String getServerName() {
		return serverName;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	
	public String getCurrentMapName() {
		return currentMapName;
	}
	
	@Override
	public NetGameEvent buildEvent() {
		throw new RuntimeException("not expected to be used");
	}

	@Override
	public void parseEvent(NetGameEvent event) throws DataCorruptedException {
		checkNotNull(event);
		checkArgument(event.getName().equals(NAME));
		
		final NetGameEventValue<?>[] arguments = event.getArguments();
		
		PacketHelper.checkArgsCount(ARGS_COUNT, arguments);
		checkArgumentTypes(arguments);
		
		assignArgumentsToFields(arguments);
	}

	private void assignArgumentsToFields(NetGameEventValue<?>[] arguments) {
		serverPort = (Integer) arguments[0].getValue();
		serverName = (String) arguments[1].getValue();
		currentMapName = (String) arguments[2].getValue();
	}

	private void checkArgumentTypes(NetGameEventValue<?>[] arguments)
			throws DataCorruptedException {
		PacketHelper.checkArgumentType(Integer.class, arguments[0]);
		PacketHelper.checkArgumentType(String.class, arguments[1]);
		PacketHelper.checkArgumentType(String.class, arguments[2]);
	}

}
