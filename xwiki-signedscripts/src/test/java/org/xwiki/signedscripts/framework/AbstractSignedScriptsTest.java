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
package org.xwiki.signedscripts.framework;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmock.Expectations;
import org.junit.Before;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.crypto.internal.UserDocumentUtils;
import org.xwiki.crypto.passwd.PasswordCryptoService;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.crypto.x509.XWikiX509KeyPair;
import org.xwiki.crypto.x509.internal.DefaultXWikiX509KeyPair;
import org.xwiki.test.AbstractMockingComponentTestCase;


/**
 * Base class for tests that need certificates and key pairs.
 * 
 * @version $Id$
 * @since 2.5
 */
public abstract class AbstractSignedScriptsTest extends AbstractMockingComponentTestCase
{
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

    /** Password for the test key pair. */
    private final static String KP_PASS = "passwrd";

    /** PEM encoded test key pair. */
    private final static String KP_PEM = "-----BEGIN PKCS12-----\n"
                                       + "MIACAQMwgAYJKoZIhvcNAQcBoIAkgASCA+gwgDCABgkqhkiG9w0BBwGggCSABIID\n"
                                       + "6DCCBUcwggVDBgsqhkiG9w0BDAoBAqCCBPowggT2MCgGCiqGSIb3DQEMAQMwGgQU\n"
                                       + "DLXkMgFZ7bV6TCHoW3fPqKULS5cCAgQABIIEyL98kz/ft2TGdY8WSVTspFj5DQhw\n"
                                       + "KgJL7fXlUxhV/rfA0W0hx6S61I86cBecCqRAQuh5KyEszqvIzVHwZRxp81GprVpd\n"
                                       + "bu2qETSoifOU/aNdQ/ON/F0t8xhaAla4PKNV1L/CYEmCtNnyhxATkeup4/jq2yh+\n"
                                       + "lXhLX4bEfLS92f3W5c6tAj1W3gpHBEqszso8uTgY3m0l2rt865jndRfWz7ayhkoO\n"
                                       + "GGtL6nFCuMUfkl40A5CkPG1wW3DLdn7QhYF01Chmv5GD2kZHC0AlnBE+0Hp4qxSh\n"
                                       + "iwZTMD66WGoXmaiQ/d5wqzxTvSHh9towjCLzml88yzJZOlL42T6wwwc93SWqHcGt\n"
                                       + "7tifw6eV9ox+IscfyiYiYSO/m4bidbWIcodm7bGbiOKoc91cgKAlLJ00ty73Mr/N\n"
                                       + "6CyEjjA+a4Ubb5gkW/D3t5A0nH1tr9stPHxEuAy7rx/KwkDXsrmauZwz+BcJcrc9\n"
                                       + "25IHgyu+24zimydcTndm0zc5nmODuG3JSAwgyvsMs3CvBnX4Gdj1i/rfOfZjqXH+\n"
                                       + "oIV0I7eMh5xmUxm064P38X2SY/O/vmnhv/OzvFPFOZ4JaBFje+KX0lZYU5mgu/R4\n"
                                       + "oXuqG4aOeYl1yvyLp9ZhanM2QNIbOIFhlwc7Cfa2bRhc147NPTV0Y02mc9bCSLRr\n"
                                       + "NxsUvHrelaFtZhJ4fY3LYnFuprq3WB/6znQ32xg429qNzOmInehfbf3SEbbNHoVH\n"
                                       + "uyGb6HqUFFhbfHbyQKMAOf7Sm/QBKOaoLLDiM48cXLVVc7jxleIqRghiD65loaIq\n"
                                       + "WS7jL2eunlYrrm0Lk58326u/vaT9cxtjv9GTXVW/Nswghcvc+57ffYW204Exmu8a\n"
                                       + "bjAStnxmUwr12Dqi6AsxVhbF5elns3xl0S5+MMbbpGX+2MScRPq2Vl9rHEptjc+X\n"
                                       + "Vk5cCqyIjYOWdDvKoROI3XsD0JscAdqZbZiB4iLBuR3GdXBhjBZHROcv/gU/AB1R\n"
                                       + "nTmnLvEInGVnbSk0+BFqTfUU3WStCvE/prfOt6HYpIm9S7n3s428CGrzUorPLspw\n"
                                       + "B7A02h1FQqk/Haw2/GInxELMfwHaaEUh7RpNjCwRO8nPLNWxwms5yYsyJnu5rEzH\n"
                                       + "LTIp0la8Y57l31v/N6cacYqXZOOYrPBBIb1H+dT2GEhITuRJlr/vMsdXWgt++nDr\n"
                                       + "OMADNUVHjgvh9flXRhwkyZavBIID6Kulh/1OLZPBLsw4vM5Cwl0AHUqB0tx4BIIB\n"
                                       + "Y1zocDTP0D4cyO0r5/hYeY4Ri4V4Z71iNJmvP/uZX3VkXDutC9m9pIHeewp5h/Gl\n"
                                       + "hkjd5k/mwzMdMNmXy9VXSCP9dqFGQT+Fl/0c0ZfWoH87SeCZfBBsYQHPoNGJbUoy\n"
                                       + "d882B1q+lisnB8p1yZwODtkDngbwBBINZ+LQCgu7Krn/ynQKhY78Ln4GenBsd143\n"
                                       + "RKEIWbdLqm4s7r/jvo/8Fcks0w0Pjl4pNuS+E67f8pltLhCIih/TbYmVUnC0cH7/\n"
                                       + "xcNL9m1Ycg+/SD2aTKXmAUyYVj/KgK2le/mnk0MB3JO4siSqwWXhSZQVmZcyzIFm\n"
                                       + "kZowy38oWDCciTni+0Igez08Ba2+qrVLx6n1v4piE64qVMRKcHlOWQkYJZo5EXKG\n"
                                       + "TW1zqmub+cito9wYMTYwDwYJKoZIhvcNAQkUMQIeADAjBgkqhkiG9w0BCRUxFgQU\n"
                                       + "sRGZdRMv20BvljB/8csY8NLmfcEAAAAAAAAwgAYJKoZIhvcNAQcGoIAwgAIBADCA\n"
                                       + "BgkqhkiG9w0BBwEwKAYKKoZIhvcNAQwBAzAaBBShlzat2siS78iIdmMVbPxK6Ymx\n"
                                       + "iAICBACggASCA5hw3Zz9JoynJKxWyOyhPVQDfVhyViE1tFzvh0R6owM0RiDn41KN\n"
                                       + "Oxl+EvgCFHk29raMvLtzrOxuMhY6YWLXELNKoN+KaNUn6LXc6VInCcuMC6hOgUuM\n"
                                       + "8NQ3UTucPz40705at4s0LHGfsnPrcVUxQDykpLikw8/2iy+g/O1spJHdt/FabQFr\n"
                                       + "S5FRsQp/C5Rg2y82u2Hv/B3buO9qrMtwOt2RBIzS0eISK6zHBH6p9aDQrynmYGbG\n"
                                       + "D2b5VR5mSGUxeuFRqXKVG7jxgq/gF6x2CbcnJgfEUoJmbCDHCQXjkeyJQ47/0loW\n"
                                       + "WFyPq/9FJrnHd0GIhXbZSrZqqe8LCayvsb4iu1Wec0tkzBTsO7l8srAMceNrcLKG\n"
                                       + "30MUV+GwpxTsSBEzRcQ4nnuaukNgPzm7TKI8T3kIiTxQfJORAa7luXMVtkJZtPOx\n"
                                       + "ahFht/H89lwfQ+1zCQwl442l/hQFh2wFP33yEY/sbHvapjFaRA70BUC5ecih7Y7G\n"
                                       + "q3io3bGVpLfhmZKkSIzOweKeKgSxVHPP4wncsfOWq41MCCzXqgtlfTHwo+SeU8SR\n"
                                       + "/c+0wR2+p+IV6SUo6731Sx/O1gPH6NA5elqfOiQKY8cnsPZH3Yc/RlPz+wq2sbe7\n"
                                       + "B78nfioWGGxppCNR/F11LOohzis6Gv8FL5BmaLHYkPvRROpcVT7dbCGelXS9Wf6C\n"
                                       + "/yn38uY9urxYFfdGra8EggGRzQXLdscpWUBztPNkR+eEZgR2gN5oqP6PlgWXecj3\n"
                                       + "P0mVSWdbpwS1ge2Ssj6RQOxdujKqhLCW2nTHvU0nc/8ad/jsWmWmhwGcm4kH9T5a\n"
                                       + "mOxF3iNt/Jrb3StjJeCrLMR2vrIL7xaHCWNScCZbcBOKok0kpHE0jCBRJujEim9F\n"
                                       + "2QX1HcghBG28EoUkCB1dZBg8FIkLb8lm0B93gfuEfREPBhHwmf+0YE0q1LIHXO/X\n"
                                       + "znvP7Xbn/WwMZyR11sBnTIADodJZ/tTOoSmYQJFTGvaZ36uZGwGKOs1BN4jVmPYl\n"
                                       + "nXEe/s8Xbi6bF4wCKgTcOt/wJXSw6+h4Fi8E0vPbTQcZUng3csrtmOqQB3XGuNlL\n"
                                       + "34b6CPqnI6UvjFzaFXV0cdaT/IrBGsust54r8ht2BHhWDsvzxqKDesFOwaH52DcD\n"
                                       + "bts1ruIAGH9F56XXm+GZ/HePoIeCIWCJRDcOmPywroprbL94t8jXZNtmkFT6aS9S\n"
                                       + "RAzI5ECU5bwmV8gwiIAzgGAfGC8PcSEAAAAAAAAAAAAAAAAAAAAAAAAwPTAhMAkG\n"
                                       + "BSsOAwIaBQAEFGUuAySAeiDnL9TGRCAvVmD/A9ekBBRtkszvvjtmY3IwJRoKKw2u\n"
                                       + "/2TFmQICBAAAAA==\n"
                                       + "-----END PKCS12-----\n";

