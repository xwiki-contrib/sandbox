package org.xwiki.opensocial.component.internal;

import org.apache.shindig.auth.AnonymousSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.social.core.model.PersonImpl;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.jmock.core.constraint.IsEqual;
import org.jmock.Mock;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.opensocial.component.HelloWorld;
import org.xwiki.test.AbstractXWikiComponentTestCase;

public class DefaultHelloWorldTest extends AbstractXWikiComponentTestCase
{
    private Mock mockDocumentAccessBridge;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractXWikiComponentTestCase#registerComponents()
     */
    @Override
    protected void registerComponents() throws Exception
    {
        this.mockDocumentAccessBridge = mock(DocumentAccessBridge.class);
        DefaultComponentDescriptor<DocumentAccessBridge> descriptor =
            new DefaultComponentDescriptor<DocumentAccessBridge>();
        descriptor.setRole(DocumentAccessBridge.class);
        getComponentManager().registerComponent(descriptor,
            (DocumentAccessBridge) this.mockDocumentAccessBridge.proxy());
    }

    /**
     * Testing the hello
     * 
     * @throws Exception
     */
    public void testSayHello() throws Exception
    {
        String expected = "Hello World! true";

        DefaultHelloWorld hello = (DefaultHelloWorld) getComponentManager().lookup(HelloWorld.class, "default");
        mockDocumentAccessBridge.expects(once()).method("isDocumentViewable").will(returnValue(true));
        hello.setDocumentAccessBridge((DocumentAccessBridge) mockDocumentAccessBridge.proxy());

        String actual = hello.sayHello();

        assertEquals(expected, actual);
    }

    /**
     * @throws Exception
     */
    public void testGetPerson() throws Exception
    {
        Person expected = new PersonImpl();
        expected.setId("XWiki.anamarias");
        Name name = new NameImpl();
        name.setGivenName("Anamaria");
        name.setFamilyName("Stoica");
        expected.setName(name);

        String userId = "XWiki.anamarias";
        UserId uid = new UserId(UserId.Type.userId, userId);
        SecurityToken token = new AnonymousSecurityToken();

        DefaultHelloWorld hello = (DefaultHelloWorld) getComponentManager().lookup(HelloWorld.class, "default");
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.anamarias"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("Anamaria"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.anamarias"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("last_name")).will(returnValue("Stoica"));
        hello.setDocumentAccessBridge((DocumentAccessBridge) mockDocumentAccessBridge.proxy());

        Person actual = hello.getPerson(uid, null, token).get();
        assertEquals(expected.getId(), actual.getId());
        assertNotNull(actual.getName());
        assertEquals(expected.getName().getGivenName(), actual.getName().getGivenName());
        assertEquals(expected.getName().getFamilyName(), actual.getName().getFamilyName());

        // assertEquals(expected, actual);
    }
}
