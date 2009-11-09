package org.xwoot.iwoot;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xwoot.iwoot.xwootclient.XWootClientAPI;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;

public class IWoot
{
    private XWootClientAPI xwoot;
    private WikiContentManager wcm;
    private Integer id;

    //logger
    private final Log logger = LogFactory.getLog(this.getClass());

    public IWoot(XWootClientAPI wootAPI, WikiContentManager wcm, Integer id)
    { 
        this.xwoot = wootAPI;
        this.wcm = wcm;
        this.id=id;
        this.logger.info("Iwoot engine created. Id : "+id);
    }

    //    public synchronized Map<String, Map> getPages() throws IWootException 
    //    {
    //        this.reconnectXwoot();
    //        HashMap< String, Map> result=new HashMap<String, Map>();
    //        Collection spaces;
    //        try {
    //            spaces = this.wcm.getListSpaceId(); 
    //            Iterator i=spaces.iterator();
    //            while(i.hasNext()){
    //                String space=(String)i.next();
    //                Collection pages=this.wcm.getListPageId(space);
    //                Iterator j = pages.iterator();
    //                while(j.hasNext()){
    //                    String page = (String)j.next();
    //                    Map pageMap=this.wcm.getFields(page);
    //                    result.put(page, pageMap);
    //                }
    //            }
    //        } catch (WikiContentManagerException e) {
    //            throw new IWootException(this.id+" : Problem with WikiContentManager when getting pages",e);
    //        }
    //        this.disconnectXWoot();
    //        return result;
    //    }

//    private synchronized List<String> getPagesNames() throws IWootException
//    { 
//        this.reconnectXwoot();
//        ArrayList< String> result=new ArrayList<String>();
//        Collection spaces;
//        try {
//            spaces = this.wcm.getListSpaceId();
//        } catch (WikiContentManagerException e) {
//            throw new IWootException(this.id+" : Problem with WikiContentManager (getListSpaceId)",e);
//        }
//        Iterator i=spaces.iterator();
//        while(i.hasNext()){
//            String space=(String)i.next();
//            try {
//                Collection pages=this.wcm.getListPageId(space);
//                result.addAll(pages);
//            } catch(WikiContentManagerException e ){
//                throw new IWootException(this.id+" : Problem with WikiContentManager (getListPageId)",e);
//            }  
//        }
//        this.disconnectXWoot();
//        return result;
//    }

    public synchronized Document getPageList(String pagesHRef) throws IWootException{
        List list;
        try {
            this.xwoot.connectToContentManager();
            list = this.xwoot.getPageList(String.valueOf(this.id));
            this.xwoot.disconnectFromContentManager();
            return this.wcm.PageListToXml(pagesHRef, list);
        } catch (XWootClientException e) {
            throw new IWootException("Problem with XWoot client (get page list)",e);
        } catch (WikiContentManagerException e) {
            throw new IWootException("Problem with Wiki Content Manager (page list to xml)",e);
        }
       
    }
    
//  {
//        List<String> list=this.getPagesNames();
//        if (list!=null){
//            try {
//                return this.wcm.PageListToXml(pagesHRef, list);
//            } catch (WikiContentManagerException e) {
//                throw new IWootException(this.id+" : Problem with WikiContentManager (pageListToXml)",e);
//            }
//        }
//        return null;
//  }

    public synchronized Document getPage(String pageId,String href) throws IWootException
    {
        Map pageMap=null;
        try {
            pageMap=this.wcm.getFields(pageId);
            if (pageMap==null){
                return null;
            }
            String render=this.wcm.renderContent(pageId);
            pageMap.put(WikiContentManager.RENDERCONTENT, render);
            return this.wcm.toXml(pageId,href,pageMap);
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (getFields)",e);
        }

    }


    public synchronized boolean removepage(String pageId) throws IWootException{
        try {
            return this.wcm.removePage(pageId);
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (removePage)",e);
        }
    }

    public synchronized boolean storePage(String pageId,Document document) throws IWootException{
        try {
            Map resultMap=this.wcm.fromXml(document);
            return (!(this.wcm.setFields(pageId, resultMap)==null));          
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (setFields)",e);
        }
    }

    public boolean existPage(Document page) throws IWootException
    {
        try {
            String pageId=this.getPageId(page);
            return this.wcm.existPage(pageId);
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (existPage)",e);  
        }
    }

    public boolean createPage(Document newPage) throws IWootException
    {
        try {
            Map resultMap=this.wcm.fromXml(newPage);
            this.wcm.createPage((String)resultMap.get(WikiContentManager.ID),(String) resultMap.get(WikiContentManager.CONTENT));
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (createPage)",e);
        }
        return true;       
    }

    public XWootClientAPI getXwoot()
    {
        return this.xwoot;
    }

    public String getPageId(Document document) throws IWootException
    {
        try {
            return this.wcm.fromXml(document).get(WikiContentManager.ID);
        } catch (WikiContentManagerException e) {
            throw new IWootException(e);
        }
    }

    public Integer getId()
    {
        return this.id;
    }

    public WikiContentManager getWcm()
    {
        return this.wcm;
    }
}
