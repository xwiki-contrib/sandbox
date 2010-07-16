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
}

