package pl.graniec.gear.masterserver.packets;

import pl.graniec.gear.masterserver.NetGameEventValue;
import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;

final class PacketHelper {
	static <T> void checkArgumentType(Class<T> expectedClass, Object argument)
	throws DataCorruptedException {
		if (expectedClass.isInstance(argument)) {
			throw new DataCorruptedException(
					String.format(
							"expected datatype of %s but %s given",
							expectedClass.getSimpleName(),
							argument.getClass().getSimpleName()
					)
			);
		}
	}
	
	static void checkArgsCount(int expected, NetGameEventValue<?>[] arguments)
	throws DataCorruptedException {
		if (arguments.length != expected) {
			throw new DataCorruptedException(
					String.format(
							"expected %d arguments but %d given",
							expected, arguments.length
					)
			);
		}
	}
}
