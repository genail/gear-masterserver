package pl.graniec.gear.masterserver.packets;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import pl.graniec.gear.masterserver.NetGameEvent;
import pl.graniec.gear.masterserver.NetGameEventValue;
import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;

public class HelloPacket implements Packet {

	// static fields
	
	public static final String NAME = "HELLO";
	
	private static final int ARGS_COUNT = 2;
	
	
	// non-static fields
	
	private int versionMajor, versionMinor;
	
	
	// non-static methods
	
	public int getVersionMajor() {
		return versionMajor;
	}
	
	public int getVersionMinor() {
		return versionMinor;
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
		versionMajor = (Integer) arguments[0].getValue();
		versionMinor = (Integer) arguments[1].getValue();
	}

	private void checkArgumentTypes(NetGameEventValue<?>[] arguments)
			throws DataCorruptedException {
		PacketHelper.checkArgumentType(Integer.class, arguments[0]);
		PacketHelper.checkArgumentType(Integer.class, arguments[1]);
	}

}
