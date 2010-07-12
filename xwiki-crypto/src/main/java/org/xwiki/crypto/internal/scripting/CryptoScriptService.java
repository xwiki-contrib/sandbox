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
import java.security.InvalidParameterException;
import java.security.cert.X509Certificate;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.crypto.ScriptSigner;
import org.xwiki.crypto.data.SignedScript;
import org.xwiki.script.service.ScriptService;

import org.bouncycastle.jce.netscape.NetscapeCertRequest;
import org.bouncycastle.util.encoders.Base64;

/**
 * Script service allowing a user to sign text, determine the validity and signer of already signed text,
 * create keys, and register new certificates.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component("crypto")
public class CryptoScriptService implements ScriptService
{
    /** Used for dealing with non cryptographic stuff like getting user document names and URLs. */
    @Requirement
    private UserDocumentUtils userDocUtils;

    /** Used for the actual key making, also holds any secrets. */
    private Keymaker theKeymaker = new Keymaker();

    /**
     * Creates an array of X509Certificate containing:
     * 1. A certificate from the given <a href="http://en.wikipedia.org/wiki/Spkac">SPKAC</a>
     * 2. A certificate authority certificate which will validate the first certificate in the array.
     *
     * Safari, Firefox, Opera, return through the <keygen> element an SPKAC request
     * (see the specification in html5)
     *
     * @param spkacSerialization a <a href="http://en.wikipedia.org/wiki/Spkac">SPKAC</a> Certificate Signing Request
     * @param daysOfValidity number of days before the certificate should become invalid.
     * @return an array of 2 X509Certificates.
     * @throws java.security.GeneralSecurityException if something goes wrong while creating the certificate.
     */
    public X509Certificate[] certsFromSpkac(String spkacSerialization, int daysOfValidity)
        throws GeneralSecurityException
    {
        if (spkacSerialization == null) {
            throw new InvalidParameterException("SPKAC parameter is null");
        }
        NetscapeCertRequest certRequest = new NetscapeCertRequest(Base64.decode(spkacSerialization));

        // Determine the webId by asking who's creating the cert (needed only for FOAFSSL compatibility)
        String userName = userDocUtils.getCurrentUser();
        String webID = userDocUtils.getUserDocURL(userName);

        return this.theKeymaker.makeClientAndAuthorityCertificates(certRequest.getPublicKey(),
                                                                   daysOfValidity,
                                                                   true,
                                                                   webId,
                                                                   userName);
    }

    /**
     * Produce a pkcs7 signature for the given text.
     * Text will be signed with the key belonging to the author of the code which calls this.
     * TODO: Implement this.
     *
     * @param textToSign the text which the user wishes to sign.
     * @return a signature which can be used to validate the signed text.
     * @throws GeneralSecurityException if anything goes wrong during signing.
     */
    public String signText(String textToSign)
        throws GeneralSecurityException
    {
        throw new GeneralSecurityException("Not implemented yet.");
    }

    /**
     * Validate a pkcs7 signature and return the name of the user who signed it.
     *
     * @param text the text which has been signed.
     * @param signature the signature on the text in base-64 format.
     * @return the name of the user who signed the text or null if the signature is invalid.
     * @throws GeneralSecurityException if anything goes wrong.
     */
    public String validateText(String text, String signature)
        throws GeneralSecurityException
    {
    }


    /** Enclosed script signer. */
    @Requirement
    private ScriptSigner signer;

    /**
     * @param code code to sign
     * @param fingerprint certificate fingerprint identifying the private key to use
     * @return signed script object
     * @throws GeneralSecurityException on errors
     * @see org.xwiki.crypto.ScriptSigner#sign(java.lang.String, java.lang.String)
     */
    public SignedScript sign(String code, String fingerprint) throws GeneralSecurityException
    {
        return signer.sign(code, fingerprint);
    }

    /**
     * @param signedScript serialized signed script object 
     * @return code contained in the signed script
     * @throws GeneralSecurityException if verification fails or on errors
     * @see org.xwiki.crypto.ScriptSigner#getVerifiedCode(java.lang.String)
     */
    public SignedScript getVerifiedCode(String signedScript) throws GeneralSecurityException
    {
        return signer.getVerifiedCode(signedScript);
    }
}

