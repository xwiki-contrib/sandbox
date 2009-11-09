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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.swizzle.confluence.Attachment;
import org.codehaus.swizzle.confluence.SearchResult;
import org.codehaus.swizzle.confluence.ServerInfo;
import org.codehaus.swizzle.confluence.Space;
import org.codehaus.swizzle.confluence.SpaceSummary;
import org.eclipse.core.runtime.Assert;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiClass;
import org.xwiki.xmlrpc.model.XWikiClassSummary;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiObjectSummary;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwiki.xmlrpc.model.XWikiPageHistorySummary;
import org.xwiki.xmlrpc.model.XWikiPageSummary;

/**
 * This class implements a remote XWiki data storage. Basically it wraps the XWiki XMLRPC interface.
 */
public class RemoteXWikiDataStorage implements IDataStorage
{
    private XWikiXmlRpcClient rpc;

    private boolean disposed;

    public RemoteXWikiDataStorage(String endpoint, String userName, String password) throws XWikiEclipseException
    {
        try {
            rpc = new XWikiXmlRpcClient(endpoint);
            rpc.login(userName, password);
            disposed = false;
        } catch (Exception e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized void dispose()
    {
        Assert.isTrue(!disposed);

        try {
            rpc.logout();
        } catch (XmlRpcException e) {
            // Ignore
        }

        disposed = true;
    }

    public synchronized XWikiPage getPage(String pageId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            XWikiPage page = rpc.getPage(pageId);

            /*
             * Adjust the page ID in order to embed the language in its id. This is necessary because the page returned
             * does not embed in its id extended information. So for example requesting a page Main.WebHome?language=fr
             * returns a page whose id is Main.WebHome and its language field is 'fr'
             */
            page.setId(pageId);

            return page;
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized List<XWikiPageSummary> getPages(String spaceKey) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getPages(spaceKey);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized List<XWikiPageSummary> getPages(String spaceKey, int start, int limit)
        throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            List<XWikiPageSummary> summaries = rpc.getPages(spaceKey, start, limit);
            return summaries;
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized XWikiPageSummary getPageSummaryAtIndex(String spaceKey, int index) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getPageSummaryAtIndex(spaceKey, index);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized List<SpaceSummary> getSpaces() throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getSpaces();
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized List<SpaceSummary> getSpaces(int start, int limit) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getSpaces(start, limit);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public SpaceSummary getSpaceSumary(String spaceKey) throws XWikiEclipseException
    {
        List<SpaceSummary> spaces = getSpaces();
        for (SpaceSummary space : spaces) {
            if (space.getKey().equals(spaceKey))
                return space;
        }

        return null;
    }

    public synchronized void removeSpace(String spaceKey) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            rpc.removeSpace(spaceKey);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized XWikiPage storePage(XWikiPage page) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            String originalPageId = page.getId();
            page = rpc.storePage(page);
            /*
             * Adjust the page ID in order to embed the language in its id. This is necessary because the page returned
             * does not embed in its id extended information. So for example requesting a page Main.WebHome?language=fr
             * returns a page whose id is Main.WebHome and its language field is 'fr'
             */
            page.setId(originalPageId);

            return page;
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized Space addSpace(Space space) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.addSpace(space);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized List<Attachment> getAttachments(String pageId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getAttachments(pageId);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized ServerInfo getServerInfo() throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getServerInfo();
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized boolean removePage(String pageId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.removePage(pageId);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }

    }

    public synchronized List<XWikiObjectSummary> getObjects(String pageId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getObjects(pageId);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized XWikiObject getObject(String pageId, String className, int objectId)
        throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getObject(pageId, className, objectId);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized XWikiClass getClass(String classId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getClass(classId);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public synchronized XWikiObject storeObject(XWikiObject object) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.storeObject(object);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }

    }

    public synchronized void storeClass(XWikiClass xwikiClass) throws XWikiEclipseException
    {
        // Do nothing
    }

    public synchronized boolean exists(String pageId)
    {
        try {
            XWikiPage page = getPage(pageId);
        } catch (XWikiEclipseException e) {
            return false;
        }

        return true;
    }

    public synchronized boolean existsSpace(String spaceKey)
    {
        try {
            SpaceSummary space = getSpaceSumary(spaceKey);
            if (space == null)
                return false;
        } catch (XWikiEclipseException e) {
            return false;
        }

        return true;
    }

    public synchronized boolean exists(String pageId, String className, int objectId)
    {
        try {
            XWikiObject object = getObject(pageId, className, objectId);
        } catch (XWikiEclipseException e) {
            return false;
        }

        return true;
    }

    public List<XWikiClassSummary> getClasses() throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getClasses();
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public boolean removeObject(String pageId, String className, int objectId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.removeObject(pageId, className, objectId);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public XWikiPageSummary getPageSummary(String pageId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        String[] pageIdComponents = pageId.split("\\."); //$NON-NLS-1$
        Assert.isTrue(pageIdComponents.length == 2);

        List<XWikiPageSummary> pageSummaries = getPages(pageIdComponents[0]);

        for (XWikiPageSummary pageSummary : pageSummaries) {
            if (pageSummary.getId().equals(pageId)) {
                return pageSummary;
            }
        }

        return null;
    }

    public List<XWikiPageHistorySummary> getPageHistory(String pageId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.getPageHistory(pageId);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public List<XWikiPageSummary> getAllPageIds() throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        List<XWikiPageSummary> result = new ArrayList<XWikiPageSummary>();

        try {
            List<SearchResult> searchResults = rpc.searchAllPagesIds();
            for (SearchResult searchResult : searchResults) {
                XWikiPageSummary pageSummary = new XWikiPageSummary();
                pageSummary.setId(searchResult.getId());
                pageSummary.setTitle(searchResult.getTitle());
                result.add(pageSummary);
            }
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }

        return result;
    }

    public List<XWikiPageSummary> getConstrainedPageIds(final String linkPrefix, final Map parameters)
        throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        List<XWikiPageSummary> result = new ArrayList<XWikiPageSummary>();

        try {
            // With patched xwiki-xmlrpc
            List<SearchResult> searchResults = rpc.search(linkPrefix, parameters, 0);
            for (SearchResult searchResult : searchResults) {
                XWikiPageSummary pageSummary = new XWikiPageSummary();
                pageSummary.setId(searchResult.getId());
                pageSummary.setTitle(searchResult.getTitle());
                result.add(pageSummary);
            }
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
        return result;
    }

    public Boolean removeAttachment(Attachment attachment) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.removeAttachment(attachment);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public Boolean moveAttachment(String oldpageId, String oldFileName, String newpageId, String newfileName)
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.moveAttachment(oldpageId, oldFileName, newpageId, newfileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Attachment addAttachment(Attachment attachment, byte[] data) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            // contentID ignored.
            return rpc.addAttachment(Integer.parseInt("1"), attachment, data);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        }
    }

    public Boolean copyAttachment(Attachment attachment, String newpageId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        return rpc.copyAttachment(attachment, newpageId);
    }

    public Boolean copyPage(String oldpageId, String newpageId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.copyPage(oldpageId, newpageId);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean renamePage(String oldpageId, String newpageId) throws XWikiEclipseException
    {
        Assert.isTrue(!disposed);

        try {
            return rpc.movePage(oldpageId, newpageId);
        } catch (XmlRpcException e) {
            throw new XWikiEclipseException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean copyAttachment(Attachment attachment, String newpageId, String newfileName)
    {
        Assert.isTrue(!disposed);

        return rpc.copyAttachment(attachment, newpageId, newfileName);
    }

    public Integer getNumberOfPages(String spaceKey) throws XmlRpcException
    {
        Assert.isTrue(!disposed);

        return rpc.getNumberOfPages(spaceKey);
    }

    public Integer getNumberOfSpaces() throws XmlRpcException
    {
        Assert.isTrue(!disposed);

        return rpc.getNumberOfSpaces();
    }

}
