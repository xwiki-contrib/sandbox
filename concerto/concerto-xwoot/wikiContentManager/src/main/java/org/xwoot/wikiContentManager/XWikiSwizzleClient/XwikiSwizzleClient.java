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

package org.xwoot.wikiContentManager.XWikiSwizzleClient;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.swizzle.confluence.Attachment;
import org.codehaus.swizzle.confluence.Page;
import org.codehaus.swizzle.confluence.PageSummary;
import org.codehaus.swizzle.confluence.Space;
import org.codehaus.swizzle.confluence.SpaceSummary;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiClassSummary;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiObjectSummary;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XwikiSwizzleClient implements WikiContentManager
{
    private Properties properties;

    private XWikiXmlRpcClient client;

    private static final String SEPARATECHAR = ".";

    private static final String XWIKI_ENDPOINT = "xwiki_endpoint";
    
    private static final String XWIKI_USERNAME="xwiki_username";
    
    private static final String XWIKI_PASSWORD="xwiki_password";
    
    private static final int VOID_CHARACTER = 67;

    private boolean isPermanentlyConnected;

    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * Creates a new XwikiSwizzleClient object.
     * 
     * @param path DOCUMENT ME!
     * @throws XWikiSwizzleClientException 
     */
    public XwikiSwizzleClient(String path) throws XWikiSwizzleClientException
    {
        this.loadProperties(path);
    }

    public XwikiSwizzleClient(String url, String login, String pwd)
    {
        this.loadProperties(url, login, pwd);
    }

    private void loadProperties(String path) throws XWikiSwizzleClientException
    {
        if (this.properties == null) {
            this.properties = new Properties();

            try {
                File file = new File(path);
                this.properties.load(new FileInputStream(file)); 
                System.out.println(this.properties);
            } catch (IOException e) {
                this.logger.error(e);
                throw new XWikiSwizzleClientException(e);
            }
        } else {
            return;
        }

    }

    private void loadProperties(String server, String login, String pwd)
    {
        if (this.properties == null) {
            this.properties = new Properties();
        }
        this.properties.put(XWIKI_ENDPOINT, server);
        this.properties.put(XWIKI_USERNAME, login);
        this.properties.put(XWIKI_PASSWORD, pwd);

    }

    synchronized private boolean relogin() throws XWikiSwizzleClientException
    {
        if (this.getClient() != null) {
            return true;
        }
        this.relogin(this.properties.getProperty(XWIKI_USERNAME), this.properties.getProperty(XWIKI_PASSWORD));
        return false;
    }

    synchronized private boolean relogin(String username, String password) throws XWikiSwizzleClientException
    {
        if (this.getClient() != null) {
            return true;
        }

        try{
            this.client = new XWikiXmlRpcClient(this.properties.getProperty(XWIKI_ENDPOINT));   
        } catch (MalformedURLException e) {
            this.logger.warn("XWOOT : WARNING ! Malformed URL : " + this.properties.getProperty(XWIKI_ENDPOINT));
            throw new XWikiSwizzleClientException(e+this.properties.getProperty(XWIKI_ENDPOINT)+" ; "+XWIKI_ENDPOINT+" ; "+this.properties); 
        }
        try {
            this.getClient().login(username, password);
        } catch (XmlRpcException e) {
            this.logger.error(e+" -- xwiki endpoint : "+this.properties.getProperty(XWIKI_ENDPOINT)+" -- username : "+username);
            throw new XWikiSwizzleClientException(e);
        }
        return false;
    }

    synchronized public void login() throws XWikiSwizzleClientException
    {
        this.relogin();
    }

    synchronized public void logout() throws XWikiSwizzleClientException
    {
        this.logout(false);
    }

    synchronized private void logout(boolean b) throws XWikiSwizzleClientException
    {        
        if (b || this.getClient() == null || this.isPermanentlyConnected) {
            return;
        }
        try {
            this.getClient().logout();
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        }
        this.client = null;
    }

    private boolean existSpace(String space) throws XWikiSwizzleClientException
    {
        boolean b = this.relogin();
        boolean result = false;
        try {
            List spaces = this.getClient().getSpaces();
            if (spaces != null) {
                Iterator i = spaces.iterator();
                while (i.hasNext() && !result) {
                    result = space.equals(((SpaceSummary) i.next()).getKey());
                }
            }
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        } 
        this.logout(b);
        return result;

    }

    public boolean existPage(String pageId) throws XWikiSwizzleClientException
    {
        boolean result = false;
        boolean b = this.relogin();
        String space = this.getSpaceNameWithPageId(pageId);
        try {
            List pages = this.getClient().getPages(space);
            if (pages != null) {
                Iterator i = pages.iterator();
                while (i.hasNext() && !result) {
                    result = pageId.equals(((PageSummary) i.next()).getId());
                }
            }
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        } 
        this.logout(b);
        return result;
    }

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

    private Page getWikiPage(String pageId) throws XWikiSwizzleClientException
    {
        // if user have not connected client, method do it for him
        // else it's to the user to do the connection gestion...
        if (!this.existPage(pageId)) {
            return null;
        }
        boolean b = this.relogin();

        Page page = null;

        try {
            page = this.getClient().getPage(pageId);
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        }

        this.logout(b);

        return page;
    }

    private Page storeWikiPage(Page page) throws XWikiSwizzleClientException
    {
        // if user have not connected client, method do it for him
        // else it's to the user to do the connection gestion...
        boolean b = this.relogin();
        Page result = null;
        try {
            result = this.getClient().storePage(page);
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        } 
        this.logout(b);

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws XWikiSwizzleClientException
     */
    public void disconnect() throws XWikiSwizzleClientException
    {
        this.logout(false);
        this.client = null;
        this.isPermanentlyConnected = false;
    }

    public void connect() throws XWikiSwizzleClientException
    {
        this.isPermanentlyConnected = true;
        this.relogin();
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
//     */
//    public Map createComment(String content, Date created, String creator, String id, String pageId, String title,
//        String url)
//    {
//        Comment com = new Comment(new Hashtable());
//        com.setContent(content);
//
//        if (created != null) {
//            com.setCreated(created);
//        }
//
//        com.setCreator(creator);
//        com.setId(id);
//        com.setPageId(pageId);
//        com.setTitle(title);
//        com.setUrl(url);
//
//        Map result = com.toMap();
//
//        return result;
//    }

    /**
     * DOCUMENT ME!
     * 
     * @param spaceKey DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    public void createSpace(String spaceKey) throws XWikiSwizzleClientException
    {
        if (this.existSpace(spaceKey)) {
            return;
        }
        boolean b = this.relogin();

        Space newSpace = new Space(new Hashtable());
        newSpace.setName(spaceKey);
        newSpace.setKey(spaceKey);

        try {
            this.getClient().addSpace(newSpace);
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        } 
        // this.createPage(spaceKey + ".WebHome","");

        this.logout(b);
    }

    // /**
    // * DOCUMENT ME!
    // *
    // * @param pageId
    // * DOCUMENT ME!
    // * @param commentId
    // * DOCUMENT ME!
    // *
    // * @return DOCUMENT ME!
    // * @throws XWikiSwizzleClientException
    // */
    // public Map getComment(String pageId, String commentId)
    // throws XWikiSwizzleClientException {
    // this.login();
    //
    // Map result = null;
    // try {
    // List l = this.getClient().getComments(pageId);
    // if (l != null) {
    // Iterator i = l.iterator();
    //
    // while (i.hasNext() && result==null) {
    // Comment com = (Comment) i.next();
    //
    // if (com.getId()!=null && com.getId().compareTo(commentId) == 0) {
    // result=com.toMap();
    // }
    // }
    // }
    // } catch (ConfluenceException e) {
    // this.logger.error("Problem with getComment (Confluence)"+e.getMessage());
    // } catch (SwizzleException e) {
    // this.logger.error("Problem with getComment (Swizzle)"+e.getMessage());
    // }
    //
    // this.logout();
    //
    // return result;
    //
    // }

//    /**
//     * DOCUMENT ME!
//     * 
//     * @param pageId DOCUMENT ME!
//     * @return DOCUMENT ME!
//     * @throws XWikiSwizzleClientException
//     */
//    public List<Map> getComments(String pageId) throws XWikiSwizzleClientException
//    {
//        if (!this.existPage(pageId)) {
//            return null;
//        }
//        boolean b = this.relogin();
//
//        List<Map> result = new ArrayList<Map>();
//
//        try {
//            List l = this.getClient().getComments(pageId);
//            if (l != null) {
//                Iterator i = l.iterator();
//                while (i.hasNext()) {
//                    Comment com = (Comment) i.next();
//                    if (com != null) {
//                        result.add(com.toMap());
//                    }
//                }
//            }
//        } catch (XmlRpcException e) {
//            this.logger.error(e);
//            throw new XWikiSwizzleClientException(e);
//        } 
//
//        this.logout(b);
//        return result;
//    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    synchronized public Map<String, String> getFields(String pageId) throws XWikiSwizzleClientException
    {
        if (!this.existPage(pageId)) {
            return null;
        }
        boolean b = this.relogin();
        Map<String, String> result = null;

        Page page = this.getWikiPage(pageId);
        if (page != null) {
            result = page.toMap();
        }

        this.logout(b);
        return result;
    }
    
    synchronized public String renderContent(String pageId) throws XWikiSwizzleClientException{
        String result=null;
        Map<String, String> page = this.getFields(pageId);
        if (page!=null){
            boolean b = this.relogin();
            try {
                result=this.client.renderContent(page.get(WikiContentManager.SPACE),page.get(WikiContentManager.ID),page.get(WikiContentManager.CONTENT));
            } catch (XmlRpcException e) {
                this.logger.error(e);
                throw new XWikiSwizzleClientException(e);
            }   
            this.logout(b);
        }
        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param space DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    public Collection getListPageId(String space) throws XWikiSwizzleClientException
    {
        if (!this.existSpace(space)) {
            return null;
        }

        boolean b = this.relogin();

        List<String> result = new ArrayList<String>();

        try {
            Iterator i;
            List l = this.getClient().getPages(space);
            if (l != null) {
                i = l.iterator();
                while (i.hasNext()) {
                    PageSummary ps = (PageSummary) i.next();
                    if (ps != null) {
                        String page = ps.getId();
                        if ((page != null) && !page.equals("")) {
                            result.add(page);
                        }
                    }
                }
            }
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        } 

        this.logout(b);
        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    public Collection getListSpaceId() throws XWikiSwizzleClientException
    {
        boolean b = this.relogin();

        List<String> result = new ArrayList<String>();
        try {
            List l = this.getClient().getSpaces();
            if (l != null) {
                Iterator i = l.iterator();
                while (i.hasNext()) {
                    SpaceSummary ssum = (SpaceSummary) i.next();
                    if (ssum != null) {
                        String space = ssum.getKey();
                        if ((space != null) && (!space.equals(""))) {
                            result.add(space);
                        }
                    }
                }
            }
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        } 
        this.logout(b);

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    synchronized public String getPageContent(String pageId) throws XWikiSwizzleClientException
    {
        Page result = this.getWikiPage(pageId);

        if (result == null) {
            return null;
        }
        return result.getContent();
    }

    public String getWikiURL()
    {
        return this.properties.getProperty(XWIKI_ENDPOINT).replaceAll("/xmlrpc", "");
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isConnected()
    {
        return this.getClient() != null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    synchronized public Map<String, String> createPage(String pageId, String content)
    throws XWikiSwizzleClientException
    {
        if (this.existPage(pageId)) {
            return null;
        }
        boolean b = this.relogin();

        // create the new Page
        Page newPage = new Page(new Hashtable());
        newPage.setSpace(this.getSpaceNameWithPageId(pageId));
        newPage.setTitle(this.getPageNameWithPageId(pageId));
        newPage.setContent(content);

        Page p = this.storeWikiPage(newPage);
        this.logout(b);
        Map result = null;
        if (p != null) {
            result = p.toMap();
        }
        return result;

    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    public void overwritePageContent(String pageId, String value) throws XWikiSwizzleClientException
    {
        String space = this.getSpaceNameWithPageId(pageId);
        if (!this.existSpace(space)) {
            this.createSpace(space);
        }

        Page p = null;
        boolean b = this.relogin();
        if (this.existPage(pageId)) {
            try {
                p = this.getClient().getPage(pageId);
                p.setContent(value);
            } catch (XmlRpcException e) {
                this.logger.error(e);
                throw new XWikiSwizzleClientException(e);
            } 
            this.storeWikiPage(p);
        } else {
            this.createPage(pageId, value);
        }
        this.logout(b);

    }

//    /**
//     * DOCUMENT ME!
//     * 
//     * @param pageId DOCUMENT ME!
//     * @param comments DOCUMENT ME!
//     * @throws XWikiSwizzleClientException
//     */
//    public void overWriteComments(String pageId, List<Map> comments) throws XWikiSwizzleClientException
//    {
//        if (!this.existPage(pageId)) {
//            this.createPage(pageId, "");
//        }
//        boolean b = this.relogin();
//        try {
//            List c = this.getClient().getComments(pageId);
//            if ((c != null) && !c.isEmpty()) {
//                Iterator i = c.iterator();
//
//                while (i.hasNext()) {
//                    Comment com = (Comment) i.next();
//                    if (com != null) {
//                        this.getClient().removeComment(com.getId());
//                    }
//                }
//            }
//            for (int j = 0; j < comments.size(); j++) {
//                this.getClient().addComment(new Comment(comments.get(j)));
//            }
//        } catch (XmlRpcException e) {
//            this.logger.error(e);
//            throw new XWikiSwizzleClientException(e);
//        } 
//        this.logout(b);
//
//    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    synchronized public boolean removePage(String pageId) throws XWikiSwizzleClientException
    {
        // if user have not connected client, method do it for him
        // else it's to the user to do the connection gestion...

        if (!this.existPage(pageId)) {
            return false;
        }
        boolean b = this.relogin();

        try {
            this.getClient().removePage(pageId);
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        }
        this.logout(b);

        return true;

    }

    /**
     * DOCUMENT ME!
     * 
     * @param spaceKey DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    public void removeSpace(String spaceKey) throws XWikiSwizzleClientException
    {
        if (!this.existSpace(spaceKey)) {
            return;
        }
        boolean b = this.relogin();
        try {
            this.getClient().removeSpace(spaceKey);
        } catch (XmlRpcException e) {
            this.logger.error(e);
            throw new XWikiSwizzleClientException(e);
        }

        this.logout(b);
    }

//    /**
//     * DOCUMENT ME!
//     * 
//     * @param pageId DOCUMENT ME!
//     * @param comment DOCUMENT ME!
//     * @return DOCUMENT ME!
//     * @throws XWikiSwizzleClientException
//     */
//    public Map setComment(String pageId, Map comment) throws XWikiSwizzleClientException
//    {
//        if (!this.existPage(pageId)) {
//            return null;
//        }
//        boolean b = this.relogin();
//
//        Map result = null;
//        try {
//            Comment c = this.getClient().addComment(new Comment(comment));
//            if (c != null) {
//                result = c.toMap();
//            }
//        } catch (XmlRpcException e) {
//            this.logger.error(e);
//            throw new XWikiSwizzleClientException(e);
//        }
//
//        this.logout(b);
//
//        return result;
//
//    }
//    
//    /**
//     * DOCUMENT ME!
//     * 
//     * @param pageId DOCUMENT ME!
//     * @param comment DOCUMENT ME!
//     * @return DOCUMENT ME!
//     * @throws XWikiSwizzleClientException
//     */
//    public boolean removeComment(String commentId) throws XWikiSwizzleClientException
//    {
//        boolean b = this.relogin();
//        boolean result=false;
//
//        try {
//            result = this.getClient().removeComment(commentId).booleanValue(); 
//        } catch (XmlRpcException e) {
//            this.logger.error(e);
//            throw new XWikiSwizzleClientException(e);
//        }
//
//        this.logout(b);
//
//        return result;
//
//    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param fields DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws XWikiSwizzleClientException
     */
    public Map<String, String> setFields(String pageId, Map<String, String> fields) throws XWikiSwizzleClientException
    {
        if (!this.existPage(pageId)) {
            return null;
        }

        Page newPage = new Page(fields);

        newPage = this.storeWikiPage(newPage);

        return newPage.toMap();
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
     * @throws XWikiSwizzleClientException
     */
    synchronized public String setPageContent(String pageId, String value, String algo, byte[] rmd)
    throws NoSuchAlgorithmException, XWikiSwizzleClientException
    {
        String result = null;
        Page page = null;
        String pageContent = "";

        // if user have not connected client, method do it for him
        // else it's to the user to do the connection gestion...
        boolean b = this.relogin();

        page = this.getWikiPage(pageId);

        if (page != null) {
            pageContent = page.getContent();
        }

        byte[] messageDigest = this.getDigest(pageContent, algo);

        if (MessageDigest.isEqual(messageDigest, rmd)) {
            if (page == null) {
                Map p = this.createPage(pageId, value);
                if (p == null) {
                    throw new XWikiSwizzleClientException("Problem with setPageContent : can't create the page");
                }
            } else {
                page.setContent(value);
                this.storeWikiPage(page);
            }
        } else {
            if ((pageContent == null)
                || ((pageContent.length() == 1) && (pageContent.codePointAt(0) == VOID_CHARACTER))
                || (pageContent.length() < 1)) {
                result = "";
            } else {
                result = pageContent;
            }
        }

        this.logout(b);

        return result;
    }

//    public Map<Integer,List<String>> getTags(String pageId) throws XWikiSwizzleClientException{
//        if (!this.existPage(pageId)) {
//            return null;
//        }
//        boolean b = this.relogin();
//        
//        Map<Integer,List<String>> result=new Hashtable<Integer, List<String>>();
//        try {
//            List<XWikiObjectSummary> list=this.client.getObjects(pageId);
//            for(XWikiObjectSummary o : list){
//                if (!o.getClassName().equals(WikiContentManager.TAGCLASSID)){
//                    list.remove(o);
//                }
//            }
//
//            XWikiObject temp=null;
//            for(XWikiObjectSummary o : list){
//                temp=this.client.getObject(o);
//                result.put(Integer.valueOf(temp.getId()),(List<String>)temp.getProperty(WikiContentManager.TAGSPROPERTYID));
//            }
//        } catch (XmlRpcException e) {
//            this.logger.error(e);
//            throw new XWikiSwizzleClientException(e);
//        }
//        this.logout(b);
//        return result;
//    }
//    
//    public boolean overwriteTags(String pageId, Integer id,List tags) throws XWikiSwizzleClientException {
//        if (!this.existPage(pageId) || id==null || id.intValue() < 0) {
//            return false;
//        } 
//        boolean b = this.relogin();
//        
//        XWikiObject tagsObj;
//        try {
//            tagsObj = this.client.getObject(pageId,WikiContentManager.TAGCLASSID,id);
//            tagsObj.setProperty(WikiContentManager.TAGSPROPERTYID, tags);
//            this.client.storeObject(tagsObj);
//        } catch (XmlRpcException e) {
//            this.logger.error(e);
//            throw new XWikiSwizzleClientException(e);
//        } 
//        this.logout(b);
//        return true;
//    }
//    
//    public boolean addTag(String pageId, Integer id,String tag) throws XWikiSwizzleClientException {
//        if (!this.existPage(pageId)) {
//            return false;
//        } 
//        boolean b = this.relogin();
//        XWikiObject tagsObj=null;
//        int idObj=-1;
//        if (id==null){
//            idObj=-1;
//        }
//        else{
//            idObj=id.intValue();
//        }
//        
//        try{
//            // if unknown id 
//            if (idObj==-1){
//                // search an existing tags object
//                List<XWikiObjectSummary> objs=this.client.getObjects(pageId);
//                for(XWikiObjectSummary o:objs){
//                    if (!o.getClassName().equals(WikiContentManager.TAGCLASSID)){
//                        objs.remove(o);
//                    }
//                }
//                // get the id of the first existing tags object
//                if (objs.size()>0){
//                 idObj=objs.get(0).getId();
//                 tagsObj= this.client.getObject(pageId, WikiContentManager.TAGCLASSID,Integer.valueOf(idObj));
//                }
//                // no tags object found : create one
//                else{
//                    this.logger.warn("No tag object in page : "+pageId);
//                    Map map=new Hashtable<String, String>();
//                    map.put("className", WikiContentManager.TAGCLASSID);
//                    map.put(WikiContentManager.PAGEID, pageId);
//                    tagsObj=new XWikiObject(map);
//                    List l=new ArrayList();
//                    tagsObj.setProperty(WikiContentManager.TAGSPROPERTYID,l);
//                }
//            }else{
//                tagsObj= this.client.getObject(pageId, WikiContentManager.TAGCLASSID,Integer.valueOf(idObj));
//            }
//            
//        } catch (Exception e) {
//            this.logger.error(e);
//            throw new XWikiSwizzleClientException(e);
//        }
//       
//        List l=(List) tagsObj.getProperty(WikiContentManager.TAGSPROPERTYID);
//        if (l==null){
//            l=new ArrayList<String>();
//        }
//        l.add(tag);
//        tagsObj.setProperty(WikiContentManager.TAGSPROPERTYID, l);
//        
//        try {
//            this.client.storeObject(tagsObj);
//        } catch (XmlRpcException e) {
//            this.logger.error(e);
//            throw new XWikiSwizzleClientException(e);
//        }
//        this.logout(b);
//        return true;    
//    }
    
    public void essai() throws Exception{
        String spaceKey="Main";
        String pageName=spaceKey+".WebHome";
        this.login();
        List<XWikiObjectSummary> list=this.client.getObjects(pageName);
        //this.client.getClass()
       //this.client.getObjects(ps);
        for(XWikiObjectSummary o : list){
            System.out.println("==== Object ====");
            System.out.println(" => Summary ToString : "+o.toString());  
            XWikiObject ob=this.client.getObject(o);
            System.out.println(" => Object ToString : "+ob);
            System.out.println(" => ToMap : "+ob.toMap());
            Map map=ob.toMap();
            System.out.println("propertyToValueMap : "+map.get("propertyToValueMap"));
            if (map.get("propertyToValueMap") instanceof List){
                System.out.println("true");
            }
            System.out.println(" => Properties ToString : "+ob.getProperties());
            System.out.println(" => Properties : ");
            Set<String> temp=ob.getProperties();
            
           
            Iterator i=temp.iterator();
            while(i.hasNext()){
                String val=(String)i.next();
                System.out.println(val+" : "+ob.getProperty(val)+ob.getProperty(val).getClass());
                }
        }
        
       
      /*  XWikiObject essai=this.client.getObject(list.get(0));
        List l=(List) essai.getProperty("tags");
        l.add("XMLRPC");
        essai.setProperty("tags", l);
        this.client.storeObject(essai);*/
        List<XWikiClassSummary> l2=this.getClient().getClasses();
        for (XWikiClassSummary cs:l2){
            System.out.println(this.getClient().getClass(cs));
        }
        
        System.out.println(this.client.getClass("XWiki.TagClass"));
        
        List<Attachment> list2=this.client.getAttachments(pageName);
        for(Attachment o : list2){
            System.out.println(o.toString());   
        }

        this.logout();
    }

    synchronized public XWikiXmlRpcClient getClient()
    {
        return this.client;
    }

    public static Map<String, String> fromXmlStatic(Document pageXml)
    {
        if (pageXml==null){
            return null;
        }
        if (pageXml.getDocumentElement()==null){
            return null;
        }
        
        // get entries 
        NodeList entries = pageXml.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES);
        if (entries ==null || entries.getLength()==0){
            return null;
        }
        
        // get list of entries
        NodeList list=entries.item(0).getChildNodes();
        
        if (list==null || list.getLength()==0){
            return null;
        }
       
        Map<String,String> result=new HashMap<String, String>();
        for (int i=0;i<list.getLength();i++){
           
            if (list.item(i)!=null && 
                list.item(i).getChildNodes()!=null && 
                list.item(i).getChildNodes().item(0)!=null && 
                list.item(i).getChildNodes().item(1)!=null){
                if (PAGEMDTABLE.getCollection().contains(list.item(i).getChildNodes().item(0).getTextContent()) || 
                    CONTENT.equals(list.item(i).getChildNodes().item(0).getTextContent())){
                    result.put(list.item(i).getChildNodes().item(0).getTextContent(),list.item(i).getChildNodes().item(1).getTextContent());
                }
            }      
        }
        return result;
    }

    public static Document toXmlStatic(String pageId,String href,Map<String, String> pageMap) throws WikiContentManagerException
    {
        if (pageMap==null){
            return null;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance (); 
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder ();
        } catch (ParserConfigurationException e) {
            throw new WikiContentManagerException(e);
        } 

        Document document=builder.newDocument();

        // Propriétés du DOM
        document.setXmlVersion("1.0");
        document.setXmlStandalone(true);

        // Création de l'arborescence du DOM
        Element racine = document.createElement(XML_NODE_NAME_XWIKIPAGE);
        racine.setAttribute(XML_ATTRIBUTE_NAME_XWIKIPAGEID, pageId);
        racine.setAttribute(XML_ATTRIBUTE_NAME_HREF, href);
        Element entries = document.createElement(XML_NODE_NAME_ENTRIES);  

        Iterator i=pageMap.entrySet().iterator(); 
        Element entry=null;
        Element key=null;
        Element value=null;

        while(i.hasNext()){
            Entry k=(Entry) i.next();

            entry=document.createElement(XML_NODE_NAME_ENTRY);

            key = document.createElement(XML_NODE_NAME_ENTRY_KEY);
            key.appendChild(document.createTextNode((String)k.getKey()));
            entry.appendChild(key);

            value = document.createElement(XML_NODE_NAME_ENTRY_VALUE);
            value.appendChild(document.createTextNode((String)k.getValue()));
            entry.appendChild(value);

            entries.appendChild(entry);
        }
        racine.appendChild(entries);
        document.appendChild(racine);
        document.normalizeDocument();
        return document;

    }

    public Map<String, String> fromXml(Document pageXml) throws WikiContentManagerException
    {
        return XwikiSwizzleClient.fromXmlStatic(pageXml);
    }

    public Document toXml(String pageId, String href, Map<String, String> pageMap) throws WikiContentManagerException
    {
        return XwikiSwizzleClient.toXmlStatic(pageId, href, pageMap);
       
    }
    
    public static Document PageListToXmlStatic(String pagesHRef,List<String> list) throws WikiContentManagerException{
        if (list!=null){
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance (); 
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder ();
            } catch (ParserConfigurationException e) {
                throw new WikiContentManagerException(e);
            } 

            Document document=builder.newDocument();

            // Propriétés du DOM
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);

            // Création de l'arborescence du DOM
            Element racine = document.createElement(WikiContentManager.XML_NODE_NAME_XWIKIPAGELIST);
            racine.setAttribute(WikiContentManager.XML_ATTRIBUTE_NAME_LISTSIZE, String.valueOf(list.size()));

            Iterator i=list.iterator();
            Element page=null;

            while(i.hasNext()){                    
                String k=(String) i.next();

                page=document.createElement(WikiContentManager.XML_NODE_NAME_XWIKIPAGE);
                page.setAttribute(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID, k);
                page.setAttribute(WikiContentManager.XML_ATTRIBUTE_NAME_HREF, pagesHRef+"/"+k);
                racine.appendChild(page);
            }
            document.appendChild(racine);
            document.normalizeDocument(); 
            return document;
        }
        return null;
    }
    
    public static List<String> PageListFromXmlStatic(Document doc){
        if (doc==null){
            return null;
        }
        if (doc.getFirstChild()==null){
            return null;
        }
        
        // get entries 
        NodeList entries = doc.getFirstChild().getChildNodes();
        if (entries ==null || entries.getLength()==0){
            return null;
        }
        
        if (doc.getFirstChild().getAttributes()==null || doc.getFirstChild().getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_LISTSIZE)==null){
            return null;
        }
        
        NodeList entriesList = doc.getFirstChild().getChildNodes();
        
        Integer listSize=Integer.valueOf(doc.getFirstChild().getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_LISTSIZE).getNodeValue());
        
        List<String> result=new ArrayList<String>();
        
        if (listSize.intValue()==0){
            return result;
        }
        
        for (int i=0;i<listSize.intValue();i++){
            if (entriesList!=null && entriesList.item(i)!=null && 
                entriesList.item(i).getAttributes()!=null && 
                entriesList.item(i).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID)!=null){
                result.add(entriesList.item(i).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID).getTextContent());
            }
        }
        
        return result;        
    }
    
    public Document PageListToXml(String pagesHRef,List<String> list) throws WikiContentManagerException{
        return PageListToXmlStatic(pagesHRef, list);
    }
    
    public List<String> PageListFromXml(Document doc){
        return PageListFromXmlStatic(doc);
    }

}
