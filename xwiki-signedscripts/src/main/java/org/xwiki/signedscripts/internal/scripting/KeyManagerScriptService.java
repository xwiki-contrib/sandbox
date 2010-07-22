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
@Component(roles = { ScriptService.class }, hints = { "keymanager" })
public class KeyManagerScriptService implements ScriptService, KeyManager
{
    /** Enclosed key manager. */
    @Requirement
    private KeyManager keyManager;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#createKeyPair(java.lang.String, java.lang.String, int)
     */
    public String createKeyPair(String authorName, String password, int daysOfValidity) throws GeneralSecurityException
    {
        return keyManager.createKeyPair(authorName, password, daysOfValidity);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#registerCertificate(org.xwiki.crypto.x509.XWikiX509Certificate)
     */
    public void registerCertificate(XWikiX509Certificate certificate) throws GeneralSecurityException
    {
        keyManager.registerCertificate(certificate);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#unregister(java.lang.String)
     */
    public void unregister(String fingerprint) throws GeneralSecurityException
    {
        keyManager.unregister(fingerprint);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#getCertificate(java.lang.String)
     */
    public XWikiX509Certificate getCertificate(String fingerprint) throws GeneralSecurityException
    {
        return keyManager.getCertificate(fingerprint);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#getKeyPair(java.lang.String)
     */
    public XWikiX509KeyPair getKeyPair(String fingerprint) throws GeneralSecurityException
    {
        return keyManager.getKeyPair(fingerprint);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#getLocalRootCertificate()
     */
    public XWikiX509Certificate getLocalRootCertificate()
    {
        return keyManager.getLocalRootCertificate();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#getKnownFingerprints()
     */
    public Set<String> getKnownFingerprints()
    {
        return keyManager.getKnownFingerprints();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.KeyManager#getTrustedFingerprint(java.lang.String)
     */
    public String getTrustedFingerprint(String userName)
    {
        return keyManager.getTrustedFingerprint(userName);
    }
}
