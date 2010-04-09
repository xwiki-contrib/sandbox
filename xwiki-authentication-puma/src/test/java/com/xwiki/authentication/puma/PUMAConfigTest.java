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
    public void testGetGroupMappingsWithoutProperty() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_groupsMapping", context); will(returnValue(null));
            allowing(xwikiMock).Param("xwiki.authentication.puma.groupsMapping"); will(returnValue(null));
        }});

        Map<String, Collection<String>> groupMapping = this.config.getGroupMapping(this.context);

        Assert.assertNull(groupMapping);
    }

    @Test
    public void testGetGroupMappingsWithEmptyProperty() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_groupsMapping", context); will(returnValue(""));
            allowing(xwikiMock).Param("xwiki.authentication.puma.groupsMapping"); will(returnValue(""));
        }});

        Map<String, Collection<String>> groupMapping = this.config.getGroupMapping(this.context);

        Assert.assertTrue(groupMapping.isEmpty());
    }

    @Test
    public void testGetGroupMappingsWithOneCouple() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_groupsMapping", context); will(returnValue("xwikigroup=pumagroup"));
        }});

        Map<String, Collection<String>> groupMapping = this.config.getGroupMapping(this.context);

        Assert.assertEquals(new HashSet<String>(Arrays.asList("xwikigroup")), groupMapping.get("pumagroup"));
    }
    
    @Test
    public void testGetGroupMappingsWithTwoCouples() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_groupsMapping", context); will(returnValue("xwikigroup=pumagroup|xwikigroup2=pumagroup2"));
        }});

        Map<String, Collection<String>> groupMapping = this.config.getGroupMapping(this.context);

        Assert.assertEquals(new HashSet<String>(Arrays.asList("xwikigroup")), groupMapping.get("pumagroup"));
        Assert.assertEquals(new HashSet<String>(Arrays.asList("xwikigroup2")), groupMapping.get("pumagroup2"));
    }
    
    @Test
    public void testGetGroupMappingsWithTwoCouplesMixed() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_groupsMapping", context); will(returnValue("xwikigroup=pumagroup|xwikigroup2=pumagroup|xwikigroup=pumagroup2|xwikigroup2=pumagroup2"));
        }});

        Map<String, Collection<String>> groupMapping = this.config.getGroupMapping(this.context);

        Assert.assertEquals(new HashSet<String>(Arrays.asList("xwikigroup", "xwikigroup2")), groupMapping.get("pumagroup"));
        Assert.assertEquals(new HashSet<String>(Arrays.asList("xwikigroup", "xwikigroup2")), groupMapping.get("pumagroup2"));
    }
    
    @Test
    public void testGetUserMappingsWithoutProperty() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_userMapping", context); will(returnValue(null));
            allowing(xwikiMock).Param("xwiki.authentication.puma.userMapping"); will(returnValue(null));
        }});

        Map<String, String> userMapping = this.config.getUserMapping(this.context);

        Assert.assertNull(userMapping);
    }

    @Test
    public void testGetUserMappingsWithEmptyProperty() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_userMapping", context); will(returnValue(""));
            allowing(xwikiMock).Param("xwiki.authentication.puma.userMapping"); will(returnValue(""));
        }});

        Map<String, String> userMapping = this.config.getUserMapping(this.context);

        Assert.assertTrue(userMapping.isEmpty());
    }
    
    @Test
    public void testGetUserMappingsWithOneCouple() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_userMapping", context); will(returnValue("xwikifield=pumagfield"));
        }});

        Map<String, String> userMapping = this.config.getUserMapping(this.context);

        Assert.assertEquals("pumagfield", userMapping.get("xwikifield"));
    }
    
    @Test
    public void testGetUserMappingsWithTwoCouples() throws Exception
    {
        this.mockery.checking(new Expectations() {{
            allowing(xwikiMock).getXWikiPreference("puma_userMapping", context); will(returnValue("xwikifield=pumagfield,xwikifield2=pumagfield2"));
        }});

        Map<String, String> userMapping = this.config.getUserMapping(this.context);

        Assert.assertEquals("pumagfield", userMapping.get("xwikifield"));
        Assert.assertEquals("pumagfield2", userMapping.get("xwikifield2"));
    }
}
