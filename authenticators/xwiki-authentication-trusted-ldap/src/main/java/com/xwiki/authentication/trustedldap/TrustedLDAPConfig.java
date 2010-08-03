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
 *
 */

package com.xwiki.authentication.trustedldap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.authentication.Config;

public class TrustedLDAPConfig extends Config
{
    /** LogFactory <code>LOGGER</code>. */
    private static final Log LOG = LogFactory.getLog(TrustedLDAPConfig.class);

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
        return getMapParam("remoteUserMapping." + propertyName, '|', Collections.<String, String> emptyMap(), context);
    }
}
