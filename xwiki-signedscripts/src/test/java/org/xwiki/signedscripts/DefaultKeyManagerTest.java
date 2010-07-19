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
import java.security.cert.CertificateExpiredException;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.crypto.x509.XWikiX509KeyPair;
import org.xwiki.crypto.x509.internal.X509SignatureService;
import org.xwiki.signedscripts.internal.DefaultKeyManager;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;


/**
 * Test for the {@link DefaultKeyManager} component.
 * 
 * @version $Id$
 * @since 2.5
 */
public class DefaultKeyManagerTest extends AbstractMockingComponentTestCase
{
    /** Some test test. */
    private static final String TEST_TEXT = "Extreme performance, configurability and a top-notch user and developer "
                                          + "community are all hallmarks of the Gentoo experience.";

    /** Fingerprint of the test certificate. */
    private static final String CERT_FP = "eb31104d2fb1bc8495cf39e75124aef3f9ab7bfb";

    /** PEM encoded test certificate (XWiki SAS Web Certificate). */
    private static final String CERT_PEM = "-----BEGIN CERTIFICATE-----\n"
                                         + "MIIDWTCCAsKgAwIBAgIDEl9SMA0GCSqGSIb3DQEBBQUAME4xCzAJBgNVBAYTAlVT\n"
                                         + "MRAwDgYDVQQKEwdFcXVpZmF4MS0wKwYDVQQLEyRFcXVpZmF4IFNlY3VyZSBDZXJ0\n"
                                         + "aWZpY2F0ZSBBdXRob3JpdHkwHhcNMTAwNDE2MDI0NTU3WhcNMTEwNTE5MDEzNjIw\n"
                                         + "WjCB4zEpMCcGA1UEBRMgQnZ2MGF3azJ0VUhSOVBCdG9VdndLbEVEYVBpbkpoanEx\n"
                                         + "CzAJBgNVBAYTAkZSMRcwFQYDVQQKFA4qLnh3aWtpc2FzLmNvbTETMBEGA1UECxMK\n"
                                         + "R1Q0MDc0ODAzNjExMC8GA1UECxMoU2VlIHd3dy5yYXBpZHNzbC5jb20vcmVzb3Vy\n"
                                         + "Y2VzL2NwcyAoYykxMDEvMC0GA1UECxMmRG9tYWluIENvbnRyb2wgVmFsaWRhdGVk\n"
                                         + "IC0gUmFwaWRTU0woUikxFzAVBgNVBAMUDioueHdpa2lzYXMuY29tMIGfMA0GCSqG\n"
                                         + "SIb3DQEBAQUAA4GNADCBiQKBgQCSiflt/i6ZlqNODL8LQLPwNfXEdb3J+II1NXye\n"
                                         + "InrU3yRCybF7DG8NGIrvy+0o40YI+I4Q1Fcvv890IObdQdHmFtz8OKzKXT+giEG7\n"
                                         + "LxJXW3DDb9NckOsbjbNuNFSA9E/aQalrxbDVWyO0droG1v3vDBmG/KzfQkPmoE8g\n"
                                         + "P4qPsQIDAQABo4GuMIGrMB8GA1UdIwQYMBaAFEjmaPkr0rKV10fYIyAQTzOYkJ/U\n"
                                         + "MA4GA1UdDwEB/wQEAwIE8DAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIw\n"
                                         + "OgYDVR0fBDMwMTAvoC2gK4YpaHR0cDovL2NybC5nZW90cnVzdC5jb20vY3Jscy9z\n"
                                         + "ZWN1cmVjYS5jcmwwHQYDVR0OBBYEFHbS5h/MPHDXIIn5ived2HiF6AwiMA0GCSqG\n"
                                         + "SIb3DQEBBQUAA4GBALPfA0VQS9pCFYl9co6k3AYLx+gWg6FsTn3aYZRjS9Eeg2qR\n"
                                         + "f7XuiIlq2ZLb1r0SA8Unn2uw2wrHXnqw2I/AARawI/vT4toKGjJwLB8cONLE6cyO\n"
                                         + "rC4qW/5AUann6D1r26EWLSGYh62AcX/jUT4bjoWLhMhblxyLOgbBe8uYPLMH\n"
                                         + "-----END CERTIFICATE-----\n";

