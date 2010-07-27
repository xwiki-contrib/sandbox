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
import java.io.IOException;

import java.security.SecureRandom;

import org.xwiki.crypto.internal.SerializationUtils;
import org.xwiki.crypto.passwd.KeyDerivationFunction;


/**
 * The abstract key derivation function.
 * Provides guess/trial based determination of the correct number of iterations for a given processor time requirement.
 *
 * @since 2.5
 * @version $Id$
 */
public abstract class AbstractKeyDerivationFunction implements KeyDerivationFunction, Serializable
{
    /**
     * Fields in this class are set in stone!
     * Any changes may result in encrypted data becoming unreadable.
     * This class should be extended if any changes need to be made.
     */
    private static final long serialVersionUID = 1L;

    /** Number of bytes length of the salt. This is statically set to 16. */
    private final transient int saltSize = 16;

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.KeyDerivationFunction#serialize()
     */
    public byte[] serialize() throws IOException
    {
        return SerializationUtils.serialize(this);
    }

    /**
     * {@inheritDoc}
     *
     * @see KeyDerivationFunction#init(int, int)
     */
    public void init(final int millisecondsOfProcessorTimeToSpend,
                     final int derivedKeyLength)
    {
        // Generate the salt.
        final byte[] salt = new byte[this.saltSize];
        new SecureRandom().nextBytes(salt);

        // Try with 20 cycles.
        int testIterationCount = 20;

        this.init(salt, testIterationCount, derivedKeyLength);

        // Since the error is % wise, it grows as the total increases.
        // use a test length dependent on the target time to spend.
        int testLength = millisecondsOfProcessorTimeToSpend / 100;
        // Mimimum test length is 4ms.
        if (testLength < 4) {
            testLength = 4;
        }

        // Time a test run.
        long time = System.currentTimeMillis();
        // Run until 4 milliseconds have gone by.
        int numberOfCycles = 0;
        while ((System.currentTimeMillis() - time) < testLength) {
            this.hashPassword(salt);
            numberOfCycles += testIterationCount;
        }
        // Set the iteration count to target run time / testLength (because the test run went testLength milliseconds)
        // multiplied by the number of cycles in the test run.
        int iterationCount = (millisecondsOfProcessorTimeToSpend / testLength) * numberOfCycles;

        // Set the final iterationCount value.
        this.init(salt, iterationCount, derivedKeyLength);
    }

    /**
     * Initialize the function manually.
     *
     * @param salt the random salt to add to the password before hashing.
     * @param iterationCount the number of iterations which the internal function should run.
     * @param derivedKeyLength the number of bytes of length the derived key should be (dkLen)
     */
    public abstract void init(final byte[] salt,
                              final int iterationCount,
                              final int derivedKeyLength);
}
