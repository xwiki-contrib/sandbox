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
    private final static String KP_PEM = "-----BEGIN XWIKI CERTIFICATE AND PRIVATE KEY-----\n"
        + "rO0ABXNyADZvcmcueHdpa2kuY3J5cHRvLng1MDkuaW50ZXJuYWwuRGVmYXVsdFhX\n"
        + "aWtpWDUwOUtleVBhaXIAAAAAAAAAAQIAA1sAEmVuY29kZWRDZXJ0aWZpY2F0ZXQA\n"
        + "AltCWwAbcGFzc3dvcmRFbmNyeXB0ZWRQcml2YXRlS2V5cQB+AAFMABNwcml2YXRl\n"
        + "S2V5QWxnb3JpdGhtdAASTGphdmEvbGFuZy9TdHJpbmc7eHB1cgACW0Ks8xf4BghU\n"
        + "4AIAAHhwAAAC0jCCAs4wggG2oAMCAQICEE4Pllm50w/KEAXChGGsB3swDQYJKoZI\n"
        + "hvcNAQEFBQAwIzEhMB8GCgmSJomT8ixkAQEMEXh3aWtpOlhXaWtpLkFkbWluMB4X\n"
        + "DTEwMDgwMjE2MzYzMVoXDTExMDcyNDE2MzYzMVowIzEhMB8GCgmSJomT8ixkAQEM\n"
        + "EXh3aWtpOlhXaWtpLkFkbWluMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC\n"
        + "AQEAkee64i7QX/eVn/GWX4zi3CsZlXh/oCb4M+oOPy4MEX4jS5I7gLOSBbu5YukZ\n"
        + "aF4zuIu9zpBt2OqSI49N5WyE+FTWNyRxww1F0iGByDsYsJm/tocL8qQLn8cRtNQG\n"
        + "aGf67rxvr0MY2VkkzsrGEw8R42p+Jw/DMajetaEizGIaSHEevSay27f8NMp9KObQ\n"
        + "ewPGNIHc7tWxsAgiiNvOfWd/lFJWjfpTWrF6gCm35917vnQiVPIZSu4iAgfa6hGf\n"
        + "2jDhVBYtSq/TSWsfZFTAosIlp7VL+NzckW11bp2thN59YGkyyseo9y5n6gBeSb0R\n"
        + "AS1Xs8buroOcY5mdA161uw+vgQIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQBhKkkK\n"
        + "drS+fcfLPDIdhL3Nnlk7siAYyKTz3C/SGY3WBLkAnGGNk/qV/bigoc1iE5GpwgKx\n"
        + "RaGoVxO40HzNrt2vww80uEpoyV4v7dGeigW+AkDqeOD4up8EXZ5GN5MK+jjs02E9\n"
        + "mRlx4eIER+EXlY1IONOZ73vmTsWX/Kh25RgihxPweFn1hoWLcFpbX1zH5LtTdA3M\n"
        + "BUEbsuOqQU3FQXIlVmhCzfyLh4pVCEYiVB0UrHMQK5RAhQUp6/j5XFW3YeiwsEes\n"
        + "ldOkIo80zqe9BKOrjttGxjPWTspnX2eP46vfLvcgkgdESd0L2UltNfLYtMhfqkzd\n"
        + "10gmN3iJ1fjehg3hdXEAfgAEAAAIhqztAAVzcgA4b3JnLnh3aWtpLmNyeXB0by5w\n"
        + "YXNzd2QuaW50ZXJuYWwuQ0FTVDVQYXNzd29yZENpcGhlcnRleHQAAAAAAAAAAQIA\n"
        + "AHhyADtvcmcueHdpa2kuY3J5cHRvLnBhc3N3ZC5pbnRlcm5hbC5BYnN0cmFjdFBh\n"
        + "c3N3b3JkQ2lwaGVydGV4dAAAAAAAAAABAgACWwAKY2lwaGVydGV4dHQAAltCTAAL\n"
        + "a2V5RnVuY3Rpb250AC9Mb3JnL3h3aWtpL2NyeXB0by9wYXNzd2QvS2V5RGVyaXZh\n"
        + "dGlvbkZ1bmN0aW9uO3hwdXIAAltCrPMX+AYIVOACAAB4cAAABmDV3IlCF6x5Z18w\n"
        + "ETxZgNFX10h/O51zYLwZQ7cq7yj+TRE5K3ClMWMlaxBTn00r8S7MipXQJeC09ILj\n"
        + "RDfHY2u/WGMxHMvf13wup6kr7mwl53w4CVsCKJLOvfhqiRKLV3PLraKtvhl35VcV\n"
        + "SKk0neCDbAU8Q1O8Jk02r7eUSeuiIYP92rRsUogor16Y9XWot+UPluUTZ6avqiCz\n"
        + "ZvP2nuC87GRX3hDKH6i2nFBaGlyuShXeP29Qe1Ae+7LL4KFLU+E1bvBJpKXuYS1u\n"
        + "zsnwSzA/TO62m+4BZ72Wl5+v5CAMY7EKAFJZ8FxDr76Bpp0+uE1i+irRBiucpH89\n"
        + "uU+Ixb4frwDsfFD6kGLTQztv73H/oIU7yWxFGNs9OQSYli4kmjOTvePlhE/29McJ\n"
        + "mSH3ywhZV4qA+unLYubxDfL1x9kF4cpN9L17YysJOxMrJwAsB80nvOhqdTCWbt+E\n"
        + "XJZbh1mVXq+El87ukzKoh82WIL5LE0bRq8ufwfx2pAVMs8ilB7hd7aknLjV4LUJI\n"
        + "QJtOEJn4jaI/4qyUEkpBnvmjmZ4wG/+LepvhKl862Coz8NLXkKp+Zp0UaJ+w/dy+\n"
        + "sX1CHzmmU3YjRy7UOYHyKKfbhrwZlOg8U3mwq+n/axcBDpWB1VCob3TMpQ3qTx0P\n"
        + "ZbrRSWHzdhYmdWLo627ZaMR5X/ZsJ4uT60nsMCGawTyDjW/OGC+m9fpd3HYnKU4G\n"
        + "NPOlbVRogHOmWnxx+VGzqBnIzrd0zbSTWiiieNM+F7eQF5MQ3EeskohLduEU0jH3\n"
        + "l02HXgU3mKsi/hmxx3Va23/0uWCY7KsC1DrDv42VKDtmP/H+97IntZmAXOLYbwke\n"
        + "ILgKGtVA0LmwTgbAgA/YSqbBKtJ3MWdWTFbsgHx1pMNCe6+0xXxQtVm57OdvkPwY\n"
        + "hLtjBY3zpPgyGzG0OVTGhjGq8zlsho8IJowhKGK0HhQfWElLadkIAwBYF2s2qaG2\n"
        + "zGTQqIokzBtMVgMBemurmOF6WYb4NNSPUMMsuSwKNpo0fHygtd31gw29Ctca1m5I\n"
        + "8g27wO6FdA1RqkQ0ciuu6uXnovDVlrz7r5VpAQkZQjtsMzft5xhBKxFAq0CFPQxZ\n"
        + "gymbk1XCH4A3sxwYs/X1iB1OWM/1q4oWYiVJQEQ565AqnUEedQVkls/tDuKNYy+M\n"
        + "k5JPu/F+4Nh1hzRsKhj2u137HnrOV7hBetlXsRZsntwbInMkP+9JNaFRzXMNAn05\n"
        + "e+Jg44g8pBsQzNzqNQ4YKuIKhWNDAb3LPqOnMVnXrv4Q42bIfA/V+jV4noW5yKDs\n"
        + "qCKoVxjrlLtWDaSEHwkcnL/vo8GSje9SAh6CAYJWsiOYKTYNvuxIFJkXGV2lCoAY\n"
        + "fzsEvGl4qYBoNUlb6HQ64zj6FypZ3g+UcTP6bDUfVv4lD/n16zYBHkQ8NR/ggP1m\n"
        + "a0E2lpTZI/bVDMd4el8/LwxmGD2it+hFpDkeJc+Z+i+vn58eUNvL4mXUUhfqleFD\n"
        + "3Y5yfCbn+OTZKbFldDXKgH2TxpmYpwkfUY28GB0eGx0cSHBIBGQEtvIoQDTBw8ZU\n"
        + "Odil8dqEoyT9cGn11fDC+GkQ+yJha8aLEZ1njMrfElFYuX0tY8Ps14iAIkzoqa2w\n"
        + "IiOyZ7w7qhTthoIq/8s73Iz9qFYqdCVguQYhGTjUR4ne6U2CO8vPilPikzAFN/ZH\n"
        + "W+TGU3RexH6IIMdo7SlxvYLjuMg9MvQr9raa9KlduPiEEboyyW/pxwPC/m144uib\n"
        + "sw3vR8fiou+0NclpNdM4I+HZcgfbRZ0YZ1QqTg8i2/OKNSPwHIgOze/IX/HOIZgm\n"
        + "XYhOnsIyGMk5xQPAqyqkk9QNYPAuJu+ltcpXSMt1a0r0/weOW+RDeapciftxc39Z\n"
        + "vVTPmVvgObaPw/w7JD3kDRR4hv+4lPZ3MXTN1YSREUw9mzoFF8MS0gHsMQy8Uu5C\n"
        + "N2j7hUCHzvuFEdAIkjZJiJPsA3NN/zDeIV+F5LDwCPcsrhcfRR763/6ygCf3QOMg\n"
        + "rTs2lGHTvIay5zdBn/rl8+0C5KlTMTQqPndtTWvSmOnuWp0/5nGrl9jt/n4uDZaM\n"
        + "tvYmwr4/h0GmVvUNvQmeFiLTKsQsuzMfO81ADpzbRdrW09FDlch1J4ptl16eypvv\n"
        + "KRgBv6kw/4PuL7GPvi3p74f4Jir4x6RepKfO0G1nOtEAEMd5SO1zcgBGb3JnLnh3\n"
        + "aWtpLmNyeXB0by5wYXNzd2QuaW50ZXJuYWwuU2NyeXB0TWVtb3J5SGFyZEtleURl\n"
        + "cml2YXRpb25GdW5jdGlvbgAAAAAAAAABAgAFSQAJYmxvY2tTaXplSQAQZGVyaXZl\n"
        + "ZEtleUxlbmd0aEkADW1lbW9yeUV4cGVuc2VJABBwcm9jZXNzb3JFeHBlbnNlWwAE\n"
        + "c2FsdHEAfgACeHIASG9yZy54d2lraS5jcnlwdG8ucGFzc3dkLmludGVybmFsLkFi\n"
        + "c3RyYWN0TWVtb3J5SGFyZEtleURlcml2YXRpb25GdW5jdGlvbgAAAAAAAAABAgAA\n"
        + "eHAAAAAIAAAAGAAABAAAAAABdXEAfgAFAAAAEMQg3KDPN+kyZ2dh1EHNhy10AANS\n"
        + "U0E=\n"
        + "-----END XWIKI CERTIFICATE AND PRIVATE KEY-----\n";

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
            final PasswordCryptoService mockCrypto = getComponentManager().lookup(PasswordCryptoService.class);
            final UserDocumentUtils mockUtils = getComponentManager().lookup(UserDocumentUtils.class);
            this.userFingerprints.add(getTestCertFingerprint());
            this.userFingerprints.add(getTestKeyPair().getFingerprint());
            getMockery().checking(new Expectations() {{
                allowing(mockCrypto).encryptText(with(any(String.class)), with(any(String.class)));
                    will(returnValue("-----BEGIN XWIKI CERTIFICATE AND PRIVATE KEY-----\nENCRYPTED+DATA\n"
                        + "-----END XWIKI CERTIFICATE AND PRIVATE KEY-----"));
                allowing(mockUtils).getCurrentUser();
                    will(returnValue(USER));
                allowing(mockUtils).getCertificateFingerprintsForUser(with(USER));
                    will(returnValue(userFingerprints));
                allowing(mockUtils).addCertificateFingerprint(with(USER), with(any(String.class)));
            }});
        } catch (ComponentLookupException exception) {
            System .out.println("\n\n\nFUCK:\n" + exception.getMessage());
            exception/*.getCause().getCause()*/.printStackTrace();
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
                this.cachedKeyPair = DefaultXWikiX509KeyPair.fromBase64String(KP_PEM);
            }
            return this.cachedKeyPair;
        } catch (Exception exception) {
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

