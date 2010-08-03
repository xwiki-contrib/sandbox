package com.xwiki.authentication;

import java.security.Principal;
import java.util.Arrays;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.securityfilter.realm.SimplePrincipal;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.user.api.XWikiGroupService;

public class AuthServiceImplTest extends AbstractBridgedComponentTestCase
{
    private Mockery mockery = new Mockery()
    {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private XWiki xwikiMock;
    
    private XWikiGroupService xwikiGroupServiceMock;

    private AbstractAuthServiceImpl authService;

    private XWikiDocument user;
    
    private XWikiDocument group;
    private BaseObject groupObject;
    
    private BaseClass groupClass;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.xwikiMock = this.mockery.mock(XWiki.class);

        this.xwikiGroupServiceMock = this.mockery.mock(XWikiGroupService.class);
        
        getContext().setWiki(this.xwikiMock);
        getContext().setDatabase("xwiki");

        this.authService = new AbstractAuthServiceImpl()
        {
            @Override
            protected Principal authenticateInContext(boolean local, XWikiContext context) throws XWikiException
            {
                return new SimplePrincipal("principal");
            }
        };

        this.user = new XWikiDocument(new DocumentReference("xwiki", "XWiki", "user"));
        BaseObject userObject = new BaseObject();
        userObject.setClassName("XWiki.XWikiUser");
        this.user.addXObject(userObject);

        this.groupClass = new BaseClass(){
            public BaseCollection fromMap(Map<String, ? extends Object> map, BaseCollection object)
            {
                object.setStringValue("member", (String) map.get("member"));
                
                return object;
            }
        };
        this.groupClass.setName("XWiki.XWikiGroups");
        
        this.group = new XWikiDocument(new DocumentReference("xwiki", "XWiki", "group"));
        this.groupObject = new BaseObject();
        this.groupObject.setClassName("XWiki.XWikiGroups");
        this.groupObject.setStringValue("member", this.user.getFullName());
        this.group.addXObject(this.groupObject);
    }

    @After
    public void tearDown()
    {
        this.mockery.assertIsSatisfied();
    }

    @Test
    public void testSyncGroupsMembership() throws Exception
    {
        this.mockery.checking(new Expectations()
        {{
            allowing(xwikiMock).getGroupService(getContext()); will(returnValue(xwikiGroupServiceMock));
            allowing(xwikiMock).getXClass(groupClass.getDocumentReference(), getContext()); will(returnValue(groupClass));
            allowing(xwikiGroupServiceMock).getAllGroupsNamesForMember(user.getFullName(), 0, 0, getContext()); will(returnValue(Arrays.<String> asList()));
            oneOf(xwikiMock).getDocument(group.getFullName(), getContext()); will(returnValue(group));
            oneOf(xwikiMock).getGroupClass(getContext()); will(returnValue(groupClass));
            oneOf(xwikiMock).saveDocument(group, getContext());
        }});

        this.authService.syncGroupsMembership(this.user, Arrays.asList(group.getFullName()), Arrays.<String> asList(),
            getContext());
    }
    
    @Test
    public void testSyncGroupsMembershipWhenAlreadySynced() throws Exception
    {
        this.mockery.checking(new Expectations()
        {{
            allowing(xwikiMock).getGroupService(getContext()); will(returnValue(xwikiGroupServiceMock));
            allowing(xwikiMock).getXClass(groupClass.getDocumentReference(), getContext()); will(returnValue(groupClass));
            allowing(xwikiGroupServiceMock).getAllGroupsNamesForMember(user.getFullName(), 0, 0, getContext()); will(returnValue(Arrays.<String> asList(group.getFullName())));
        }});

        this.groupObject.setStringValue("member", this.user.getFullName());

        this.authService.syncGroupsMembership(this.user, Arrays.asList(group.getFullName()), Arrays.<String> asList(),
            getContext());
    }
}
