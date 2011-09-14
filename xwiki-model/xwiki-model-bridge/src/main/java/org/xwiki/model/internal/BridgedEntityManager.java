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

import java.net.MalformedURLException;
import java.util.List;

import org.xwiki.model.Entity;
import org.xwiki.model.EntityManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.ModelException;
import org.xwiki.model.UniqueReference;
import org.xwiki.model.Version;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class BridgedEntityManager implements EntityManager
{
    private XWikiContext xcontext;

    public BridgedEntityManager(XWikiContext xcontext)
    {
        this.xcontext = xcontext;
    }

    @Override
    public <T extends Entity> T getEntity(UniqueReference uniqueReference)
    {
        T result = null;
        EntityReference reference = uniqueReference.getReference();
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
                if (hasEntity(uniqueReference)) {
                    result = (T) new BridgedWiki(getXWikiContext());
                }
                break;
            default:
                throw new ModelException("Not supported");
        }

        return result;
    }

    @Override
    public boolean hasEntity(UniqueReference uniqueReference)
    {
        boolean result;
        EntityReference reference = uniqueReference.getReference();
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

    @Override
    public <T extends Entity> T addEntity(UniqueReference uniqueReference)
    {
        Entity result;

        EntityReference reference = uniqueReference.getReference();
        if (reference.getType().equals(EntityType.WIKI)) {
            result = new BridgedWiki(getXWikiContext());

        } else {
            throw new ModelException("Not supported");
        }

        return (T) result;
    }

    @Override
    public void removeEntity(UniqueReference uniqueReference)
    {
        throw new ModelException("Not supported");
    }

    @Override
    public void rollback(Version versionToRollbackTo)
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
}