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
 */
package org.xwiki.crypto.passwd.internal;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import org.xwiki.crypto.passwd.PasswordVerificationFunction;


/**
 * Utilities for getting the serialization and deserialization of functions.
 *
 * @since 2.5
 * @version $Id:$
 */
public class PasswordVerificationFunctionUtils
{
    /**
     * This method will accept a serialized version of any PasswordVerificationFunction defined in the system.
     *
     * @param serialized the byte array to create the PasswordVerificationFunction from.
     * @return a function made from deserializing the given array.
     * @throws IOException if something goes wrong in the serialization framework.
     * @throws ClassNotFoundException if the required key derivation function is not present on the system.
     */
    public PasswordVerificationFunction deserialize(final byte[] serialized)
        throws IOException,
               ClassNotFoundException
    {
        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized));
        return (PasswordVerificationFunction) ois.readObject();
    }

    /**
     * Convert the given PasswordVerificationFunction to a byte array which when passed to deserialize() will 
     * make a function which will validate the same password.
     *
     * @param toSerialize the PasswordVerificationFunction to convert into a byte array.
     * @return the given function as a byte array.
     * @throws IOException if something goes wrong in the serialization framework.
     */
    public byte[] serialize(PasswordVerificationFunction toSerialize) throws IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(128);
        final ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(toSerialize);
        oos.flush();
        return out.toByteArray();
    }
}
