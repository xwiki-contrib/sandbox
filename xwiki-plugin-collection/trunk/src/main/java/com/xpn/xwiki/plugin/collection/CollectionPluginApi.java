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
package com.xpn.xwiki.plugin.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.objects.classes.ListItem;

public class CollectionPluginApi extends Api
{
    private CollectionPlugin plugin;

    public CollectionPluginApi(CollectionPlugin plugin, XWikiContext context)
    {
        super(context);
        this.plugin = plugin;
    }

    public CollectionPlugin getPlugin()
    {
        if (hasProgrammingRights()) {
            return plugin;
        }
        return null;
    }

    /**
     * Exports a set of pages as a JAR
     *
     * @param packageName Name of the package to use for the export file name
     * @param selectList List of page names to export
     * @param withVersions Should versions be included in the export
     * @return false if failure, true if success. The upload will takeover the connection in case of success
     */
    public boolean exportToXAR(String packageName, List selectList, boolean withVersions)
    {
        try {
            plugin.exportToXAR(packageName, selectList, withVersions, context);
            return true;
        } catch (Exception e) {
            context.put("exception", e);
            return false;
        }
    }

    /**
     * Returns a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public String getRenderedContentWithLinks(String documentName)
    {
        return getRenderedContentWithLinks(documentName, null, false);
    }

    /**
     * Returns a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @param selectlist list of pages to include 
     * @return the transcluded result in xhtml syntax.
     */
    public String getRenderedContentWithLinks(String documentName, List<String> selectlist)
    {
        return getRenderedContentWithLinks(documentName, null, false);
    }

