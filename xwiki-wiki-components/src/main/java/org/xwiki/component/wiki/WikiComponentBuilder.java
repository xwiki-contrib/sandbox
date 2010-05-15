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
import org.xwiki.model.reference.DocumentReference;

/**
 * Constructs a {@link WikiComponent} out of the data contained in the document pointed by a {@link DocumentReference}.
 * 
 * @since 2.4-M2
 * @version $Id$
 */
@ComponentRole
public interface WikiComponentBuilder
{

    /**
     * Builds a wiki component representation extracting the data stored as objects of document.
     * 
     * @param reference the reference to the document that holds component definition objects
     * @return the constructed component definition
     * @throws InvalidComponentDefinitionException when the data in the document is not a valid component definition
     * @throws WikiComponentException the builder failed to create the component out of the document (for example due to
     *             a failure by tge underlying store, etc.)
     */
    WikiComponent build(DocumentReference reference) throws InvalidComponentDefinitionException, WikiComponentException;

}
