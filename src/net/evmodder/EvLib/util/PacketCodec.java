package net.evmodder.EvLib.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Stateless wire-format, encryption, and authentication primitives shared by EvMod and EvModDB.
 * This class owns no sockets, connections, request lifecycle, or callback state.
 */
public final class PacketCodec{
	private PacketCodec(){}

	private static final Logger LOGGER = LoggerUtils.getLogger(PacketCodec.class.getSimpleName());
	public static final long MAX_REQUEST_TIMEOUT_MILLIS = 60_000;
	public static final int MAX_REQUEST_ID = 0xFFFFFF;
	public static final int AUTH_TAG_SIZE = 16;
	private static final int MAX_TCP_FRAME_SIZE = 0xFFFF;
	private record DerivedKeys(SecretKeySpec encryption, SecretKeySpec authentication){}
	private static final ConcurrentHashMap<String, DerivedKeys> KEY_CACHE = new ConcurrentHashMap<>();
	private static final ThreadLocal<Cipher> CIPHER = ThreadLocal.withInitial(()->{
		try{return Cipher.getInstance("AES/ECB/NoPadding");}
		catch(NoSuchAlgorithmException | NoSuchPaddingException ex){throw new IllegalStateException(ex);}
	});
	private static final ThreadLocal<Mac> AUTHENTICATOR = ThreadLocal.withInitial(()->{
		try{return Mac.getInstance("HmacSHA256");}
		catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
	});

	private static DerivedKeys createKeySpecs(final String keyString){
		try{
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
			final byte[] encryptionDigest = digest.digest(keyBytes);
			final byte[] key = new byte[16];
			System.arraycopy(encryptionDigest, 0, key, 0, key.length);
			digest.update("EvModDB authentication key\0".getBytes(StandardCharsets.UTF_8));
			return new DerivedKeys(
				/*encryption=*/new SecretKeySpec(key, "AES"),
				/*authentication=*/new SecretKeySpec(digest.digest(keyBytes), "HmacSHA256")
			);
		}
		catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
	}
	private static Cipher getCipher(String keyString, int mode)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
		// setup AES cipher in CBC mode with PKCS #5 padding
		// Actually, since we encode 16 bytes (or multiples or 16), use ECB/NoPadding
		Cipher cipher = CIPHER.get();

//		// setup an IV (initialization vector) that should be
//		// randomly generated for each input that's encrypted
//		byte[] iv = new byte[cipher.getBlockSize()];
//		new SecureRandom().nextBytes(iv);
//		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		// hash keyString with SHA-256 and crop the output to 128-bit for key
		SecretKeySpec keySpec = KEY_CACHE.computeIfAbsent(keyString, PacketCodec::createKeySpecs).encryption();

		cipher.init(mode, keySpec/*, ivSpec*/);
		return cipher;
	}
	public static byte[] encrypt(byte[] data, String keyString){
		if(data.length % 16 != 0){
			LOGGER.severe("Invalid message length, must be multiple of 16, got: "+data.length);
			return null;
		}
		try{
			Cipher cipher = getCipher(keyString, Cipher.ENCRYPT_MODE);
			return cipher.doFinal(data);
		}
		catch(Exception e){
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
			return null;
		}
	}
	public static byte[] decrypt(byte[] data, String keyString){
		if(data.length % 16 != 0){
			LOGGER.warning("Invalid message length, must be multiple of 16, got: "+data.length);
			return null;
		}
		try{
			Cipher cipher = getCipher(keyString, Cipher.DECRYPT_MODE);
			return cipher.doFinal(data);
		}
		catch(Exception e){
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
			return null;
		}
	}
	private static byte[] authenticationTag(final byte[] data, final int authenticatedLength,
			final String keyString, final byte[] context){
		try{
			final Mac authenticator = AUTHENTICATOR.get();
			authenticator.init(KEY_CACHE.computeIfAbsent(keyString, PacketCodec::createKeySpecs).authentication());
			authenticator.update((byte)(context == null ? 0 : 1)); // Separate request and response MAC domains.
			if(context != null) authenticator.update(context); // Bind a response to its exact authenticated request.
			authenticator.update(data, 0, authenticatedLength);
			return authenticator.doFinal();
		}
		catch(InvalidKeyException ex){throw new IllegalStateException(ex);}
	}
	public static void writeAuthenticationTag(final byte[] data, final int authenticatedLength,
			final String keyString, final byte[] context){
		if(authenticatedLength < 0 || data.length-authenticatedLength < AUTH_TAG_SIZE){
			throw new IllegalArgumentException("Insufficient authentication-tag space");
		}
		final byte[] tag = authenticationTag(data, authenticatedLength, keyString, context);
		System.arraycopy(tag, 0, data, authenticatedLength, AUTH_TAG_SIZE);
	}
	public static boolean verifyAuthenticationTag(final byte[] data, final int authenticatedLength,
			final String keyString, final byte[] context){
		if(authenticatedLength < 0 || data.length-authenticatedLength < AUTH_TAG_SIZE) return false;
		final byte[] expected = authenticationTag(data, authenticatedLength, keyString, context);
		int difference = 0;
		for(int i=0; i<AUTH_TAG_SIZE; ++i) difference |= expected[i]^data[authenticatedLength+i];
		return difference == 0;
	}
	public static void putUnsignedMedium(final ByteBuffer buffer, final int value){
		if(value < 0 || value > MAX_REQUEST_ID) throw new IllegalArgumentException("Unsigned medium out of range: "+value);
		buffer.put((byte)(value>>>16)).put((byte)(value>>>8)).put((byte)value);
	}
	public static int getUnsignedMedium(final ByteBuffer buffer){
		return Byte.toUnsignedInt(buffer.get())<<16 | Byte.toUnsignedInt(buffer.get())<<8 | Byte.toUnsignedInt(buffer.get());
	}

	public static void writeFrame(final OutputStream out, final byte[] message) throws IOException{
		if(message.length > MAX_TCP_FRAME_SIZE) throw new IllegalArgumentException("TCP frame is too large: "+message.length);
		final byte[] frame = new byte[Short.BYTES+message.length];
		frame[0] = (byte)(message.length>>>8);
		frame[1] = (byte)message.length;
		System.arraycopy(message, 0, frame, Short.BYTES, message.length);
		out.write(frame);
	}

	public static byte[] toByteArray(UUID... uuids){
		ByteBuffer bb = ByteBuffer.allocate(uuids.length*16);
		for(UUID uuid : uuids) bb.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
}