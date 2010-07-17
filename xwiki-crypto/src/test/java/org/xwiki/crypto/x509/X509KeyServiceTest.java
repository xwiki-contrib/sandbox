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
package org.xwiki.crypto.x509;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateExpiredException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.crypto.x509.XWikiX509KeyPair;
import org.xwiki.crypto.x509.internal.X509KeyService;

/**
 * KeyService test, insure that the key service is able to make keys without throwing an exception.
 * 
 * @version $Id$
 * @since 2.5
 */
public class X509KeyServiceTest
{
    /** This is a public key generated in the browser and passed to the server. */
    private final String spkacSerialization = "MIICTTCCATUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQClGP/B+AJN\n"
                                            + "sNuxSMTG9IuSXdW2voNwu9I8lYqEI1sDl55PFWmTTuOqofmLRsa2TVj4AjFNEBKH\n"
                                            + "abYUGB6DlWe2IfjFAx8qMEI4+/LztLU33dH9heUxyfLdZjT86xg1IBG1Sya2QhUC\n"
                                            + "QkfxuFGnflyiROtddNsWTccSYMGViwcFOUHyFPpIDb30ashs7Kvks6SyE/WJp0Vn\n"
                                            + "tNl8ToBrMmGtHzgz6qoji9hEYbFrCyRzA+GfwfhlimM0N0shHUnHayUsgoU2r39k\n"
                                            + "ifR09s4E2k50mjV8a/T4DIB0qheeuvCyQwf3+FrNFFCeQjIw5RJWGJ9TMgfqyhdV\n"
                                            + "dcp02z6Ro9kNAgMBAAEWDVRoZUNoYWxsZW5nZTEwDQYJKoZIhvcNAQEEBQADggEB\n"
                                            + "AENF+mjJiWHDYQiEcPjCrPEL9o0W7lEiQYu38LeC5ijLL9sQKpXiofcGWD0Oo8lF\n"
                                            + "zT/QoCrgpOdRNQgh2y4/KWpqq0F3Q8pkUh31a9hLtaWHcy2J0JrPUlspXPH2jc2M\n"
                                            + "+2e69KdYL/3Q9BZSx5KuU5+TqttNa5qBokTT9KCAQG0ptFh/sDbjHM50NqqUaQyx\n"
                                            + "UF03o6CMXpyu/bdI9ZCoLVos3keK6QtjBG1ADSS7dIaYpvHtCjDzjja6vFKEA7gx\n"
                                            + "CFTIVzEUCHxlCVEWkFMWVLoLy5GPfCHcI6JkltuA2fSBP/2g+1O5QSXzb5KBeQkg\n"
                                            + "WEhN3mjR+QNRnjKo3bKpq1g=";

    /** The tested key service. */
    private final X509KeyService service = new X509KeyService();

    @Test
    public void certsFromSpkacTest() throws Exception
    {
        this.service.certsFromSpkac(this.spkacSerialization, 1, "my webid", "xwiki:XWiki.Me");
    }

    @Test
    public void newCertAndPrivateKeyTestShortPwd() throws Exception
    {
        // 7 character password should definitely work
        this.service.newCertAndPrivateKey(1, "my webid", "xwiki:XWiki.Me", "passwor");
    }

    @Test
    public void newCertAndPrivateKeyTestLongPwd() throws Exception
    {
        // long password should work because of password mangling
        this.service.newCertAndPrivateKey(1,
                                          "my webid",
                                          "xwiki:XWiki.Me",
                                          "this is a very, very, very, very, very, looooooong passphrase");
    }

    @Test
    public void newPrivateKeyCorrectPassword() throws GeneralSecurityException
    {
        String password = "ololol";
        XWikiX509KeyPair keyPair = this.service.newCertAndPrivateKey(1, "my webid", "xwiki:XWiki.Me", password);
        Assert.assertNotNull("Private key is null", keyPair.getPrivateKey(password));
    }

    @Test
    public void newPrivateKeyWrongPassword() throws GeneralSecurityException
    {
        String password = "ololol";
        XWikiX509KeyPair keyPair = this.service.newCertAndPrivateKey(1, "my webid", "xwiki:XWiki.Me", password);
        try {
            Assert.assertNotNull("Private key is null", keyPair.getPrivateKey("asdf"));
        } catch (GeneralSecurityException exception) {
            Throwable cause = exception.getCause();
            Assert.assertNotNull("Unexpected error", cause);
            Assert.assertTrue("Unknown error message", cause.getMessage().contains("wrong password or corrupted file"));
        }
    }

    @Test
    public void testNewCertIsValid() throws GeneralSecurityException
    {
        XWikiX509KeyPair keyPair = this.service.newCertAndPrivateKey(1, "my webid", "xwiki:XWiki.Me", "bla");
        XWikiX509Certificate cert = keyPair.getCertificate();
        cert.checkValidity();
        cert.verify(cert.getPublicKey());
    }

    @Test(expected = CertificateExpiredException.class)
    public void testNewExpiredCertIsInvalid() throws GeneralSecurityException
    {
        XWikiX509KeyPair keyPair = this.service.newCertAndPrivateKey(0, "my webid", "xwiki:XWiki.Me", "bla");
        XWikiX509Certificate cert = keyPair.getCertificate();
        cert.verify(cert.getPublicKey());
        cert.checkValidity();
    }
}
