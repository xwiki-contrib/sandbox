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

import java.util.Arrays;
import java.io.IOException;

import org.xwiki.crypto.passwd.PasswordVerificationFunction;
import org.xwiki.crypto.passwd.KeyDerivationFunction;


/**
 * Default password verification function wraps a key derivation function and stores the hash output.
 *
 * @since 2.5
 * @version $Id:$
 */
public class DefaultPasswordVerificationFunction implements PasswordVerificationFunction
{
    private static final PasswordVerificationFunctionUtils PASSWORD_UTILS =
        new PasswordVerificationFunctionUtils();

    private byte[] passwordHash;

    private KeyDerivationFunction underlyingHashFunction;

    /**
     * {@inheritDoc}
     *
     * @see PasswordVerificationFunction#init(KeyDerivationFunction, byte[])
     */
    public void init(final KeyDerivationFunction underlyingHashFunction,
                     final byte[] password)
    {
        this.underlyingHashFunction = underlyingHashFunction;
        this.passwordHash = this.underlyingHashFunction.hashPassword(password);
    }

    /**
     * {@inheritDoc}
     *
     * @see PasswordVerificationFunction#serialize()
     */
    public byte[] serialize() throws IOException
    {
        return PASSWORD_UTILS.serialize(this);
    }

    /**
     * {@inheritDoc}
     *
     * @see PasswordVerificationFunction#init(KeyDerivationFunction, byte[])
     */
    public boolean isPasswordCorrect(final byte[] password)
    {
        return Arrays.equals(this.passwordHash, this.underlyingHashFunction.hashPassword(password));
    }
}
