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

import java.security.SecureRandom;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

import org.xwiki.crypto.passwd.internal.scrypt.Scrypt;

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
@Component("scrypt/1")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class ScryptKeyDerivationFunction extends DefaultKeyDerivationFunction
{
    /** @serial abstract number referring to how much memory should be expended for hashing the password. */
    private int memoryExpense;

    /** @serial abstract number referring to how much CPU power should be expended for hashing the password. */
    private int processorExpense;

    /** @serial how many bytes long the output key should be. */
    private int derivedKeyLength;

    /** @serial random salt to frustrate cracking attempts. */
    private byte[] salt;

    /** In version 1.0, the block size is always 8. */
    private volatile int blockSize = 8;

    /** In version 1.0, the salt size is always 16. */
    private volatile int saltSize = 8;

    /** The scrypt function engine. */
    private volatile Scrypt engine;

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.DefaultKeyDerivationFunction#init(int)
     */
    public void init(final int derivedKeyLength)
    {
        this.derivedKeyLength = derivedKeyLength;

        // Generate the salt.
        SecureRandom random = new SecureRandom();
        this.salt = new byte[this.saltSize];
        random.nextBytes(this.salt);

        // set processorExpense and memoryExpense statically for now...
        this.memoryExpense = 131072;
        this.processorExpense = 2;
    }

    /**
     * @param password the user supplied password.
     * @return a key derived from the password and the parameters.
     */
    public byte[] hashPassword(final byte[] password)
    {
        throw new RuntimeException("Not implemented yet");
    }
}
