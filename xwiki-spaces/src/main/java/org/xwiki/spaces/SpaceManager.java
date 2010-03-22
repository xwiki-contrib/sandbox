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
 * An interface to manage Spaces. Spaces (with an upper-case S) are a higher abstraction than the technical document
 * spaces (lower-case s) in the wiki. They define extra properties for such wiki spaces, like a display name, or a type
 * of space. Those properties are stored in a object of class <tt>XWiki.SpaceClass</tt>, in the home page of the space
 * this Space represents. The Space Manager role is to create, retrieve and manipulate such Space entities.
 * 
 * @version $Id$
 */
@ComponentRole
public interface SpaceManager
{
    /**
     * Creates a space.
     * 
     * @param key the technical space name. Used as the "wiki space" name of the documents for that space.
     * @param name the display name of the space.
     * @throws SpaceAlreadyExistsException when attempting to create a space that already exists.
     * @throws IllegalSpaceKeyException when the space key does not validate against the configured regex
     * @throws SpaceManagerException when an error occur at a lower level trying to create the space.
     */
    void createSpace(String key, String name) throws IllegalSpaceKeyException, SpaceAlreadyExistsException,
        SpaceManagerException;

    /**
     * Creates a space.
     * 
     * @param key the technical space name. Used as the "wiki space" name of the documents for that space.
     * @param name the display name of the space.
     * @param type the type of space to create.
     * @throws SpaceAlreadyExistsException when attempting to create a space that already exists.
     * @throws IllegalSpaceKeyException when the space key does not validate against the configured regex
     * @throws SpaceManagerException when an error occur at a lower level trying to create the space.
     */
    void createSpace(String key, String name, String type) throws IllegalSpaceKeyException,
        SpaceAlreadyExistsException, SpaceManagerException;

    /**
     * Checks if a space key will be accepted upon creation or not. The key is validated against an optional regular
     * expression retrieved from configuration. If the validation expression is blank, all keys are considered valid and
     * the check will always return true.
     * 
     * @param key the to test
     * @return true if the key is legal and would be accepted by {@link #createSpace} methods, false otherwise.
     */
    boolean isLegalSpaceKey(String key);
    
    /**
     * Adds a member to the Space.
     * 
     * @param spaceKey the key of the space to add the member to
     * @param userReference the reference to the document of user to add as member
     * @throws SpaceDoesNotExistsException when trying to add a member to a Space that does not exists
     * @throws SpaceManagerException when and error occur at a lower level trying to add the member.
     */
    void addMember(String spaceKey, DocumentReference userReference)
        throws SpaceDoesNotExistsException, SpaceManagerException;

    /**
     * Adds a manager to the Space.
     * 
     * @param spaceKey the key of the space to add the member to
     * @param userReference the reference to the document of user to add as member
     * @throws SpaceDoesNotExistsException when trying to add a member to a Space that does not exists
     * @throws SpaceManagerException when and error occur at a lower level trying to add the member.
     */
    void addManager(String spaceKey, DocumentReference userReference)
        throws SpaceDoesNotExistsException, SpaceManagerException;
}
