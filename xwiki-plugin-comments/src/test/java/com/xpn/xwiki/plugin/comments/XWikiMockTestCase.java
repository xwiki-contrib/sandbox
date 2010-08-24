package com.xpn.xwiki.plugin.comments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.stub.CustomStub;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.notify.XWikiNotificationManager;
import com.xpn.xwiki.plugin.XWikiPluginManager;
import com.xpn.xwiki.store.XWikiHibernateStore;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.test.AbstractXWikiComponentTestCase;
import com.xpn.xwiki.user.api.XWikiRightService;

public abstract class XWikiMockTestCase extends AbstractXWikiComponentTestCase
{

    protected XWikiContext context;

    protected XWiki xwiki;

    protected Mock mockXWikiStore;

    protected Mock mockXWikiRightService;

    protected Mock mockXWiki;

    protected Map docs = new HashMap();
    protected Map commentsObj = new HashMap();
    protected XWikiPluginManager pluginManager = new XWikiPluginManager();

    protected void setUp() throws Exception
    {
        super.setUp();
        context = new XWikiContext();
        xwiki = new XWiki();
        context.setWiki(xwiki);
        mockXWikiStore =
            mock(XWikiHibernateStore.class, new Class[] {XWiki.class, XWikiContext.class},
                new Object[] {xwiki, context});
        mockXWikiStore.stubs().method("loadXWikiDoc").will(
            new CustomStub("Implements XWikiStoreInterface.loadXWikiDoc")
            {
                public Object invoke(Invocation invocation) throws Throwable
                {
                    XWikiDocument shallowDoc = (XWikiDocument) invocation.parameterValues.get(0);

                    if (docs.containsKey(shallowDoc.getFullName())) {
                        return (XWikiDocument) docs.get(shallowDoc.getFullName());
                    } else {
                        return shallowDoc;
                    }
                }
            });
        this.mockXWikiStore.stubs().method("saveXWikiDoc").will(
            new CustomStub("Implements XWikiStoreInterface.saveXWikiDoc")
            {
                public Object invoke(Invocation invocation) throws Throwable
                {
                    XWikiDocument document = (XWikiDocument) invocation.parameterValues.get(0);
                    document.setNew(false);
                    document.setStore((XWikiStoreInterface) mockXWikiStore.proxy());
                    docs.put(document.getFullName(), document);
                    return null;
                }
            });
        this.mockXWikiStore.stubs().method("deleteXWikiDoc").will(
            new CustomStub("Implements XWikiStoreInterface.deleteXWikiDoc")
            {
                public Object invoke(Invocation invocation) throws Throwable
                {
                    XWikiDocument document = (XWikiDocument) invocation.parameterValues.get(0);
                    // delete the document from the map
                    String documentKey = document.getFullName();
                    if (!document.getLanguage().equals("")) {
                        documentKey += "." + document.getLanguage();
                    }
                    docs.remove(documentKey);
                    return null;
                }
            });
        this.mockXWikiStore.stubs().method("searchDocumentsNames").will(
            new CustomStub("Implements XWikiStoreInterface.searchDocumentsNames")
            {
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<String> names = new ArrayList<String>();
                    return names;
                }
            });
        this.mockXWikiStore.stubs().method("getTranslationList").will(returnValue(new ArrayList<Object>()));

        mockXWikiRightService = mock(XWikiRightService.class, new Class[] {}, new Object[] {});
        mockXWikiRightService.stubs().method("hasAccessLevel").withAnyArguments().will(returnValue(true));

        xwiki.setConfig(new XWikiConfig());
        xwiki.setNotificationManager(new XWikiNotificationManager());
        
        xwiki.setStore((XWikiStoreInterface) mockXWikiStore.proxy());
        xwiki.setRightService((XWikiRightService) mockXWikiRightService.proxy());
        initPlugin();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    protected void initPlugin()
    {
        xwiki.setPluginManager(pluginManager);
        pluginManager.addPlugin("comments", CommentsPlugin.class.getName(), context);
    }
}
