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

package org.xwiki.xoo.xwikilib;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

/**
 * The context for the XWiki Components
 * 
 * @version $Id$
 * @since 1.0 M
 */

public class XWikiComponentContext implements Initializable
{
    private EmbeddableComponentManager componentManager;

    /**
     * Creates the context for the XWiki Components
     */
    public void initializeComponentContext()
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.initialize(this.getClass().getClassLoader());
        this.componentManager = ecm;
    }

    /**
     * @return a componentManager
     */
    public EmbeddableComponentManager getComponentManager()
    {
        return this.componentManager;
    }

    /**
     * {@inheritDoc}
     */
    public void initialize() throws InitializationException
    {

    }
}
