package pl.graniec.gear.masterserver;

public class NetGameEventValue <T> {
	public enum Type {
		NULL,
		UINTEGER,
		INTEGER,
		NUMBER,
		BOOLEAN,
		STRING,
		// COMPLEX not supported
	}
	
	private final T value;
	
	public NetGameEventValue(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "NetGameEventValue [value=" + value + "]";
	}
	
}
