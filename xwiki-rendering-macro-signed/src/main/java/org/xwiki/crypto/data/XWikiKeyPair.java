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
import java.security.cert.CertificateEncodingException;

import org.apache.commons.codec.binary.Base64;


/**
 * Wrapper class storing a {@link PrivateKey} and the corresponding {@link XWikiX509Certificate}.
 * TODO password-protect the private key
 * 
 * @version $Id$
 * @since 2.5
 */
public class XWikiKeyPair
{
    /** Private key. */
    private final PrivateKey key;

    /** Certificate. */
    private final XWikiX509Certificate certificate;

    /**
     * Create new {@link XWikiKeyPair}.
     * 
     * @param key the private key to use
     * @param certificate the certificate to use
     */
    public XWikiKeyPair(PrivateKey key, XWikiX509Certificate certificate)
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
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("XWikiKeyPair\n");
        builder.append("------------\n");
        builder.append(getCertificate().toString());
        builder.append(exportPrivateKey());
        return builder.toString();
    }

    /**
     * @return the certificate
     */
    public XWikiX509Certificate getCertificate()
    {
        return certificate;
    }

    /**
     * @return the public key
     */
    public PublicKey getPublicKey()
    {
        return certificate.getPublicKey();
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

    /**
     * Get the internal X509 certificate and RSA private key in a standard PEM format.
     * 
     * @return the certificate and private key in PEM format
     * @throws CertificateEncodingException on errors (very unlikely)
     */
    public String export() throws CertificateEncodingException
    {
        return getCertificate().export() + exportPrivateKey();
    }

    /**
     * @return the private key in PKCS#8 PEM format
     */
    private String exportPrivateKey()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("-----BEGIN PRIVATE KEY-----\n");
        builder.append(Base64.encodeBase64String(getPrivateKey().getEncoded()));
        builder.append("-----END PRIVATE KEY-----\n");
        return builder.toString();
    }
}

