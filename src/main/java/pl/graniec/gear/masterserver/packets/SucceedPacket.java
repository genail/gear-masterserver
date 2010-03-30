package pl.graniec.gear.masterserver.packets;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import pl.graniec.gear.masterserver.NetGameEvent;
import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;

public class SucceedPacket implements Packet {

	// static fields
	
	public static final String NAME = "SUCCEED";
	
	
	// non-static methods
	
	@Override
	public NetGameEvent buildEvent() {
		return new NetGameEvent(NAME);
		
	}

	@Override
	public void parseEvent(NetGameEvent event) throws DataCorruptedException {
		checkNotNull(event);
		checkArgument(event.getName().equals(NAME));
	}

}
