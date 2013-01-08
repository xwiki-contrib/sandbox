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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConfig;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConnection;
import com.xwiki.authentication.Config;

public class TrustedLDAPConfig extends Config
{
    /** LogFactory <code>LOGGER</code>. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TrustedLDAPConfig.class);

    protected static final String PREF_KEY = "trustedldap";

    protected static final String CONF_KEY = "xwiki.authentication.trustedldap";

    public TrustedLDAPConfig()
    {
        super(PREF_KEY, CONF_KEY);
    }

    public Pattern getRemoteUserParser(XWikiContext context)
    {
        String param = getParam("remoteUserParser", null, context);

        return param != null ? Pattern.compile(param) : null;
    }

    public List<String> getRemoteUserMapping(int groupId, XWikiContext context)
    {
        return getListParam("remoteUserMapping." + groupId, ',', Collections.<String> emptyList(), context);
    }

    public Map<String, String> getRemoteUserMapping(String propertyName, XWikiContext context)
    {
        return getRemoteUserMapping(propertyName, false, context);
    }

    public Map<String, String> getRemoteUserMapping(String propertyName, boolean forceLowerCaseKey, XWikiContext context)
    {
        return getMapParam("remoteUserMapping." + propertyName, '|', Collections.<String, String> emptyMap(),
            forceLowerCaseKey, context);
    }

    public String getLDAPBindDN(Map<String, String> remoteUserLdapConfiguration, XWikiContext context)
    {
        String login = remoteUserLdapConfiguration.get("login");
        String password = remoteUserLdapConfiguration.get("password");

        String remoteUser_bind_DN = remoteUserLdapConfiguration.get("ldap_bind_DN");

        return MessageFormat.format(remoteUser_bind_DN != null ? remoteUser_bind_DN : XWikiLDAPConfig.getInstance()
            .getLDAPBindDN(context), XWikiLDAPConnection.escapeLDAPDNValue(login), XWikiLDAPConnection
            .escapeLDAPDNValue(password));
    }

    public String getLDAPBindPassword(Map<String, String> remoteUserLdapConfiguration, XWikiContext context)
    {
        String login = remoteUserLdapConfiguration.get("login");
        String password = remoteUserLdapConfiguration.get("password");

        String remoteUser_bind_pass = remoteUserLdapConfiguration.get("ldap_bind_pass");

        return MessageFormat.format(remoteUser_bind_pass != null ? remoteUser_bind_pass : XWikiLDAPConfig.getInstance()
            .getLDAPBindPassword(context), login, password);
    }
}
