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
package org.xwiki.spaces;

import org.xwiki.component.annotation.ComponentRole;

/**
 * Defines the configuration possibilities for the Spaces module.
 * 
 * @version $Id$
 */
@ComponentRole
public interface SpaceManagerConfiguration
{

    /**
     * @return the value of the default type of space to create when none is provided. The type of space is the field of
     *         name <tt>type</tt> in the XWiki class <tt>XWiki.SpaceClass</tt>.
     */
    String getDefaultSpaceType();

    /**
     * @return a regular expression valid space names should validate against in order for the space name to be valid.
     *         An empty string meaning that all space names are accepted.
     */
    String getSpaceNameValidationRegex();

    /**
     * @return the comma separated XWiki right access levels the space managers are provided with. for example 
     * <tt>"view, comment, edit, admin, delete"</tt>.
     */
    String getSpaceManagersAccessLevels();

    /**
     * @return the comma separated XWiki right access levels the space members are provided with. for example <tt>"view,
     *         comment, edit"</tt>.
     */
    String getSpaceMembersAccessLevels();

    /**
     * @return the document to include as content of the Space home. None if empty.
     */
    String getSpaceHomeInclude();
}
