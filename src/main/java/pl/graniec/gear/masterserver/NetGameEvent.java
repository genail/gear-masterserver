package pl.graniec.gear.masterserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import pl.graniec.gear.masterserver.exceptions.DataCorruptedException;

public class NetGameEvent {
	
	// static fields
	
	private static final Logger LOGGER =
		Logger.getLogger(NetGameEvent.class.getName());
	
	private static final String TYPE_STRING = "string";

	private static final String TYPE_BOOLEAN = "boolean";

	private static final String TYPE_NUMBER = "number";

	private static final String TYPE_INTEGER = "integer";

	private static final String TYPE_UINTEGER = "uinteger";

	private static final String TYPE_NULL = "null";

	// 2^15 / 2 because writeUTF() writes in UTF-8 and can be unpredictable
	private static final int PACKET_SIZE_LIMIT = 16384;
	
	
	// static methods
	
	public static NetGameEvent fromStream(InputStream inStream)
	throws IOException, DataCorruptedException {
		
		try {
			final byte[] buffer = readEventIntoBuffer(inStream);
			final Document xmlDocument = buildDocument(buffer);
			
			final String eventName = readEventName(xmlDocument);
			final NetGameEventValue<?>[] eventValues = readEventValues(xmlDocument);
			
			return new NetGameEvent(eventName, eventValues);
		} catch (final JDOMException e) {
			throw new DataCorruptedException("invalid xml: " + e.getMessage());
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private static NetGameEventValue<?>[] readEventValues(Document xmlDocument)
	throws DataCorruptedException {
		final Element rootElement = xmlDocument.getRootElement();
		final List<Element> childrenElements =
			rootElement.getChildren();
		
		final NetGameEventValue<?>[] values =
			new NetGameEventValue<?>[childrenElements.size()];
		
		int i = 0;
		for (final Element el : childrenElements) {
			values[i++] = parseEventValue(el.getName(), el.getValue());
		}
		
		return values;
	}
	
	private static NetGameEventValue<?> parseEventValue(
			String name, String value
	) throws DataCorruptedException {
		
		try {
			if (name.equals(TYPE_NULL)) {
				return new NetGameEventValue<Object>(null);
			} else if (name.equals(TYPE_UINTEGER)) {
				return new NetGameEventValue<Long>(Long.parseLong(value));
			} else if (name.equals(TYPE_INTEGER)) {
				return new NetGameEventValue<Integer>(Integer.parseInt(value));
			} else if (name.equals(TYPE_NUMBER)) {
				return new NetGameEventValue<Float>(Float.parseFloat(value));
			} else if (name.equals(TYPE_BOOLEAN)) {
				return new NetGameEventValue<Boolean>(value.equals("true"));
			} else if (name.equals(TYPE_STRING)) {
				return new NetGameEventValue<String>(value);
			} else {
				throw new DataCorruptedException(
						"datatype not supported: " + name
				);
			}
		} catch (final NumberFormatException e) {
			throw new DataCorruptedException("invalid number format");
		}
	}
	
	private static String readEventName(Document xmlDocument) {
		final Element rootElement = xmlDocument.getRootElement();
		return rootElement.getName();
	}
	
	private static Document buildDocument(byte[] buffer)
	throws JDOMException, IOException {
		final ByteArrayInputStream in = new ByteArrayInputStream(buffer);
		
		final SAXBuilder builder = new SAXBuilder();
        final Document doc = builder.build(in);
        
        return doc;
	}

	private static byte[] readEventIntoBuffer(InputStream inStream)
			throws IOException, DataCorruptedException {
		final DataInputStream dataInStream = new DataInputStream(inStream);
		final int bufferSize = dataInStream.readUnsignedShort();
		
		if (bufferSize > PACKET_SIZE_LIMIT) {
			throw new DataCorruptedException("exceed packet size");
		}
		
		byte[] buffer = new byte[bufferSize];
		dataInStream.read(buffer, 0, bufferSize);
		
		buffer = trimBytes(buffer);
		
		return buffer;
	}

	private static byte[] trimBytes(byte[] buffer) {
		return new String(buffer).trim().getBytes();
	}
	
	
	// fields
	
	private final String name;
	
	private final NetGameEventValue<?>[] arguments;
	
	
	// non-static methods
	
	public NetGameEvent(String name, NetGameEventValue<?>...arguments) {
		this.name = name;
		this.arguments = arguments;
	}
	
	public final String getName() {
		return name;
	}
	
	public NetGameEventValue<?>[] getArguments() {
		return Arrays.copyOf(arguments, arguments.length);
	}
	
	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append(name);
		buf.append("(");
		
		for (int i = 0; i < arguments.length; ++i) {
			buf.append(arguments[i].getValue());
			
			if (i + 1 != arguments.length) {
				buf.append(",");
			}
		}
		
		buf.append(")");
		return buf.toString();
	}
	
	public final void toStream(OutputStream outStream) throws IOException {
		final String xmlString = buildXmlString();
		writeXmlToStream(xmlString, outStream);
	}

	private void writeXmlToStream(String xmlString, OutputStream outStream)
	throws IOException {
		final DataOutputStream dataOutStream = new DataOutputStream(outStream);
		
		if (xmlString.length() > PACKET_SIZE_LIMIT) {
			LOGGER.severe("Outgoing message too long: " + getName());
			return;
		}
		
		LOGGER.finest("sending: " + xmlString);
		
		final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		writeXmlToByteArray(xmlString, byteArrayOut);
		
		final byte[] byteArray = byteArrayOut.toByteArray();
		
		// this is needed because first numbers is length and must be
		// in little endian
		swapTwoFirstBytes(byteArray);
		
		dataOutStream.write(byteArray);
	}

	private void writeXmlToByteArray(String xmlString,
			final ByteArrayOutputStream byteArrayOut) throws IOException {
		
		// writeUTF methods writes string length as signed short first.
		// ClanLib requires that data part to be unsigned short.
		// So when packet size won't exceed 2^15 then everything should be
		// allright.
		
		final DataOutputStream tempOutputStream =
			new DataOutputStream(byteArrayOut);
		tempOutputStream.writeUTF(xmlString);
	}

	private void swapTwoFirstBytes(byte[] byteArray) {
		final byte a = byteArray[0];
		byteArray[0] = byteArray[1];
		byteArray[1] = a;
	}

	private String buildXmlString() {
		final StringBuffer buf = new StringBuffer();
		
		buf.append(startTag(getName()));
		
		for (final NetGameEventValue<?> value : arguments) {
			buf.append(buildValueXmlString(value));
		}
		
		buf.append(endTag(getName()));
		return buf.toString();
	}
	
	private String buildValueXmlString(NetGameEventValue<?> value) {
		final Object valueObj = value.getValue();
		
		String typeName, valueStr;
		
		if (valueObj == null) {
			typeName = TYPE_NULL;
			valueStr = "";
		} else if (valueObj instanceof Long) {
			typeName = TYPE_UINTEGER;
			valueStr = ((Long) valueObj).toString();
		} else if (valueObj instanceof Integer) {
			typeName = TYPE_INTEGER;
			valueStr = ((Integer) valueObj).toString();
		} else if (valueObj instanceof Float) {
			typeName = TYPE_NUMBER;
			valueStr = ((Float) valueObj).toString();
		} else if (valueObj instanceof Boolean) {
			typeName = TYPE_BOOLEAN;
			valueStr = ((Boolean) valueObj).toString();
		} else {
			throw new RuntimeException(
					"type not supported: " + valueObj.getClass()
			);
		}
		
		return startTag(typeName) + valueStr + endTag(typeName);
	}
	
	private String startTag(String tagName) {
		return "<" + tagName + ">";
	}
	
	private String endTag(String tagName) {
		return "</" + tagName + ">";
	}
	
}
