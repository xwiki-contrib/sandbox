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

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.crypto.KeyManager;
import org.xwiki.crypto.data.XWikiCertificate;
import org.xwiki.crypto.data.XWikiKeyPair;


/**
 * Default implementation of the {@link KeyManager}.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
public class DefaultKeyManager implements KeyManager, Initializable
{
    /** Algorithm to use when generating keys. */
    private static final String KEY_ALGORITHM = "RSA";

    /** The algorithm to use for signing certificates. */
    private static final String SIGN_ALGORITHM = "SHA1withRSA";

    /** Signing algorithm key size in bits. */
    private static final int KEY_SIZE = 2048;

    /** Key pair generator. */
    private KeyPairGenerator kpGen;

    /** FIXME. */
    private Map<String, XWikiCertificate> certMap = new HashMap<String, XWikiCertificate>(); 

    /** FIXME. */
    private Map<String, XWikiKeyPair> keysMap = new HashMap<String, XWikiKeyPair>(); 

    /**
     * {@inheritDoc}
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        Security.addProvider(new BouncyCastleProvider());
        try {
            kpGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            kpGen.initialize(KEY_SIZE);
        } catch (NoSuchAlgorithmException exception) {
            throw new InitializationException("Failed to initialize key pair generator.", exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.KeyManager#createKeyPair(java.lang.String, java.lang.String, java.util.Date)
     */
    public String createKeyPair(String authorName, String signWithFingerprint, Date expires) throws
        GeneralSecurityException
    {
        KeyPair kp = this.kpGen.generateKeyPair();

        // TODO rights and actions
        XWikiCertificate signCert = null;
        if (signWithFingerprint != null) {
            signCert = getCertificate(signWithFingerprint);
        }
        X500Principal author = new X500Principal("CN=test");
        // self-signed
        X500Principal issuer = author;
        PrivateKey signKey = kp.getPrivate();
        if (signCert != null) {
            issuer = signCert.getCertificate().getSubjectX500Principal();
            signKey = getKeyPair(signWithFingerprint).getPrivateKey();
        }

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setSubjectDN(author);
        certGen.setIssuerDN(issuer);
        certGen.setSerialNumber(new BigInteger(128, new SecureRandom()));

        certGen.setNotBefore(new Date());
        if (expires != null) {
            certGen.setNotAfter(expires);
        } else {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.add(Calendar.YEAR, 25);
            certGen.setNotAfter(calendar.getTime());
        }

        certGen.setPublicKey(kp.getPublic());
        certGen.setSignatureAlgorithm(SIGN_ALGORITHM);

        XWikiCertificate cert = new XWikiCertificate(certGen.generate(signKey), signWithFingerprint, this);
        String fingerprint = cert.getFingerprint();
        XWikiKeyPair keys = new XWikiKeyPair(kp.getPrivate(), cert);
        this.certMap.put(fingerprint, cert);
        this.keysMap.put(fingerprint, keys);
        return fingerprint;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.KeyManager#getCertificate(java.lang.String)
     */
    public XWikiCertificate getCertificate(String fingerprint)
    {
        return this.certMap.get(fingerprint);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.KeyManager#getKeyPair(java.lang.String)
     */
    public XWikiKeyPair getKeyPair(String fingerprint)
    {
        return this.keysMap.get(fingerprint);
    }
}

