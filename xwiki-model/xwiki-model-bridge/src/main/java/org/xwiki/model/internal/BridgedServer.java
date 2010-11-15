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
import org.xwiki.model.Entity;
import org.xwiki.model.EntityNotFoundException;
import org.xwiki.model.Server;
import org.xwiki.model.Wiki;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

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
        throw new RuntimeException("Not supported");
    }

    public <T extends Entity> T getEntity(EntityReference reference) throws EntityNotFoundException
    {
        T result;
        switch (reference.getType()) {
            case DOCUMENT:
                try {
                    result = (T) new BridgedDocument(getXWiki().getDocument(
                        new DocumentReference(reference), getXWikiContext()));
                } catch (XWikiException e) {
                    throw new EntityNotFoundException("Couldn't locate Document from reference [" + reference + "]", e);
                }
                break;
            case WIKI:
                result = (T) new BridgedWiki(new WikiReference(reference), getXWikiContext());
                break;
            default:
                throw new RuntimeException("Not supported");
        }

        return result;
    }

    public Wiki getWiki(String wikiName)
    {
        throw new RuntimeException("Not supported");
    }

    public List<Wiki> getWikis()
    {
        throw new RuntimeException("Not supported");
    }

    public boolean hasEntity(EntityReference reference)
    {
        throw new RuntimeException("Not supported");
    }

    public boolean hasWiki(String wikiName)
    {
        throw new RuntimeException("Not supported");
    }

    public void removeEntity(EntityReference reference)
    {
        throw new RuntimeException("Not supported");
    }

    public void removeWiki(String wikiName)
    {
        throw new RuntimeException("Not supported");
    }

    public void save(String comment, boolean isMinorEdit, Map<String, String> extraParameters)
    {
        throw new RuntimeException("Not supported");
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
