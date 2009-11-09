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

package org.xwoot.mockiphone.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import java.util.List;

import org.xwoot.mockiphone.MockIphoneException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MockIphonePageManager
{
  
    private String workingDir;
    public final static String PAGELISTFILENAME=".pageList";

    public MockIphonePageManager(String workingDir)
    {
        this.workingDir=workingDir;
    }

    private synchronized MockIphonePage loadPage(String pageName) throws MockIphoneException 
    {
        if (!this.existPage(pageName)) {
            return null;
        }
    
        XStream xstream = new XStream(new DomDriver());
        try {
            MockIphonePage page = (MockIphonePage) xstream.fromXML(new FileInputStream(this.workingDir + File.separator + pageName));
            return page;
        } catch (FileNotFoundException e) { 
            throw new MockIphoneException("File not found : "+this.workingDir + File.separator + pageName+"\n",e);
        }
    }

    private synchronized void storePage(MockIphonePage page) throws MockIphoneException
    {
        if (page==null){
            return;
        }
        XStream xstream = new XStream(new DomDriver());
    
        OutputStreamWriter osw;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(this.workingDir + File.separator + page.getPageName()), Charset
                .forName(System.getProperty("file.encoding")));
            PrintWriter output = new PrintWriter(osw);
    
            output.print(xstream.toXML(page));
            output.flush();
            output.close();
        } catch (FileNotFoundException e) { 
            throw new MockIphoneException("File not found : "+this.workingDir + File.separator + page.getPageName()+"\n",e);
        }
    
    }

    private synchronized void unloadPage(MockIphonePage page) throws MockIphoneException
    {
        this.storePage(page);
        System.runFinalization();
        System.gc();
    }
    
    public void removePageFile(String pageName)
    {
        File f=new File(this.workingDir+File.separatorChar+pageName);
      
        if (f.exists()){
            System.out.println(f.delete());
        }
    }

    public void deletePage(String pageName) throws MockIphoneException{
        MockIphonePage page=this.loadPage(pageName);
        page.setModified(true);
        page.setRemoved(true);
        this.unloadPage(page);   
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pagesDir DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public boolean existPage(String pageName)
    {
        File dir = new File(this.workingDir);
        String[] listPages = dir.list();
    
        // for each page on the site
        for (String listPage : listPages) {
            if (listPage.equals(pageName)) {
                return true;
            }
        }
    
        return false;
    }

    static public void savePageList(String workingDir,List list) throws MockIphoneException
    {  
        File listFile=new File(workingDir+File.separatorChar+PAGELISTFILENAME);
        if (listFile.exists()){
            listFile.delete();
        }
        XStream xstream = new XStream(new DomDriver());
        try {
            PrintWriter pw =
                new PrintWriter(new FileOutputStream(listFile));
            pw.print(xstream.toXML(list));
            pw.flush();
            pw.close();
        }catch (FileNotFoundException e) {
            throw new MockIphoneException("File not found : "+ listFile + "\n",e);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pagesDir DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws MockIphoneException 
     * @throws FileNotFoundException DOCUMENT ME!
     */
    static public Collection getManagedPageNames(String pagesDir) throws MockIphoneException 
    {
        File dir = new File(pagesDir);
        String[] listPages = dir.list();
        ArrayList result=new ArrayList<String>();
        for (int i=0;i<listPages.length;i++){
            result.add(listPages[i]);
        }
        result.remove(PAGELISTFILENAME);
        return result;
    }

    public void setPageContent(String pageName, String xmlContent) throws MockIphoneException
    {
        String pn=pageName;
        if (!this.existPage(pageName)){
            pn=this.createPage(xmlContent);
        }
       MockIphonePage page=this.loadPage(pn);
       if (page==null){
           return;
       }
       page.setContent(xmlContent);
       this.unloadPage(page);
       
        
    }

    public Boolean isModified(String pageName) throws MockIphoneException
    { 
        MockIphonePage page=this.loadPage(pageName);
        if (page==null){
            return null;
        }
        return Boolean.valueOf(page.isModified());
    }

    public MockIphonePage getPage(String pageName) throws MockIphoneException
    {
        MockIphonePage page=this.loadPage(pageName);
        return page;
    }

    public String createPage(String content) throws MockIphoneException
    {
        MockIphonePage newPage=new MockIphonePage("",new Hashtable<String, String>(),false,false);
        //setContent get pageName in content
        newPage.setContent(content);
        String result=newPage.getPageName();
        this.unloadPage(newPage);  
        return result;
    }

    public void setModified(String pageName, boolean b) throws MockIphoneException
    {
        MockIphonePage page=this.loadPage(pageName);
        if (page==null){
            return;
        }
        page.setModified(b);
        this.unloadPage(page);
        
    }

    public void addPage(MockIphonePage pageM) throws MockIphoneException
    {
        this.unloadPage(pageM);    
    }
}
