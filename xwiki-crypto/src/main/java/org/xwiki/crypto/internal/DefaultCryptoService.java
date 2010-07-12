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
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.crypto.Converter;
import org.xwiki.crypto.CryptoService;
import org.xwiki.crypto.data.XWikiX509Certificate;
import org.xwiki.script.service.ScriptService;

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.netscape.NetscapeCertRequest;
import org.bouncycastle.x509.X509Store;

/**
 * Service allowing a user to sign text, determine the validity and signer of already signed text, and create keys.
 * FIXME merge with {@link PKCS7CryptoService}
 * 
 * @version $Id$
 * @since 2.5
 */
@Component
public class DefaultCryptoService implements CryptoService
{
    /** Used for dealing with non cryptographic stuff like getting user document names and URLs. */
    @Requirement
    private UserDocumentUtils userDocUtils;

    /** Base64 encoder. */
    @Requirement("base64")
    private Converter base64;

    /** Used for the actual key making, also holds any secrets. */
    private final Keymaker theKeymaker = new Keymaker();

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.CryptoService#certsFromSpkac(String, int)
     */
    public XWikiX509Certificate[] certsFromSpkac(final String spkacSerialization, final int daysOfValidity)
        throws GeneralSecurityException
    {
        if (spkacSerialization == null) {
            throw new InvalidParameterException("SPKAC parameter is null");
        }
        NetscapeCertRequest certRequest = new NetscapeCertRequest(base64.decode(spkacSerialization));

        // Determine the webId by asking who's creating the cert (needed only for FOAFSSL compatibility)
        String userName = userDocUtils.getCurrentUser();
        String webID = userDocUtils.getUserDocURL(userName);

        X509Certificate[] certs = this.theKeymaker.makeClientAndAuthorityCertificates(certRequest.getPublicKey(),
                                                                                      daysOfValidity,
                                                                                      true,
                                                                                      webId,
                                                                                      userName);
        return new String[] {
//XXX
            base64.encode(certs[0].getEncoded()),
            base64.encode(certs[1].getEncoded())
        };
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.CryptoService#certsFromPublicKey(PublicKey, int)
     */
    public String[] certsFromPublicKey(final String key, final int daysOfValidity)
    {
        if (key == null) {
            throw new InvalidParameterException("Public key parameter is null");
        }

        // Determine the webId by asking who's creating the cert (needed only for FOAFSSL compatibility)
        String userName = userDocUtils.getCurrentUser();
        String webID = userDocUtils.getUserDocURL(userName);

        // In this case the non-repudiation bit is cleared because we assume the private key is stored on the server
        // where it is vulnerable.
        return this.theKeymaker.makeClientAndAuthorityCertificates(key, daysOfValidity, false, webId, userName);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.CryptoService#newKeyPair()
     */
    public String[] newKeyPair()
    {
        KeyPair pair = this.theKeymaker.newKeyPair();
        return new String[] {
            base64.encode(pair.getPrivate().getEncoded()),
            base64.encode(pair.getPublic().getEncoded())
        };
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.CryptoService#signText(String, String)
     */
    public String signText(final String textToSign, final String privateKeyToSignWith)
        throws GeneralSecurityException
    {
        throw new GeneralSecurityException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.CryptoService#verifyText(String)
     */
    public XWikiX509Certificate verifyText(final String text, final String signature) throws GeneralSecurityException
    {
        try {
            CMSSignedData cmsData = new CMSSignedData(new CMSProcessableByteArray(data), signature);
            X509Store certStore = cmsData.getCertificates(CERT_STORE_TYPE, PROVIDER);
            SignerInformationStore signers = cmsData.getSignerInfos();

            if (signers.getSigners().size() == 0) {
                throw new GeneralSecurityException("No signers found");
            }

            boolean result = true;
            for (SignerInformation signer : signers.getSigners()) {
                for (X509Certificate cert : certStore.getMatches(signer.getSID())) {
                    // Get the user named in this certificate, then get their user page and make sure their page
                    // this certificate added as an XObject.
                    
                }
            }
            return result;
        } catch (GeneralSecurityException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeneralSecurityException(exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.CryptoService#encryptText(String, String)
     */
    public String encryptText(final String textToEncrypt, final String publicKeyToEncryptFor)
        throws GeneralSecurityException
    {
        throw new GeneralSecurityException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.crypto.CryptoService#decryptText(String, String)
     */
    public String decryptText(final String textToDecrypt, final String privateKeyToDecryptWith)
        throws GeneralSecurityException
    {
        throw new GeneralSecurityException("Not implemented yet.");
    }
}
