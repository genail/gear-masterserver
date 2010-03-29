package pl.graniec.gear.masterserver;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class Registry {
	
	// static fields
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER =
		Logger.getLogger(Registry.class.getName());
	
	
	private static final Registry INSTANCE = new Registry();
	
	
	// static methods
	
	public static Registry getInstance() {
		return INSTANCE;
	}
	
	
	// non-static fields
	
	private final Map<String, RegistryEntry> entriesMap =
		new HashMap<String, RegistryEntry>();
	
	private final Set<RegistryEntry> cleanseCandidatesSet =
		new HashSet<RegistryEntry>();
	
	
	// non-static methods
	
	private Registry() {
		// empty
	}
	
	private String toEntryKey(RegistryEntry entry) {
		return toEntryKey(entry.getServerAddr(), entry.getServerPort());
	}
	
	private String toEntryKey(String serverAddr, int serverPort) {
		return serverAddr + ":" + serverPort;
	}
	
	/**
	 * @param entry
	 * @return <code>true</code> if entry successfully added, <code>false</code>
	 * if already exists.
	 */
	public synchronized boolean addEntry(RegistryEntry entry) {
		if (entriesMap.containsValue(entry)) {
			return false;
		}
		
		entriesMap.put(toEntryKey(entry), new RegistryEntry(entry));
		return true;
	}
	
	public synchronized boolean removeEntry(RegistryEntry entry) {
		return entriesMap.remove(toEntryKey(entry)) != null;
	}
	
	/**
	 * @param serverAddr
	 * @param serverPort
	 * @return <code>true</code> if entry updated as alive, <code>false</code>
	 * if entry does not exists.
	 */
	public synchronized boolean keepAlive(RegistryEntry entry) {
		final String entryKey = toEntryKey(entry);
		final RegistryEntry existingEntry = entriesMap.get(entryKey);
		
		if (existingEntry == null) {
			return false;
		}
		
		cleanseCandidatesSet.remove(existingEntry);
		return true;
	}
	
	/**
	 * Updates entry data. This also works as 'keep alive'.
	 * 
	 * @param entry
	 * @return <code>true</code> if entry successfully updated,
	 * <code>false</code> if entry does not exists.
	 */
	public synchronized boolean updateEntry(RegistryEntry entry) {
		final String entryKey = toEntryKey(entry);
		final RegistryEntry existingEntry = entriesMap.get(entryKey);
		
		if (existingEntry == null) {
			return false;
		}

		cleanseCandidatesSet.remove(existingEntry);
		entriesMap.put(entryKey, new RegistryEntry(entry));
		
		return true;
	}
	
}
