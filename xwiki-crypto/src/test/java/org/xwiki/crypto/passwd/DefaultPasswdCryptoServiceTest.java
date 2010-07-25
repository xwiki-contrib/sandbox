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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.crypto.passwd.internal.DefaultPasswdCryptoService;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;


/**
 * Tests {@link DefaultPasswdCryptoService}.
 * 
 * @version $Id$
 * @since 2.5
 */
public class DefaultPasswdCryptoServiceTest extends AbstractMockingComponentTestCase
{
    /** Length = 272 byte = 17 * 16 */
    private final String textToEncrypt = "Congress shall make no law respecting an establishment of religion, or "
                                       + "prohibiting the free exercise thereof; or abridging the freedom of speech, "
                                       + "or of the press; or the right of the people peaceably to assemble, and to "
                                       + "petition the Government for a redress of grievances.";

    /** Length = 113 byte = 7 * 16 + 1 */
    private final String anotherText = "The length of this text is 113 byte. This is 1 byte more "
                                     + "than a multiple of block size for 128 bit block ciphers.";

    private final String password = "Snuffle";

    @MockingRequirement
    protected DefaultPasswdCryptoService service;

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

    protected String getEncrypted()
    {
        return "------BEGIN PASSWORD CAST5CBC-WHIRLPOOL CIPHERTEXT-----\n"
             + "Kg8YLwtnchmtWKd2yYCn2tilT04=:\n"
             + "r4FlnYUOedRfc31ghGgIhc9rkpRtwDhLO5CPc3wgTuPfHMaAZ/mfdXk3rz2TKtXE\n"
             + "xanPkLR5th5RelKhe0DEj2lQH0bTtajlDqKBoo7OsfR2HZHCt+q5jRzTf645vN8X\n"
             + "AeNRF1Im7brfOwoY+J2MC1bf4HD98r4/rjKfMyd4bdTAjXvB+OyOGje1LFHWP2km\n"
             + "qBh5TO9CHMxdAzFvakAFE/oCxqactwU002dHsF7/4EWncnvwIeQpXAYtcaRrVW5Q\n"
             + "irUSsQk+UDG2/sF6bGtqPS3SZjHkIlEYgf8MINjcnvBsmDtDsXZL8xza+E7dQSi4\n"
             + "HI7DjwU7vvUuv3pyAVLM8g8vfS46ehx0pkqKEDrVKFuFktzn48xStw==\n"
             + "------END CIPHERTEXT------";
    }

    @Test
    public void decryptWithWrongPasswordTest() throws Exception
    {
        final String enciphered = this.service.encryptText(textToEncrypt, password);
        Assert.assertNull(this.service.decryptText(enciphered, "wrong password"));
    }

    @Test
    public void encryptDecryptOneByteMore() throws GeneralSecurityException
    {
        // the last block contains only one character
        String ciphertext = this.service.encryptText(anotherText, password);
        String plaintext = this.service.decryptText(ciphertext, password);
        Assert.assertEquals(anotherText, plaintext);
    }

    @Test
    public void encryptDecryptOneByteLess() throws GeneralSecurityException
    {
        // the last block has one byte of padding
        final String shorter = anotherText.substring(0, anotherText.length()-3);
        String ciphertext = this.service.encryptText(shorter, password);
        String plaintext = this.service.decryptText(ciphertext, password);
        Assert.assertEquals(shorter, plaintext);
    }

    @Test
    public void protectPasswordTest() throws Exception
    {
        String protectedPassword = this.service.protectPassword("Hello World!");
        Assert.assertTrue(this.service.isPasswordCorrect("Hello World!", protectedPassword));
        Assert.assertFalse(this.service.isPasswordCorrect("Wrong Passwd", protectedPassword));
    }
}
