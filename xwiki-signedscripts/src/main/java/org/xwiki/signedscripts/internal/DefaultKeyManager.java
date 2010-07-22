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

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.crypto.x509.XWikiX509KeyPair;
import org.xwiki.crypto.x509.internal.DefaultXWikiX509KeyPair;
import org.xwiki.signedscripts.KeyManager;

/**
 * Default implementation of the {@link KeyManager}.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
public class DefaultKeyManager extends AbstractLogEnabled implements KeyManager, Initializable
{
    /** Algorithm to use when generating keys. */
    private static final String KEY_ALGORITHM = "RSA";

    /** The algorithm to use for signing certificates. */
    private static final String SIGN_ALGORITHM = "SHA1withRSA";

    /** Signing algorithm key size in bits. */
    private static final int KEY_SIZE = 2048;

    /** Key pair generator. */
    private KeyPairGenerator kpGen;

    /** Fingerprint of the local root certificate. */
    private String localRootFingerprint;

    /** FIXME. */
    private Map<String, XWikiX509Certificate> certMap = new HashMap<String, XWikiX509Certificate>();

    /** FIXME. */
    private Map<String, XWikiX509KeyPair> keysMap = new HashMap<String, XWikiX509KeyPair>();

    /** FIXME Remove. */
    private final String localRootPwd = "blah";

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        // register Bouncycastle provider if needed
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        try {
            kpGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            kpGen.initialize(KEY_SIZE);
        } catch (GeneralSecurityException exception) {
            getLogger().debug(exception.getMessage(), exception);
            throw new InitializationException("Failed to initialize key pair generator.", exception);
        }
        // FIXME read local and global root certs
        // FIXME DEBUG
        try {
            regenerateLocalRoot();
        } catch (GeneralSecurityException exception) {
            getLogger().debug(exception.getMessage(), exception);
            throw new InitializationException(exception.getMessage(), exception);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.crypto.signedscripts.KeyManager#createKeyPair(java.lang.String, java.lang.String, int)
     */
    public String createKeyPair(String authorName, String password, int daysOfValidity) throws GeneralSecurityException
    {
        KeyPair kp = this.kpGen.generateKeyPair();

        // TODO rights and actions
        // local root certificate might not be present if we are generating it, default to self-signed
        X500Principal author = new X500Principal("CN=" + authorName);
        X500Principal issuer = author;
        PrivateKey signKey = kp.getPrivate();
        String signFingerprint = null;
        if (this.localRootFingerprint != null) {
            XWikiX509Certificate signCert = getLocalRootCertificate();
            issuer = signCert.getSubjectX500Principal();
            signKey = getLocalRootKeyPair().getPrivateKey(this.localRootPwd);
            signFingerprint = signCert.getFingerprint();
        }

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setSubjectDN(author);
        certGen.setIssuerDN(issuer);
        certGen.setSerialNumber(new BigInteger(128, new SecureRandom()));

        certGen.setNotBefore(new Date());
        long day = 1000 * 3600 * 24;
        certGen.setNotAfter(new Date(System.currentTimeMillis() + day * daysOfValidity));

        certGen.setPublicKey(kp.getPublic());
        certGen.setSignatureAlgorithm(SIGN_ALGORITHM);

        XWikiX509Certificate cert = new XWikiX509Certificate(certGen.generate(signKey), signFingerprint);
        String fingerprint = cert.getFingerprint();
        XWikiX509KeyPair keys = new DefaultXWikiX509KeyPair(kp.getPrivate(), password, cert);
        this.certMap.put(fingerprint, cert);
        this.keysMap.put(fingerprint, keys);
        return fingerprint;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.crypto.KeyManager#getCertificate(java.lang.String)
     */
    public XWikiX509Certificate getCertificate(String fingerprint) throws GeneralSecurityException
    {
        XWikiX509Certificate cert = this.certMap.get(fingerprint);
        if (cert == null) {
            throw new GeneralSecurityException("Certificate with fingerprint \"" + fingerprint + "\" not found");
        }
        return cert;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.crypto.KeyManager#getKeyPair(java.lang.String)
     */
    public XWikiX509KeyPair getKeyPair(String fingerprint) throws GeneralSecurityException
    {
        XWikiX509KeyPair kp = this.keysMap.get(fingerprint);
        if (kp == null) {
            throw new GeneralSecurityException("Key pair with fingerprint \"" + fingerprint + "\" was not found");
        }
        return kp;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.crypto.KeyManager#registerCertificate(org.xwiki.crypto.data.XWikiX509Certificate)
     */
    public void registerCertificate(XWikiX509Certificate certificate) throws GeneralSecurityException
    {
        this.certMap.put(certificate.getFingerprint(), certificate);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.crypto.KeyManager#unregister(java.lang.String)
     */
    public void unregister(String fingerprint) throws GeneralSecurityException
    {
        String localFingerprint = getLocalRootCertificate().getFingerprint();
        this.certMap.remove(fingerprint);
        this.keysMap.remove(fingerprint);
        if (localFingerprint.equals(fingerprint)) {
            regenerateLocalRoot();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.crypto.KeyManager#getLocalRootCertificate()
     */
    public XWikiX509Certificate getLocalRootCertificate()
    {
        return getLocalRootKeyPair().getCertificate();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.crypto.KeyManager#getKnownFingerprints()
     */
    public Set<String> getKnownFingerprints()
    {
        return this.certMap.keySet();
    }

    /**
     * Generate a new key pair and replace the local root certificate with it.
     * 
     * @throws GeneralSecurityException on errors
     */
    private void regenerateLocalRoot() throws GeneralSecurityException
    {
        this.certMap.remove(this.localRootFingerprint);
        this.keysMap.remove(this.localRootFingerprint);
        this.localRootFingerprint = null;
        this.localRootFingerprint = createKeyPair("Local Root", this.localRootPwd, 365);
    }

    /**
     * Get the local root key pair.
     * 
     * @return local root key pair object
     */
    private XWikiX509KeyPair getLocalRootKeyPair()
    {
        try {
            return getKeyPair(this.localRootFingerprint);
        } catch (GeneralSecurityException exception) {
            getLogger().debug(exception.getMessage(), exception);
            throw new RuntimeException("Should not happen: " + exception.getMessage(), exception);
        }
    }
}
