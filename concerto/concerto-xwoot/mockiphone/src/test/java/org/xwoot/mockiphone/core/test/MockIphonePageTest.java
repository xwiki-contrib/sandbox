package org.xwoot.mockiphone.core.test;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.xwoot.mockiphone.core.MockIphonePage;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;


public class MockIphonePageTest
{
    
    @Test
    public void testtoXml() throws WikiContentManagerException{
        String pageName="test.essai";
        WikiContentManager wcm=WikiContentManagerFactory.getMockFactory().createWCM();
        Map pageMap=wcm.createPage(pageName, "Some datas");
        System.out.println(pageMap);
        MockIphonePage mPage=new MockIphonePage(pageName,pageMap,false,false);
       // String content=mPage.toXML();
        String content2="<LocalPage>  <XWikiPage>    <entry>      <string>id</string>      <string>test.WebHome</string>    </entry>    <entry>      <string>content</string>      <string>Yopla </string>    </entry>    <entry>      <string>modifier</string>      <string>XWiki.Admin</string>    </entry>    <entry>      <string>parentId</string>      <string></string>    </entry>    <entry>      <string>title</string>      <string>WebHome</string>    </entry>    <entry>      <string>created</string>      <string>Tue Oct 21 15:18:04 CEST 2008</string>    </entry>    <entry>      <string>space</string>      <string>test</string>    </entry>    <entry>      <string>homePage</string>      <string>true</string>    </entry>    <entry>      <string>creator</string>      <string>XWiki.Admin</string>    </entry>  </XWikiPage></LocalPage>";
        mPage.setContent(content2);
        Assert.assertNotNull(content2);
        //TODO ...
        
    }

}
