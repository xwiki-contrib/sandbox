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

package com.xwiki.authentication.puma;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiAuthService;

public class PUMAConfig
{
    /**
     * LogFactory <code>LOGGER</code>.
     */
    private static final Log LOG = LogFactory.getLog(PUMAConfig.class);

    protected static final String PREF_KEY = "puma";

    protected static final String CONF_KEY = "xwiki.authentication.puma";

    public String getParam(String name, XWikiContext context)
    {
        return getParam(name, "", context);
    }

    public String getParam(String name, String def, XWikiContext context)
    {
        String param = null;
        try {
            param = context.getWiki().getXWikiPreference(PREF_KEY + "_" + name, context);
        } catch (Exception e) {
        }

        if (param == null || param.length() == 0) {
            try {
                param = context.getWiki().Param(CONF_KEY + "." + name);
            } catch (Exception e) {
            }
        }

        if (param == null) {
            return def;
        }

        return param;
    }

    public Map<String, String> getMapParam(String name, Map<String, String> def, XWikiContext context)
    {
        Map<String, String> mappings = new HashMap<String, String>();

        String str = getParam(name, null, context);

        if (!StringUtils.isEmpty(str)) {
            String[] fields = StringUtils.split(str, '|');

            for (int i = 0; i < fields.length; i++) {
                String[] field = StringUtils.split(fields[i], '=');
                if (2 == field.length) {
                    String key = field[0];
                    String value = field[1];

                    mappings.put(key, value);
                } else {
                    LOG.error("Error parsing " + name + " attribute in xwiki.cfg: " + fields[i]);
                }
            }
        }

        return mappings;
    }

    public Map<String, Collection<String>> getManyToManyParam(String name, Map<String, Collection<String>> def,
        boolean left, XWikiContext context)
    {
        Map<String, Collection<String>> manyToMany = new HashMap<String, Collection<String>>();

        String str = getParam(name, null, context);

        if (str.trim().length() > 0) {
            String[] mappingTable = str.split("\\|");

            for (int i = 0; i < mappingTable.length; ++i) {
                String mapping = mappingTable[i].trim();

                int splitIndex = mapping.indexOf('=');

                if (splitIndex < 1) {
                    LOG.error("Error parsing [" + name + "] attribute: " + mapping);
                } else {
                    String leftProperty = left ? mapping.substring(0, splitIndex) : mapping.substring(splitIndex + 1);
                    String rightProperty = left ? mapping.substring(splitIndex + 1) : mapping.substring(0, splitIndex);

                    Collection<String> rightCollection = manyToMany.get(leftProperty);

                    if (rightCollection == null) {
                        rightCollection = new HashSet<String>();
                        manyToMany.put(leftProperty, rightCollection);
                    }

                    rightCollection.add(rightProperty);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[" + name + "] mapping found: " + leftProperty + " " + rightCollection);
                    }
                }
            }
        }

        return manyToMany;
    }

    // PUMA parameters

    public Map<String, String> getUserMappings(XWikiContext context)
    {
        return getMapParam("userMapping", null, context);
    }

    public Map<String, Collection<String>> getGroupMappings(XWikiContext context)
    {
        return getManyToManyParam("groupsMapping", null, false, context);
    }

    public XWikiAuthService getFalbackAuthenticator(XWikiContext context)
    {
        String authenticatorClassName = getParam("falback", null, context);

        XWikiAuthService authenticator = null;
        try {
            authenticator = (XWikiAuthService) Class.forName(authenticatorClassName).newInstance();
        } catch (Exception e) {
            LOG.error("Faild to get falback authenticator [" + authenticatorClassName + "]", e);
        }

        return authenticator;
    }
}
