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
import java.security.GeneralSecurityException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.InvalidCipherTextException;

import org.xwiki.crypto.internal.SerializationUtils;
import org.xwiki.crypto.internal.Convert;
import org.xwiki.crypto.passwd.PasswordCiphertext;
import org.xwiki.crypto.passwd.KeyDerivationFunction;


/**
 * A service allowing users to encrypt and decrypt text using a password.
 * <p>
 * Note: Subclasses implementing other encryption methods should override at least
 * {@link #getKeyDerivationFunction()}, {@link #getCipher()} and {@link #getKeyLength()}
 * and avoid using fields since this class is serialized to produce the ciphertext.</p>
 *
 * @version $Id:$
 * @since 2.5
 */
public abstract class AbstractPasswordCiphertext implements PasswordCiphertext
{
    /** The actual encrypted text. */
    private byte[] ciphertext;

    /** The function for determining the key from the password (this includes the salt). */
    private KeyDerivationFunction keyFunction;

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.passwd.PasswordCiphertext#init(String, String)
     */
    public synchronized void init(final String plaintext, final String password)
        throws GeneralSecurityException
    {
        this.keyFunction = this.getKeyDerivationFunction(this.getKeyLength() + this.getCipher().getBlockSize());

        PaddedBufferedBlockCipher cipher = this.getCipher();
        cipher.reset();
        cipher.init(true, this.makeKey(password));

        try {
            final byte[] message = Convert.stringToBytes(plaintext);
            this.ciphertext = new byte[cipher.getOutputSize(message.length)];

            int length = cipher.processBytes(message, 0, message.length, this.ciphertext, 0);
            cipher.doFinal(this.ciphertext, length);
        } catch (InvalidCipherTextException e) {
            // I don't think this should ever happen for encrypting.
            throw new GeneralSecurityException("Failed to encrypt text", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.passwd.PasswordCiphertext#decryptText(String)
     */
    public synchronized String decryptText(final String password)
        throws GeneralSecurityException
    {
        PaddedBufferedBlockCipher cipher = this.getCipher();
        cipher.reset();
        cipher.init(false, this.makeKey(password));

        try {
            final byte[] out = new byte[cipher.getOutputSize(ciphertext.length)];

            int length = cipher.processBytes(ciphertext, 0, ciphertext.length, out, 0);
            int remaining = cipher.doFinal(out, length);

            // length+remaining is the actual length of the output. getOutputSize is close but still leaves a few
            // nulls at the top of the array.
            final byte[] unpadded = new byte[length + remaining];
            System.arraycopy(out, 0, unpadded, 0, unpadded.length);

            return Convert.bytesToString(unpadded);
        } catch (InvalidCipherTextException e) {
            // We are going to assume here that the password was wrong.
            return null;
        }
    }

    /**
     * Generate a key (and an initialization vector) for the encryption engine.
     * This function uses a {@link KeyDerivationFunction}
     * This function expectes this.keyFunction#hashPassword to return a byte array
     * which is the size of the key + size of the initializration vector.
     *
     * @param password The user supplied password for encryption/decryption.
     * @return a symmetric key
     */
    private synchronized CipherParameters makeKey(final String password)
    {
        final byte[] passbytes = Convert.stringToBytes(password);

        final byte[] key = new byte[this.getKeyLength()];
        final byte[] iv = new byte[this.getCipher().getBlockSize()];

        final byte[] keyAndIV = this.keyFunction.hashPassword(passbytes);

        System.arraycopy(keyAndIV, 0, key, 0, key.length);
        System.arraycopy(keyAndIV, key.length, iv, 0, iv.length);

        return new ParametersWithIV(new KeyParameter(key), iv);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.passwd.PasswordCiphertext#serialize()
     */
    public byte[] serialize()
        throws IOException
    {
        return SerializationUtils.serialize(this);
    }

    /**
     * @return the key length in bytes.
     */
    protected int getKeyLength()
    {
        return 16;
    }

    /**
     * Get the key derivation function initialized and ready to call hashPassword.
     *
     * @param keySize the length of the output key in bytes.
     * @return initialized key derivation function.
     */
    protected abstract KeyDerivationFunction getKeyDerivationFunction(int keySize);


    /**
     * The cipher engine. It is very important to wrap the engine with CBC or similar, otherwise
     * large patches of the same data will translate to large patches of the same ciphertext.
     * see: http://en.wikipedia.org/wiki/Block_cipher_modes_of_operation
     *
     * @return the cipher engine to use.
     */
    protected abstract PaddedBufferedBlockCipher getCipher();
}
