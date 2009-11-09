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

package org.xwoot.mockiphone.web.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.mockiphone.MockIphoneException;
import org.xwoot.mockiphone.iwootclient.IWootClientException;
import org.xwoot.mockiphone.web.MockIphoneSite;
import org.xwoot.wikiContentManager.WikiContentManagerException;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class DefaultApp extends HttpServlet
{
    private static final long serialVersionUID = -3758874922535817475L;

    /**
     * DOCUMENT ME!
     * 
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {  
        try { 
            String content="";
            String action="";
            String pageName="";
            boolean nopage=false;
            action=request.getParameter("action");
            pageName=request.getParameter("pagename");    
            
            System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : Default page - action : "+action+"\n");
            
            if("editpage".equals(action)){ 
                System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : edit page content : "+pageName+"\n");
                request.setAttribute("editpage",Boolean.valueOf(true));
                content=MockIphoneSite.getInstance().getMockIphoneSiteEngine().getPage(pageName);
            }
            else if("createpage".equals(action)){
                System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : edit page content : "+pageName+"\n");
                request.setAttribute("editpage",Boolean.valueOf(true));
                content=MockIphoneSite.getInstance().getMockIphoneSiteEngine().getTemplate();
            }
            else{
                request.setAttribute("editpage",Boolean.valueOf(false));
            }
            
            try{
                if("addpage".equals(action)){  
                    System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : add page to management : "+pageName+"\n");
                    MockIphoneSite.getInstance().getMockIphoneSiteEngine().askPage(pageName);
                }
                else if("viewpage".equals(action)){ 
                    System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : view page content : "+pageName+"\n");
                }
                else if("deletepage".equals(action)){ 
                    System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : remove page : "+pageName+"\n");
                    MockIphoneSite.getInstance().getMockIphoneSiteEngine().removePage(pageName);
                }
                else if("sendpage".equals(action)){
                    System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : send page : "+pageName+"\n");
                    MockIphoneSite.getInstance().getMockIphoneSiteEngine().sendPage(pageName);
                }
                else if("savepage".equals(action)){
                    String newContent=request.getParameter("newcontent");
                    System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : save page modifications : "+pageName+"\n");
                    System.out.println(newContent);
                    MockIphoneSite.getInstance().getMockIphoneSiteEngine().setPageContent(pageName, newContent);
                }
                else if("refreshlist".equals(action)){
                    System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : refresh page list \n");
                    MockIphoneSite.getInstance().getMockIphoneSiteEngine().refreshPageList();
                    nopage=true;
                }
                else {
                    System.out.print("Site " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId() + " : no action\n");
                    nopage=true;
                }   
                
                request.setAttribute("pagelist",MockIphoneSite.getInstance().getMockIphoneSiteEngine().askPageList());
            }catch(IWootClientException e){
                throw new ServletException(e);
            } catch (WikiContentManagerException e) {
                throw new ServletException(e);
            }
         
            if (pageName!=null && !pageName.equals("") && !nopage){
                request.setAttribute("pagename",pageName);
                if (content==""){
                    content=MockIphoneSite.getInstance().getMockIphoneSiteEngine().getPage(pageName);
                }
            }
            Map temp=MockIphoneSite.getInstance().getMockIphoneSiteEngine().getManagedPages();
            request.setAttribute("nopage",Boolean.valueOf(nopage)); 
            request.setAttribute("content",content); 
            request.setAttribute("pagename",pageName); 
            request.setAttribute("managedpagelist", temp);
            request.setAttribute("iscurrentpagemodified",temp.get(pageName));
            request.getRequestDispatcher("/pages/DefaultApp.jsp").forward(request, response);
        } catch (MockIphoneException e) {
            throw new ServletException(e);
        }
        return;
    }
}