    /** Current user name to use. */
    protected final static String USER = "XWiki.Admin";

    /** Cached instance of the test certificate, used by {@link #getTestCert()}. */
    private XWikiX509Certificate cachedCert;

    /** Cached instance of the test key pair, used by {@link #getTestKeyPair()}. */
    private XWikiX509KeyPair cachedKeyPair;

    /** Need to register new fingerprints manually, since {@link UserDocumentUtils} is mocked. */
    private final List<String> userFingerprints = new LinkedList<String>();

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractMockingComponentTestCase#setUp()
     */
    @Before
    @Override
    public void setUp() throws Exception
    {
        // register BC provider first
        Security.addProvider(new BouncyCastleProvider());

        // inject mocking requirements
        super.setUp();
        try {
            // mock document utils
            final UserDocumentUtils mockUtils = getComponentManager().lookup(UserDocumentUtils.class);
            this.userFingerprints.add(getTestCertFingerprint());
            this.userFingerprints.add(getTestKeyPair().getFingerprint());
            getMockery().checking(new Expectations() {{
                allowing(mockUtils).getCurrentUser();
                    will(returnValue(USER));
                allowing(mockUtils).getCertificateFingerprintsForUser(with(USER));
                    will(returnValue(userFingerprints));
                allowing(mockUtils).addCertificateFingerprint(with(USER), with(any(String.class)));
            }});
        } catch (ComponentLookupException exception) {
            // ignore, PKCS7SignedScriptTest does not use @MockingRequirement, which causes lookup to fail
        }
    }

