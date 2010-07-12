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
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.crypto.XWikiSignature;
import org.xwiki.crypto.data.XWikiX509Certificate;
import org.xwiki.crypto.data.XWikiX509KeyPair;


/**
 * Implementation of {@link XWikiSignature} that uses PKCS7 encoding. Signatures are stored as a
 * PKCS#7 signed data objects with embedded signer certificate and detached content.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component("pkcs7signature")
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
public class PKCS7Signature implements XWikiSignature, Initializable
{
    /** Unique SHA1 OID. TODO find a way to convert algorithm name to the corresponding OID */
    private static final String SHA1_OID = CMSSignedGenerator.DIGEST_SHA1;

    /** Bouncy Castle provider name. */
    private static final String PROVIDER = "BC";

    /** Type of the certificate store to use for PKCS7 encoding/decoding. */
    private static final String CERT_STORE_TYPE = "Collection";

    /**
     * {@inheritDoc}
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.XWikiSignature#sign(byte[], org.xwiki.crypto.data.XWikiX509KeyPair)
     */
    public byte[] sign(byte[] data, XWikiX509KeyPair keyPair) throws GeneralSecurityException
    {
        XWikiX509Certificate certificate = keyPair.getCertificate();
        PrivateKey key = keyPair.getPrivateKey();

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        Collection<?> certs = Collections.singleton(certificate);
        CertStore store = CertStore.getInstance(CERT_STORE_TYPE, new CollectionCertStoreParameters(certs));

        try {
            gen.addCertificatesAndCRLs(store);
            gen.addSigner(key, certificate, SHA1_OID);
            CMSSignedData cmsData = gen.generate(new CMSProcessableByteArray(data), false, PROVIDER);

            return cmsData.getEncoded();
        } catch (GeneralSecurityException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeneralSecurityException(exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.XWikiSignature#verify(byte[], byte[], org.xwiki.crypto.data.XWikiX509Certificate)
     */
    public boolean verify(byte[] data, byte[] signature, XWikiX509Certificate certificate)
        throws GeneralSecurityException
    {
        try {
            CMSSignedData cmsData = new CMSSignedData(new CMSProcessableByteArray(data), signature);
            CertStore certStore = cmsData.getCertificatesAndCRLs(CERT_STORE_TYPE, PROVIDER);
            SignerInformationStore signers = cmsData.getSignerInfos();

            if (signers.getSigners().size() == 0) {
                throw new GeneralSecurityException("No signers found");
            }
            boolean result = true;
            for (Iterator<?> it = signers.getSigners().iterator(); it.hasNext();) {
                SignerInformation signer = (SignerInformation) it.next();
                Collection< ? extends Certificate> certs = certStore.getCertificates(signer.getSID());
                for (Iterator<? extends Certificate> cit = certs.iterator(); cit.hasNext();) {
                    if (!XWikiX509Certificate.calculateFingerprint(cit.next()).equals(certificate.getFingerprint())) {
                        throw new GeneralSecurityException("Unknown signer certificate.");
                    }
                    result &= signer.verify(certificate.getPublicKey(), PROVIDER);
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

