package pl.graniec.gear.masterserver.registry;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Registry {
	
	// static fields
	
	private static final Logger LOGGER =
		Logger.getLogger(Registry.class.getName());
	
	
	private static final Registry INSTANCE = new Registry();
	
	
	// static methods
	
	public static Registry getInstance() {
		return INSTANCE;
	}
	
	
	// non-static fields
	
	private final Set<RegistryEntry> entriesSet =
		new HashSet<RegistryEntry>();
	
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
	 * @return <code>true</code> if entry successfully added, <code>false</code>
	 * if already exists.
	 */
	public synchronized boolean addEntry(RegistryEntry entry) {
		if (entriesSet.contains(entry)) {
			return false;
		}
		
		entriesSet.add(entry);
		return true;
	}
	
	public synchronized boolean removeEntry(RegistryEntry entry) {
		return entriesSet.remove(entry);
	}
	
	/**
	 * @return <code>true</code> if entry updated as alive, <code>false</code>
	 * if entry does not exists.
	 */
	public synchronized boolean keepAlive(RegistryEntry entry) {
		
		if (!entriesSet.contains(entry)) {
			return false;
		}
		
		if (cleanseCandidatesSet.contains(entry)) {
			cleanseCandidatesSet.remove(entry);
		}
		
		return true;
	}
	
	public synchronized final RegistryEntry[] getEntries() {
		return entriesSet.toArray(new RegistryEntry[entriesSet.size()]);
	}
	
	final synchronized void cleanse() {
		
		LOGGER.finer(
				String.format(
						"cleaning registry %d => %d entries",
						entriesSet.size(),
						entriesSet.size() - cleanseCandidatesSet.size()
				)
		);
		
		for (final RegistryEntry entryToClean : cleanseCandidatesSet) {
			entriesSet.remove(toEntryKey(entryToClean));
		}
		
		cleanseCandidatesSet.clear();
		cleanseCandidatesSet.addAll(entriesSet);
	}
	
}