    /**
     * Returns a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @param selectlist list of pages to include 
     * @param ignoreInitialPage ignore initial page 
     * @return the transcluded result in xhtml syntax.
     */
    public String getRenderedContentWithLinks(String documentName, List<String> selectlist, boolean ignoreInitialPage)
    {
        try {
            if ((documentName == null) || hasAccessLevel("view", documentName)) {
                return plugin.getRenderedContentWithLinks(documentName, selectlist, ignoreInitialPage, context);
            } else {
                return null;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }
    }

    /**
     * Returns a pdf or rtf of a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public boolean exportWithLinks(String documentName, String type)
    {
        try {
            if ((documentName == null) || hasAccessLevel("view", documentName)) {
                plugin.exportWithLinks(documentName, type, context);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return false;
        }
    }

    /**
     * Returns a pdf or rtf of a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public boolean exportWithLinks(String documentName, List<String> selectlist, String type)
    {
        try {
            if ((documentName == null) || hasAccessLevel("view", documentName)) {
                plugin.exportWithLinks(documentName, selectlist,
                    type, context);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return false;
        }
    }

    /**
     * Returns a pdf or rtf of a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public boolean exportWithLinks(String packageName, String documentName, List<String> selectlist, String type)
    {
        try {
            if ((documentName == null) || hasAccessLevel("view", documentName)) {
                plugin.exportWithLinks(packageName, documentName,
                    selectlist, type, context);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return false;
        }
    }

   /**
     * Returns a pdf or rtf of a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public boolean exportWithLinks(String packageName, String documentName, List<String> selectlist, boolean ignoreInitialPage, String type)
    {
        try {
            if ((documentName == null) || hasAccessLevel("view", documentName)) {
                plugin.exportWithLinks(packageName, documentName,
                    selectlist, ignoreInitialPage, type, context);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return false;
        }
    }

    /**
     * Split a string to a List
     *
     * @return list
     */
    public List<String> split(String str, String splitstr)
    {
        return Arrays.asList(StringUtils.split(str, splitstr));
    }

    /**
     * escape content to be included in an JS file
     *
     * @return escaped content
     */
    public String escapeForJS(String origtext)
    {
        if (origtext == null) {
            return "";
        }
        String text = origtext.replaceAll("\\\\", "\\\\");
        text = text.replaceAll("'", "\\\\'");
        text = text.replaceAll("\"", "\\\\x22");
        return text;
    }

    /**
     * escape content to be included in an XML file
     *
     * @return escaped content
     */
    public String escapeForXML(String origtext)
    {
        if (origtext == null) {
            return "";
        }
        String text = origtext;
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(">", "&gt;");
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll("'", "&apos;");
        text = text.replaceAll("\"", "&quot;");
        return text;
    }

    /**
     * Returns the list of linked docs in the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return list of linked docs
     */
    public List<String> getLinks(String documentName)
    {
        try {
            if (hasAccessLevel("view", documentName)) {
                return plugin.getLinks(documentName, null, context);
            } else {
                return null;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }
    }

    /**
     * Returns the list of linked docs in the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @param space space to limit the search for 
     * @return list of linked docs
     */
    public List<String> getLinks(String documentName, String space)
    {
        try {
            if (hasAccessLevel("view", documentName)) {
                return plugin.getLinks(documentName, space, context);
            } else {
                return null;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }
    }

    /**
     * Get links for the given xdom
     */
    public List<ListItem> getLinksTreeList(String documentName)
    {
        try {
            if (hasAccessLevel("view", documentName)) {
                return plugin.getLinksTreeList(documentName, context);
            } else {
                return null;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }
    }

    /**
     * Get links for the given xdom
     */
    public List<ListItem> getLinksTreeList(String documentName, String space)
    {
        try {
            if (hasAccessLevel("view", documentName)) {
                return plugin.getLinksTreeList(documentName, space, context);
            } else {
                return null;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }
    }

    /**
     * Fix to the linked pages function in XWiki document which allows to call it from a different context than the current
     * document
     *
     * @return list of linked pages
     */
    public List<String> getLinkedPages(String documentName)
    {
        try {
            if (hasAccessLevel("view", documentName)) {
                return plugin.getLinkedPages(
                    context.getWiki().getDocument(documentName, context),
                    context);
            } else {
                return null;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }
    }

    /**
     * Retrieves the class names that represent collections from the preferences
     *
     * @return list of collections comma separated
     */
    public String getCollectionsClassName(XWikiContext context)
    {
        return plugin.getCollectionsClassName(context);
    }

    /**
     * Is a document representing a collection
     *
     * @param docName document to check
     * @return true if it represents a collection
     */
    public boolean isCollection(String docName)
    {
        return plugin.isCollection(docName, context);
    }

    /**
     * Is a document representing a collection of a specific className
     *
     * @param docName document to check
     * @param className class name to detect if it is a collection
     * @return true if it represents a collection
     */
    public boolean isCollection(String docName, String className)
    {
        return plugin.isCollection(docName, className, context);
    }

    /**
     * List collections in which the document is present
     *
     * @param docName document to check collections for
     * @return list of documents that are collections
     */
    public List<String> getCollections(String docName)
    {
        return plugin.getCollections(docName, context);
    }

    /**
     * List collections in which the document is present
     *
     * @param docName document to check collections for
     * @param pageList pageList to exclude
     * @return list of documents that are collections
     */
    public List<String> getCollections(String docName, ArrayList<String> pageList)
    {
        return plugin.getCollections(docName, pageList, context);
    }

    /**
     * Retrieves the collections in which the document docName is present and include the path to this collection with
     * the document.
     *
     * @param docName document to search collections for
     * @return map of pages representing collections and the path as the map values
     */
    public Map<String, String> getCollectionsWithPath(String docName)
    {
        return plugin.getCollectionsWithPath(docName, context);
    }

    /**
     * Retrieves the collections in which the document docName is present and include the path to this collection with
     * the document.
     *
     * @param docName document to search collections for
     * @param pageList list of pages already traversed to avoid infinite loops
     * @return map of pages representing collections and the path as the map values
     */
    public Map<String, String> getCollectionsWithPath(String docName, String path, ArrayList<String> pageList)
    {
        return plugin.getCollectionsWithPath(docName, path, pageList, context);
    }

    /**
     * List collections in which the document is present and limited to collections of a specific class
     *
     * @param docName document to check collections for
     * @param className class name to detect if it is a collection
     * @return list of documents that are collections
     */
    public List<String> getCollections(String docName, String className)
    {
        return plugin.getCollections(docName, className, context);
    }

    /**
     * List collections in which the document is present and limited to collections of a specific class
     *
     * @param docName document to check collections for
     * @param className class name to detect if it is a collection
     * @param pageList pageList to exclude
     * @return list of documents that are collections
     */
    public List<String> getCollections(String docName, String className, ArrayList<String> pageList)
    {
        return plugin.getCollections(docName, className, pageList, context);
    }

    /**
     * Retrieves the collections in which the document docName is present and include the path to this collection with
     * the document and limited to collections of a specific class.
     *
     * @param docName document to search collections for
     * @param className class name to detect if it is a collection
     * @return map of pages representing collections and the path as the map values
     */
    public Map<String, String> getCollectionsWithPath(String docName, String className)
    {
        return plugin.getCollectionsWithPath(docName, className, context);
    }

    /**
     * Retrieves the collections in which the document docName is present and include the path to this collection with
     * the document and limited to collections of a specific class.
     *
     * @param docName document to search collections for
     * @param className class name to detect if it is a collection
     * @param pageList list of pages already traversed to avoid infinite loops
     * @return map of pages representing collections and the path as the map values
     */
    public Map<String, String> getCollectionsWithPath(String docName, String className, String path,
        ArrayList<String> pageList)
    {
        return plugin.getCollectionsWithPath(docName, className, path, pageList, context);
    }

    /**
     * Retrieve the breadcrumb path by looking up parents
     *
     * @param docName page from which to start the breadcrumb
     */
    public List<String> getBreadcrumb(String docName)
    {
        return plugin.getBreadcrumb(docName, context);
    }

    /**
     * Retrieves the breadcrumb path for a document first by looking at the request.bc param then by looking up parents
     *
     * @param docName page from which to start the breadcrumb
     */
    public List<String> getBreadcrumbFromParents(String docName)
    {
        return plugin.getBreadcrumbFromParents(docName, new ArrayList<String>(), context);
    }
}
