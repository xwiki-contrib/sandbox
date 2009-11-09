package org.xwoot.iwoot.xwootclient;

import java.util.List;

public interface XWootClientAPI
{
   // public boolean addNeighbour(String neighborURL);

    void connectToContentManager();

    void disconnectFromContentManager(); 

    boolean isContentManagerConnected();
  
    boolean isConnectedToP2PNetwork();
   
    List getPageList(String id) throws XWootClientException;
}
