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
package org.xwiki.crypto;

import org.junit.Test;

import org.xwiki.crypto.data.XWikiX509Certificate;
import org.xwiki.crypto.data.XWikiX509KeyPair;

import org.xwiki.crypto.internal.KeyService;

/**
 * KeyService test, insure that the key service is able to make keys without throwing an exception.
 * 
 * @version $Id$
 * @since 2.5
 */
public class KeyServiceTest
{
    private final KeyService service = new KeyService();

    public void certsFromSpkacTest() throws Exception
    {
        //this.service.certsFromSpkac(this.spkacSerialization, 1, "my webid", "xwiki:XWiki.Me");
    }

    @Test
    public void newCertAndPrivateKeyTest() throws Exception
    {
        this.service.newCertAndPrivateKey(1, "my webid", "xwiki:XWiki.Me", "pass");
    }
}
