package com.xwiki.authentication.puma;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;


public class PUMAConfigTest
{
    private Mockery mockery = new Mockery()
    {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private PUMAConfig config;
    
    private XWiki xwikiMock;
    
    private XWikiContext context;

    @Before
    public void setUp() throws Exception
    {
        this.xwikiMock = this.mockery.mock(XWiki.class);

        this.context = new XWikiContext();
        this.context.setWiki(this.xwikiMock);
        
        this.config = new PUMAConfig();
    }

    @After
    public void tearDown()
    {
        this.mockery.assertIsSatisfied();
    }

    @Test
    public void testGetGroupMappingsWithOneCouple() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_groupsMapping", context); will(returnValue("xwikigroup=pumagroup"));
        }});

        Map<String, Collection<String>> groupMapping = this.config.getGroupMappings(this.context);

        Assert.assertEquals(new HashSet<String>(Arrays.asList("xwikigroup")), groupMapping.get("pumagroup"));
    }
    
    @Test
    public void testGetGroupMappingsWithTwoCouples() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_groupsMapping", context); will(returnValue("xwikigroup=pumagroup|xwikigroup2=pumagroup2"));
        }});

        Map<String, Collection<String>> groupMapping = this.config.getGroupMappings(this.context);

        Assert.assertEquals(new HashSet<String>(Arrays.asList("xwikigroup")), groupMapping.get("pumagroup"));
        Assert.assertEquals(new HashSet<String>(Arrays.asList("xwikigroup2")), groupMapping.get("pumagroup2"));
    }
}
