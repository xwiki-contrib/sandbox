package org.xwoot.iwoot.xwootclient.mock;

import org.xwoot.iwoot.xwootclient.XWootClientAPI;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.wikiContentManager.WikiContentManager;

public class XWootClientMockFactory extends XWootClientFactory
{   
    @Override
    public XWootClientAPI createXWootClient(WikiContentManager wcm) throws XWootClientMockException
    {
        return new XWootClientMock(wcm);
    }

    @Override
    public XWootClientAPI createXWootClient(String url) throws XWootClientException
    {
        throw new XWootClientException("bad constructor for this type ofxwootClient");
    }

}
