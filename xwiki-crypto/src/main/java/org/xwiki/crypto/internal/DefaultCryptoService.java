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
package org.xwiki.crypto.internal;

import java.security.GeneralSecurityException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.crypto.CryptoService;
import org.xwiki.crypto.data.XWikiX509Certificate;
import org.xwiki.crypto.data.XWikiX509KeyPair;

/**
 * Service allowing a user to sign text, determine the validity and signer of already signed text, and create keys.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
public class DefaultCryptoService implements CryptoService, Initializable
{
    /** Used for dealing with non cryptographic stuff like getting user document names and URLs. */
    @Requirement
    private UserDocumentUtils userDocUtils;

    /** Handles the generation of keys. */
    private final KeyService keyService = new KeyService();

    /** For signing and verifying signatures on text. */
    private final SignatureService signatureService = new SignatureService();

    /**
     * {@inheritDoc}
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize()
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#certsFromSpkac(java.lang.String, int)
     */
    public XWikiX509Certificate[] certsFromSpkac(final String spkacSerialization, final int daysOfValidity)
        throws GeneralSecurityException
    {
        String userName = userDocUtils.getCurrentUser();
        String webID = userDocUtils.getUserDocURL(userName);
        return this.keyService.certsFromSpkac(spkacSerialization, daysOfValidity, webID, userName);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#newCertAndPrivateKey(int)
     */
    public XWikiX509KeyPair newCertAndPrivateKey(final int daysOfValidity)
        throws GeneralSecurityException
    {
        String userName = userDocUtils.getCurrentUser();
        String webID = userDocUtils.getUserDocURL(userName);
        return this.keyService.newCertAndPrivateKey(daysOfValidity, webID, userName);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#signText(java.lang.String, org.xwiki.crypto.data.XWikiX509KeyPair)
     */
    public String signText(final String textToSign, final XWikiX509KeyPair toSignWith)
        throws GeneralSecurityException
    {
        return this.signatureService.signText(textToSign, toSignWith);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#verifyText(java.lang.String, java.lang.String)
     */
    public XWikiX509Certificate verifyText(final String signedText, final String base64Signature)
        throws GeneralSecurityException
    {
        return this.signatureService.verifyText(signedText, base64Signature);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#encryptText(java.lang.String, org.xwiki.crypto.data.XWikiX509Certificate[])
     */
    public String encryptText(String plaintext, XWikiX509Certificate[] certificatesToEncryptFor)
        throws GeneralSecurityException
    {
        throw new GeneralSecurityException("Not implemented yet. ");
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#decryptText(java.lang.String, org.xwiki.crypto.data.XWikiX509KeyPair)
     */
    public String decryptText(String base64Ciphertext, XWikiX509KeyPair toDecryptWith)
        throws GeneralSecurityException
    {
        throw new GeneralSecurityException("Not implemented yet.");
    }
}
