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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
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
    /** The tested key service. */
    private final X509KeyService service = new X509KeyService();

    @Ignore
    @Test
    public void certsFromSpkacTest() throws Exception
    {
        //this.service.certsFromSpkac(this.spkacSerialization, 1, "my webid", "xwiki:XWiki.Me");
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
}
