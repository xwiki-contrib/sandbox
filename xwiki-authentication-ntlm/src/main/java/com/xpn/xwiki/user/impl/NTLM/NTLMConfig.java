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

package com.xpn.xwiki.user.impl.NTLM;

import com.xpn.xwiki.XWikiContext;

public class NTLMConfig {

    protected static final String CONF_KEY = "xwiki.authentication.ntlm";

    public String getParam(String name, XWikiContext context) {
        return getParam(name, "", context);
    }

    public String getParam(String name, String def, XWikiContext context) {
        String param = null;
        try {
            param = context.getWiki().getXWikiPreference(name, context);
        } catch (Exception e) {
        }
        if (param == null || param.length() == 0) {
            try {
                param = context.getWiki().Param(CONF_KEY + "." + name);
            } catch (Exception e) {
            }
        }
        if (param == null)
            return def;
        return param;
    }
}