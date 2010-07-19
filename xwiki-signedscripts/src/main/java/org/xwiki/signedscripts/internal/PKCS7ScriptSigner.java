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
package org.xwiki.signedscripts.internal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.crypto.x509.X509CryptoService;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.crypto.x509.XWikiX509KeyPair;
import org.xwiki.signedscripts.KeyManager;
import org.xwiki.signedscripts.ScriptSigner;
import org.xwiki.signedscripts.SignedScript;
import org.xwiki.signedscripts.SignedScriptKey;


/**
 * Script signing component using PKCS7 encoding for signatures.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component
public class PKCS7ScriptSigner implements ScriptSigner
{
    /** PKCS7 crypto service. */
    @Requirement
    private X509CryptoService pkcs7;

    /** Key manager. */
    @Requirement
    private KeyManager keyManager;

    /**
     * {@inheritDoc}
     * @see org.xwiki.signedscripts.ScriptSigner#sign(java.lang.String, java.lang.String, java.lang.String)
     */
    public SignedScript sign(String code, String fingerprint, String password) throws GeneralSecurityException
    {
        SignedScript script = prepareScriptForSigning(code, fingerprint);
        XWikiX509KeyPair keyPair = this.keyManager.getKeyPair(script.get(SignedScriptKey.FINGERPRINT));
        try {
            String signature = pkcs7.signText(script.getDataToSign(), keyPair, password);
            return new PKCS7SignedScript(script, signature);
        } catch (GeneralSecurityException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeneralSecurityException("Failed to sign a script.", exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.signedscripts.ScriptSigner#prepareScriptForSigning(java.lang.String, java.lang.String)
     */
    public SignedScript prepareScriptForSigning(String code, String fingerprint) throws GeneralSecurityException
    {
        PKCS7SignedScript script = new PKCS7SignedScript(code, fingerprint);
        XWikiX509Certificate certificate = this.keyManager.getCertificate(script.get(SignedScriptKey.FINGERPRINT));

        // get certificate data
        script.set(SignedScriptKey.AUTHOR, certificate.getAuthorName());
        script.set(SignedScriptKey.AUTHORITY, certificate.getIssuerName());

        script.set(SignedScriptKey.CREATEDON, getDateFormat().format(new Date()));
        // FIXME ExpiresOn
        // script.set(SignedScriptKey.EXPIRESON, dateFormat.format(expiresOn));

        // FIXME find out xwiki version
        script.set(SignedScriptKey.XWIKIVERSION, "2.4M2");
        // FIXME bind to document

        return script;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.signedscripts.ScriptSigner#constructSignedScript(org.xwiki.signedscripts.SignedScript, java.lang.String)
     */
    public SignedScript constructSignedScript(SignedScript preparedScript, String base64Signature)
        throws GeneralSecurityException
    {
        try {
            PKCS7SignedScript signedScript = new PKCS7SignedScript(preparedScript, base64Signature);
            // return verified script to avoid constructing invalid script objects
            return getVerifiedScript(signedScript.serialize());
        } catch (IOException exception) {
            throw new GeneralSecurityException(exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.signedscripts.ScriptSigner#getVerifiedScript(java.lang.String)
     */
    public SignedScript getVerifiedScript(String signedScript) throws GeneralSecurityException
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
            checkValidity(script);

            // verify the certificate
            verify(certificate);

            // FIXME check XWiki and document constraints
            // XWIKIVERSION("XWikiVersion"),
            // DOCUMENT("Document", ""),

            String signature = script.get(SignedScriptKey.SIGNATURE);
            XWikiX509Certificate signCert = pkcs7.verifyText(script.getDataToSign(), signature);
            if (signCert == null || !signCert.equals(certificate)) {
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
     * Check validity of the signature, i.e. if the creation date lays in the past and expiration
     * date lays in the future.
     * 
     * @param script the script object to check
     * @throws ParseException if the date is stored in an invalid format
     * @throws GeneralSecurityException if the validation fails
     */
    private void checkValidity(PKCS7SignedScript script) throws ParseException, GeneralSecurityException
    {
        Date now = new Date();
        Date created = getDateFormat().parse(script.get(SignedScriptKey.CREATEDON));
        Date expires = null;
        if (script.isSet(SignedScriptKey.EXPIRESON)) {
            expires = getDateFormat().parse(script.get(SignedScriptKey.EXPIRESON));
        }
        if (created.after(now) || (expires != null && expires.before(now))) {
            throw new GeneralSecurityException("Signed script has expired or is not yet valid.");
        }
    }

    /**
     * Check validity and verify the given certificate. Recursively validates parent certificates by their
     * fingerprints. Throws an exception if the verification fails or on errors.
     * 
     * @param certificate the certificate to verify
     * @throws GeneralSecurityException on verification failure or errors
     */
    private void verify(XWikiX509Certificate certificate) throws GeneralSecurityException
    {
        // check validity
        certificate.checkValidity();

        // verify this certificate. Note that the key manager will throw an error if the parent is not trusted
        XWikiX509Certificate parentCert = keyManager.getCertificate(certificate.getIssuerFingerprint());
        PublicKey key = parentCert.getPublicKey();
        certificate.verify(key);

        // verify the parent
        if (!certificate.equals(parentCert)) {
            verify(parentCert);
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

