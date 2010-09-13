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
package org.xwiki.extension.internal;

import java.util.Collections;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;

@Component("ExtensionManagerApplicationStarted")
public class ExtensionManagerApplicationListener extends AbstractLogEnabled implements EventListener
{
    private static final List<Event> EVENTS = Collections.<Event> singletonList(new ApplicationStartedEvent());

    @Requirement
    private ComponentManager componentManager;

    public List<Event> getEvents()
    {
        return EVENTS;
    }

    public String getName()
    {
        return "ExtensionManagerApplicationStarted";
    }

    public void onEvent(Event arg0, Object arg1, Object arg2)
    {
        try {
            // local extensions are loaded in default ExtensionManager initialization
            this.componentManager.lookup(ExtensionManager.class);
        } catch (ComponentLookupException e) {
            getLogger().error("Failed to initialize Extension Manager", e);
        }
    }
}
