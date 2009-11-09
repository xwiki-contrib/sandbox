package org.xwoot.wikiContentManager;

public abstract class WikiContentManagerFactory
{

    private final static String SWIZZLE = "org.xwoot.wikiContentManager.XWikiSwizzleClient.XWikiSwizzleClientFactory";

    private final static String MOCK =
        "org.xwoot.wikiContentManager.mockWikiContentManager.MockWikiContentManagerFactory";

    // private static String SWIZZLE="";

    private static String MOCKLOCATION = WikiContentManagerFactory.MOCK;

    private static String SWIZZLELOCATION = WikiContentManagerFactory.SWIZZLE;

    public static WikiContentManagerFactory getMockFactory() throws WikiContentManagerException
    {
        try {
            return (WikiContentManagerFactory) Class.forName(WikiContentManagerFactory.MOCKLOCATION).newInstance();
        } catch (Exception e) {
            throw new WikiContentManagerException("Problem with wiki content manager factory", e);
        }
    }

    public static WikiContentManagerFactory getSwizzleFactory() throws WikiContentManagerException
    {
        try {
            return (WikiContentManagerFactory) Class.forName(WikiContentManagerFactory.SWIZZLELOCATION).newInstance();
        } catch (Exception e) {
            throw new WikiContentManagerException("Problem with wiki content manager factory", e);
        }
    }

    public abstract WikiContentManager createWCM() throws WikiContentManagerException;

    public abstract WikiContentManager createWCM(String path) throws WikiContentManagerException;

    public abstract WikiContentManager createWCM(String url, String login, String pwd)
        throws WikiContentManagerException;
}
