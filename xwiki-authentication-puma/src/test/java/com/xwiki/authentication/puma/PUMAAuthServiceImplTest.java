package com.xwiki.authentication.puma;

import java.util.Arrays;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.ibm.portal.puma.Group;
import com.ibm.portal.um.PumaLocator;
import com.ibm.portal.um.PumaProfile;
import com.ibm.portal.um.User;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.user.api.XWikiGroupService;

public class PUMAAuthServiceImplTest extends AbstractBridgedComponentTestCase
{
    private Mockery mockery = new Mockery()
    {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private XWiki xwikiMock;

    private XWikiGroupService xwikiGroupServiceMock;
    
    private PumaLocator pumaLocatorMock;
    
    private PumaProfile pumaProfileMock;
    
    private User pumaUserMock;
    
    private PUMAAuthServiceImpl authService;

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
        
        this.pumaLocatorMock = this.mockery.mock(PumaLocator.class);
        
        this.pumaProfileMock = this.mockery.mock(PumaProfile.class);
        
        this.pumaUserMock = this.mockery.mock(User.class);
        
        getContext().setWiki(this.xwikiMock);
        getContext().setDatabase("xwiki");

        this.authService = new PUMAAuthServiceImpl();

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
    public void testSyncGroupsMembershipFromPUMA() throws Exception
    {
        final Group pumaGroup1 = this.mockery.mock(Group.class, "group1");
        final Group pumaGroup2 = this.mockery.mock(Group.class, "group2");
        
        this.mockery.checking(new Expectations()
        {{
            // PUMAAuthServiceImpl#syncGroupsMembershipFromPUMA
            allowing(xwikiMock).getXWikiPreference(with(any(String.class)), with(any(XWikiContext.class))); will(returnValue(group.getFullName() + "=cn=group,ou=groups,o=xwiki,c=org"
                    + "|XWiki.group2=cn=group2,ou=groups,o=xwiki,c=org"));
            allowing(pumaLocatorMock).findGroupsByPrincipal(pumaUserMock, true); will(returnValue(Arrays.asList(pumaGroup1, pumaGroup2)));
            allowing(pumaProfileMock).getIdentifier(pumaGroup1); will(returnValue("cn=group,ou=groups,o=xwiki,c=org"));
            allowing(pumaProfileMock).getIdentifier(pumaGroup2); will(returnValue("cn=group3,ou=groups,o=xwiki,c=org"));
            // AbstractAuthServiceImpl#syncGroupsMembership
            allowing(xwikiMock).getGroupService(getContext()); will(returnValue(xwikiGroupServiceMock));
            allowing(xwikiMock).getXClass(groupClass.getDocumentReference(), getContext()); will(returnValue(groupClass));
            allowing(xwikiGroupServiceMock).getAllGroupsNamesForMember(user.getFullName(), 0, 0, getContext()); will(returnValue(Arrays.<String> asList()));
            // user added to the group
            oneOf(xwikiMock).getDocument(group.getFullName(), getContext()); will(returnValue(group));
            oneOf(xwikiMock).getGroupClass(getContext()); will(returnValue(groupClass));
            oneOf(xwikiMock).saveDocument(group, getContext());
        }});

        this.authService.syncGroupsMembershipFromPUMA(this.user, this.pumaUserMock, this.pumaLocatorMock, this.pumaProfileMock, getContext());
    }
    
    @Test
    public void testSyncGroupsMembershipFromPUMAWhenAlreadySynced() throws Exception
    {
        final Group pumaGroup1 = this.mockery.mock(Group.class, "group1");
        final Group pumaGroup2 = this.mockery.mock(Group.class, "group2");
        
        this.mockery.checking(new Expectations()
        {{
            // PUMAAuthServiceImpl#syncGroupsMembershipFromPUMA
            allowing(xwikiMock).getXWikiPreference(with(any(String.class)), with(any(XWikiContext.class))); will(returnValue(group.getFullName() + "=cn=group,ou=groups,o=xwiki,c=org"
                + "|XWiki.group2=cn=group2,ou=groups,o=xwiki,c=org"));
            allowing(pumaLocatorMock).findGroupsByPrincipal(pumaUserMock, true); will(returnValue(Arrays.asList(pumaGroup1, pumaGroup2)));
            allowing(pumaProfileMock).getIdentifier(pumaGroup1); will(returnValue("cn=group,ou=groups,o=xwiki,c=org"));
            allowing(pumaProfileMock).getIdentifier(pumaGroup2); will(returnValue("cn=group3,ou=groups,o=xwiki,c=org"));
            // AbstractAuthServiceImpl#syncGroupsMembership
            allowing(xwikiMock).getGroupService(getContext()); will(returnValue(xwikiGroupServiceMock));
            allowing(xwikiMock).getXClass(groupClass.getDocumentReference(), getContext()); will(returnValue(groupClass));
            allowing(xwikiGroupServiceMock).getAllGroupsNamesForMember(user.getFullName(), 0, 0, getContext()); will(returnValue(Arrays.<String> asList(group.getFullName())));
        }});

        this.groupObject.setStringValue("member", this.user.getFullName());

        this.authService.syncGroupsMembershipFromPUMA(this.user, this.pumaUserMock, this.pumaLocatorMock, this.pumaProfileMock, getContext());
    }
}
