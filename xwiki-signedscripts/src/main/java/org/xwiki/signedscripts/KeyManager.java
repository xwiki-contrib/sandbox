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
package org.xwiki.signedscripts;

import java.security.GeneralSecurityException;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.crypto.x509.XWikiX509KeyPair;


/**
 * Key management component. Allows to create and store key pairs (certificates and private keys).
 * TODO throw something other than GeneralSecurityException
 * 
 * @version $Id$
 * @since 2.5
 */
@ComponentRole
public interface KeyManager
{
    /**
     * Create and register a new key pair (private key and a certificate) signed by the local root.
     * TODO should require global admin rights
     * 
     * @param authorName author name to use
     * @param password the password to use for encrypting the key
     * @param daysOfValidity how many days should the new certificate be valid
     * @return fingerprint of the new key pair
     * @throws GeneralSecurityException on errors or insufficient access rights
     */
    String createKeyPair(String authorName, String password, int daysOfValidity) throws GeneralSecurityException;

    /**
     * Register the given certificate as trusted. Scripts, signed with the corresponding private key
     * will be considered as trusted.
     * TODO should require global admin rights
     * 
     * @param certificate the certificate to register
     * @throws GeneralSecurityException on errors or insufficient access rights
     * @see #parseCertificate(String)
     */
    void registerCertificate(XWikiX509Certificate certificate) throws GeneralSecurityException;

    /**
     * Unregister the certificate or key pair with the given fingerprint. The code previously signed with
     * this certificate/key pair will no longer be considered trusted.
     * <p>
     * Note that unregistering the local root certificate will generate a new one.<p>
     * TODO should require global admin rights
     * 
     * @param fingerprint fingerprint of the certificate or key pair to unregister
     * @throws GeneralSecurityException on errors or insufficient access rights
     */
    void unregister(String fingerprint) throws GeneralSecurityException;

    /**
     * Get a certificate by fingerprint.
     * 
     * @param fingerprint certificate fingerprint to use
     * @return the corresponding certificate
     * @throws GeneralSecurityException if the certificate does not exist
     */
    XWikiX509Certificate getCertificate(String fingerprint) throws GeneralSecurityException;

    /**
     * Get a key pair by certificate fingerprint.
     * TODO should require PR rights
     * TODO should only work for the user's own certificate, i.e. better remove parameter
     * 
     * @param fingerprint certificate fingerprint to use
     * @return the corresponding key pair
     * @throws GeneralSecurityException if the key pair does not exist or on insufficient access rights
     */
    XWikiX509KeyPair getKeyPair(String fingerprint) throws GeneralSecurityException;

    /**
     * Get the local root certificate.
     * 
     * @return local root certificate object
     */
    XWikiX509Certificate getLocalRootCertificate();

    /**
     * Get the global root certificate. Note that this certificate might have been unregistered and
     * is no longer considered trusted.
     * 
     * @return global root certificate object
     */
    XWikiX509Certificate getGlobalRootCertificate();

    /**
     * Get a set of all known trusted certificate fingerprints.
     * 
     * @return set of all known fingerprints
     */
    Set<String> getKnownFingerprints();
}

