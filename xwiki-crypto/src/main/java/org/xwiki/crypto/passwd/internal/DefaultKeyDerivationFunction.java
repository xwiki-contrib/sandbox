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

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

import org.xwiki.crypto.passwd.KeyDerivationFunction;

/**
 * The default Key Derivation Function.
 * To add a new Key Derivation Function, extend this class and override init(int), isInitialized and hashPassword
 * Since all subclasses of this class are serializable, be careful to make sure any values not needed to reconstruct
 * the object are declared volatile. Dependencies injected by the component manager will be available when init(int)
 * is called but not when it has been deserialized.
 *
 * @since 2.5
 * @version $Id$
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultKeyDerivationFunction implements KeyDerivationFunction, Serializable
{
    /** Serial version ID to distinguish different versions of the class. */
    private static final long serialVersionUID = -921894479866760486L;

    /**
     * All calls will be passed through to this function.
     * By default use scrypt.
     */
    @Requirement("scrypt/1")
    private volatile KeyDerivationFunction wrappedFunction;

    /**
     * This method will accept a serialized version of any KeyDerivationFunction defined in the system.
     *
     * @param serialized the byte array to create the KeyDerivationFunction from.
     * @return a function made from deserializing the given array.
     * @throws IOException if something goes wrong in the serialization framework.
     * @throws ClassNotFoundException if the required key derivation function is not present on the system.
     */
    public static KeyDerivationFunction deserialize(final byte[] serialized) throws IOException, ClassNotFoundException
    {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized));
        return (KeyDerivationFunction) ois.readObject();
    }

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.KeyDerivationFunction#getSerialized()
     */
    public byte[] getSerialized() throws IOException
    {
        if (!this.isInitialized()) {
            throw new IllegalStateException("Can't serialize a key derivation function until it has been initialized.");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(128);
        ObjectOutputStream oos = new ObjectOutputStream(out);
        if (this.getClass().equals(DefaultKeyDerivationFunction.class)) {
            // If this is a DefaultKeyDerivationFunction wrapping another KDF
            // then serialize the wrapped KDF.
            oos.writeObject(this.wrappedFunction);
        } else {
            // Otherwise this is a subclass of DefaultKDF so serialize this object.
            oos.writeObject(this);
        }
        oos.flush();
        return out.toByteArray();
    }

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.KeyDerivationFunction#init(int)
     */
    public void init(final int derivedKeyLength)
    {
        this.wrappedFunction.init(derivedKeyLength);
    }

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.KeyDerivationFunction#isInitialized()
     */
    public boolean isInitialized()
    {
        return this.wrappedFunction.isInitialized();
    }

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.KeyDerivationFunction#hashPassword(byte[])
     */
    public byte[] hashPassword(final byte[] password)
    {
        return this.wrappedFunction.hashPassword(password);
    }
}
