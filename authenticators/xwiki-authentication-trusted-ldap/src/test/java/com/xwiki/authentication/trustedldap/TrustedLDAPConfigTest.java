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
package com.xwiki.authentication.trustedldap;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

public class TrustedLDAPConfigTest extends AbstractBridgedComponentTestCase
{
    private Mockery mockery = new Mockery()
    {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private XWiki xwikiMock;

    private TrustedLDAPConfig config;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.xwikiMock = this.mockery.mock(XWiki.class);

        getContext().setWiki(this.xwikiMock);
        getContext().setDatabase("xwiki");

        this.mockery.checking(new Expectations()
        {
            {
                allowing(xwikiMock).Param(with(any(String.class)));
                will(returnValue(null));
            }
        });

        this.config = new TrustedLDAPConfig();
    }

    // Tests

    @Test
    public void getLDAPBindDN()
    {
        this.mockery.checking(new Expectations()
        {{
            allowing(xwikiMock).getXWikiPreference("ldap_bind_DN", getContext()); will(returnValue("preferences {0} {1} bind dn"));
        }});

        Map<String, String> remoteUserLdapConfiguration = new HashMap<String, String>();
        
        remoteUserLdapConfiguration.put("login", "login");
        remoteUserLdapConfiguration.put("password", "password");

        Assert.assertEquals("preferences login password bind dn", this.config.getLDAPBindDN(remoteUserLdapConfiguration, getContext()));
        
        remoteUserLdapConfiguration.put("ldap_bind_DN", "custom {0} {1} bind dn");
        
        Assert.assertEquals("custom login password bind dn", this.config.getLDAPBindDN(remoteUserLdapConfiguration, getContext()));
    }

    @Test
    public void getLDAPBindPassword()
    {
        this.mockery.checking(new Expectations()
        {{
            allowing(xwikiMock).getXWikiPreference("ldap_bind_pass", getContext()); will(returnValue("preferences {0} {1} bind pass"));
        }});

        Map<String, String> remoteUserLdapConfiguration = new HashMap<String, String>();
        
        remoteUserLdapConfiguration.put("login", "login");
        remoteUserLdapConfiguration.put("password", "password");

        Assert.assertEquals("preferences login password bind pass", this.config.getLDAPBindPassword(remoteUserLdapConfiguration, getContext()));
        
        remoteUserLdapConfiguration.put("ldap_bind_pass", "custom {0} {1} bind pass");
        
        Assert.assertEquals("custom login password bind pass", this.config.getLDAPBindPassword(remoteUserLdapConfiguration, getContext()));
    }
}
