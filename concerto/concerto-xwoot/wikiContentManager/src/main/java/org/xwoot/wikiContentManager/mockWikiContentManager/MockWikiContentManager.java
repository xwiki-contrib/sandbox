/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.wikiContentManager.mockWikiContentManager;

import org.w3c.dom.Document;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.XWikiSwizzleClient.XwikiSwizzleClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MockWikiContentManager implements WikiContentManager
{
    private final static String SEPARATECHAR = ".";

    private Map<String, Map> pages;

    private static final int VOID_CHARACTER = 67;

    /**
     * Creates a new MockXWiki object.
     * 
     * @param path DOCUMENT ME!
     */
    public MockWikiContentManager()
    {
        this.pages = new Hashtable<String, Map>();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception DOCUMENT ME!
     * @throws PageNotFoundException DOCUMENT ME!
     */
    public void overwritePageContent(String pageId, String value)
    {
        Map p = this.pages.get(pageId);
        if (p == null) {
            this.createPage(pageId, value);
            return;
        }
        p.put(WikiContentManager.CONTENT, value);
        this.pages.put(pageId, p);
    }

//    /**
//     * DOCUMENT ME!
//     * 
//     * @param content DOCUMENT ME!
//     * @param created DOCUMENT ME!
//     * @param creator DOCUMENT ME!
//     * @param id DOCUMENT ME!
//     * @param pageId DOCUMENT ME!
//     * @param title DOCUMENT ME!
//     * @param url DOCUMENT ME!
//     * @return DOCUMENT ME!
//     * @throws Exception DOCUMENT ME!
//     */
//    public Map createComment(String content, Date created, String creator, String id, String pageId, String title,
//        String url)
//    {
//        return null;
//    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Map<String, String> createPage(String pageId, String content)
    {

        // create the new Page
        Map newPage = new Hashtable();
        newPage.put(WikiContentManager.SPACE, this.getSpaceNameWithPageId(pageId));
        newPage.put(WikiContentManager.ID, pageId);
        newPage.put(WikiContentManager.CONTENT, content);
        newPage.put(WikiContentManager.TITLE, this.getPageNameWithPageId(pageId));

        this.pages.put(pageId, newPage);

        return newPage;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param spaceKey DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception DOCUMENT ME!
     */
    public void createSpace(String spaceKey)
    {

        this.createPage(spaceKey + ".WebHome", "");

    }

//    /**
//     * DOCUMENT ME!
//     * 
//     * @param pageId DOCUMENT ME!
//     * @param commentId DOCUMENT ME!
//     * @return DOCUMENT ME!
//     * @throws SwizzleException
//     * @throws Exception DOCUMENT ME!
//     */
//    public Map getComment(String pageId, String commentId)
//    {
//        return null;
//
//    }

//    /**
//     * DOCUMENT ME!
//     * 
//     * @param pageId DOCUMENT ME!
//     * @return DOCUMENT ME!
//     * @throws SwizzleException
//     * @throws Exception DOCUMENT ME!
//     */
//    public List<Map> getComments(String pageId)
//    {
//        return null;
//    }

    private byte[] getDigest(String p, String algo) throws NoSuchAlgorithmException
    {
        String page = "";

        if (p != null) {
            page = p;
        }

        MessageDigest md = MessageDigest.getInstance(algo);
        byte[] b = page.getBytes();
        md.update(b);

        return md.digest();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception DOCUMENT ME!
     */
    public Map<String, String> getFields(String pageId)
    {
        return this.pages.get(pageId);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param space DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception DOCUMENT ME!
     */
    public Collection getListPageId(String space)
    {
        List<String> result = new ArrayList<String>();
        Collection c = this.pages.entrySet();
        Iterator i = c.iterator();

        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            String s = (String) ((Map) e.getValue()).get(WikiContentManager.SPACE);

            if ((space.equals(s))) {
                result.add((String) ((Map) e.getValue()).get(WikiContentManager.ID));
            }
        }
        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception DOCUMENT ME!
     */
    public Collection getListSpaceId()
    {

        List<String> result = new ArrayList<String>();
        Collection c = this.pages.entrySet();
        Iterator i = c.iterator();

        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            String space = (String) ((Map) e.getValue()).get(WikiContentManager.SPACE);

            if ((space != null) && (!space.equals("")) && !result.contains(space)) {
                result.add(space);
            }
        }
        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception DOCUMENT ME!
     */
    public String getPageContent(String pageId)
    {
        Map result = this.pages.get(pageId);

        if (result == null) {
            return null;
        }

        return (String) result.get(WikiContentManager.CONTENT);
    }

    private String getPageNameWithPageId(String pageId)
    {
        int l = pageId.lastIndexOf(SEPARATECHAR);

        if (l == -1) {
            return pageId;
        }

        return pageId.substring(l + SEPARATECHAR.length(), pageId.length());
    }

    private String getSpaceNameWithPageId(String pageName)
    {
        int l = pageName.lastIndexOf(SEPARATECHAR);

        if (l == -1) {
            return "";
        }

        return pageName.substring(0, l);
    }

    public String getWikiURL()
    {
        return null;
    }

//    /**
//     * DOCUMENT ME!
//     * 
//     * @param pageId DOCUMENT ME!
//     * @param comments DOCUMENT ME!
//     * @throws SwizzleException
//     * @throws Exception DOCUMENT ME!
//     */
//    public void overWriteComments(String pageId, List<Map> comments)
//    {
//        return;
//    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception DOCUMENT ME!
     */
    public boolean removePage(String pageId)
    {

        return this.pages.remove(pageId) != null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param spaceKey DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception DOCUMENT ME!
     */
    public void removeSpace(String spaceKey)
    {
        Iterator i = this.pages.entrySet().iterator();
        String s = "";
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            Map m = (Map) e.getValue();
            if (m != null)
                s = (String) m.get(WikiContentManager.SPACE);
            if ((s != null) && s.equals(spaceKey)) {
                i.remove();
               // this.pages.remove(e.getKey());
            }
        }
    }

//    // little hack :
//    /**
//     * DOCUMENT ME!
//     * 
//     * @param pageId DOCUMENT ME!
//     * @param comment DOCUMENT ME!
//     * @return DOCUMENT ME!
//     * @throws SwizzleException
//     * @throws Exception DOCUMENT ME!
//     */
//    public Map setComment(String pageId, Map comment)
//    {
//
//        return null;
//
//    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param fields DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception
     * @throws Exception DOCUMENT ME!
     * @throws ClassCastException DOCUMENT ME!
     */
    public Map<String, String> setFields(String pageId, Map<String, String> fields)
    {
        this.pages.put(pageId, new Hashtable(fields));
        return this.pages.get(pageId);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param algo DOCUMENT ME!
     * @param rmd DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws NoSuchAlgorithmException
     */
    public String setPageContent(String pageId, String value, String algo, byte[] rmd) throws NoSuchAlgorithmException
    {
        String result = null;
        Map page = null;

        page = this.pages.get(pageId);

        if (page == null) {
            this.createPage(pageId, "");
            page = this.pages.get(pageId);
        }

        byte[] messageDigest = this.getDigest((String) page.get(WikiContentManager.CONTENT), algo);

        if (MessageDigest.isEqual(messageDigest, rmd)) {
            page.put(WikiContentManager.CONTENT, value);
            this.pages.put(pageId, page);
        } else {
            String resultS = (String) page.get(WikiContentManager.CONTENT);

            if ((resultS == null) || ((resultS.length() == 1) && (resultS.codePointAt(0) == VOID_CHARACTER))
                || (resultS.length() < 1)) {
                result = "";
            } else {
                result = resultS;
            }
        }

        return result;
    }

    public void login() throws WikiContentManagerException
    {
        return;

    }

    public void logout() throws WikiContentManagerException
    {
        return;

    }

    public boolean existPage(String pageKey) throws WikiContentManagerException
    {
        if (this.pages==null){
            return false;
        }
        return this.pages.containsKey(pageKey);
    }

    public Map<String, String> fromXml(Document pageXml) throws WikiContentManagerException
    {
        return XwikiSwizzleClient.fromXmlStatic(pageXml);
    }

    public Document toXml(String pageId, String href, Map<String, String> pageMap) throws WikiContentManagerException
    {
        return XwikiSwizzleClient.toXmlStatic(pageId, href, pageMap);
        
    }
    
    public Document PageListToXml(String pagesHRef,List<String> list) throws WikiContentManagerException{
        return XwikiSwizzleClient.PageListToXmlStatic(pagesHRef, list);
    }
    
    public List<String> PageListFromXml(Document doc){
        return XwikiSwizzleClient.PageListFromXmlStatic(doc);
    }

  
    public String renderContent(String pageId) throws WikiContentManagerException
    {
        return this.getPageContent(pageId);
    }
//
//    public boolean removeComment() 
//    {
//        return true;
//    }
//
//    public boolean addTag() 
//    {
//        return false;
//    }
//
//    public boolean overwriteTags()
//    {
//        return false;
//    }
//
//    public Map<Integer, List<String>> getTags()
//    {
//        return null;
//    }
}
