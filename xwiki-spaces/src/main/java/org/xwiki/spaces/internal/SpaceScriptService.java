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
import org.xwiki.script.service.ScriptService;
import org.xwiki.spaces.IllegalSpaceKeyException;
import org.xwiki.spaces.SpaceAlreadyExistsException;
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
     * Helper that adds a message from an exception in the XWiki context.
     * 
     * @param e the exception which message to add in the context.
     */
    private void addMessageInContext(Exception e)
    {
        ((XWikiContext) execution.getContext().getProperty("xwikicontext")).put("message", e.getMessage());
    }
}
