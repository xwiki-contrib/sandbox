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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiAuthService;
import com.xwiki.authentication.Config;

/**
 * Get PUMA authenticator configuration.
 * 
 * @version $Id$
 */
public class PUMAConfig extends Config
{
    /**
     * LogFactory <code>LOGGER</code>.
     */
    private static final Log LOG = LogFactory.getLog(PUMAConfig.class);

    public PUMAConfig()
    {
        super("puma", "xwiki.authentication.puma");
    }
    
    public Map<String, String> getUserMapping(XWikiContext context)
    {
        return getMapParam("userMapping", null, context);
    }

    public Map<String, Collection<String>> getGroupMapping(XWikiContext context)
    {
        return getOneToManyParam("groupsMapping", null, false, context);
    }

    public XWikiAuthService getFalbackAuthenticator(XWikiContext context)
    {
        String authenticatorClassName = getParam("falback", null, context);

        XWikiAuthService authenticator = null;

        if (authenticatorClassName != null) {
            try {
                authenticator = (XWikiAuthService) Class.forName(authenticatorClassName).newInstance();
            } catch (Exception e) {
                LOG.error("Faild to get falback authenticator [" + authenticatorClassName + "]", e);
            }
        }

        return authenticator;
    }
}
