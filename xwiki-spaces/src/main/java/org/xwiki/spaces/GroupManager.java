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
import org.xwiki.model.reference.DocumentReference;

/**
 * An component to manipulate group of users.
 * 
 * @version $Id$
 */
@ComponentRole
public interface GroupManager
{
    /**
     * Adds a user to a group.
     * 
     * @param user the reference to the document of the user to add to the group
     * @param group the reference of the group to add the user to
     * @throws GroupManagerException when an error occurs when trying to add the user to the group.
     */
    void addUserToGroup(DocumentReference user, DocumentReference group) throws GroupManagerException;

    /**
     * Removes a user from a group.
     * 
     * @param user the reference to the document of the user to remove from the group
     * @param group the reference of the group to remove the user from
     * @throws GroupManagerException when an error occurs when trying to remove the user from the group. 
     */
    void removeUserFromGroup(DocumentReference user, DocumentReference group) throws GroupManagerException;
    
    /**
     * @param user the reference to the document of the user to check the group membership
     * @param group the reference of the group to check if the user is member of
     * @return true if the user is member of the group, false otherwise.
     * @throws GroupManagerException when an error occurs when trying to determine if the user is member of the group
     */
    boolean isMemberOfGroup(DocumentReference user, DocumentReference group) throws GroupManagerException;
    
}
