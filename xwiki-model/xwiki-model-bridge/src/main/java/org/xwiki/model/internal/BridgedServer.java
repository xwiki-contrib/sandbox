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
package org.xwiki.model.internal;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.XWikiDocument;

import org.xwiki.model.Entity;
import org.xwiki.model.EntityIterator;
import org.xwiki.model.ModelException;
import org.xwiki.model.Server;
import org.xwiki.model.Wiki;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class BridgedServer implements Server
{
    private XWikiContext xcontext;

    public BridgedServer(XWikiContext xcontext)
    {
        this.xcontext = xcontext;
    }

    public Wiki addWiki(String wikiName)
    {
        throw new ModelException("Not supported");
    }

    public <T extends Entity> T getEntity(EntityReference reference)
    {
        T result = null;
        switch (reference.getType()) {
            case DOCUMENT:
                try {
                    // Since the old model API always return a XWikiDocument even if it doesn't exist, we need to check
                    // if the document is new or not.
                    XWikiDocument xdoc = getXWiki().getDocument(new DocumentReference(reference), getXWikiContext());
                    if (!xdoc.isNew()) {
                        result = (T) new BridgedDocument(xdoc);
                    }
                } catch (XWikiException e) {
                    throw new ModelException("Error loading document [" + reference + "]", e);
                }
                break;
            case SPACE:
                // A space exists if there's at least one document in it.
                try {
                    List<String> spaces = getXWiki().getSpaces(getXWikiContext());
                    if (spaces.contains(reference.getName())) {
                        result = (T) new BridgedSpace();
                    }
                } catch (XWikiException e) {
                    throw new ModelException("Error verifying existence of space [" + reference + "]", e);
                }
                break;
            case WIKI:
                // TODO: Need to load the wiki details. FTM only checking if it exists
                if (hasEntity(reference)) {
                    result = (T) new BridgedWiki(new WikiReference(reference), getXWikiContext());
                }
                break;
            default:
                throw new ModelException("Not supported");
        }

        return result;
    }

    public Wiki getWiki(String wikiName)
    {
        return getEntity(new WikiReference(wikiName));
    }

    public EntityIterator<Wiki> getWikis()
    {
        throw new ModelException("Not supported");
    }

    public boolean hasEntity(EntityReference reference)
    {
        boolean result;
        switch (reference.getType()) {
            case DOCUMENT:
                result = getXWiki().exists(new DocumentReference(reference), getXWikiContext());
                break;
            case WIKI:
                try {
                    result = getXWiki().getServerURL(new WikiReference(reference).getName(), getXWikiContext()) != null;
                } catch (MalformedURLException e) {
                    result = false;
                }
                break;
            default:
                throw new ModelException("Not supported");
        }
        return result;
    }

    public boolean hasWiki(String wikiName)
    {
        throw new ModelException("Not supported");
    }

    public void removeEntity(EntityReference reference)
    {
        throw new ModelException("Not supported");
    }

    public void removeWiki(String wikiName)
    {
        throw new ModelException("Not supported");
    }

    public void save(String comment, boolean isMinorEdit, Map<String, String> extraParameters)
    {
        throw new ModelException("Not supported");
    }

    public XWiki getXWiki()
    {
        return this.xcontext.getWiki();
    }

    public XWikiContext getXWikiContext()
    {
        return this.xcontext;
    }

    @Override
    public <T extends Entity> T addEntity(EntityReference reference)
    {
        throw new ModelException("Not supported");
    }
}
