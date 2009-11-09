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

package org.xwoot.mockiphone;

//Harg ! Coupling between patch and XWoot ...
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xwoot.mockiphone.core.MockIphonePage;
import org.xwoot.mockiphone.core.MockIphonePageManager;
import org.xwoot.mockiphone.iwootclient.IWootClient;
import org.xwoot.mockiphone.iwootclient.IWootClientException;
import org.xwoot.mockiphone.iwootclient.rest.IWootRestClient;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.XWikiSwizzleClient.XwikiSwizzleClient;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MockIphone
{

    private Integer id;

    private final Log logger = LogFactory.getLog(this.getClass());

    private String workingDir;

    private IWootClient iwootRestClient;
    
    private MockIphonePageManager pageManager;

    public IWootClient getIwootRestClient()
    {
        return this.iwootRestClient;
    }

    /**
     * Creates a new IWoot object.
     * 
     * @param iwootUrl DOCUMENT ME!
     * @param id DOCUMENT ME!
     * @param WORKINGDIR DOCUMENT ME!
     * @throws MockIphoneException 
     * 
     */
    public MockIphone(String workingDir, Integer id, String iwootUrl) throws MockIphoneException
    {
        this.id=id;
        this.workingDir = workingDir;
        this.createWorkingDir();
        this.iwootRestClient=new IWootRestClient(iwootUrl);
        this.pageManager=new MockIphonePageManager(workingDir);
        this.logger.info(this.id + " : MockIphone engine created. working directory : " + workingDir + "\n\n");
    }

    /**
     * DOCUMENT ME!
     * 
     * @param dir DOCUMENT ME!
     */
    public static void deleteDirectory(File dir)
    {
        if (dir.exists()) {
            String[] children = dir.list();

            for (String element : children) {
                File f = new File(dir, element);
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }

            dir.delete();
        }
    }

    public void clearWorkingDir() throws MockIphoneException 
    {
        File f = new File(this.workingDir);
        System.out.println("=>" + this.workingDir);
        if (f.exists()) {
            this.logger.info(this.id + " Delete working dir mockIphone : " + f.toString());
            deleteDirectory(f);
        }
        this.createWorkingDir();
    }

    private void createWorkingDir() throws MockIphoneException 
    {
        File working = new File(this.workingDir);

        if (!working.exists() && !working.mkdir()) {
            throw new MockIphoneException("Can't create xwoot directory: " + working);
        }

        if (!working.isDirectory()) {
            throw new RuntimeException(working + " is not a directory");
        } else if (!working.canWrite()) {
            throw new MockIphoneException("Can't write in directory: " + working);
        }
    }

    public void setPageContent(String pageName,String xmlContent) throws MockIphoneException{
        this.logger.info("Add page : "+pageName+" to management.");
        this.pageManager.setPageContent(pageName, xmlContent);
    } 

    public Map getManagedPages() throws MockIphoneException{
        Map map=new HashMap<String, Boolean>();
        Collection list=MockIphonePageManager.getManagedPageNames(this.workingDir);
        Iterator i=list.iterator();
        while(i.hasNext()){
            String pageName=(String)i.next();
            Boolean b=this.pageManager.isModified(pageName);
            if (b!=null){
                map.put(pageName, b);
            }
        }
        return map;
    }

    public Integer getId()
    {
        return this.id;
    }

    public String getPage(String pageName) throws MockIphoneException{
        if (pageName==null || pageName.equals("")){
            return "";
        }
        return this.pageManager.getPage(pageName).toXML();
    }

    public void createPage(String content) throws MockIphoneException{
        this.pageManager.createPage(content);
    }

    public void refreshPageList() throws MockIphoneException, IWootClientException
    {
        Document doc=this.iwootRestClient.getPageList();
        if (doc==null){
            return;
        }
        MockIphonePageManager.savePageList(this.workingDir, XwikiSwizzleClient.PageListFromXmlStatic(doc));
    }

    public void removePage(String pageName) throws MockIphoneException
    {
        if (pageName==null || pageName.equals("")){
            return;
        }
       this.pageManager.deletePage(pageName);
    }  

    public String getTemplate(){
        return MockIphonePage.getTemplate();

    }

    public void sendPage(String pageName) throws MockIphoneException, IWootClientException, WikiContentManagerException{
        MockIphonePage page=this.pageManager.getPage(pageName);
        if (page==null){
            return;
        }
        /*if (page.isRemoved(this.workingDir)){
            page.removePageFile(this.workingDir);
            this.iwootRestClient.removePage(pageName);
            return ;
        }*/
        
        Document doc=XwikiSwizzleClient.toXmlStatic(pageName,this.iwootRestClient.getUri() + "/" + IWootRestClient.PAGESKEY+"/"+pageName , page.getContent());
        if (this.getPageList().contains(page.getPageName())){
            System.out.println(this.iwootRestClient.putPage(pageName, doc));
        }else{
            System.out.println(this.iwootRestClient.postPage(pageName,doc));
        }
        this.pageManager.setModified(pageName,false); 
    }

    List getPageList() throws IWootClientException{
        Document doc =this.iwootRestClient.getPageList();
        if (doc==null){
            return new ArrayList<String>();
        }
        List l=XwikiSwizzleClient.PageListFromXmlStatic(doc);
        return l;
    }
    
    public List askPageList() throws MockIphoneException, IWootClientException{    
        List l=this.getPageList();
        MockIphonePageManager.savePageList(this.workingDir,l);
        Collection list=MockIphonePageManager.getManagedPageNames(this.workingDir);
        if (l!=null && list!=null){
            l.removeAll(list);
        }
        return l;
    }

    public void askPage(String pageName) throws MockIphoneException, IWootClientException
    {
        if (pageName==null || pageName.equals("")){
            return;
        }
        Document doc=this.iwootRestClient.getPage(pageName);
        if (doc==null){
            return;
        }
        Map page = XwikiSwizzleClient.fromXmlStatic(doc);
        this.logger.info("Ask page : "+pageName+" to rest server.");
        this.logger.info("Page  : "+page);
        
        this.pageManager.removePageFile(this.workingDir);
        MockIphonePage pageM=new MockIphonePage(pageName,page,false,false);
        this.pageManager.addPage(pageM);
    }
}
