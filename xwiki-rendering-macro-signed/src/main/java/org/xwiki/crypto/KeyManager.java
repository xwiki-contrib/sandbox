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
package org.xwiki.crypto;

import java.security.GeneralSecurityException;
import java.util.Date;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.crypto.data.XWikiCertificate;
import org.xwiki.crypto.data.XWikiKeyPair;


/**
 * Key management component. Allows to create and store key pairs (certificates and private keys).
 * 
 * @version $Id$
 * @since 2.5
 */
@ComponentRole
public interface KeyManager
{
    /**
     * Create and register a new key pair (private key and a certificate).
     * 
     * @param authorName author name to use
     * @param signWithFingerprint fingerprint of the certificate to use for signing, self-signed if null
     * @param expires expiration date, never expires if null
     * @return fingerprint of the new key pair
     * @throws GeneralSecurityException on errors
     */
    String createKeyPair(String authorName, String signWithFingerprint, Date expires) throws
        GeneralSecurityException;

    /**
     * Get the certificate by fingerprint.
     * 
     * @param fingerprint certificate fingerprint to use
     * @return the corresponding certificate
     */
    XWikiCertificate getCertificate(String fingerprint);

    /**
     * Get a key pair by certificate fingerprint.
     * 
     * @param fingerprint certificate fingerprint to use
     * @return the corresponding key pair
     */
    XWikiKeyPair getKeyPair(String fingerprint);
}

