package org.xwoot.mockiphone.core;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.xwoot.wikiContentManager.WikiContentManager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class MockIphonePage implements Serializable
{
    
    private static final long serialVersionUID = 6228704590406225117L;
    private String pageName;
    private Map content;
    private boolean isModified;
    private boolean isRemoved;
    
    public MockIphonePage(String pageName, Map content, boolean isModified, boolean isRemoved)
    {
        this.pageName = pageName;
        this.content = content;
        this.isModified = isModified;
        this.isRemoved = isRemoved;
    }
    
    public String getPageName()
    {
        return this.pageName;
    }

    public void setPageName(String pageName)
    {
        this.pageName = pageName;
    }

    public Map getContent()
    {
        return this.content;
    }

    public void setContent(Map content)
    {
        this.content = content;
        this.setModified(true);
    }

    public boolean isModified()
    {
        return this.isModified;
    }

    public void setModified(boolean isModified)
    {
        this.isModified = isModified;
    }

    public boolean isRemoved()
    {
        return this.isRemoved;
    }

    public void setRemoved(boolean isRemoved)
    {
        this.isRemoved = isRemoved;
        this.setModified(true);
    }
    
    public String toXML()
    {
       
       //XStream xstream = new XStream(new JettisonMappedXmlDriver());
       // XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
       XStream xstream = new XStream();
       xstream.omitField(MockIphonePage.class, "isModified");
       xstream.omitField(MockIphonePage.class, "isRemoved");
       xstream.omitField(MockIphonePage.class, "pageName");
       xstream.aliasField("XWikiPage",MockIphonePage.class, "content");
      // xstream.useAttributeFor(MockIphonePage.class, "pageName");
       xstream.alias("LocalPage", MockIphonePage.class);
       
       return xstream.toXML(this);     
    }
    
    public static String getTemplate(){ 
        // create the new Page
        Map newPage = new Hashtable();
        newPage.put(WikiContentManager.SPACE, "template");
        newPage.put(WikiContentManager.ID, "template.page");
        newPage.put(WikiContentManager.CONTENT, "template content");
        newPage.put(WikiContentManager.TITLE, "page");
        MockIphonePage page=new MockIphonePage("",newPage,false,false);
        return page.toXML();
    }

    public void setContent(String xmlContent)
    {
        XStream xstream = new XStream(new DomDriver());
        xstream.aliasField("XWikiPage",MockIphonePage.class, "content");
        xstream.alias("LocalPage", MockIphonePage.class);
        MockIphonePage temp=(MockIphonePage)xstream.fromXML(xmlContent);
        this.setContent(temp.getContent());
        this.setPageName((String) this.getContent().get(WikiContentManager.ID));
        this.setModified(true);
    }
   
}
