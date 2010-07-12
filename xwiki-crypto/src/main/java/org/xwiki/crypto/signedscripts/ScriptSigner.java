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
package org.xwiki.crypto.signedscripts;

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
     * Create a signed script object by signing given code with the private key identified by the fingerprint.
     * TODO specify optional parameters, like expiration date
     * TODO should require PR and work only with user's own fingerprint (i.e. remove parameter)
     * 
     * @param code code to sign
     * @param fingerprint certificate fingerprint identifying the private key to use
     * @return signed script object
     * @throws GeneralSecurityException on errors
     * @see KeyManager
     */
    SignedScript sign(String code, String fingerprint) throws GeneralSecurityException;

    /**
     * Construct the string that needs to be signed to construct a valid signature for a {@link SignedScript}.
     * This data can be transfered to the client and signed using a private key stored in the browser.
     * 
     * @param code code to sign
     * @param fingerprint certificate fingerprint identifying the certificate to use
     * @return the data to sign represented in a string (not encoded)
     * @throws GeneralSecurityException on errors
     * @see #constructSignedScript(String, String)
     */
    String getDataToSign(String code, String fingerprint) throws GeneralSecurityException;

    /**
     * Create a signed script object by combining the given code and signature. The signature must have been
     * produced by {@link #getDataToSign(String, String)} called with the same code. 
     * 
     * @param code signed code
     * @param base64Signature Base64 encoded signature of the data obtained by {@link #getDataToSign(String, String)}
     * @return signed script object
     * @throws GeneralSecurityException on errors
     * @see {@link #getDataToSign(String, String)}, {@link #sign(String, String)}
     */
    SignedScript constructSignedScript(String code, String base64Signature) throws GeneralSecurityException;

    /**
     * Create a signed script by parsing and verifying a serialized signed script.
     * 
     * @param signedScript serialized signed script object 
     * @return code contained in the signed script
     * @throws GeneralSecurityException if verification fails or on errors
     * @see SignedScript#serialize()
     */
    SignedScript getVerifiedCode(String signedScript) throws GeneralSecurityException;
}

