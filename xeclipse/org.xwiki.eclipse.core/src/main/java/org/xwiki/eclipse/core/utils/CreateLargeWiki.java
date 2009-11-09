package org.xwiki.eclipse.core.utils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.swizzle.confluence.Space;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiPage;

//FIXME: Delete this.
// Class to assist creation of a large wiki to test feasibility of lazy retrieval of data. 

public class CreateLargeWiki
{
    private static XWikiXmlRpcClient rpc;

    public static void main(String[] args) throws MalformedURLException, XmlRpcException
    {
        String url = "http://127.0.0.1:8080/xwiki/xmlrpc/confluence";
        String user = "vnkatesh";
        String pass = "password";
        rpc = new XWikiXmlRpcClient(url);
        rpc.login(user, pass);
        for (int i = 0; i < 564; i++) {
            String random = getRandom(8);
            try {
                // createPage("Main", random, random, null, random);
                createSpace(random);
                System.out.println(".");
            } catch (XmlRpcException e) {
                // TODO: handle exception
            }

        }
    }

    public static void createSpace(String name) throws XmlRpcException
    {
        java.util.HashMap map = new java.util.HashMap(2);
        map.put("key", name);
        map.put("name", name);
        rpc.addSpace(new Space(map));
    }

    public static String getRandom(int n)
    {
        char[] pw = new char[n];
        int c = 'A';
        int r1 = 0;
        for (int i = 0; i < n; i++) {
            r1 = (int) (Math.random() * 3);
            switch (r1) {
                case 0:
                    c = '0' + (int) (Math.random() * 10);
                    break;
                case 1:
                    c = 'a' + (int) (Math.random() * 26);
                    break;
                case 2:
                    c = 'A' + (int) (Math.random() * 26);
                    break;
            }
            pw[i] = (char) c;
        }
        return new String(pw);
    }

    public static void createPage(String spaceKey, String name, String title, String language, String content)
        throws XWikiEclipseException, XmlRpcException
    {
        XWikiPage xwikiPage = new XWikiPage();
        xwikiPage.setSpace(spaceKey);
        xwikiPage.setTitle(title);
        if (language != null) {
            xwikiPage.setId(String.format("%s.%s?language=%s", spaceKey, name, language));
        } else {
            xwikiPage.setId(String.format("%s.%s", spaceKey, name));
        }
        xwikiPage.setContent(content);
        xwikiPage.setVersion(1);
        xwikiPage.setMinorVersion(1);
        xwikiPage.setContentStatus("");
        xwikiPage.setCreated(new Date());
        xwikiPage.setCreator("");
        if (language != null) {
            xwikiPage.setLanguage(language);
        } else {
            xwikiPage.setLanguage("");
        }
        xwikiPage.setModified(new Date());
        xwikiPage.setModifier("");
        xwikiPage.setParentId("");
        xwikiPage.setTranslations(new ArrayList<String>());
        xwikiPage.setUrl("");

        rpc.storePage(xwikiPage);
    }

}
