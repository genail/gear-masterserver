package pl.graniec.gear.masterserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamHelper {

	private static final Logger LOGGER =
		Logger.getLogger(StreamHelper.class.getName());
	
	public static void closeStream(InputStream stream) {
		if (stream == null) {
			return;
		}
		
		try {
			stream.close();
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "failed to close stream", e);
		}
	}
	
	public static void closeStream(OutputStream stream) {
		if (stream == null) {
			return;
		}
		
		try {
			stream.close();
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "failed to close stream", e);
		}
	}
}
