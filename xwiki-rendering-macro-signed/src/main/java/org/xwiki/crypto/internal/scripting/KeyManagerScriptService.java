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

import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.crypto.KeyManager;
import org.xwiki.crypto.data.XWikiCertificate;
import org.xwiki.crypto.data.XWikiKeyPair;
import org.xwiki.script.service.ScriptService;


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
     * @param expires expiration date, never expires if null
     * @return fingerprint of the new key pair
     * @throws GeneralSecurityException on errors or insufficient access rights
     * @see org.xwiki.crypto.KeyManager#createKeyPair(java.lang.String, java.util.Date)
     */
    public String createKeyPair(String authorName, Date expires) throws GeneralSecurityException
    {
        return keyManager.createKeyPair(authorName, expires);
    }

    /**
     * @param certificate the certificate to register
     * @throws GeneralSecurityException on errors or insufficient access rights
     * @see org.xwiki.crypto.KeyManager#registerCertificate(org.xwiki.crypto.data.XWikiCertificate)
     */
    public void registerCertificate(XWikiCertificate certificate) throws GeneralSecurityException
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
    public XWikiCertificate getCertificate(String fingerprint) throws GeneralSecurityException
    {
        return keyManager.getCertificate(fingerprint);
    }

    /**
     * @param encoded X509 certificate in PEM format
     * @return corresponding certificate object
     * @throws GeneralSecurityException on parse errors
     * @see org.xwiki.crypto.KeyManager#parseCertificate(java.lang.String)
     */
    public XWikiCertificate parseCertificate(String encoded) throws GeneralSecurityException
    {
        return keyManager.parseCertificate(encoded);
    }

    /**
     * @param fingerprint certificate fingerprint to use
     * @return the corresponding key pair
     * @throws GeneralSecurityException if the key pair does not exist or on insufficient access rights
     * @see org.xwiki.crypto.KeyManager#getKeyPair(java.lang.String)
     */
    public XWikiKeyPair getKeyPair(String fingerprint) throws GeneralSecurityException
    {
        return keyManager.getKeyPair(fingerprint);
    }

    /**
     * @return local root certificate object
     * @see org.xwiki.crypto.KeyManager#getLocalRootCertificate()
     */
    public XWikiCertificate getLocalRootCertificate()
    {
        return keyManager.getLocalRootCertificate();
    }

    /**
     * @return global root certificate object
     * @see org.xwiki.crypto.KeyManager#getGlobalRootCertificate()
     */
    public XWikiCertificate getGlobalRootCertificate()
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

