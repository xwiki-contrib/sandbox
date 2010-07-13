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
package org.xwiki.crypto.data.internal;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;

import org.xwiki.crypto.data.XWikiX509Certificate;
import org.xwiki.crypto.data.XWikiX509KeyPair;
import org.xwiki.crypto.internal.Convert;


/**
 * Wrapper class storing a {@link PrivateKey} and the corresponding {@link XWikiX509Certificate}.
 * TODO password-protect the private key
 * 
 * @version $Id$
 * @since 2.5
 */
public class DefaultXWikiX509KeyPair implements XWikiX509KeyPair
{
    /** Private key. */
    private final PrivateKey key;

    /** Certificate. */
    private final XWikiX509Certificate certificate;

    /**
     * Create new {@link XWikiX509KeyPair}.
     * 
     * @param key the private key to use
     * @param certificate the certificate to use
     */
    public DefaultXWikiX509KeyPair(PrivateKey key, XWikiX509Certificate certificate)
    {
        this.key = key;
        this.certificate = certificate;
    }

    /**
     * Create a private key by parsing the given string. The string should contain a private key
     * encoded in PEM format.
     * 
     * @param pemEncoded private key in PEM format
     * @return the parsed private key
     * @throws GeneralSecurityException on parse errors
     */
    public static PrivateKey privateKeyFromString(String pemEncoded) throws GeneralSecurityException
    {
        // FIXME implement privateKeyFromString
        throw new GeneralSecurityException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiX509KeyPair#hashCode()
     */
    @Override
    public int hashCode()
    {
        return getCertificate().hashCode();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiX509KeyPair#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DefaultXWikiX509KeyPair) {
            DefaultXWikiX509KeyPair kp = (DefaultXWikiX509KeyPair) obj;
            return getFingerprint().equals(kp.getFingerprint()) && getPrivateKey().equals(kp.getPrivateKey());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiX509KeyPair#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("XWikiX509KeyPair\n");
        builder.append("------------\n");
        builder.append(getCertificate().toString());
        builder.append(exportPrivateKey());
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiX509KeyPair#getCertificate()
     */
    public XWikiX509Certificate getCertificate()
    {
        return certificate;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiX509KeyPair#getPublicKey()
     */
    public PublicKey getPublicKey()
    {
        return certificate.getPublicKey();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiX509KeyPair#getPrivateKey()
     */
    public PrivateKey getPrivateKey()
    {
        return key;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiX509KeyPair#getFingerprint()
     */
    public String getFingerprint()
    {
        return certificate.getFingerprint();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiX509KeyPair#export()
     */
    public String export() throws CertificateEncodingException
    {
        return getCertificate().export() + exportPrivateKey();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiX509KeyPair#exportPrivateKey()
     */
    public String exportPrivateKey()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("-----BEGIN PRIVATE KEY-----\n");
        builder.append(Convert.toChunkedBase64String(getPrivateKey().getEncoded()));
        builder.append("-----END PRIVATE KEY-----\n");
        return builder.toString();
    }
}

