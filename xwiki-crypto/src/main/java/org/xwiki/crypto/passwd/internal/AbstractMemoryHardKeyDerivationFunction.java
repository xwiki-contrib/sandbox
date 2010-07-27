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

import java.io.IOException;

import org.xwiki.crypto.internal.SerializationUtils;
import org.xwiki.crypto.passwd.MemoryHardKeyDerivationFunction;

/**
 * Abstract memory hard key derivation function.
 * to subclass this, simply implement init(int, int, int) from MemoryHardKeyDerivationFunction
 * and isInitialized() and hashPassword(byte[]) from  KeyDerivationFunction.
 * Be careful, this class is serializable, serialization and deserialization should yield a function which provides
 * the same password to key mapping, and make sure fields unnecessary to this are declared transient.
 *
 * @since 2.5
 * @version $Id$
 */
public abstract class AbstractMemoryHardKeyDerivationFunction implements MemoryHardKeyDerivationFunction
{
    /**
     * Fields in this class are set in stone!
     * Any changes may result in encrypted data becoming unreadable.
     * This class should be extended if any changes need to be made.
     */
    private static final long serialVersionUID = 1L;

    /** Number of bytes length of the salt. This is statically set to 16. */
    private final transient int saltSize = 16;

    /** Amount of memory to use by default (4MB). */
    private final transient int defaultNumberOfKilobytesOfMemoryToUse = 4096;

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.MemoryHardKeyDerivationFunction#serialize()
     */
    public byte[] serialize() throws IOException
    {
        return SerializationUtils.serialize(this);
    }

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.MemoryHardKeyDerivationFunction#init(int, int)
     */
    public void init(final int millisecondsOfProcessorTimeToSpend,
                     final int derivedKeyLength)
    {
        this.init(this.defaultNumberOfKilobytesOfMemoryToUse,
                  millisecondsOfProcessorTimeToSpend,
                  derivedKeyLength);
    }
}
