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

import org.junit.Test;
import org.junit.Assert;

import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.crypto.digests.SHA1Digest;

import org.xwiki.crypto.passwd.internal.PBKDF2KeyDerivationFunction;


/**
 * Tests PBKDF2KeyDerivationFunctionTest to ensure conformance with PKCS#5v2 standard for PBKDF2.
 *
 * @since 2.5
 * @version $Id$
 */
public class PBKDF2KeyDerivationFunctionTest
{
    private final byte[] salt = { 0x12, 0x34, 0x56, 0x78, 0x78, 0x56, 0x34, 0x12 };

    private final PBKDF2KeyDerivationFunction function = new PBKDF2KeyDerivationFunction(new SHA1Digest());

    /** from: http://www.ietf.org/rfc/rfc3211.txt */
    @Test
    public void pbkdf2ConformanceTest1() throws Exception
    {
        String password = "password";

        byte[] out = this.function.generateDerivedKey(password.getBytes("US-ASCII"), this.salt, 5, 8);
        String outStr = new String(Hex.encode(out), "US-ASCII");
        String expectOut = "d1daa78615f287e6";
        Assert.assertTrue("\nExpected: " + expectOut + "\n     Got: " + outStr,
                          expectOut.equals(outStr));
    }

    /** from: http://www.ietf.org/rfc/rfc3211.txt */
    @Test
    public void pbkdf2ConformanceTest2() throws Exception
    {
        String password = "All n-entities must communicate with other n-entities via n-1 entiteeheehees";
        byte[] out = this.function.generateDerivedKey(password.getBytes("US-ASCII"), this.salt, 500, 16);
        String outStr = new String(Hex.encode(out), "US-ASCII");
        String expectOut = "6a8970bf68c92caea84a8df285108586";
        Assert.assertTrue("\nExpected: " + expectOut + "\n     Got: " + outStr,
                          expectOut.equals(outStr));
    }

    @Test
    public void initializationProcessorTimeGuessingTest()
    {
        this.function.init(20000, 20);
        long time = System.currentTimeMillis();
        this.function.hashPassword("password".getBytes());
        System.out.println("Time taken: " + (System.currentTimeMillis() - time));
    }
}
