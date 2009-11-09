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
package org.xwoot.xwootApp.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.xwoot.xwootApp.XWootException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWootPage implements Serializable
{
    /**  */
    private static final long serialVersionUID = 6228704590406225117L;

    /**
     * DOCUMENT ME!
     * 
     * @param lastVuePagesDir DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws XWootException 
     * @throws FileNotFoundException DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
    static public Collection getManagedPageNames(String lastVuePagesDir) throws XWootException 
    {
        File dir = new File(lastVuePagesDir);
        String[] listPages = dir.list();
        List<String> coll = new ArrayList<String>();

        // for each space
        for (String listPage : listPages) {
            XStream xstream = new XStream(new DomDriver());
            XWootPage currentPage;
            try {
                currentPage = (XWootPage) xstream.fromXML(new FileInputStream(lastVuePagesDir + File.separator + listPage));
                coll.add(currentPage.getPageName());
            } catch (FileNotFoundException e) {
                throw new XWootException("File not found : "+lastVuePagesDir + File.separator + listPage+"\n",e);
            }
            
        }

        return coll;
    }

    private String pageName;

    private String content; // file content

    private String filename;

    /**
     * Creates a new XWootPage object.
     * 
     * @param pageName DOCUMENT ME!
     * @param content DOCUMENT ME!
     */
    public XWootPage(String pageName, String content)
    {
        this.pageName = pageName;
        this.content = content;
        try {
            this.filename = new String(Base64.encodeBase64(pageName.getBytes("UTF-8")), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This won't happen.
        }
    }

    public void createPage(String lastVuePagesDir) throws XWootException
    {
        if (!this.existPage(lastVuePagesDir)) {
            XStream xstream = new XStream();
            try {
                PrintWriter pw =
                    new PrintWriter(new FileOutputStream(lastVuePagesDir + File.separator + this.getFileName()));
                pw.print(xstream.toXML(this));
                pw.flush();
                pw.close();
            }catch (FileNotFoundException e) {
                throw new XWootException("File not found : "+lastVuePagesDir + File.separator + this.getFileName()+"\n",e);
            }
           
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param lastVuePagesDir DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public boolean existPage(String lastVuePagesDir)
    {
        File dir = new File(lastVuePagesDir);
        String[] listPages = dir.list();

        // for each page on the site
        for (String listPage : listPages) {
            if (listPage.equals(this.getFileName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getContent()
    {
        return this.content;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getFileName()
    {
        return this.filename;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getPageName()
    {
        return this.pageName;
    }

    public synchronized void loadPage(String lastVuePagesDir) throws XWootException 
    {
        if (!this.existPage(lastVuePagesDir)) {
            this.createPage(lastVuePagesDir);
        }

        XStream xstream = new XStream(new DomDriver());
        XWootPage page;
        try {
            page = (XWootPage) xstream.fromXML(new FileInputStream(lastVuePagesDir + File.separator + this.getFileName()));
            if ((page.getContent() == null)
                || ((page.getContent().length() == 1) && (page.getContent().codePointAt(0) == 67))
                || (page.getContent().length() < 1)) {
                page.setContent("");
            }

            this.setContent(page.getContent());
        } catch (FileNotFoundException e) { 
            throw new XWootException("File not found : "+lastVuePagesDir + File.separator + this.getFileName()+"\n",e);
        }

       
    }

    /**
     * DOCUMENT ME!
     * 
     * @param content DOCUMENT ME!
     */
    public void setContent(String content)
    {
        this.content = content;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     */
    public void setPageName(String pageName)
    {
        this.pageName = pageName;
        try {
            this.filename = new String(Base64.encodeBase64(pageName.getBytes("UTF-8")), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This won't happen.
        }
    }

    private synchronized void storePage(String lastVuePagesDir) throws XWootException
    {
        XStream xstream = new XStream();

        OutputStreamWriter osw;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(lastVuePagesDir + File.separator + this.getFileName()), Charset
                .forName(System.getProperty("file.encoding")));
            PrintWriter output = new PrintWriter(osw);

            output.print(xstream.toXML(this));
            output.flush();
            output.close();
        } catch (FileNotFoundException e) { 
            throw new XWootException("File not found : "+lastVuePagesDir + File.separator + this.getFileName()+"\n",e);
        }
       
    }

    public synchronized void unloadPage(String lastVuePagesDir) throws XWootException
    {
        this.storePage(lastVuePagesDir);
        System.runFinalization();
        System.gc();
    }
}
