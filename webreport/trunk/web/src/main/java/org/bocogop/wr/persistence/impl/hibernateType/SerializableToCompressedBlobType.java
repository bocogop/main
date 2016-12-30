package org.bocogop.wr.persistence.impl.hibernateType;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.bocogop.wr.util.CompressionUtil;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.SerializationHelper;
import org.hibernate.type.SerializationException;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;
import org.hibernate.usertype.UserType;

/**
 * @author barrycon
 * 
 */
public class SerializableToCompressedBlobType implements UserType {

	private static byte[] toBytes(Object object) throws SerializationException {
		return SerializationHelper.serialize((Serializable) object);
	}

	private Object fromBytes(byte[] bytes) throws SerializationException {
		return SerializationHelper.deserialize(bytes, Serializable.class.getClassLoader());
	}

	@Override
	public int[] sqlTypes() {
		return new int[] { Types.BLOB };
	}

	@Override
	public Class<?> returnedClass() {
		return Serializable.class;
	}

	@Override
	public boolean equals(Object one, Object another) throws HibernateException {
		if (one == another)
			return true;

		if (one == null || another == null)
			return false;

		return one.equals(another)
				|| PrimitiveByteArrayTypeDescriptor.INSTANCE.areEqual(toBytes(one), toBytes(another));
	}

	@Override
	public int hashCode(Object value) throws HibernateException {
		return PrimitiveByteArrayTypeDescriptor.INSTANCE.extractHashCode(toBytes(value));
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		Blob blob = rs.getBlob(names[0]);
		if (rs.wasNull())
			return null;
		int length = (int) blob.length();
		byte[] primaryResult = blob.getBytes(1, length);
		byte[] decompressedResult = CompressionUtil.decompress(primaryResult, false);
		return fromBytes(decompressedResult);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
			throws HibernateException, SQLException {
		if (value != null) {
			byte[] toSet = toBytes(value);
			byte[] compressedToSet = CompressionUtil.compress(toSet);
			if (session.getFactory().getDialect().useInputStreamToInsertBlob()) {
				st.setBinaryStream(index, new ByteArrayInputStream(compressedToSet), compressedToSet.length);
			} else {
				st.setBlob(index, Hibernate.getLobCreator(session).createBlob(compressedToSet));
			}
		} else {
			st.setNull(index, sqlTypes()[0]);
		}
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		throw new UnsupportedOperationException("Blobs are not cacheable");
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		throw new UnsupportedOperationException("Blobs are not cacheable");
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

}
