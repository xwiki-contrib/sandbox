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
package org.xwiki.crypto.x509;

import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.GeneralSecurityException;


/**
 * Wrapper which contains a {@link PrivateKey} and the corresponding {@link XWikiX509Certificate}.
 * This class is capable of holding a chain of certificates from the user's certificate back to the root certificate.
 * 
 * @version $Id$
 * @since 2.5
 */
public interface XWikiX509KeyPair extends Serializable
{
    /**
     * @return the user's certificate
     * @throws GeneralSecurityException if the certificate cannot be deserialized.
     */
    XWikiX509Certificate getCertificate() throws GeneralSecurityException;

    /**
     * @return the public key
     * @throws GeneralSecurityException if the certificate with the public key cannot be deserialized.
     */
    PublicKey getPublicKey() throws GeneralSecurityException;

    /**
     * Get the private key from the key pair.
     *
     * @param password the password needed to decrypt the private key.
     * @return the private key or null if the password is incorrect.
     * @throws GeneralSecurityException if the private key cannot be decrypted.
     */
    PrivateKey getPrivateKey(final String password) throws GeneralSecurityException;

    /**
     * @return certificate fingerprint
     * @throws GeneralSecurityException if the certificate to fingerprint cannot be deserialized.
     */
    String getFingerprint() throws GeneralSecurityException;

    /**
     * @return this key pair as a byte array, the private key will remain password encrypted as it is in memory.
     * @throws IOException if something goes wrong within the serialization framework.
     */
    byte[] serialize() throws IOException;

    /**
     * @return this key pair {@link #serialize()}d and converted to a base-64 encoded String.
     * @throws IOException if something goes wrong within the serialization framework.
     */
    String serializeAsBase64() throws IOException;
}

