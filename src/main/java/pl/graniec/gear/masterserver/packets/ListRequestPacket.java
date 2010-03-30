package pl.graniec.gear.masterserver.packets;

import pl.graniec.gear.masterserver.NetGameEvent;
import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;

public final class ListRequestPacket implements Packet {

	// static fields
	
	public static final String NAME = "LISTREQUEST";
	
	
	// non-static methods
	
	@Override
	public NetGameEvent buildEvent() {
		throw new RuntimeException("not expected to be used");
	}

	@Override
	public void parseEvent(NetGameEvent event) throws DataCorruptedException {
		// TODO some options in future
	}

}
