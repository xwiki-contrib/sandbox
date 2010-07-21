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
package org.xwiki.crypto.passwd;

import java.security.GeneralSecurityException;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.xwiki.crypto.passwd.internal.DefaultPasswdCryptoService;


/**
 * Password based encryption test.
 * 
 * @version $Id$
 * @since 2.5
 */
public class DefaultPasswdCryptoServiceTest
{
    private final String textToEncrypt = "Congress shall make no law respecting an establishment of religion, or "
                                       + "prohibiting the free exercise thereof; or abridging the freedom of speech, "
                                       + "or of the press; or the right of the people peaceably to assemble, and to "
                                       + "petition the Government for a redress of grievances.";

    private final String password = "Snuffle";

    protected PasswdCryptoService service = new DefaultPasswdCryptoService();

    @Test
    public void encryptDecryptTest() throws Exception
    {
        final String out = this.service.encryptText(textToEncrypt, password);
        final String decrypted = this.service.decryptText(out, password);
        Assert.assertTrue(this.textToEncrypt.equals(decrypted));
    }

    @Test
    public void saltTest() throws Exception
    {
        final String encrypt1 = this.service.encryptText(textToEncrypt, password);
        final String encrypt2 = this.service.encryptText(textToEncrypt, password);
        Assert.assertFalse(encrypt1.equals(encrypt2));
    }

    @Test
    public void decryptRegressionTest() throws Exception
    {
        final String decrypted = this.service.decryptText(this.getEncrypted(), password);
        Assert.assertTrue(this.textToEncrypt.equals(decrypted));
    }

    @Test
    public void decryptWithWrongPasswordTest() throws Exception
    {
        final String enciphered = this.service.encryptText(textToEncrypt, password);
        Assert.assertNull(this.service.decryptText(enciphered, "wrong password"));
    }

    protected String getEncrypted()
    {
        return "------BEGIN PASSWORD CAST5CBC-WHIRLPOOL CIPHERTEXT-----\n"
             + "s503S2OFeOGxA03C4qERpmrRvdY=:\n"
             + "ERg1+7nZV/SpC4DkcaswgG4QxiTySAmoQeqrpkcPIO0ZmArcaq7cdPz31tml8A7p\n"
             + "Gy6HiBHp1LylEKd+RwQ2uUAtEFovVM+NHzXAFHfnsFW8htnb7GreQR6YHCZEygYY\n"
             + "sX0sAXPlSeIgiQTi8HPZB4IB9kfQeOCgvDznuxtFLLwvtWKESyqgLDlt2OcCBi5a\n"
             + "hHXrxBDSYe5Euf2aNzGL3iXoysi+P3VP9kshTZ92V4be2sWcUVyJBh0zJwL9G+pa\n"
             + "x+ItWPaSTJH5nfvkPpToKC28njJ/FtkdKqBPS7hcepFWhJDdtn0NDy4MuU01VojK\n"
             + "5teYdCQmmvxefUDKler8rlrOs7hcacAfvDpS3SEl+2jztJAFbqMKuw==\n"
             + "------END CIPHERTEXT------";
    }
}
