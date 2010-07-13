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

/**
 * Wrapper for storing a {@link PrivateKey} and the corresponding {@link XWikiX509Certificate}.
 * 
 * @version $Id$
 * @since 2.5
 */
public interface XWikiX509KeyPair
{
    /**
     * @return the certificate
     */
    public XWikiX509Certificate getCertificate();

    /**
     * @return the public key
     */
    public PublicKey getPublicKey();

    /**
     * @return the private key
     */
    public PrivateKey getPrivateKey();

    /**
     * @return certificate fingerprint
     */
    public String getFingerprint();

    /**
     * Get the internal X509 certificate and RSA private key in a standard PEM format.
     * 
     * @return the certificate and private key in PEM format
     * @throws CertificateEncodingException on errors (very unlikely)
     */
    public String export() throws CertificateEncodingException;

    /**
     * @return the private key in PKCS#8 PEM format
     */
    private String exportPrivateKey();
}

