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
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.xwiki.crypto.data.XWikiX509Certificate;
import org.xwiki.crypto.data.XWikiX509KeyPair;


/**
 * Implementation of {@link XWikiSignature} that uses PKCS7 encoding. Signatures are stored as a
 * PKCS#7 signed data objects with embedded signer certificate and detached content.
 * 
 * @version $Id$
 * @since 2.5
 */
public class SignatureService
{
    /** Unique SHA1 OID. TODO find a way to convert algorithm name to the corresponding OID */
    private static final String SHA1_OID = CMSSignedGenerator.DIGEST_SHA1;

    /** Bouncy Castle provider name. */
    private static final String PROVIDER = "BC";

    /** Type of the certificate store to use for PKCS7 encoding/decoding. */
    private static final String CERT_STORE_TYPE = "Collection";

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#signText(java.lang.String, org.xwiki.crypto.data.XWikiX509KeyPair)
     */
    public String signText(final String textToSign,
                           final XWikiX509KeyPair toSignWith,
                           final String password)
        throws GeneralSecurityException
    {
        XWikiX509Certificate certificate = toSignWith.getCertificate();

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        Collection<?> certs = Collections.singleton(certificate);
        CertStore store = CertStore.getInstance(CERT_STORE_TYPE, new CollectionCertStoreParameters(certs));

        try {
            gen.addCertificatesAndCRLs(store);
            gen.addSigner(toSignWith.getPrivateKey(password), certificate, SHA1_OID);
            byte[] data = textToSign.getBytes();
            CMSSignedData cmsData = gen.generate(new CMSProcessableByteArray(data), false, PROVIDER);

            return Convert.toBase64String(cmsData.getEncoded());
        } catch (GeneralSecurityException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeneralSecurityException(exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.CryptoService#verifyText(java.lang.String, java.lang.String)
     */
    public XWikiX509Certificate verifyText(String signedText, String base64Signature) throws GeneralSecurityException
    {
        try {
            byte[] data = signedText.getBytes();
            byte[] signature = Convert.fromBase64String(base64Signature);
            CMSSignedData cmsData = new CMSSignedData(new CMSProcessableByteArray(data), signature);
            CertStore certStore = cmsData.getCertificatesAndCRLs(CERT_STORE_TYPE, PROVIDER);
            SignerInformationStore signers = cmsData.getSignerInfos();

            int numSigners = signers.getSigners().size();
            if (numSigners == 0) {
                throw new GeneralSecurityException("No signers found");
            }
            if (numSigners > 1) {
                throw new GeneralSecurityException("Only one signature is supported, found " + numSigners);
            }
            XWikiX509Certificate result = null;
            for (Iterator<?> it = signers.getSigners().iterator(); it.hasNext();) {
                if (result != null) {
                    throw new GeneralSecurityException("Only one certificate is supported");
                }
                SignerInformation signer = (SignerInformation) it.next();
                Collection< ? extends Certificate> certs = certStore.getCertificates(signer.getSID());
                for (Iterator<? extends Certificate> cit = certs.iterator(); cit.hasNext();) {
                    Certificate certificate = cit.next();
                    if (!signer.verify(certificate.getPublicKey(), PROVIDER)) {
                        return null;
                    }
                    // FIXME I don't really need the certificate here, fingerprint would suffice
                    if (certificate instanceof X509Certificate) {
                        result = new XWikiX509Certificate((X509Certificate) certificate, null);
                    }
                }
            }
            return result;
        } catch (GeneralSecurityException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeneralSecurityException(exception);
        }
    }
}

