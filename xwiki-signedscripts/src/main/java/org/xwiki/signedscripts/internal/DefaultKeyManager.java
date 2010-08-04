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

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.crypto.internal.UserDocumentUtils;
import org.xwiki.crypto.x509.X509CryptoService;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.crypto.x509.XWikiX509KeyPair;
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
    /** Used to get user certificates. */
    @Requirement
    private UserDocumentUtils docUtils;

    /** Used to generate key pairs. */
    @Requirement
    private X509CryptoService cryptoService;

    /** FIXME. */
    private Map<String, XWikiX509Certificate> certMap = new HashMap<String, XWikiX509Certificate>();

    /** FIXME. */
    private Map<String, XWikiX509KeyPair> keysMap = new HashMap<String, XWikiX509KeyPair>();

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
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#createKeyPair(java.lang.String, int)
     */
    public synchronized String createKeyPair(String password, int daysOfValidity)
        throws GeneralSecurityException
    {
        // TODO rights and actions
        // generate a self-signed certificate
        XWikiX509KeyPair keys = cryptoService.newCertAndPrivateKey(daysOfValidity, password);
        XWikiX509Certificate cert = keys.getCertificate();
        String fingerprint = cert.getFingerprint();
        try {
            // register the certificate in user document first (might fail)
            this.docUtils.addCertificateFingerprint(this.docUtils.getCurrentUser(), fingerprint);
            this.certMap.put(fingerprint, cert);
            this.keysMap.put(fingerprint, keys);
        } catch (Exception exception) {
            throw new GeneralSecurityException(exception.getMessage(), exception);
        }
        // FIXME user creates its own key pair => admin must register it afterwards
        return fingerprint;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#getCertificate(java.lang.String)
     */
    public XWikiX509Certificate getCertificate(String fingerprint)
    {
        return this.certMap.get(fingerprint);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#getKeyPair()
     */
    public XWikiX509KeyPair getKeyPair() throws GeneralSecurityException
    {
        // FIXME check access rights
        return this.keysMap.get(getTrustedFingerprint(docUtils.getCurrentUser()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#registerCertificate(org.xwiki.crypto.x509.XWikiX509Certificate, java.lang.String)
     */
    public void registerCertificate(XWikiX509Certificate certificate, String userName) throws GeneralSecurityException
    {
        if (!docUtils.getCertificateFingerprintsForUser(userName).contains(certificate.getFingerprint())) {
            throw new GeneralSecurityException("Given certificate is not owned by the user \"" + userName + "\"");
        }
        this.certMap.put(certificate.getFingerprint(), certificate);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#unregister(java.lang.String)
     */
    public void unregister(String fingerprint) throws GeneralSecurityException
    {
        this.certMap.remove(fingerprint);
        this.keysMap.remove(fingerprint);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#getKnownFingerprints()
     */
    public Set<String> getKnownFingerprints()
    {
        return this.certMap.keySet();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#getTrustedFingerprint(java.lang.String)
     */
    public String getTrustedFingerprint(String userName)
    {
        for (String fp : docUtils.getCertificateFingerprintsForUser(userName)) {
            if (certMap.containsKey(fp)) {
                return fp;
            }
        }
        return null;
    }
}
