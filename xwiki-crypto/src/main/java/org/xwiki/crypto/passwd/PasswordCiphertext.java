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
package org.xwiki.crypto.passwd;

import java.io.Serializable;
import java.io.IOException;

import java.security.GeneralSecurityException;


/**
 * Ciphertext represents a single password encrypted text.
 * It can be serialized and deserialized and the same password will be able to decrypt it.
 *
 * @version $Id:$
 * @since 2.5
 */
public interface PasswordCiphertext extends Serializable
{
    /**
     * Initialize this ciphertext with a given plaintext and password.
     * To get the plaintext back, use decryptText with the same password.
     *
     * @param plaintext the text which will be encrypted.
     * @param password the password used to encrypt the plaintext.
     * @throws GeneralSecurityException if something goes wrong while encrypting.
     */
    void init(final String plaintext, final String password)
        throws GeneralSecurityException;

    /**
     * Get the plaintext back from this ciphertext.
     *
     * @param password the user supplied password.
     * @return the original plaintext or null if the password was wrong.
     * @throws GeneralSecurityException if something goes wrong while decrypting.
     */
    String decryptText(final String password)
        throws GeneralSecurityException;

    /**
     * Serialize this ciphertext into a byte array which can later be deserialized and the text decrypted from that.
     *
     * @return a byte array representing this object.
     * @throws IOException if something goes wrong in the serialization framework.
     */
    byte[] serialize()
        throws IOException;
}
