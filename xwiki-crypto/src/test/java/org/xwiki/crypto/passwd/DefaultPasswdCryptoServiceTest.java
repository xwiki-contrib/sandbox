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
        return "------BEGIN PASSWORD CIPHERTEXT-----\n"
             + "rO0ABXNyADhvcmcueHdpa2kuY3J5cHRvLnBhc3N3ZC5pbnRlcm5hbC5DQVNUNVBh\n"
             + "c3N3b3JkQ2lwaGVydGV4dAAAAAAAAAABAgAAeHIAO29yZy54d2lraS5jcnlwdG8u\n"
             + "cGFzc3dkLmludGVybmFsLkFic3RyYWN0UGFzc3dvcmRDaXBoZXJ0ZXh0wxB+AJ0R\n"
             + "Z6ACAAJbAApjaXBoZXJ0ZXh0dAACW0JMAAtrZXlGdW5jdGlvbnQAL0xvcmcveHdp\n"
             + "a2kvY3J5cHRvL3Bhc3N3ZC9LZXlEZXJpdmF0aW9uRnVuY3Rpb247eHB1cgACW0Ks\n"
             + "8xf4BghU4AIAAHhwAAABGKu0L1JPS2is0XR+QaXeW078cZi7yEmJkqOOql1qyRG4\n"
             + "7KGvhVXVWNaxLr3CJlGd87O82SYH6rq2JtPmzw45KE9ps2anip/BQ0gBx7cE6akn\n"
             + "9FgulJ5uGSqnqE3nfE1jQtTTzTXBkANj7in8GcH2p9Sfqb/yD2xlrMQRHQPW5pbC\n"
             + "yB9xj/HkFFTz3y0Jrb/0xSEQQWwaV6EKf21xM5TEXawygxbtOYOvyywCNINDs2l1\n"
             + "sd9WYJZSNEutN2KcCj9ieYxyowYqkHcQ7xQ5RUqDLY9Zv4KoxVPQX7mGJ8GtYoqH\n"
             + "uD410tUGtki082DyBZ2EgtIHSEMnPf3Y5w6iGNovdYf9h1LMVF55ugImXxBr5k4m\n"
             + "X+f1xkHEK4FzcgBGb3JnLnh3aWtpLmNyeXB0by5wYXNzd2QuaW50ZXJuYWwuU2Ny\n"
             + "eXB0TWVtb3J5SGFyZEtleURlcml2YXRpb25GdW5jdGlvbgAAAAAAAAABAgAFSQAJ\n"
             + "YmxvY2tTaXplSQAQZGVyaXZlZEtleUxlbmd0aEkADW1lbW9yeUV4cGVuc2VJABBw\n"
             + "cm9jZXNzb3JFeHBlbnNlWwAEc2FsdHEAfgACeHIASG9yZy54d2lraS5jcnlwdG8u\n"
             + "cGFzc3dkLmludGVybmFsLkFic3RyYWN0TWVtb3J5SGFyZEtleURlcml2YXRpb25G\n"
             + "dW5jdGlvbgAAAAAAAAABAgAAeHAAAAAIAAAAGAAABAAAAAABdXEAfgAFAAAAEOFG\n"
             + "Wz/q3V9lnKrIW6eRQBY=\n"
             + "------END PASSWORD CIPHERTEXT------";
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
