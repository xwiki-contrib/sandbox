package org.xwoot.wikiContentManager.mockWikiContentManager;

import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;

public class MockWikiContentManagerFactory extends WikiContentManagerFactory
{

    @Override
    public WikiContentManager createWCM() throws WikiContentManagerException
    {
        return new MockWikiContentManager();
    }

    @Override
    public WikiContentManager createWCM(String path) throws WikiContentManagerException
    {
        throw new MockWikiContentManagerException("bad constructor for this type of WCM (Mock)");
    }

    @Override
    public WikiContentManager createWCM(String URL, String login, String pwd) throws WikiContentManagerException
    {
        throw new MockWikiContentManagerException("bad constructor for this type of WCM (Mock)");
    }

}
