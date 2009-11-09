package org.xwoot.iwoot.xwootclient.servlet;

import org.xwoot.iwoot.xwootclient.XWootClientAPI;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.wikiContentManager.WikiContentManager;

public class XWootClientServletFactory extends XWootClientFactory
{   
    @Override
    public XWootClientAPI createXWootClient(String xwootURL) throws XWootClientServletException
    {
        return new XWootClientServlet(xwootURL);
    }

    @Override
    public XWootClientAPI createXWootClient(WikiContentManager wcm) throws XWootClientException
    {
        throw new XWootClientException("bad constructor for this type ofxwootClient");
    }
}
