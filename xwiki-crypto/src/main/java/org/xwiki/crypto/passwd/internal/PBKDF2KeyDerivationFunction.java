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

import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;


/**
 * Password-Based Key Derivation Function 2.
 * This is an implementation of the PBKDF2 which is defined as part of RSA's PKCS#5
 * see: http://www.ietf.org/rfc/rfc2898.txt
 *
 * @since 2.5
 * @version $Id$
 */
public class PBKDF2KeyDerivationFunction extends AbstractKeyDerivationFunction
{
    /**
     * Fields in this class are set in stone!
     * Any changes may result in encrypted data becoming unreadable.
     * This class should be extended if any changes need to be made.
     */
    private static final long serialVersionUID = 1L;

    /** Number of times to cycle the hash increasing processor expense. */
    private int iterationCount;

    /** The length of the derived key (output) which should be produced. */
    private int derivedKeyLength;

    /** A unique randomly generated byte array for frustrating attacks in bulk. */
    private byte[] salt;

    /** A serializable representation of which digest to use. */
    private final String digestClassName;

    /** 
     * Internal State of functionF, this is only a class scoped variable to prevent the accumulation of 
     * large amounts of garbage from the array being periodically allocated and dumped.
     */
    private transient byte[] state;

    /** The message authentication code function to use. */
    private transient Mac hMac;

    /**
     * Default Constructor.
     * Uses SHA-1 digest for compatabulity with PKCS#5
     */
    public PBKDF2KeyDerivationFunction()
    {
        this(new SHA1Digest());
    }

    /**
     * Constructor with digest specified.
     *
     * @param hash the digest to use for the internal Hash-based Message Authentication Code function.
     */
    public PBKDF2KeyDerivationFunction(Digest hash)
    {
        this.digestClassName = hash.getClass().getName();
        this.hMac = new HMac(hash);
        this.state = new byte[this.hMac.getMacSize()];
    }

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.AbstractKeyDerivationFunction#init(byte[], int, int)
     */
    @Override
    public void init(final byte[] salt,
                     final int iterationCount,
                     final int derivedKeyLength)
    {
        this.salt = salt;
        this.iterationCount = iterationCount;
        this.derivedKeyLength = derivedKeyLength;
    }

    /**
     * {@inheritDoc}
     *
     * @see: org.xwiki.crypto.AbstractKeyDerivationFunction#hashPassword(byte[])
     */
    public synchronized byte[] hashPassword(byte[] password)
    {
        return generateDerivedKey(password, this.salt, this.iterationCount, this.derivedKeyLength);
    }

    /**
     * Generate the PBKDF2 derived key.
     * This is an implementation of PBKDF2(P, S, c, dkLen) defined in http://www.ietf.org/rfc/rfc2898.txt
     *
     * @param password the user supplied password expressed as a byte array.
     * @param salt the random salt to add to the password before hashing.
     * @param iterationCount the number of iterations which the internal function (F) should run.
     * @param derivedKeyLength the number of bytes of length the derived key should be (dkLen)
     * @return a byte array of length derivedKeyLength containing data derived from the password and salt.
     *         suitable for a key.
     */
    public synchronized byte[] generateDerivedKey(final byte[] password,
                                                  final byte[] salt,
                                                  final int iterationCount,
                                                  final int derivedKeyLength)
    {
        // If this was deserialized, then the hmac must be loaded.
        if (this.hMac == null) {
            try {
                this.hMac = new HMac((Digest) Class.forName(this.digestClassName).newInstance());
                this.state = new byte[this.hMac.getMacSize()];
            } catch (Exception e) {
                throw new RuntimeException("Apparently this object was serialized when a digest ("
                                           + this.digestClassName
                                           + ") was available which is not available now.", e);
            }
        }

        try {
            int hLen = hMac.getMacSize();

            // "Let l be the number of hLen-octet blocks in the derived key" (rfc2898)
            int numberOfBlocks = (derivedKeyLength + hLen - 1) / hLen;

            final byte[] currentIterationAsByteArray = new byte[4];
            final byte[] key = new byte[numberOfBlocks * hLen];

            for (int i = 1; i <= numberOfBlocks; i++) {
                this.integerToByteArray(i, currentIterationAsByteArray);
                this.functionF(password, salt, iterationCount, currentIterationAsByteArray, key, (i - 1) * hLen);
            }

            // Usually the key ends up being longer than the desired key length so it must be truncated
            byte[] out = new byte[derivedKeyLength];
            System.arraycopy(key, 0, out, 0, derivedKeyLength);

            return out;
        } finally {
            // Set state to 0's in order to prevent the last state being read later.
            System.arraycopy(new byte[this.state.length], 0, this.state, 0, this.state.length);
        }
    }

    /**
     * Convert an integer to byte array in big-endian byte order.
     * <p> 
     * Takes an int and an array of bytes. This array should be 4 bytes long.
     * Doesn't return anything in order to recycle the same memory locations.</p>
     *
     * @param integer the int which will be converted to an array of bytes.
     * @param outArray the array to populate with the output, this array should be 4 bytes.
     */
    protected void integerToByteArray(int integer, byte[] outArray)
    {
        outArray[0] = (byte) (integer >>> 24);
        outArray[1] = (byte) (integer >>> 16);
        outArray[2] = (byte) (integer >>> 8);
        outArray[3] = (byte) integer;
    }

    /**
     * PBKDF#2 internal function F.
     * This is an implementation of F(P, S, c, l) defined in http://www.ietf.org/rfc/rfc2898.txt
     *
     * @param password (P)
     * @param salt (S)
     * @param iterationCount (c)
     * @param currentIteration when this function is called in a loop
     *                         this should be the current cycle in that loop. (l)
     *                         NOTE: to recycle memory, this parameter is given as a 4 byte array representing an int.
     * @param out the array which will be modified to contain the output.
     * @param outOffset the out array will be written to beginning at this index.
     */
    protected synchronized void functionF(byte[] password,
                                          byte[] salt,
                                          int iterationCount,
                                          byte[] currentIteration,
                                          byte[] out,
                                          int outOffset)
    {
        CipherParameters passwordParam = new KeyParameter(password);
        this.hMac.init(passwordParam);

        if (salt != null) {
            this.hMac.update(salt, 0, salt.length);
        }

        this.hMac.update(currentIteration, 0, currentIteration.length);
        this.hMac.doFinal(this.state, 0);

        System.arraycopy(this.state, 0, out, outOffset, this.state.length);

        if (iterationCount < 1) {
            throw new IllegalArgumentException("iteration count must be at least 1.");
        }
        
        // i is initialized to 1 because the first cycle happened above.
        for (int i = 1; i < iterationCount; i++) {
            this.hMac.init(passwordParam);
            this.hMac.update(this.state, 0, this.state.length);
            this.hMac.doFinal(this.state, 0);

            // xor the current state against the output.
            // the output is never longer than state.length so this xor against the entire output.
            for (int j = 0; j < this.state.length; j++) {
                out[outOffset + j] ^= this.state[j];
            }
        }
    }
}
