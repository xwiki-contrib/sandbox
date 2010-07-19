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

import org.junit.Before;
import org.xwiki.crypto.passwd.internal.AESPasswdCryptoService;
import org.xwiki.test.annotation.MockingRequirement;


/**
 * Tests {@link AESPasswdCryptoService}.
 * 
 * @version $Id$
 * @since 2.5
 */
public class AESPasswdCryptoServiceTest extends DefaultPasswdCryptoServiceTest
{
    /** The tested service. */
    @MockingRequirement
    private AESPasswdCryptoService aes;

    /**
     * {@inheritDoc}
     * @see org.xwiki.test.AbstractComponentTestCase#setUp()
     */
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        // a little hack
        this.service = this.aes;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.passwd.DefaultPasswdCryptoServiceTest#getEncrypted()
     */
    @Override
    protected String getEncrypted()
    {
        return "------BEGIN PASSWORD AES256CBC-SHA384 CIPHERTEXT-----\n"
             + "YKwhXsIlodoSw6Thhszljiu5vgs=:\n"
             + "FsXVgpNTekDAbg/3tiNwjhyXOULvEqvQa11mmEcem0a3qa0ICuO3JYmquVpQMdYo\n"
             + "jt/dt5ykDK6jCFsw5hu4TrS7oXmSoKBR0iwas+enkX3HJcC7O+fOyDz/2LPyxjjR\n"
             + "ZBcUEkIdmaIanOcqS0dQ3UPTY018FGoog8/TjM24R271Uz68aCQf2F8nNe4Ib9QP\n"
             + "DxJ/WY6ISBsx/Fw0+tU/zJ/DTGmtR8/cyYxJOQZZ+0VPlm5XkkoqP35WPZ1MgEdi\n"
             + "d2spqBCogwcSY9EVpYCrpwTiJEn/YXw0qB3tsJNFlpVrofvp/bRxKkpDfc+TzdNu\n"
             + "iPI7MD9IKIDJlrHgeHGOdxz3foK/sBDNhwb4qkIzyu7i0sHv6Upy096waN2siPIn\n"
             + "------END CIPHERTEXT------\n";
    }
}

