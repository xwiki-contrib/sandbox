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
package org.xwoot.contentprovider;

import java.util.Properties;

import junit.framework.TestCase;

import org.xwoot.contentprovider.XWootContentProviderConfiguration;

public class XWootContentProviderConfigurationTest extends TestCase
{
    public void testIgnoreAccept()
    {
        Properties properties = new Properties();
        properties.setProperty("ignore", "Main.*");
        properties.setProperty("accept", "Main.WebHome");

        XWootContentProviderConfiguration xwcpc = new XWootContentProviderConfiguration(properties);
        assertEquals(true, xwcpc.isIgnored("Main.Foo"));
        assertEquals(false, xwcpc.isIgnored("Main.WebHome"));
    }

    public void testAddRemove()
    {
        Properties properties = new Properties();

        XWootContentProviderConfiguration xwcpc = new XWootContentProviderConfiguration(properties);

        assertEquals(false, xwcpc.isIgnored("Main.WebHome"));

        xwcpc.addIgnorePattern("Main.*");
        assertEquals(true, xwcpc.isIgnored("Main.WebHome"));

        xwcpc.addAcceptPattern("Main.WebHome");
        assertEquals(false, xwcpc.isIgnored("Main.WebHome"));

        xwcpc.removeAcceptPattern("Main.WebHome");
        assertEquals(true, xwcpc.isIgnored("Main.WebHome"));

        xwcpc.removeIgnorePattern("Main.*");
        assertEquals(false, xwcpc.isIgnored("Main.WebHome"));
    }

}
