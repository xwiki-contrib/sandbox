/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.xwiki.store.hibernate.types;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.UUID;

import org.hibernate.usertype.UserType;


/**
 * Hibernate UserType to store UUIDs as BINARY in the database.
 *
 * @version $Id$
 */
public class UUIDToBinaryType implements UserType
{
    /** The SQL type which is used to store the UUID. */
    private static final int TYPE = Types.VARBINARY;

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(final Object value)
    {
        // UUIDs are immutable.
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    public Object assemble(final Serializable stored, final Object owner)
    {
        if (stored == null || stored.getClass() != byte[].class) {
            return null;
        }
        return UUIDToBinaryType.bytesToUUID((byte[]) stored);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    public Serializable disassemble(final Object value)
    {
        if (value.getClass() != UUID.class) {
            throw new ClassCastException("HibernateUUIDType can only disassemble objects of class "
                                         + "java.lang.UUID object of class " + value.getClass()
                                         + " not acceptable.");
        }
        return UUIDToBinaryType.bytesFromUUID((UUID) value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(final Object x, final Object y)
    {
        if (x == null) {
            return (x == y);
        }
        return x.equals(y);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    public int hashCode(final Object obj)
    {
        return (obj == null) ? 0 : obj.hashCode();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    public boolean isMutable() {
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(final ResultSet results, final String[] names, final Object owner)
        throws SQLException
    {
        final byte[] value = results.getBytes(names[0]);
        return (value != null) ? UUIDToBinaryType.bytesToUUID(value) : null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    public void nullSafeSet(final PreparedStatement statement,
                            final Object value,
                            final int parameterIndex)
        throws SQLException
    {
        if (value == null) {
            statement.setNull(parameterIndex, UUIDToBinaryType.TYPE);
        } else if (value.getClass() != UUID.class) {
            throw new ClassCastException("Expecting a java.lang.UUID, instead got a: " + value.getClass());
        } else {
            statement.setBytes(parameterIndex, UUIDToBinaryType.bytesFromUUID((UUID) value));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public Object replace(final Object original, final Object target, final Object owner)
    {
        return original;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    public Class returnedClass()
    {
        return UUID.class;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    public int[] sqlTypes()
    {
        // create a new int[] per call lest a wiley user change the value.
        return new int[] {UUIDToBinaryType.TYPE};
    }

    /**
     * Convert a UUID into a byte array.
     * The output is 16 bytes in little endian order.
     *
     * @param value the UUID to convert.
     * @return a byte array representing the UUID.
     */
    private static byte[] bytesFromUUID(final UUID value)
    {
        final byte[] out = new byte[16];
        UUIDToBinaryType.longToBytesLittle(value.getLeastSignificantBits(), out, 0);
        UUIDToBinaryType.longToBytesLittle(value.getMostSignificantBits(), out, 8);
        return out;
    }

    /**
     * Convert a byte array to a UUID.
     * Conversion is done in little endian order.
     *
     * @param bytes a byte array at least 16 bytes long.
     * @return a UUID derived from the first 16 bytes of the array.
     */
    private static UUID bytesToUUID(final byte[] bytes)
    {
        return new UUID(UUIDToBinaryType.bytesToLongLittle(bytes, 8),
                        UUIDToBinaryType.bytesToLongLittle(bytes, 0));
    }

    /**
     * Convert a long to a byte array.
     * Conversion is done in little endian order.
     *
     * @param value a number of the long type.
     * @param output bytes will be written into this array.
     * @param offset place in the array to begin writing (will write 8 bytes starting here)
     */
    private static void longToBytesLittle(final long value, final byte[] output, final int offset)
    {
        for (int i = 0; i < 8; i++) {
            output[offset + i] = (byte) (value >>> (i * 8));
        }
    }

    /**
     * Convert part of a byte array to a long.
     * Conversion is done in little endian order.
     *
     * @param input read values from this byte array.
     * @param offset place in the array to begin reading (will read 8 bytes starting here)
     * @return a long generated from the given bytes.
     */
    private static long bytesToLongLittle(final byte[] input, final int offset)
    {
        long out = 0L;
        for (int i = 7; i >= 0; i--) {
            out <<= 8;
            out |= (long) input[offset + i] & 0xFF;
        }
        return out;
    }
}
