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
package org.xwiki.component.wiki;

import org.xwiki.component.annotation.ComponentRole;

/**
 * A WikiComponentManager is responsible for registering and unregistering components that are defined as wiki pages.
 * Each {@link WikiComponent} managed by such manager is associated to a {@link org.xwiki.model.DocumentReference}. The
 * referred document contains XObjects that define the role, hint and behavior (method bodies) of the component. This
 * document may also define requirements (other components to be binded in the method bodies execution context) and
 * possible extra interfaces (for example to implement {@link org.xwiki.component.phase.Initializable}).
 * 
 * @since 2.4-M2
 * @version $Id$
 */
@ComponentRole
public interface WikiComponentManager
{

    /**
     * Registers the passed component against the underlying component repository.
     * 
     * @param component the component to register
     * @throws WikiComponentException when failed to register the component against the CM
     */
    void registerWikiComponent(WikiComponent component) throws WikiComponentException;

}
