package org.bocogop.shared.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bocogop.shared.model.HashSummary;

/**
 * Central utility for hashing needs.
 */
public class HashUtils {
	public static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	public static final String DEFAULT_ALGORITHM = "SHA-256";

	public static HashSummary hash(Object itemToHash, String algorithm) throws Exception {
		return hash(itemToHash, null, algorithm);
	}

	public static HashSummary hash(Object itemToHash) throws Exception {
		return hash(itemToHash, null, DEFAULT_ALGORITHM);
	}

	public static HashSummary hash(Object itemToHash, String key, String algorithm) throws NoSuchAlgorithmException {
		if (itemToHash == null)
			return null;

		// TODO: if MessageDigest is thread-safe, could share static instance
		MessageDigest digest = java.security.MessageDigest.getInstance(algorithm);

		/*
		 * logger.info(
		 * "Hash performed using MessageDigest [name / version / info]: [" +
		 * digest.getProvider().getName() + " / " +
		 * digest.getProvider().getVersion() + " / " +
		 * digest.getProvider().getInfo() + "]");
		 */

		int hash = itemToHash.hashCode();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(hash);
		if (key != null) {
			try {
				baos.write(key.getBytes());
			} catch (IOException e) {
				throw new RuntimeException("Unexpected IOException while attempting to write hash", e);
			}
		}
		digest.update(baos.toByteArray());
		byte[] hashSig = digest.digest();
		char buf[] = new char[hashSig.length * 2];
		for (int i = 0, x = 0; i < hashSig.length; i++) {
			buf[x++] = HEX_CHARS[(hashSig[i] >>> 4) & 0xf];
			buf[x++] = HEX_CHARS[hashSig[i] & 0xf];
		}
		return new HashSummary(new String(buf), digest);
	}
}
