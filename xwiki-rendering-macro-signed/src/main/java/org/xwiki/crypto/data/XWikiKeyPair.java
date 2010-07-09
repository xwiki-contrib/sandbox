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
package org.xwiki.crypto.data;

import java.security.PrivateKey;
import java.security.PublicKey;


/**
 * Wrapper class storing a {@link PrivateKey} and the corresponding {@link XWikiCertificate}. 
 * 
 * @version $Id$
 * @since 2.5
 */
public class XWikiKeyPair
{
    /** Private key. */
    private final PrivateKey key;

    /** Certificate. */
    private final XWikiCertificate certificate;

    /**
     * Create new {@link XWikiKeyPair}.
     * 
     * @param key the private key to use
     * @param certificate the certificate to use
     */
    public XWikiKeyPair(PrivateKey key, XWikiCertificate certificate)
    {
        this.key = key;
        this.certificate = certificate;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return getCertificate().hashCode();
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof XWikiKeyPair) {
            XWikiKeyPair kp = (XWikiKeyPair) obj;
            return getFingerprint().equals(kp.getFingerprint()) && getPrivateKey().equals(kp.getPrivateKey());
        }
        return false;
    }

    /**
     * @return the certificate
     */
    public XWikiCertificate getCertificate()
    {
        return certificate;
    }

    /**
     * @return the public key
     */
    public PublicKey getPublicKey()
    {
        return certificate.getCertificate().getPublicKey();
    }

    /**
     * @return the private key
     */
    public PrivateKey getPrivateKey()
    {
        return key;
    }

    /**
     * @return certificate fingerprint
     */
    public String getFingerprint()
    {
        return certificate.getFingerprint();
    }
}

