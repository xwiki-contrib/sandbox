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
package org.xwiki.contrib.authentication.jdbc.internal;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Role;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.RegexEntityReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.XObjectAddedEvent;
import com.xpn.xwiki.internal.event.XObjectDeletedEvent;
import com.xpn.xwiki.internal.event.XObjectUpdatedEvent;

@Role
@Named("org.xwiki.contrib.authentication.jdbc.internal.UserEventListener")
public class UserEventListener implements EventListener
{
    /**
     * The reference to match class XWiki.XWikiUsers on main wiki.
     */
    private static final RegexEntityReference USERSCLASS_REFERENCE = new RegexEntityReference(
        Pattern.compile("xwiki:XWiki.XWikiUsers\\[\\d*\\]"), EntityType.OBJECT);

    /**
     * The matched events.
     */
    private static final List<Event> EVENTS = Arrays.<Event> asList(new XObjectAddedEvent(USERSCLASS_REFERENCE),
        new XObjectDeletedEvent(USERSCLASS_REFERENCE), new XObjectUpdatedEvent(USERSCLASS_REFERENCE));

    @Inject
    private UserSynchronizer synchronizer;

    @Inject
    private Logger logger;

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public String getName()
    {
        return UserEventListener.class.getName();
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument userDoc = (XWikiDocument) source;
        XWikiContext xcontext = (XWikiContext) data;

        if (event instanceof XObjectAddedEvent) {
            addUser(userDoc, xcontext);
        } else if (event instanceof XObjectDeletedEvent) {
            deleteUser(userDoc, xcontext);
        } else {
            updateUser(userDoc, xcontext);
        }
    }

    private void addUser(XWikiDocument userDocument, XWikiContext xcontext)
    {
        try {
            this.synchronizer.insertJDBCUser(userDocument);
        } catch (Exception e) {
            this.logger.error("Failed to create JDBC user from XWiki user [" + userDocument + "]", e);
        }
    }

    private void deleteUser(XWikiDocument userDocument, XWikiContext xcontext)
    {
        try {
            this.synchronizer.deleteJDBCUser(userDocument);
        } catch (Exception e) {
            this.logger.error("Failed to delete JDBC user from XWiki user [" + userDocument + "]", e);
        }
    }

    private void updateUser(XWikiDocument userDocument, XWikiContext xcontext)
    {
        try {
            this.synchronizer.updateJDBCUser(userDocument);
        } catch (Exception e) {
            this.logger.error("Failed to update JDBC user from XWiki user [" + userDocument + "]", e);
        }
    }
}
