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
package org.xwiki.eclipse.core.notifications;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;

/**
 * A singleton implementing a notification manager. XWiki Core classes should use this singleton for publishing events.
 */
public class NotificationManager
{
    private static NotificationManager sharedInstance;

    private ListenerList listenerList;

    /**
     * A private class that contains information about a listener and the even type it is interested in.
     */
    static private class ListenerEntry
    {
        private Set<CoreEvent.Type> eventTypes;

        private ICoreEventListener listener;

        public ListenerEntry(ICoreEventListener listener, CoreEvent.Type[] eventTypes)
        {
            Assert.isNotNull(listener);
            this.listener = listener;

            Assert.isNotNull(eventTypes);
            this.eventTypes = new HashSet<CoreEvent.Type>();
            for (CoreEvent.Type type : eventTypes) {
                this.eventTypes.add(type);
            }
        }

        public Set<CoreEvent.Type> getEventTypes()
        {
            return new HashSet<CoreEvent.Type>(eventTypes);
        }

        public ICoreEventListener getListener()
        {
            return listener;
        }
    }

    private NotificationManager()
    {
        listenerList = new ListenerList();
    }

    /**
     * @return The shared instance.
     */
    public synchronized static NotificationManager getDefault()
    {
        if (sharedInstance == null) {
            sharedInstance = new NotificationManager();
        }

        return sharedInstance;
    }

    /**
     * Add a listener to core events.
     * 
     * @param listener The listener.
     * @param eventTypes An array containing the event types the listener is interested in.
     * @see CoreEvent.Type
     */
    public void addListener(ICoreEventListener listener, CoreEvent.Type[] eventTypes)
    {
        listenerList.add(new ListenerEntry(listener, eventTypes));
    }

    /**
     * Remove a listener.
     * 
     * @param listener The listener to be removed.
     */
    public void removeListener(ICoreEventListener listener)
    {
        Object[] objects = listenerList.getListeners();
        for (Object object : objects) {
            ListenerEntry listenerEntry = (ListenerEntry) object;
            if (listenerEntry.getListener().equals(listener)) {
                listenerList.remove(listenerEntry);
                break;
            }
        }
    }

    /**
     * Notify an event to all the listeners interested to that event.
     * 
     * @param type The event type.
     * @param source The object that generated the event.
     * @param data Additional data associated to the event.
     */
    public synchronized void fireCoreEvent(CoreEvent.Type type, Object source, Object data)
    {
        CoreEvent coreEvent = new CoreEvent(type, source, data);
        Object[] objects = listenerList.getListeners();
        for (Object object : objects) {
            ListenerEntry listenerEntry = (ListenerEntry) object;
            if (listenerEntry.getEventTypes().contains(type)) {
                listenerEntry.getListener().handleCoreEvent(coreEvent);
            }
        }
    }
}
