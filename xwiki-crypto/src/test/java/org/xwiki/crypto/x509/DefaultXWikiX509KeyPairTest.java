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
import java.security.KeyPair;
import java.security.cert.X509Certificate;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.crypto.x509.XWikiX509KeyPair;
import org.xwiki.crypto.x509.internal.DefaultXWikiX509KeyPair;
import org.xwiki.crypto.x509.internal.X509Keymaker;


/**
 * Tests the {@link DefaultXWikiX509KeyPair} implementation.
 * 
 * @version $Id$
 * @since 2.5
 */
public class DefaultXWikiX509KeyPairTest
{
    /** Key service used to create new certificate. */
    private final X509Keymaker keyMaker = new X509Keymaker();

    /** The password to access the file below. */
    private final String pkcs12FilePassword = "passwrd";

    /** Fingerprint for certificate in PKCS12 file, checked against Firefox. */
    private final String certFingerprint = "90ef4b926323c1992c88d4b3f0b7e9db9496490c";

    /** A PKCS#12 file represented as a base64 string. */
    private final String base64PKCS12 =
        "-----BEGIN PKCS12-----"
      + "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"
      + "-----END PKCS12-----";

    @Test
    public void testExportImport() throws GeneralSecurityException
    {
        String password = "blah";
        KeyPair kp = keyMaker.newKeyPair();
        X509Certificate cert = keyMaker.makeClientCertificate(kp.getPublic(), kp, 1, true, "web id", "xwiki:XWiki.Me");
        XWikiX509KeyPair keyPair = new DefaultXWikiX509KeyPair(kp.getPrivate(), password, cert);
        String exported = keyPair.toBase64PKCS12();
        XWikiX509KeyPair imported = new DefaultXWikiX509KeyPair(exported, password);
        Assert.assertEquals(keyPair.toBase64PKCS12(), imported.toBase64PKCS12());
        Assert.assertEquals(keyPair.getPrivateKey(password), imported.getPrivateKey(password));
        Assert.assertArrayEquals(keyPair.getCertificates(), imported.getCertificates());
        Assert.assertEquals(keyPair, imported);
    }

    @Test
    public void testImportFromBrowser() throws GeneralSecurityException
    {
        XWikiX509KeyPair imported = new DefaultXWikiX509KeyPair(this.base64PKCS12, this.pkcs12FilePassword);
        Assert.assertTrue("Wrong fingerprint after import of PKCS12",
                          this.certFingerprint.equals(imported.getCertificate().getFingerprint()));
    }
}

