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
package org.xwiki.signedscripts.internal.scripting;

import java.security.GeneralSecurityException;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.crypto.x509.XWikiX509KeyPair;
import org.xwiki.script.service.ScriptService;
import org.xwiki.signedscripts.KeyManager;

/**
 * Script service wrapping a {@link KeyManager} component.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component("keymanager")
public class KeyManagerScriptService implements ScriptService
{
    /** Enclosed key manager. */
    @Requirement
    private KeyManager keyManager;

    /**
     * @param authorName author name to use
     * @param password the password to use for encrypting the key
     * @param daysOfValidity how many days should the new certificate be valid
     * @return fingerprint of the new key pair
     * @throws GeneralSecurityException on errors or insufficient access rights
     * @see org.xwiki.crypto.KeyManager#createKeyPair(java.lang.String, java.util.Date)
     */
    public String createKeyPair(String authorName, String password, int daysOfValidity) throws GeneralSecurityException
    {
        return keyManager.createKeyPair(authorName, password, daysOfValidity);
    }

    /**
     * @param certificate the certificate to register
     * @throws GeneralSecurityException on errors or insufficient access rights
     * @see org.xwiki.crypto.KeyManager#registerCertificate(org.xwiki.crypto.data.XWikiX509Certificate)
     */
    public void registerCertificate(XWikiX509Certificate certificate) throws GeneralSecurityException
    {
        keyManager.registerCertificate(certificate);
    }

    /**
     * @param fingerprint fingerprint of the certificate or key pair to unregister
     * @throws GeneralSecurityException on errors or insufficient access rights
     * @see org.xwiki.crypto.KeyManager#unregister(java.lang.String)
     */
    public void unregister(String fingerprint) throws GeneralSecurityException
    {
        keyManager.unregister(fingerprint);
    }

    /**
     * @param fingerprint certificate fingerprint to use
     * @return the corresponding certificate
     * @throws GeneralSecurityException if the certificate does not exist
     * @see org.xwiki.crypto.KeyManager#getCertificate(java.lang.String)
     */
    public XWikiX509Certificate getCertificate(String fingerprint) throws GeneralSecurityException
    {
        return keyManager.getCertificate(fingerprint);
    }

    /**
     * @param fingerprint certificate fingerprint to use
     * @return the corresponding key pair
     * @throws GeneralSecurityException if the key pair does not exist or on insufficient access rights
     * @see org.xwiki.crypto.KeyManager#getKeyPair(java.lang.String)
     */
    public XWikiX509KeyPair getKeyPair(String fingerprint) throws GeneralSecurityException
    {
        return keyManager.getKeyPair(fingerprint);
    }

    /**
     * @return local root certificate object
     * @see org.xwiki.crypto.KeyManager#getLocalRootCertificate()
     */
    public XWikiX509Certificate getLocalRootCertificate()
    {
        return keyManager.getLocalRootCertificate();
    }

    /**
     * @return global root certificate object
     * @see org.xwiki.crypto.KeyManager#getGlobalRootCertificate()
     */
    public XWikiX509Certificate getGlobalRootCertificate()
    {
        return keyManager.getGlobalRootCertificate();
    }

    /**
     * @return set of all known fingerprints
     * @see org.xwiki.crypto.KeyManager#getKnownFingerprints()
     */
    public Set<String> getKnownFingerprints()
    {
        return keyManager.getKnownFingerprints();
    }
}
