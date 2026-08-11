package net.evmodder.EvLib.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class ServerIdRequest{
	public record Metadata(String address, String name, String endpoint){}
	// Vanilla caps server addresses at 128 chars; even a maximal normal address plus resolved endpoint is under 700 UTF-8 bytes.
	public static final int MAX_ENCODED_SIZE = 1024;
	private static final int LENGTH_BYTES = Short.BYTES*3;

	private ServerIdRequest(){}

	public static byte[] encode(final String address, final String name, final String endpoint){
		final byte[] addressBytes = bytes(address), endpointBytes = bytes(endpoint);
		final int maxMetadataBytes = MAX_ENCODED_SIZE-LENGTH_BYTES;
		// Keep identity-bearing address/endpoint data exact; only the display-name hint may be truncated to use the remaining space.
		if(addressBytes.length > maxMetadataBytes || endpointBytes.length > maxMetadataBytes
				|| addressBytes.length+endpointBytes.length > maxMetadataBytes){
			throw new IllegalArgumentException("Server address and endpoint metadata exceed "+maxMetadataBytes+" bytes");
		}
		final byte[] nameBytes = truncatedBytes(name, maxMetadataBytes-addressBytes.length-endpointBytes.length);
		final int dataLength = LENGTH_BYTES+addressBytes.length+nameBytes.length+endpointBytes.length;
		final int encodedLength = (dataLength+15)&~15;
		return ByteBuffer.allocate(encodedLength)
				.putShort((short)addressBytes.length).putShort((short)nameBytes.length).putShort((short)endpointBytes.length)
				.put(addressBytes).put(nameBytes).put(endpointBytes).array();
	}

	public static Metadata decode(final byte[] encoded){
		if(encoded == null || encoded.length < 16 || encoded.length > MAX_ENCODED_SIZE || (encoded.length&15) != 0) return null;
		final ByteBuffer buffer = ByteBuffer.wrap(encoded);
		final int addressLength = Short.toUnsignedInt(buffer.getShort());
		final int nameLength = Short.toUnsignedInt(buffer.getShort());
		final int endpointLength = Short.toUnsignedInt(buffer.getShort());
		if(addressLength+nameLength+endpointLength > buffer.remaining()) return null;
		final String address = read(buffer, addressLength), name = read(buffer, nameLength), endpoint = read(buffer, endpointLength);
		while(buffer.hasRemaining()) if(buffer.get() != 0) return null;
		return new Metadata(address, name, endpoint);
	}

	private static byte[] bytes(final String value){
		return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
	}
	private static byte[] truncatedBytes(final String value, final int maxLength){
		final byte[] bytes = bytes(value);
		if(bytes.length <= maxLength) return bytes;
		int length = maxLength;
		while(length > 0 && (bytes[length]&0xC0) == 0x80) --length;
		return Arrays.copyOf(bytes, length);
	}
	private static String read(final ByteBuffer buffer, final int length){
		final byte[] bytes = new byte[length];
		buffer.get(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}
}