    /**
     * Get the fingerprint of the test certificate. Same as {@link #getTestCert()}.getFingerprint()
     * 
     * @return fingerprint of the test certificate
     */
    protected String getTestCertFingerprint()
    {
        return CERT_FP;
    }

    /**
     * @return the test certificate
     */
    protected synchronized XWikiX509Certificate getTestCert()
    {
        try {
            if (this.cachedCert == null) {
                this.cachedCert = XWikiX509Certificate.fromPEMString(CERT_PEM);
            }
            return this.cachedCert;
        } catch (GeneralSecurityException exception) {
            // should not happen
            throw new RuntimeException(exception);
        }
    }

    /**
     * @return the test key pair
     */
    protected synchronized XWikiX509KeyPair getTestKeyPair()
    {
        try {
            if (this.cachedKeyPair == null) {
                this.cachedKeyPair = DefaultXWikiX509KeyPair.deserializeFromBase64(KP_PEM);
            }
            return this.cachedKeyPair;
        } catch (IOException exception) {
            // should not happen
            throw new RuntimeException(exception);
        }
    }

    /**
     * Manually "register" a fingerprint. It will be included into the list returned by the mocked
     * {@link UserDocumentUtils#getCertificateFingerprintsForUser(String)}
     * 
     * @param fingerprint the fingerprint to add
     */
    protected void addFingerprint(String fingerprint)
    {
        this.userFingerprints.add(fingerprint);
    }
}

