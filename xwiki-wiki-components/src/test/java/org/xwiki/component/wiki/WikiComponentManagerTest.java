package org.xwiki.component.wiki;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.wiki.internal.DefaultWikiComponent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.AbstractComponentTestCase;

public class WikiComponentManagerTest extends AbstractComponentTestCase
{    
    private WikiComponentManager manager;

    @Before
    public void setUp() throws Exception
    {
        manager = getComponentManager().lookup(WikiComponentManager.class);
    }

    @Test
    public void testRegisterWikiComponent() throws Exception
    {
        DocumentReference pseudoReference = new DocumentReference("somewiki","XWiki","MyComponent");

        DefaultWikiComponent wc = new DefaultWikiComponent(pseudoReference, WikiComponentManager.class, "test");
        
        this.manager.registerWikiComponent(wc);
        
        WikiComponentManager registered = this.getComponentManager().lookup(WikiComponentManager.class, "test");
        
        Assert.assertNotNull(registered);
    }
}
