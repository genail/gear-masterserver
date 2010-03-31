package pl.graniec.gear.masterserver.packets;

import java.util.HashSet;
import java.util.Set;

import pl.graniec.gear.masterserver.NetGameEvent;
import pl.graniec.gear.masterserver.NetGameEventValue;
import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;
import pl.graniec.gear.masterserver.registry.RegistryEntry;

public final class ServerListPacket implements Packet {

	// static fields
	
	public static final String NAME = "SERVERLIST";
	
	private static final int HEADER_SIZE = 1;
	
	private static final int ENTRY_SIZE = 2;
	
	
	// non-static fields
	
	private final Set<RegistryEntry> entriesSet = new HashSet<RegistryEntry>();
	
	public final void addRegistryEntry(RegistryEntry entry) {
		entriesSet.add(entry);
	}
	
	@Override
	public NetGameEvent buildEvent() {
		
		final int valuesSize = HEADER_SIZE + (ENTRY_SIZE * entriesSet.size());
		final NetGameEventValue<?>[] values = new NetGameEventValue<?>[valuesSize];
		
		writeEntriesCount(values);
		
		int i = HEADER_SIZE;
		
		for (final RegistryEntry entry : entriesSet) {
			writeEntry(values, i, entry);
			i += ENTRY_SIZE;
		}
		
		return new NetGameEvent(NAME, values);
	}
	
	private void writeEntriesCount(NetGameEventValue<?>[] values) {
		values[0] = new NetGameEventValue<Integer>(entriesSet.size());
	}

	private void writeEntry(
			NetGameEventValue<?>[] values,
			int offset,
			RegistryEntry entry
	) {
		values[offset++] = new NetGameEventValue<String>(entry.getServerAddr());
		values[offset++] = new NetGameEventValue<Integer>(entry.getServerPort());
	}

	@Override
	public void parseEvent(NetGameEvent event) throws DataCorruptedException {
		throw new RuntimeException("not expected to be used");
	}

}
