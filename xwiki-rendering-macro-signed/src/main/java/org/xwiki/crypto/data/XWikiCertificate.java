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
package org.xwiki.crypto.data;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.xwiki.crypto.KeyManager;


/**
 * Wrapper class for a X509 certificate used for verification of signed scripts. 
 * 
 * @version $Id$
 * @since 2.5
 */
public class XWikiCertificate
{
    /** Digest algorithm used to generate the fingerprint. */
    private static final String FINGERPRINT_ALGORITHM = "SHA1";

    /** The actual certificate. */
    private final X509Certificate certificate;

    /** Certificate fingerprint. */
    private final String fingerprint;

    /** Certificate fingrprint of the issuer. */
    private final String issuerFingerprint;

    /** Key manager where this certificate is stored. */
    private final KeyManager keyManager;

    /**
     * Create new {@link XWikiCertificate}.
     * 
     * @param certificate the actual certificate to use
     * @param issuerFp fingerprint of the issuer certificate, null if self-signed
     * @param keyManager the key manager where this certificate and its issuer are stored
     */
    public XWikiCertificate(X509Certificate certificate, String issuerFp, KeyManager keyManager)
    {
        this.certificate = certificate;
        this.keyManager = keyManager;
        this.fingerprint = XWikiCertificate.calculateFingerprint(certificate);
        if (issuerFp == null) {
            this.issuerFingerprint = this.fingerprint;
        } else {
            this.issuerFingerprint = issuerFp;
        }
    }

    /**
     * Calculate the fingerprint of the given certificate. Throws a {@link RuntimeException} on errors.
     * 
     * @param certificate the certificate to use
     * @return certificate fingerprint in hex
     */
    public static String calculateFingerprint(X509Certificate certificate)
    {
        try {
            MessageDigest hash = MessageDigest.getInstance(FINGERPRINT_ALGORITHM);
            return Hex.encodeHexString(hash.digest(certificate.getEncoded()));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return getCertificate().hashCode();
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof XWikiCertificate) {
            XWikiCertificate cert = (XWikiCertificate) obj;
            return getFingerprint().equals(cert.getFingerprint());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        final String format = "%20s : %s\n";
        StringBuilder builder = new StringBuilder();
        X509Certificate c = getCertificate();
        builder.append("XWikiCertificate\n");
        builder.append("---------------------------------------------------------------\n");
        builder.append(String.format(format, "Fingerprint", getFingerprint()));
        builder.append(String.format(format, "SubjectDN", getAuthorName()));
        builder.append(String.format(format, "IssuerDN", getIssuerName()));
        builder.append(String.format(format, "Issuer Fingerprint", getIssuerFingerprint()));
        builder.append(String.format(format, "SerialNumber", c.getSerialNumber().toString(16)));
        builder.append(String.format(format, "Start Date", c.getNotBefore()));
        builder.append(String.format(format, "Final Date", c.getNotAfter()));
        builder.append(String.format(format, "Public Key Algorithm", c.getPublicKey().getAlgorithm()));
        builder.append(String.format(format, "Signature Algorithm", c.getSigAlgName()));
        try {
            builder.append(export());
        } catch (CertificateEncodingException exception) {
            // ignore
        }
        return builder.toString();
    }

    /**
     * @return the certificate
     */
    public X509Certificate getCertificate()
    {
        return this.certificate;
    }

    /**
     * @return the fingerprint
     */
    public String getFingerprint()
    {
        return fingerprint;
    }

    /**
     * Get the internal X509 certificate in a standard PEM format.
     * 
     * @return the certificate in PEM format
     * @throws CertificateEncodingException on errors (very unlikely)
     */
    public String export() throws CertificateEncodingException
    {
        StringBuilder builder = new StringBuilder();
        builder.append("-----BEGIN CERTIFICATE-----\n");
        builder.append(Base64.encodeBase64String(getCertificate().getEncoded()));
        builder.append("-----END CERTIFICATE-----\n");
        return builder.toString();
    }

    /**
     * Get issuer name of this certificate. Same as {@link #getAuthorName()} of the certificate
     * obtained via {@link #getIssuerFingerprint()}.
     * 
     * @return issuer name
     */
    public String getIssuerName()
    {
        return getCertificate().getIssuerX500Principal().getName();
    }

    /**
     * Get the fingerprint of the issuer certificate.
     * 
     * @return issuer fingerprint
     */
    public String getIssuerFingerprint()
    {
        return issuerFingerprint;
    }

    /**
     * Get name of the author (subject name) of this certificate. 
     * @return author name
     */
    public String getAuthorName()
    {
        return getCertificate().getSubjectX500Principal().getName();
    }

    /**
     * Check validity and verify this certificate. Recursively validates parent certificates by their fingerprints.
     * Throws an exception if the verification fails or on errors.
     * 
     * @throws GeneralSecurityException on verification failure or errors
     */
    public void verify() throws GeneralSecurityException
    {
        // check validity
        getCertificate().checkValidity();

        // verify this certificate. Note that the key manager will throw an error if the parent is not trusted
        XWikiCertificate parentCert = keyManager.getCertificate(getIssuerFingerprint());
        PublicKey key = parentCert.getCertificate().getPublicKey();
        this.getCertificate().verify(key);

        // verify the parent
        if (!equals(parentCert)) {
            parentCert.verify();
        }
    }
}

