package org.bocogop.wr.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializationUtil {

	public static byte[] serializeObject(Serializable s) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(s);
			byte[] yourBytes = bos.toByteArray();
			return yourBytes;
		}
	}

	public static Object deserializeObject(byte[] serializedData) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedData);
				ObjectInput in = new ObjectInputStream(bis);) {
			Object o = in.readObject();
			return o;
		}
	}
}