    /** Tested key manager implementation. */
    @MockingRequirement
    private DefaultKeyManager keyManager;

    /**
     * {@inheritDoc}
     * @see org.xwiki.test.AbstractMockingComponentTestCase#setUp()
     */
    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        try {
            // make sure the test certificate is not registered
            keyManager.unregister(CERT_FP);
        } catch (GeneralSecurityException exception) {
            // ignore
        }
    }

    @Test
    public void testGlobalRoot() throws GeneralSecurityException
    {
        XWikiX509Certificate cert = keyManager.getGlobalRootCertificate();
        cert.checkValidity();
//        Assert.assertEquals("FIXME known global fingerprint", cert.getFingerprint());
    }

    @Test
    public void testKeyPairCreation() throws GeneralSecurityException
    {
        // several tests, because key pair generation is quite slow
        final String author = "xwiki:XWiki.Me";
        final String password = "password";
        String kp = keyManager.createKeyPair(author, password, 1);
        XWikiX509Certificate cert = keyManager.getCertificate(kp);
        XWikiX509KeyPair keyPair = keyManager.getKeyPair(kp);

        // check that the certificate is valid
        cert.checkValidity();
        cert.verify(keyManager.getLocalRootCertificate().getPublicKey());
        Assert.assertEquals(cert, keyPair.getCertificate());
        Assert.assertTrue("Unknown certificate author name", cert.getAuthorName().endsWith(author));

        // check that the private key works
        X509SignatureService sig = new X509SignatureService();
        String signed = sig.signText(TEST_TEXT, keyPair, password);
        Assert.assertEquals(cert, sig.verifyText(TEST_TEXT, signed));

        // check that the created key pair is registered
        Assert.assertTrue("Key pair not found", keyManager.getKnownFingerprints().contains(kp));
    }

    @Test(expected = CertificateExpiredException.class)
    public void testExpiredKeyPair() throws GeneralSecurityException
    {
        String kp = keyManager.createKeyPair("author", "password", 0);
        keyManager.getCertificate(kp).checkValidity();
    }

    @Test
    public void testRootsArePresent()
    {
        Set<String> known = keyManager.getKnownFingerprints();
        Assert.assertTrue("Local root not found", known.contains(keyManager.getLocalRootCertificate().getFingerprint()));
        Assert.assertTrue("Global root not found", known.contains(keyManager.getGlobalRootCertificate().getFingerprint()));
    }

    @Test(expected = GeneralSecurityException.class)
    public void testUnknownCertFingerprint() throws GeneralSecurityException
    {
        keyManager.getCertificate("tralala");
    }

    @Test(expected = GeneralSecurityException.class)
    public void testUnknownKeyPairFingerprint() throws GeneralSecurityException
    {
        keyManager.getKeyPair("tralala");
    }

    @Test
    public void testRegisterCert() throws GeneralSecurityException
    {
        XWikiX509Certificate cert = XWikiX509Certificate.fromPEMString(CERT_PEM);
        Assert.assertEquals(CERT_FP, cert.getFingerprint());
        try {
            keyManager.getCertificate(cert.getFingerprint());
            Assert.fail("Certificate allready registered");
        } catch (GeneralSecurityException exception) {
            // great, continue
            keyManager.registerCertificate(cert);
            Assert.assertEquals(cert, keyManager.getCertificate(cert.getFingerprint()));
        }
    }

    @Test
    public void testUnRegisterCert() throws GeneralSecurityException
    {
        XWikiX509Certificate cert = XWikiX509Certificate.fromPEMString(CERT_PEM);
        Assert.assertEquals(CERT_FP, cert.getFingerprint());
        keyManager.registerCertificate(cert);
        Assert.assertEquals(cert, keyManager.getCertificate(cert.getFingerprint()));
        keyManager.unregister(cert.getFingerprint());
        try {
            keyManager.getCertificate(cert.getFingerprint());
            Assert.fail("Certificate is still registered");
        } catch (GeneralSecurityException exception) {
            // ok (can't use expected, because the same exception might be thrown before
        }
    }
}

