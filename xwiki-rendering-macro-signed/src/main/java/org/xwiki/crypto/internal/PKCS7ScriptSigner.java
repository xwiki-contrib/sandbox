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
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.crypto.Converter;
import org.xwiki.crypto.KeyManager;
import org.xwiki.crypto.ScriptSigner;
import org.xwiki.crypto.XWikiSignature;
import org.xwiki.crypto.data.SignedScript;
import org.xwiki.crypto.data.SignedScriptKey;
import org.xwiki.crypto.data.XWikiX509Certificate;
import org.xwiki.crypto.data.XWikiX509KeyPair;


/**
 * Script signing component using PKCS7 encoding for signatures.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component
public class PKCS7ScriptSigner implements ScriptSigner
{
    /** Base64 encoder/decoder. */
    @Requirement("base64")
    private Converter base64;

    /** PKCS7 signer/verifier. */
    @Requirement("pkcs7signature")
    private XWikiSignature pkcs7;

    /** Key manager. */
    @Requirement
    private KeyManager keyManager;

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.ScriptSigner#sign(java.lang.String, java.lang.String)
     */
    public SignedScript sign(String code, String fingerprint) throws GeneralSecurityException
    {
        PKCS7SignedScript script = new PKCS7SignedScript(code, fingerprint);

        XWikiX509KeyPair keyPair = this.keyManager.getKeyPair(script.get(SignedScriptKey.FINGERPRINT));
        XWikiX509Certificate certificate = keyPair.getCertificate();

        // get certificate data
        script.set(SignedScriptKey.AUTHOR, certificate.getAuthorName());
        script.set(SignedScriptKey.AUTHORITY, certificate.getIssuerName());

        script.set(SignedScriptKey.CREATEDON, getDateFormat().format(new Date()));
        // FIXME ExpiresOn
        // script.set(SignedScriptKey.EXPIRESON, dateFormat.format(expiresOn));

        // FIXME find out xwiki version
        script.set(SignedScriptKey.XWIKIVERSION, "2.4M2");
        // FIXME bind to document

        try {
            byte[] signature = pkcs7.sign(script.getRawData(), keyPair);
            script.set(SignedScriptKey.SIGNATURE, base64.encode(signature));
            return script;
        } catch (GeneralSecurityException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeneralSecurityException("Failed to sign a script.", exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.ScriptSigner#getVerifiedCode(java.lang.String)
     */
    public SignedScript getVerifiedCode(String signedScript) throws GeneralSecurityException
    {
        try {
            PKCS7SignedScript script = new PKCS7SignedScript(signedScript);
            XWikiX509Certificate certificate = this.keyManager.getCertificate(script.get(SignedScriptKey.FINGERPRINT));

            // compare author and authority with the certificate
            String certAuthor = certificate.getAuthorName();
            String certIssuer = certificate.getIssuerName();
            if (!certAuthor.equals(script.get(SignedScriptKey.AUTHOR))
                || !certIssuer.equals(script.get(SignedScriptKey.AUTHORITY))) {
                throw new GeneralSecurityException("Certificate data does not match the signature");
            }

            // check validity of the signature
            Date now = new Date();
            Date created = getDateFormat().parse(script.get(SignedScriptKey.CREATEDON));
            Date expires = null;
            if (script.isSet(SignedScriptKey.EXPIRESON)) {
                expires = getDateFormat().parse(script.get(SignedScriptKey.EXPIRESON));
            }
            if (created.after(now) || (expires != null && expires.before(now))) {
                throw new GeneralSecurityException("Signed script has expired or is not yet valid.");
            }

            // verify the certificate
            certificate.verify();

            // FIXME check XWiki and document constraints
            // XWIKIVERSION("XWikiVersion"),
            // DOCUMENT("Document", ""),

            byte[] signature = base64.decode(script.get(SignedScriptKey.SIGNATURE));
            if (!pkcs7.verify(script.getRawData(), signature, certificate)) {
                throw new GeneralSecurityException("Signature is incorrect");
            }
            return script;
        } catch (GeneralSecurityException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeneralSecurityException("Failed to verify a signed script.", exception);
        }
    }

    /**
     * Get the date formatter used for CreatedOn/ExpiresOn fields.
     * 
     * @return date formatter
     */
    private DateFormat getDateFormat()
    {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat;
    }
}

