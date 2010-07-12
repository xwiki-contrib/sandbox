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
package org.xwiki.crypto;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.xwiki.component.annotation.ComponentRole;

/**
 * Service allowing components to sign text, determine the validity and signer of already signed text,
 * create keys, and register new certificates.
 * 
 * @version $Id$
 * @since 2.5
 */
@ComponentRole
public interface CryptoService
{
    /**
     * Creates an array of Base64 encoded DER formatted X509Certificates containing:
     * 1. A certificate from the given <a href="http://en.wikipedia.org/wiki/Spkac">SPKAC</a>
     * 2. A certificate authority certificate which will validate the first certificate in the array.
     *
     * Safari, Firefox, Opera, return through the <keygen> element an SPKAC request
     * (see the specification in html5)
     *
     * @param spkacSerialization a <a href="http://en.wikipedia.org/wiki/Spkac">SPKAC</a> Certificate Signing Request
     * @param daysOfValidity number of days before the certificate should become invalid.
     * @return an array of 2 X509Certificates in Base64 encoded DER format.
     * @throws GeneralSecurityException if something goes wrong while creating the certificate.
     */
    X509Certificate[] certsFromSpkac(final String spkacSerialization, final int daysOfValidity)
        throws GeneralSecurityException;

    /**
     * Creates an array of Base64 encoded DER formatted X509Certificates containing:
     * 1. A certificate from the given public key.
     * 2. A certificate authority certificate which will validate the first certificate in the array.
     *
     * @param daysOfValidity number of days before the certificate should become invalid.
     * @return an array of 2 X509Certificates in Base64 encoded DER format.
     * @throws GeneralSecurityException if something goes wrong while creating the certificate.
     */
    X509CertAndPrivateKey newCertAndPrivateKey(final int daysOfValidity)
        throws GeneralSecurityException;

    /**
     * Creates an array of Base64 encoded keys containing:
     * 1. A private key
     * 2. A matching public key.
     *
     * @return An array of Base64 encoded serialized keys.
     */
    String[] newKeyPair();

    /**
     * Decode the given Base64 String into binary.
     * Useful because some web browsers only recognize DER format and all functions here use Base64 encoded DER format
     * Note: The only difference between PEM format and Base64 encoded DER is that PEM has a header and footer.
     * For example:
     *  -----BEGIN CERTIFICATE-----
     *   base64 of DER
     *  ----END CERTIFICATE -----
     * This method does not remove header and footer.
     * Behavior is undefined if input is not all valid Base64 format.
     *
     * @param input the String of Base64 characters to decode.
     * @return binary format of input.
     * 
     */
    byte[] base64Decode(final String input);

    /**
     * Produce a pkcs#7 signature for the given text.
     * Text will be signed with the key belonging to the author of the code which calls this.
     *
     * @param textToSign the text which the user wishes to sign.
     * @param privateKeyToSignWith the private key (serialized in Base64 encoded DER format) to sign the text with.
     * @return a signature which can be used to validate the signed text.
     * @throws GeneralSecurityException if anything goes wrong during signing.
     */
    String signText(final String textToSign, final String privateKeyToSignWith) throws GeneralSecurityException;

    /**
     * Validate a pkcs#7 signature and return the name of the user who signed it.
     *
     * @param text the text which has been signed.
     * @param signature the signature on the text in Base64 encoded DER format.
     * @return the name of the user who signed the text or null if the signature is invalid.
     * @throws GeneralSecurityException if anything goes wrong.
     */
    String validateText(final String text, final String signature) throws GeneralSecurityException;

    /**
     * Encrypt a piece of text in pkcs#7/CMS/SMIME format with a public key so that only the holder of the matching 
     * private key may read it. The private key need not be on the server and this format is supported by major
     * email clients allowing sensitive data to be stored encrypted and mailed to authorized people.
     *
     * @param textToEncrypt the text to encrypt.
     * @param certificatesToEncryptFor one or more X509 certificates (in Base64 encoded DER format) belonging to people
     *                                 authorized to read the data.
     * @return cyphertext (in Base64 format) which can be decrypted back to "textToEncrypt" by any of the private keys
     *         matching certificatesToEncryptFor.
     * @throws GeneralSecurityException if something goes wrong.
     */
    String encryptText(final String textToEncrypt, final String[] certificatesToEncryptFor)
        throws GeneralSecurityException;

    /**
     * Decrypt a piece of text encrypted with encryptText.
     *
     * @param textToDecrypt the cyphertext to decrypt.
     * @param privateKeyToDecryptWith the private key (in Base64 encoded DER format) of the user who wants to decrypt 
     *                                the text.
     * @return the decrypted text or null if the private key is not authorized.
     * @throws GeneralSecurityException if something goes wrong.
     */
    String decryptText(final String textToDecrypt, final String privateKeyToDecryptWith)
        throws GeneralSecurityException;
}
