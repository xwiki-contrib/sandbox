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
package org.xwiki.contrib.authentication.ip;

import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * Authentication based on IP address.
 * 
 * @version $Id$
 * @since 4.3
 */
public class XWikiIPAuthenticator extends XWikiAuthServiceImpl
{
    /** Lawg dawg. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XWikiIPAuthenticator.class);

    /** XWiki doesn't like these chars in user names. */
    private static final Pattern INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9_]");

    @Override
    public XWikiUser checkAuth(XWikiContext context) throws XWikiException
    {
        final XWikiRequest req = context.getRequest();

        final String realName = req.getRemoteAddr();
        final String name = INVALID_CHARS.matcher(realName).replaceAll("_");
        final String fullName = "XWiki." + name;

        createUserIfNeeded(name, fullName, context);

        return new XWikiUser(fullName);
    }

    /**
     * Create a user if none exists.
     *
     * @param name the short name, must be scrubbed of chars which XWiki doesn't like.
     * @param fullName name, prefixed with 'XWiki.'.
     * @param context the ball of mud.
     * @throws XWikiException if thrown by {@link XWiki#createEmptyUser()}.
     */
    private void createUserIfNeeded(final String name,
                                    final String fullName,
                                    final XWikiContext context) throws XWikiException
    {
        final String database = context.getDatabase();
        try {
            // Switch to main wiki to force users to be global users
            context.setDatabase(context.getMainXWiki());

            final XWiki wiki = context.getWiki();

            // test if user already exists
            if (!wiki.exists(fullName, context)) {
                LOGGER.info("Need to create user [{0}]", fullName);
                wiki.createEmptyUser(name, "edit", context);
            }
        } finally {
            context.setDatabase(database);
        }
    }
}
