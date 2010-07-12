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

//import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

import org.bouncycastle.util.encoders.Base64;

/**
 * Utility class allowing conversion from Base64 encoded String to various cryptographic objects.
 * 
 * @version $Id$
 * @since 2.5
 */
public class ConversionUtils
{
    /** The number of characters of Base64 text to print between each newline. */
    private final int lineLength = 65;

    /**
     * the character set to use when converting a byte array to a string for base 64 encoding.
     * Why "US-ASCII"?! 
     * US-ASCII is 7 bit and anything over code point 127 is most definitly not Base64. US-ASCII is defined in 
     * java.nio.charset.Charset as a character set which must appear in all java implementations.
     * Almost all character encodings threat the first 128 characters the same.
     */
    private final String base64EncodingCharacterSet = "US-ASCII";

    /** The mark of the beginning of a certificate in Base64 encoded DER format (PEM) */
    private final String x509BeginCertificate = "-----BEGIN CERTIFICATE-----";

    /** The mark of the end of a certificate in Base64 encoded DER format (PEM) */
    private final String x509EndCertificate = "-----END CERTIFICATE-----";

    /** The system's newline character. */
    private final String newline = System.getProperty("line.separator");

    /**
     * Reads a string and looks for a PEM formatted Certificate and if found, decodes it and stops.
     * If there are multiple certificates in the string, only the first will be taken.
     * An invalid key or invalid Base64 encoding will result in undefined behavior.
     * Anything between 
     * -----BEGIN CERTIFICATE-----
     * and 
     * -----END CERTIFICATE-----
     * is assumed to be a base64 encoded X509Certificate
     *
     * @param stringContainingPEMFormattedCertificate the text to look through to find a certificate.
     */
    public X509Certificate decodeX509Certificate(String stringContainingPEMFormattedCertificate)
    {
        int beginningIndex = stringContainingPEMFormattedCertificate.indexOf(this.x509BeginCertificate);
        if (beginningIndex < 0) {
            throw new InvalidParameterException("No certificate found in String\n"
                                                + "expecting: " + this.x509BeginCertificate);
        }
        int endIndex = stringContainingPEMFormattedCertificate.indexOf(this.x509EndCertificate, beginningIndex);
        if (beginningIndex < 0) {
            throw new InvalidParameterException("No end of certificate found in String\n"
                                                + "expecting: " + this.x509EndCertificate);
        }
        beginningIndex += this.x509BeginCertificate.length();
        String encodedCert = stringContainingPEMFormattedCertificate.substring(beginningIndex, endIndex);
        byte[] certDER = Base64.decode(encodedCert.getBytes(this.base64EncodingCharacterSet));
        return new X509Certificate(certDER);
    }

    /**
     * Encode an X509Certificate as a PEM string
     *
     * @param input the binary data to encode
     * @return A base 64 String of characters sutable for storing the provided binary data.
     */
    public String encodeX509Certificate(X509Certificate toEncode)
    {
        return getBase64Encoded(toEncode.getEncoded());
    }

    /**
     * Take the input and encode in Base64 format, split into seperate lines so that no line is longer than
     * the value of the field: lineLength.
     *
     * @param input the binary data to encode
     * @return A base 64 String of characters sutable for storing the provided binary data.
     */
    public String getBase64Encoded(byte[] input)
    {
        byte[] encodedBytes = Base64.encode(input);
        StringBuilder out = new StringBuilder(encodedBytes.length);

        int index = 0;
        while (index + this.lineLength < encodedBytes.length) {
            out.append(new String(encodedBytes, index, this.lineLength, base64EncodingCharacterSet));
            out.append(newline);
            index += this.lineLength;
        }
        out.append(new String(encodedBytes, index, encodedBytes.length - index, base64EncodingCharacterSet));
        return out.toString();
    }



    private final String pkcs1BeginRSAPrivateKey = "-----BEGIN RSA PRIVATE KEY-----";

    private final String pkcs1EndRSAPrivateKey = "-----END RSA PRIVATE KEY-----";

    /**
     * Reads a string and looks for a PKCS#1 formatted private key and if found, decodes it and stops.
     * If there are multiple private keys in the string, the first will be taken.
     * An invalid key or invalid Base64 encoding will result in undefined behavior.
     * Anything between -----BEGIN RSA PRIVATE KEY-----
     * and -----END RSA PRIVATE KEY-----
     * is assumed to be a base64 encoded private key.
     *
     * @param textToFindKeyIn the text to look through to find a private key.
     */
    public RSAPrivateKey decodeRSAPrivateKey(String textToFindKeyIn)
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Gets the encoded form of an RSA private key in Base64 format wrapped with:
     * -----BEGIN RSA PRIVATE KEY-----
     * and
     * -----END RSA PRIVATE KEY-----
     *
     * @param key the key to encode.
     */
    public String encodeRSAPrivateKey(RSAPrivateKey key)
    {
        return
              this.pkcs1BeginRSAPrivateKey
            + this.newline
            + this.getBase64Encoded(key.getEncoded())
            + this.newline
            + this.pkcs1EndRSAPrivateKey;
    }
}
