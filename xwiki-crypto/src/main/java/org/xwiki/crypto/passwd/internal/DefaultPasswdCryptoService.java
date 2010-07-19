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

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.WhirlpoolDigest;
import org.bouncycastle.crypto.engines.CAST5Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import org.bouncycastle.crypto.prng.RandomGenerator;
import org.xwiki.component.annotation.Component;
import org.xwiki.crypto.internal.Convert;
import org.xwiki.crypto.passwd.PasswdCryptoService;


/**
 * A service allowing users to encrypt and decrypt text using a password. This service uses CAST-5 block cipher with
 * the Whirlpool hash function. The user's password is used together with a 20 byte random salt to seed the
 * pseudo random number generator based on the hash function. The generator is used to produce the needed amount
 * of data for the key and the initialization vector. The cipher is run in CBC mode. The text is enciphered and
 * the output is base64 encoded and prepended with the base64 encoded salt (which ends with a : and a newline).
 * The entire text output is wrapped with descriptive header and footer so typical
 * ciphertext might look as follows:
 * <pre>
 * ------BEGIN PASSWORD CAST5CBC-WHIRLPOOL CIPHERTEXT-----
 * 3xbbMX0oWT9ACQv9K0fFOTIr4BU=:
 * jKQsZIfnfQNfrjvvDFNIVhUhBceVhh7C7zoSd0DPGBf+gXFJymCeAApe5SkbG56q
 * j6VmngZAcypqN72vWRhGOBPu/WjDGG0tyNQnaVHLTcWjDmiCQBQqqq7sRJ/SVi/1
 * /cG7npgtTF6+9FAtONY7lg==
 * ------END CIPHERTEXT------
 * </pre>
 * <p>
 * Note: Subclasses implementing other encryption methods should override at least
 * {@link #getCipher()}, {@link #getDigest()} and {@link #getKeyLength()}</p>
 * 
 * @version $Id:$
 * @since 2.5
 */
@Component
public class DefaultPasswdCryptoService implements PasswdCryptoService
{
    /** Size of the salt in bytes. */
    private static final int SALT_SIZE = 20;

    /** The cipher engine. */
    private final PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(this.getCipher());

    /** The hash engine. */
    private final Digest hash = this.getDigest();

    /** Supply of pseudorandomness. */
    private final SecureRandom random = new SecureRandom();

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.passwd.PasswdCryptoService#encryptText(String, String)
     */
    public synchronized String encryptText(final String plaintext, final String password)
        throws GeneralSecurityException
    {
        final byte[] salt = new byte[SALT_SIZE];
        // TODO seed secure random on component initialization, use the same hash function
        this.random.nextBytes(salt);

        this.cipher.reset();
        this.cipher.init(true, this.makeKey(password, salt));

        try {
            final byte[] message = Convert.stringToBytes(plaintext);
            final byte[] out = new byte[cipher.getOutputSize(message.length)];

            int length = this.cipher.processBytes(message, 0, message.length, out, 0);
            this.cipher.doFinal(out, length);

            return this.getHeader() 
                 + Convert.toBase64String(salt)
                 + this.getEndOfSaltMark()
                 + Convert.toChunkedBase64String(out)
                 + this.getFooter();
        } catch (InvalidCipherTextException e) {
            // I don't think this should ever happen for encrypting.
            throw new GeneralSecurityException("Failed to encrypt text", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.passwd.PasswdCryptoService#decryptText(String, String)
     */
    public synchronized String decryptText(final String containingBase64Ciphertext, final String password)
        throws GeneralSecurityException
    {
        final String content = Convert.getContentBetween(containingBase64Ciphertext,
                                                         this.getHeader(),
                                                         this.getFooter());
        final String eos = this.getEndOfSaltMark();
        final String saltString = content.substring(0, content.indexOf(eos));
        final String cipherString = content.substring(content.indexOf(eos) + eos.length());

        final byte[] salt = Convert.fromBase64String(saltString);
        final byte[] ciphertext = Convert.fromBase64String(cipherString);

        this.cipher.reset();
        this.cipher.init(false, this.makeKey(password, salt));

        try {
            final byte[] out = new byte[this.cipher.getOutputSize(ciphertext.length)];

            int length = this.cipher.processBytes(ciphertext, 0, ciphertext.length, out, 0);
            int remaining = this.cipher.doFinal(out, length);

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
     * <p>
     * This function uses a pseudo-random generator based on the used hash function, seeded with the key
     * and the salt to generate the key and an initialization vector of the needed length.</p>
     * 
     *
     * @param password The user supplied password for encryption/decryption.
     * @param salt If encrypting, should be random bytes, if decrypting, should be bytes saved with the ciphertext.
     * @return a symmetric key
     */
    private synchronized CipherParameters makeKey(final String password, final byte[] salt)
    {
        final byte[] passbytes = Convert.stringToBytes(password);

        RandomGenerator prng = new DigestRandomGenerator(this.hash);
        prng.addSeedMaterial(passbytes);
        prng.addSeedMaterial(salt);

        final byte[] key = new byte[this.getKeyLength()];
        final byte[] iv = new byte[this.cipher.getBlockSize()];
        prng.nextBytes(key);
        prng.nextBytes(iv);

        return new ParametersWithIV(new KeyParameter(key), iv);
    }

    /**
     * This implementation uses CAST5-CBC. It is very important to wrap the engine with CBC or similar, otherwise
     * large patches of the same data will translate to large patches of the same ciphertext.
     * see: http://en.wikipedia.org/wiki/Block_cipher_modes_of_operation
     * @return the cipher engine to use.
     */
    protected BlockCipher getCipher()
    {
        return new CBCBlockCipher(new CAST5Engine());
    }

    /**
     * @return a new instance of the hash function to use.
     */
    protected Digest getDigest()
    {
        return new WhirlpoolDigest();
    }

    /**
     * @return the key length in bytes.
     */
    protected int getKeyLength()
    {
        return 16;
    }

    /**
     * @return the String which will mark the beginning of the base64 encoded ciphertext.
     */
    protected String getHeader()
    {
        return "------BEGIN PASSWORD CAST5CBC-WHIRLPOOL CIPHERTEXT-----\n";
    }

    /**
     * @return the string which will mark the end of the base64 encoded ciphertext.
     */
    protected String getFooter()
    {
        return "------END CIPHERTEXT------";
    }

    /**
     * @return the mark which delineates the end of the salt in the ciphertext.
     */
    protected String getEndOfSaltMark()
    {
        return ":\n";
    }
}
