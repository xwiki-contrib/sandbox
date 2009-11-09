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

package org.xwiki.xoo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.swizzle.confluence.PageSummary;
import org.codehaus.swizzle.confluence.SpaceSummary;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiPageSummary;
import org.xwiki.xoo.xwiki.PageNode;
import org.xwiki.xoo.xwiki.SpaceNode;
import org.xwiki.xoo.xwiki.XWikiStructure;

/**
 * A class that implements the actions over the structure of the XWiki server.
 * 
 * @version $Id$
 * @since 1.0 M
 */

public class XWikiStructureActions
{

    /*
     * Retrieves the structure of the XWiki server using Rpc Client.
     * @return the structure of the XWiki server
     */
    public static XWikiStructure getXWikiStructure(XWikiXmlRpcClient client)
    {
        XWikiStructure ret = new XWikiStructure();
        ret.spaces = new ArrayList<SpaceNode>();
        try {
            List<SpaceSummary> spaces = client.getSpaces();
            for (SpaceSummary space : spaces) {
                SpaceNode spaceNode = new SpaceNode();
                spaceNode.spaceSummary = space;
                spaceNode.pages = new ArrayList<PageNode>();
                List<XWikiPageSummary> pages = client.getPages(space.getKey());
                for (XWikiPageSummary page : pages) {
                    PageNode pageNode = new PageNode();
                    pageNode.pageSummary = page;
                    pageNode.attachments = client.getAttachments(page);
                    spaceNode.pages.add(pageNode);
                }
                ret.spaces.add(spaceNode);
            }

        } catch (XmlRpcException e) {
            // TODO handle error - client not connected
            e.printStackTrace();
            return null;
        }
        return ret;
    }

    /**
     * Gets all the spaces from the XWik Structure.
     * 
     * @param xwikiStructure the input xwiki structure
     * @return a list with all the spaces
     */
    public static List<SpaceSummary> getSpaces(XWikiStructure xwikiStructure)
    {
        List<SpaceSummary> spaces = new ArrayList<SpaceSummary>();
        if (xwikiStructure == null)
            return spaces;
        int n = xwikiStructure.spaces.size();
        for (int i = 0; i < n; i++)
            spaces.add(xwikiStructure.spaces.get(i).spaceSummary);
        return spaces;
    }

    /**
     * Gets all the spaces from the XWik Client.
     * 
     * @param client the XMLRPC client
     * @return a list with all the spaces
     */
    public static List<SpaceSummary> getSpaces(XWikiXmlRpcClient client)
    {
        List<SpaceSummary> ret = null;
        try {
            ret = client.getSpaces();
        } catch (XmlRpcException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Updates or adds a spaceNode and its children in an XWikiStructure object. This method doesn't modify any data on
     * the XWiki server.
     * 
     * @param client XMLRPC client
     * @param xWikStructure the XWiki Structure which will be modified
     * @param spaceId the id of the space that will be added or updated in the XWikiStructure
     */
    public static void refreshSpaceInXWikiStructure(XWikiXmlRpcClient client, XWikiStructure xWikStructure,
        String spaceId)
    {
        try {
            SpaceSummary space = client.getSpace(spaceId);
            SpaceNode spaceNode = new SpaceNode();
            spaceNode.spaceSummary = space;
            spaceNode.pages = new ArrayList<PageNode>();
            List<XWikiPageSummary> pages = client.getPages(space.getKey());
            for (XWikiPageSummary page : pages) {
                PageNode pageNode = new PageNode();
                pageNode.pageSummary = page;
                pageNode.attachments = client.getAttachments(page);
                spaceNode.pages.add(pageNode);
            }
            int index = Collections.binarySearch(xWikStructure.spaces, spaceNode);
            if (index < 0) {
                xWikStructure.spaces.add(-index - 1, spaceNode);
            } else {
                xWikStructure.spaces.set(index, spaceNode);
            }

        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
    }

    
    public static void refreshPageAttInXWikiStructure(XWikiXmlRpcClient client, XWikiStructure xWikStructure,
        PageSummary page)
    {
        try {
            String spaceId = page.getSpace();
            for (SpaceNode space : xWikStructure.spaces)
            {
                if (space.spaceSummary.getKey().equals(spaceId)){
                    for (PageNode pageNode : space.pages){
                        if (pageNode.pageSummary == page){
                            pageNode.attachments = client.getAttachments(page);
                        }
                    }
                }
            }                   
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
    }

}
