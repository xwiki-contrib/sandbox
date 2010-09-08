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

package com.xwiki.authentication;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;

/**
 * Get PUMA authenticator configuration.
 * 
 * @version $Id$
 */
public class Config
{
    /**
     * LogFactory <code>LOGGER</code>.
     */
    private static final Log LOG = LogFactory.getLog(Config.class);

    private final String prefPrefix;

    private final String confPrefix;

    public Config(String prefPrefix, String confPrefix)
    {
        this.prefPrefix = prefPrefix;
        this.confPrefix = confPrefix;
    }

    public String getParam(String name, XWikiContext context)
    {
        return getParam(name, "", context);
    }

    public String getParam(String name, String def, XWikiContext context)
    {
        String param = null;
        try {
            param = context.getWiki().getXWikiPreference(prefPrefix + "_" + name, context);
        } catch (Exception e) {
            LOG.error("Faile to get preference [" + prefPrefix + "_" + name + "]", e);
        }

        if (StringUtils.isEmpty(param)) {
            param = def;

            try {
                param = context.getWiki().Param(confPrefix + "." + name);
            } catch (Exception e) {
                LOG.error("Failed to get property [" + confPrefix + "." + name + "] from configuration file", e);
            }
        }

        return param;
    }

    public List<String> getListParam(String name, char separator, List<String> def, XWikiContext context)
    {
        List<String> list = def;

        String str = getParam(name, null, context);

        if (str != null) {
            if (!StringUtils.isEmpty(str)) {
                list = Arrays.asList(StringUtils.split(str, separator));
            } else {
                list = Collections.emptyList();
            }
        }

        return list;
    }

    public Map<String, String> getMapParam(String name, char separator, Map<String, String> def,
        boolean forceLowerCaseKey, XWikiContext context)
    {
        Map<String, String> mappings = def;

        List<String> list = getListParam(name, separator, null, context);

        if (list != null) {
            if (list.isEmpty()) {
                mappings = Collections.emptyMap();
            } else {
                mappings = new LinkedHashMap<String, String>();

                for (String fieldStr : list) {
                    int index = fieldStr.indexOf('=');
                    if (index != -1) {
                        String key = fieldStr.substring(0, index);
                        String value = index + 1 == fieldStr.length() ? "" : fieldStr.substring(index + 1);

                        mappings.put(forceLowerCaseKey ? key.toLowerCase() : key, value);
                    } else {
                        LOG.warn("Error parsing " + name + " attribute in xwiki.cfg: " + fieldStr);
                    }
                }
            }
        }

        return mappings;
    }

    public Map<String, Collection<String>> getOneToManyParam(String name, char separator,
        Map<String, Collection<String>> def, boolean left, XWikiContext context)
    {
        Map<String, Collection<String>> oneToMany = def;

        List<String> list = getListParam(name, separator, null, context);

        if (list != null) {
            if (list.isEmpty()) {
                oneToMany = Collections.emptyMap();
            } else {
                oneToMany = new LinkedHashMap<String, Collection<String>>();

                for (String mapping : list) {
                    int splitIndex = mapping.indexOf('=');

                    if (splitIndex < 1) {
                        LOG.error("Error parsing [" + name + "] attribute: " + mapping);
                    } else {
                        String leftProperty =
                            left ? mapping.substring(0, splitIndex) : mapping.substring(splitIndex + 1);
                        String rightProperty =
                            left ? mapping.substring(splitIndex + 1) : mapping.substring(0, splitIndex);

                        Collection<String> rightCollection = oneToMany.get(leftProperty);

                        if (rightCollection == null) {
                            rightCollection = new HashSet<String>();
                            oneToMany.put(leftProperty, rightCollection);
                        }

                        rightCollection.add(rightProperty);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("[" + name + "] mapping found: " + leftProperty + " " + rightCollection);
                        }
                    }
                }
            }
        }

        return oneToMany;
    }
}
