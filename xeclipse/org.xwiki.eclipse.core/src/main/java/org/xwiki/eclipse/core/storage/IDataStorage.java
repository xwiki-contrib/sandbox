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
 *
 */
package org.xwiki.eclipse.core.storage;

import java.util.List;
import java.util.Map;

import org.codehaus.swizzle.confluence.SpaceSummary;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.xmlrpc.model.XWikiClass;
import org.xwiki.xmlrpc.model.XWikiClassSummary;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiObjectSummary;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwiki.xmlrpc.model.XWikiPageHistorySummary;
import org.xwiki.xmlrpc.model.XWikiPageSummary;

/**
 * An interface for an abstract XWiki data storage component.
 */
public interface IDataStorage
{
    public void dispose();

    public List<SpaceSummary> getSpaces() throws XWikiEclipseException;

    public SpaceSummary getSpaceSumary(String spaceKey) throws XWikiEclipseException;

    public void removeSpace(String spaceKey) throws XWikiEclipseException;

    public List<XWikiPageSummary> getPages(String spaceKey) throws XWikiEclipseException;

    public XWikiPageSummary getPageSummary(String pageId) throws XWikiEclipseException;

    public XWikiPage getPage(String pageId) throws XWikiEclipseException;

    public boolean removePage(String pageId) throws XWikiEclipseException;

    public List<XWikiObjectSummary> getObjects(String pageId) throws XWikiEclipseException;

    public XWikiObject getObject(String pageId, String className, int objectId) throws XWikiEclipseException;

    public List<XWikiClassSummary> getClasses() throws XWikiEclipseException;

    public XWikiClass getClass(String classId) throws XWikiEclipseException;

    public XWikiPage storePage(XWikiPage page) throws XWikiEclipseException;

    public XWikiObject storeObject(XWikiObject object) throws XWikiEclipseException;

    public boolean removeObject(String pageId, String className, int objectId) throws XWikiEclipseException;

    public void storeClass(XWikiClass xwikiClass) throws XWikiEclipseException;

    public boolean exists(String pageId);

    public boolean exists(String pageId, String className, int objectId);

    public List<XWikiPageHistorySummary> getPageHistory(String pageId) throws XWikiEclipseException;

    public List<XWikiPageSummary> getAllPageIds() throws XWikiEclipseException;

    public List<XWikiPageSummary> getConstrainedPageIds(final String linkPrefix, final Map parameters)
        throws XWikiEclipseException;
}
