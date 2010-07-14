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
package org.xwiki.crypto.internal.scripting;

import java.security.GeneralSecurityException;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import org.xwiki.crypto.CryptoService;
import org.xwiki.crypto.data.XWikiX509Certificate;
import org.xwiki.crypto.data.XWikiX509KeyPair;

import org.xwiki.script.service.ScriptService;

/**
 * Script service allowing a user to sign text, determine the validity and signer of already signed text,
 * create keys, and register new certificates.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component(roles = { ScriptService.class }, hints = { "crypto" })
public class CryptoScriptService implements ScriptService, CryptoService
{
    /** The service which this class wraps. */
    @Requirement
    private CryptoService crypto;

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#certsFromSpkac(java.lang.String, int)
     */
    public XWikiX509Certificate[] certsFromSpkac(final String spkacSerialization, final int daysOfValidity)
        throws GeneralSecurityException
    {
        return this.crypto.certsFromSpkac(spkacSerialization, daysOfValidity);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#newCertAndPrivateKey(int)
     */
    public XWikiX509KeyPair newCertAndPrivateKey(final int daysOfValidity, final String password)
        throws GeneralSecurityException
    {
        return this.crypto.newCertAndPrivateKey(daysOfValidity, password);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#signText(java.lang.String, org.xwiki.crypto.data.XWikiX509KeyPair)
     */
    public String signText(final String textToSign, final XWikiX509KeyPair toSignWith, final String password)
        throws GeneralSecurityException
    {
        return this.crypto.signText(textToSign, toSignWith, password);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#verifyText(java.lang.String, java.lang.String)
     */
    public XWikiX509Certificate verifyText(final String signedText, final String base64Signature)
        throws GeneralSecurityException
    {
        return this.crypto.verifyText(signedText, base64Signature);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#encryptText(java.lang.String, org.xwiki.crypto.data.XWikiX509Certificate[])
     */
    public String encryptText(final String plaintext, final XWikiX509Certificate[] certificatesToEncryptFor)
        throws GeneralSecurityException
    {
        return this.crypto.encryptText(plaintext, certificatesToEncryptFor);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#decryptText(java.lang.String, org.xwiki.crypto.data.XWikiX509KeyPair)
     */
    public String decryptText(final String base64Ciphertext,
                              final XWikiX509KeyPair toDecryptWith,
                              final String password)
        throws GeneralSecurityException
    {
        return this.crypto.decryptText(base64Ciphertext, toDecryptWith, password);
    }

    /**
     * Deserialize an X509 certificate from a PEM formatted string.
     * @param pemFormatCert a String created by {@link org.xwiki.crypto.data.XWikiX509Certificate#toPEMString()}
     *                      or from OpenSSL or any other standards compliant X509 certificate generator in PEM format.
     * @return an {@link org.xwiki.crypto.data.XWikiX509Certificate} which extends 
     *         {@link java.security.cert.X509Certificate} and can be used by methods in this class as well as with
     *          third party encryption tools.
     * @throws GeneralSecurityException If there isn't a valid {@link XWikiX509Certificate#CERT_BEGIN} or
     *                                  {@link XWikiX509Certificate#CERT_END} tag, or if there is an exception parsing
     *                                  the content inbetween.
     */
    public XWikiX509Certificate certFromPEM(final String pemFormatCert)
        throws GeneralSecurityException
    {
        return XWikiX509Certificate.fromPEMString(pemFormatCert);
    }
}

