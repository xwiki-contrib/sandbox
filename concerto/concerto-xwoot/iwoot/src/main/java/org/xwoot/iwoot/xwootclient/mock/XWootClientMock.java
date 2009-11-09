package org.xwoot.iwoot.xwootclient.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.xwoot.iwoot.xwootclient.XWootClientAPI;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;

public class XWootClientMock implements XWootClientAPI
{
    private WikiContentManager wcm;
    
    public XWootClientMock(WikiContentManager wcm)
    {
       
        this.wcm = wcm;
    }
    
    public void connectToContentManager()
    {
        // TODO Auto-generated method stub 
    }

    public void disconnectFromContentManager()
    {
        // TODO Auto-generated method stub
        
    }

    public boolean isConnectedToP2PNetwork()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isContentManagerConnected()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public List getPageList(String id) throws XWootClientException
    {
        ArrayList< String> result=new ArrayList<String>();
        Collection spaces;
        try {
            spaces = this.wcm.getListSpaceId();
        } catch (WikiContentManagerException e) {
            throw new XWootClientException("Problem with WikiContentManager (getListSpaceId)",e);
        }
        Iterator i=spaces.iterator();
        while(i.hasNext()){
            String space=(String)i.next();
            try {
                Collection pages=this.wcm.getListPageId(space);
                result.addAll(pages);
            } catch(WikiContentManagerException e ){
                throw new XWootClientException("Problem with WikiContentManager (getListPageId)",e);
            }  
        }
        return result; 
    }

}
