package org.xwoot.iwoot.xwootclient;

import org.xwoot.wikiContentManager.WikiContentManager;

public abstract class XWootClientFactory
{
    private final static String SERVLET = "org.xwoot.iwoot.xwootclient.servlet.XWootClientServletFactory";

    private final static String MOCK =
        "org.xwoot.iwoot.xwootclient.mock.XWootClientMockFactory";

    // private static String SWIZZLE="";

    private static String MOCKLOCATION = XWootClientFactory.MOCK;

    private static String SERVLETLOCATION = XWootClientFactory.SERVLET;

    public static XWootClientFactory getMockFactory() throws XWootClientException
    {
        try {
            return (XWootClientFactory) Class.forName(XWootClientFactory.MOCKLOCATION).newInstance();
        } catch (Exception e) {
            throw new XWootClientException("Problem with wiki content manager factory", e);
        }
    }

    public static XWootClientFactory getServletFactory() throws XWootClientException
    {
        try {
            return (XWootClientFactory) Class.forName(XWootClientFactory.SERVLETLOCATION).newInstance();
        } catch (Exception e) {
            throw new XWootClientException("Problem with wiki content manager factory", e);
        }
    }
    
    public abstract XWootClientAPI createXWootClient(String url) throws XWootClientException;

    public abstract XWootClientAPI createXWootClient(WikiContentManager wcm) throws XWootClientException;    

}
