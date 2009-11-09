package org.xwoot.wikiContentManager.XWikiSwizzleClient;

import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;

public class XWikiSwizzleClientFactory extends WikiContentManagerFactory
{

    static final String PATH =
        "./wikiContentManager/src/test/resources/xwiki.properties";

    @Override
    public WikiContentManager createWCM() throws WikiContentManagerException
    {
        throw new XWikiSwizzleClientException("bad constructor for this type of WCM (Swizzle)");
    }

    @Override
    public WikiContentManager createWCM(String path) throws WikiContentManagerException
    {
        return new XwikiSwizzleClient(path);
    }

    @Override
    public WikiContentManager createWCM(String URL, String login, String pwd) throws WikiContentManagerException
    {
        return new XwikiSwizzleClient(URL, login, pwd);
    }

}
