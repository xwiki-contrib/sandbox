package org.xwoot.mockiphone.iwootclient;

import org.w3c.dom.Document;

public interface IWootClient
{
    public boolean putPage(String pageName,Document doc) throws IWootClientException;
    
    public boolean postPage(String pageName,Document doc) throws IWootClientException;
    
    public Document getPage(String pageName) throws IWootClientException;
    
    public Document getPageList() throws IWootClientException;
    
    public String getUri() throws IWootClientException;
}
