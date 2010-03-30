package pl.graniec.gear.masterserver.packets;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import pl.graniec.gear.masterserver.NetGameEvent;
import pl.graniec.gear.masterserver.NetGameEventValue;
import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;

public class KeepAlivePacket implements Packet {

	// static fields
	
	public static final String NAME = "KEEPALIVE";
	
	private static final int ARGS_COUNT = 1;
	
	
	// non-static fields
	
	private int serverPort;
	
	
	// non-static methods
	
	public int getServerPort() {
		return serverPort;
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
	}

	private void checkArgumentTypes(NetGameEventValue<?>[] arguments)
			throws DataCorruptedException {
		PacketHelper.checkArgumentType(Integer.class, arguments[0]);
	}

}
