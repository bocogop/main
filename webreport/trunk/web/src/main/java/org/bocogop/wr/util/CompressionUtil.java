package org.bocogop.wr.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

public class CompressionUtil {

	public static byte[] compress(byte[] content) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
			gzipOutputStream.write(content);
			gzipOutputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * @param contentBytes
	 * @param exceptionOnError
	 *            If true and the specified contentBytes do not conform to the
	 *            expected compressed format, then a RuntimeException will be
	 *            thrown. If false, the original contentBytes will be returned
	 *            unmodified.
	 * @return
	 */
	public static byte[] decompress(byte[] contentBytes, boolean exceptionOnError) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(contentBytes)), out);
		} catch (IOException e) {
			if (exceptionOnError) {
				throw new RuntimeException(e);
			} else {
				return contentBytes;
			}
		}
		return out.toByteArray();
	}

}
