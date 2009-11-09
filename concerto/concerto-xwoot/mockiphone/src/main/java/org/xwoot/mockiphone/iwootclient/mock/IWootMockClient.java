package org.xwoot.mockiphone.iwootclient.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.xwoot.mockiphone.iwootclient.IWootClient;
import org.xwoot.mockiphone.iwootclient.IWootClientException;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.XWikiSwizzleClient.XwikiSwizzleClient;

public class IWootMockClient implements IWootClient
{
    private Map contents;

    private String uri;
    
    public IWootMockClient(String uri){
        this.uri=uri;
        this.contents=new HashMap<String, String>();   
        for(int i=0;i<10;i++){
           this.contents.put("Page"+i,"Page"+i+"Content");
        }
    }

    public Document getPageList(){
        try {
            return XwikiSwizzleClient.PageListToXmlStatic("mock", new ArrayList<String>(this.contents.keySet()));
        } catch (WikiContentManagerException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean putPage(String pageName,Document page){
        Map pageMap;
        pageMap = XwikiSwizzleClient.fromXmlStatic(page);
        if (this.contents.containsKey(pageName)){
            this.contents.put(pageName, pageMap.get("Content"));
        }
        else{
            this.createPage(pageName, (String)pageMap.get("Content"));
        }
        return true;
    }
    
    public Document getPage(String pageName){
        Map result = new HashMap<String, String>();
        result.put("id", pageName);
        result.put("content",this.contents.get(pageName));
        result.put("pageId",pageName);
        try {
            return XwikiSwizzleClient.toXmlStatic(pageName, "mock", result);
        } catch (WikiContentManagerException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void createPage(String pageName,String content){
        this.contents.put(pageName,content);
    }


    public void removePage(String pageName)
    {
       this.contents.remove(pageName);
        
    }
    
    public String getUri()
    {
        return this.uri;
    }

    public boolean postPage(String pageName, Document page) throws IWootClientException
    {
        Map pageMap;
        pageMap = XwikiSwizzleClient.fromXmlStatic(page);
        if (this.contents.containsKey(pageName)){
            this.contents.put(pageName, pageMap.get("Content"));
        }
        else{
            this.createPage(pageName, (String)pageMap.get("Content"));
        }
        return true;
    }
}