package pl.graniec.gear.masterserver.packets;

import pl.graniec.gear.masterserver.NetGameEvent;
import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;

public interface Packet {

	NetGameEvent buildEvent();
	
	void parseEvent(NetGameEvent event) throws DataCorruptedException;
}
