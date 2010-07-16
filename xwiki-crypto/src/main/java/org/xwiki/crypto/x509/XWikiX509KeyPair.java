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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.GeneralSecurityException;

/**
 * Wrapper which contains a {@link PrivateKey} and the corresponding {@link XWikiX509Certificate}.
 * 
 * @version $Id$
 * @since 2.5
 */
public interface XWikiX509KeyPair
{
    /**
     * @return the chain of certificates starting with the user's certificate (the one matching the private key) and
     *         ascending up to the root certificate authority.
     */
    XWikiX509Certificate[] getCertificates();

    /**
     * @return the user's certificate
     */
    XWikiX509Certificate getCertificate();

    /**
     * @return the public key
     */
    PublicKey getPublicKey();

    /**
     * Get the private key from the underlying X500PrivateCredential.
     * If this method completes successfully, hasLeaked will henceforth return true.
     *
     * @param password the password required to get the private key.
     * @return the private key
     * @throws GeneralSecurityException if opening the encrypted key storage fails.
     */
    PrivateKey getPrivateKey(final String password) throws GeneralSecurityException;

    /**
     * @return certificate fingerprint
     */
    String getFingerprint();

    /**
     * @return the certificate and private key in a password protected PKCS#12 container.
     */
    byte[] toPKCS12();

    /**
     * @return the certificate and private key as a base64 encoded password protected PKCS#12 container.
     */
    String toBase64PKCS12();
}

