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
package org.xwiki.spaces.internal;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.script.service.ScriptService;
import org.xwiki.spaces.IllegalSpaceKeyException;
import org.xwiki.spaces.SpaceAlreadyExistsException;
import org.xwiki.spaces.SpaceDoesNotExistsException;
import org.xwiki.spaces.SpaceManager;
import org.xwiki.spaces.SpaceManagerException;

import com.xpn.xwiki.XWikiContext;

/**
 * The exposed script service for manipulation Spaces components from scripts in the wiki. Example of usage: <code>
 * $services.spaces.createSpace("MySpace", "My personnal space", "user")
 * </code>
 * 
 * @version $Id$
 */
@Component("spaces")
public class SpaceScriptService implements ScriptService
{
    /** The execution. We need it to push potential error messages in the context. */
    @Requirement
    private Execution execution;

    /** The internal component used to manage spaces. */
    @Requirement
    private SpaceManager manager;

    /** A reference resolver that transform string representations to real entities references. */
    @Requirement
    private DocumentReferenceResolver<String> referenceResolver;

    /**
     * Equivalent of {@link SpaceManager#createSpace(String, String)} except exceptions are caught and error messages
     * stored in the context for further display by scripts.
     * 
     * @see SpaceManager#createSpace(String, String)
     * @param key the key of the space to create
     * @param name the name of the space to create
     * @param type the type of space to create
     * @return 0 if everything went allright, a negative integer code otherwise.
     */
    public int createSpace(String key, String name, String type)
    {
        try {
            if (StringUtils.isBlank(type)) {
                this.manager.createSpace(key, name);
            } else {
                this.manager.createSpace(key, name, type);
            }
            return 0;
        } catch (SpaceAlreadyExistsException e) {
            this.addMessageInContext(e);
            return -1;
        } catch (IllegalSpaceKeyException e) {
            this.addMessageInContext(e);
            return -2;
        } catch (SpaceManagerException e) {
            this.addMessageInContext(e);
            return -2;
        }
    }

    /**
     * Equivalent of {@link SpaceManager#createSpace(String, String)} except exceptions are caught and error messages
     * stored in the context for further display by scripts.
     * 
     * @see SpaceManager#createSpace(String, String)
     * @param key the key of the space to create
     * @param name the name of the space to create
     * @return 0 if everything went allright, a negative integer code otherwise.
     */
    public int createSpace(String key, String name)
    {
        return this.createSpace(key, name, "");
    }

    /**
     * Adds a member to a space.
     * 
     * @see SpaceManager#addMember(String, org.xwiki.model.reference.DocumentReference)
     * @param key the key of the space to add a manager to
     * @param name the string representation of the user to add
     * @return 0 if everything went allright, a negative integer code otherwise.
     */
    public int addMember(String key, String name)
    {
        try {
            this.manager.addMember(key, this.referenceResolver.resolve(name));
            return 0;
        } catch (SpaceDoesNotExistsException e) {
            this.addMessageInContext(e);
            return -1;
        } catch (SpaceManagerException e) {
            this.addMessageInContext(e);
            return -2;
        }
    }

    /**
     * Adds a manager to a space.
     * 
     * @see SpaceManager#addManager(String, org.xwiki.model.reference.DocumentReference)
     * @param key the key of the space to add a manager to
     * @param name the string representation of the user to add
     * @return 0 if everything went allright, a negative integer code otherwise.
     */
    public int addManager(String key, String name)
    {
        try {
            this.manager.addManager(key, this.referenceResolver.resolve(name));
            return 0;
        } catch (SpaceDoesNotExistsException e) {
            this.addMessageInContext(e);
            return -1;
        } catch (SpaceManagerException e) {
            this.addMessageInContext(e);
            return -2;
        }
    }

    /**
     * Helper that adds a message from an exception in the XWiki context.
     * 
     * @param e the exception which message to add in the context.
     */
    private void addMessageInContext(Exception e)
    {
        ((XWikiContext) execution.getContext().getProperty("xwikicontext")).put("message", e.getMessage());
    }
}
