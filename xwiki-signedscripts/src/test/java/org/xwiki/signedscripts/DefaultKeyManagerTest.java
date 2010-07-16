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
package org.xwiki.signedscripts;

import java.security.GeneralSecurityException;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.signedscripts.internal.DefaultKeyManager;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;


/**
 * Test for the {@link DefaultKeyManager} component.
 * 
 * @version $Id$
 * @since 2.5
 */
public class DefaultKeyManagerTest extends AbstractMockingComponentTestCase
{
    /** Tested key manager implementation. */
    @MockingRequirement
    private DefaultKeyManager keyManager;

    @Test
    public void testGlobalRoot() throws GeneralSecurityException
    {
        XWikiX509Certificate cert = keyManager.getGlobalRootCertificate();
        cert.checkValidity();
//        Assert.assertEquals("FIXME known global fingerprint", cert.getFingerprint());
    }
}

