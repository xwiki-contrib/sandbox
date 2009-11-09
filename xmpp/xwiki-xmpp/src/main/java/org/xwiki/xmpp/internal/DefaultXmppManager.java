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
package org.xwiki.xmpp.internal;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.xmpp.XmppManager;

/**
 * Concrete implementation of a <tt>XmppManager</tt> component. The component is configured via the Plexus container.
 * 
 * @version $Id$
 */
@Component
public class DefaultXmppManager extends AbstractLogEnabled implements XmppManager, Initializable
{
    // TODO: needs to implement xmpp manger
    /** The greeting that was specified in the configuration. */
    private String greeting;

    /**
     * Says hello by returning a greeting to the caller.
     * 
     * @return A greeting.
     */
    public String sayHello()
    {
        return greeting;
    }

    public void initialize() throws InitializationException
    {
        // TODO: initialize component
    }
}
