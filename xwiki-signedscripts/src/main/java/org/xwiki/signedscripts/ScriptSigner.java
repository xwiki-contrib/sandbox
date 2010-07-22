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
package org.xwiki.signedscripts;

import java.security.GeneralSecurityException;

import org.xwiki.component.annotation.ComponentRole;

/**
 * Script signing component. Can be used to create and verify signed scripts.
 * 
 * @see SignedScript
 * @version $Id$
 * @since 2.5
 */
@ComponentRole
public interface ScriptSigner
{
    /**
     * Create a signed script object by signing given code with the private key of the current user.
     * <p>
     * TODO specify optional parameters (like expiration date)<br>
     * TODO should require PR
     * 
     * @param code code to sign
     * @param password the password to unlock the private key
     * @return signed script object
     * @throws GeneralSecurityException on errors
     * @see KeyManager
     */
    SignedScript sign(String code, String password) throws GeneralSecurityException;

    /**
     * Construct the script object in the same way as {@link #sign(String, String)}, but not sign it. The resulting
     * script contains all needed data to construct a valid signature. It can be retrieved using the method
     * {@link SignedScript#getDataToSign()}, transfered to the client and signed using a private key stored in the
     * browser.
     * <p>
     * The signing process must be completed by calling {@link #constructSignedScript(String, String)}.
     * </p>
     * 
     * @param code code to sign
     * @return initialized script object, ready to be signed
     * @throws GeneralSecurityException on errors
     * @see #constructSignedScript(String, String)
     */
    SignedScript prepareScriptForSigning(String code) throws GeneralSecurityException;

    /**
     * Create a signed script object by combining the prepared script object and the signature. The signature must have
     * been produced by signing the result of {@link SignedScript#getDataToSign(String, String)} called on the script
     * object created using {@link #prepareScriptForSigning(String)}.
     * <p>
     * The following sequence of operations:
     * 
     * <pre>
     * SignedScript preparedScript = signer.prepareScriptForSigning(code, fingerprint);
     * 
     * String signature = signExternally(preparedScript.getDataToSign());
     * 
     * SignedScript signedScript = signer.constructSignedScript(preparedScript, signature);
     * </pre>
     * 
     * (where signExternally() signs the data with the private key corresponding to the fingerprint) is equivalent to
     * 
     * <pre>
     * SignedScript signedScript = signer.sign(code, fingerprint);
     * </pre>
     * 
     * for private key stored on the server.
     * </p>
     * 
     * @param preparedScript initialized script object produced by {@link #prepareScriptForSigning(String)}
     * @param base64Signature Base64 encoded signature of the data returned by {@link SignedScript#getDataToSign()}
     * @return signed script object
     * @throws GeneralSecurityException on errors
     * @see {@link #getDataToSign(String, String)}, {@link #sign(String, String)}
     */
    SignedScript constructSignedScript(SignedScript preparedScript, String base64Signature)
        throws GeneralSecurityException;

    /**
     * Create a signed script by parsing and verifying a serialized signed script.
     * 
     * @param signedScript serialized signed script object
     * @return the parsed signed script
     * @throws GeneralSecurityException if verification fails or on errors
     * @see SignedScript#serialize()
     */
    SignedScript getVerifiedScript(String signedScript) throws GeneralSecurityException;
}
