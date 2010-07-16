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

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.crypto.x509.internal.X509Keymaker;

/**
 * Test the {@link X509Keymaker} class.
 * 
 * @version $Id$
 * @since 2.5
 */
public class X509KeymakerTest
{
    /** The tested key maker. */
    private final X509Keymaker keyMaker = new X509Keymaker();

    @Test
    public void testGenerateCertAuthority() throws GeneralSecurityException
    {
        KeyPair kp = keyMaker.newKeyPair();
        X509Certificate cert = keyMaker.makeCertificateAuthority(kp, 1);
        System.out.println(cert.getCriticalExtensionOIDs());
        Assert.assertTrue(BasicConstraints.getInstance(cert).isCA());
    }
}

