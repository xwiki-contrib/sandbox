/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.xwiki.opensocial.social.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.AnonymousSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.core.model.AddressImpl;
import org.apache.shindig.social.core.model.ListFieldImpl;
import org.apache.shindig.social.core.model.OrganizationImpl;
import org.apache.shindig.social.opensocial.model.Address;
import org.apache.shindig.social.opensocial.model.ListField;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Organization;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.jmock.Mock;
import org.jmock.core.constraint.IsAnything;
import org.jmock.core.constraint.IsEqual;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentName;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.opensocial.social.model.NameXW;
import org.xwiki.opensocial.social.model.PersonXW;
import org.xwiki.opensocial.social.spi.SocialServiceComponent;
import org.xwiki.opensocial.social.spi.internal.PersonServiceXW;
import org.xwiki.test.AbstractXWikiComponentTestCase;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PersonServiceXWTest extends AbstractXWikiComponentTestCase
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
     * @throws Exception
     */
    public void testGetPersonBasic() throws Exception
    {
        Person expected = new PersonXW();
        expected.setId("XWiki.Jane");
        Name name = new NameXW();
        name.setGivenName("Jane");
        name.setFamilyName("Doe");
        expected.setName(name);

        String userId = "XWiki.Jane";
        UserId uid = new UserId(UserId.Type.userId, userId);
        SecurityToken token = new AnonymousSecurityToken();

        PersonServiceXW personService =
            (PersonServiceXW) getComponentManager().lookup(SocialServiceComponent.class, "PersonServiceXW");
        // set values
        mockDocumentAccessBridge.expects(once()).method("exists").with(new IsEqual("XWiki.Jane")).will(
            returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentName").with(new IsEqual("XWiki.Jane")).will(
            returnValue(new DocumentName("wiki", "XWiki", "Jane")));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("Jane"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("last_name")).will(returnValue("Doe"));
        // unset values
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("avatar")).will(returnValue(null));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blog")).will(returnValue(null));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blogfeed")).will(returnValue(null));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("city")).will(returnValue(null));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("country")).will(returnValue(null));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("comment")).will(returnValue(null));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("company")).will(returnValue(null));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("email")).will(returnValue(null));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imaccount")).will(returnValue(null));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imtype")).will(returnValue(null));
        personService.setDocumentAccessBridge((DocumentAccessBridge) mockDocumentAccessBridge.proxy());

        Person actual = personService.getPerson(uid, Collections.<String> emptySet(), token).get();

        assertEquals(expected.getId(), actual.getId());
        assertNotNull(actual.getName());
        assertEquals(expected.getName().getGivenName(), actual.getName().getGivenName());
        assertEquals(expected.getName().getFamilyName(), actual.getName().getFamilyName());

        // assertEquals(expected, actual);
    }

    /**
     * @throws Exception
     */
    public void testGetPerson() throws Exception
    {
        Person expected = new PersonXW();
        expected.setId("XWiki.Jane");
        Name name = new NameXW();
        name.setGivenName("Jane");
        name.setFamilyName("Doe");
        expected.setName(name);
        expected.setThumbnailUrl("http://xwiki.org/xwiki/bin/download/XWiki/Jane/thumb.jpg");
        ((PersonXW) expected).setBlogUrl("http://janesblog.com");
        ((PersonXW) expected).setBlogfeedUrl("http://janesblog.com/feed");
        Address address = new AddressImpl();
        address.setLocality("Paris");
        address.setCountry("France");
        expected.setCurrentLocation(address);
        expected.setAboutMe("Jane's description");
        Organization org = new OrganizationImpl();
        org.setName("XWiki");
        expected.setOrganizations(Lists.<Organization> newArrayList(org));
        expected.setEmails(Lists.<ListField> newArrayList(new ListFieldImpl("work", "jane@xwiki.org")));
        expected.setIms(Lists.<ListField> newArrayList(new ListFieldImpl("gtalk", "jane.doe")));
        expected.setNickname("Jane");

        String userId = "XWiki.Jane";
        UserId uid = new UserId(UserId.Type.userId, userId);
        SecurityToken token = new AnonymousSecurityToken();

        PersonServiceXW personService =
            (PersonServiceXW) getComponentManager().lookup(SocialServiceComponent.class, "PersonServiceXW");
        // set values
        mockDocumentAccessBridge.expects(once()).method("exists").with(new IsEqual("XWiki.Jane")).will(
            returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentName").with(new IsEqual("XWiki.Jane")).will(
            returnValue(new DocumentName("wiki", "XWiki", "Jane")));
        mockDocumentAccessBridge.expects(once()).method("getAttachmentURL").with(new IsEqual("XWiki.Jane"),
            new IsEqual("thumb.jpg")).will(returnValue("http://xwiki.org/xwiki/bin/download/XWiki/Jane/thumb.jpg"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("Jane"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("last_name")).will(returnValue("Doe"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("avatar")).will(returnValue("thumb.jpg"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blog")).will(returnValue("http://janesblog.com"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blogfeed")).will(returnValue("http://janesblog.com/feed"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("city")).will(returnValue("Paris"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("country")).will(returnValue("France"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("comment")).will(returnValue("Jane's description"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("company")).will(returnValue("XWiki"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("email")).will(returnValue("jane@xwiki.org"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imaccount")).will(returnValue("jane.doe"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imtype")).will(returnValue("gtalk"));
        personService.setDocumentAccessBridge((DocumentAccessBridge) mockDocumentAccessBridge.proxy());

        Person actual = personService.getPerson(uid, Collections.<String> emptySet(), token).get();

        assertPersonEquals(actual, expected);
    }

    /**
     * Test get people for a list of 2 users: Jane and John (GroupId.Type.self)
     * 
     * @throws Exception
     */
    public void testGetPeopleSelf() throws Exception
    {
        // expected results
        // Jane
        Person jane = new PersonXW();
        jane.setId("XWiki.Jane");
        Name name1 = new NameXW();
        name1.setGivenName("Jane");
        jane.setName(name1);
        // John
        Person john = new PersonXW();
        john.setId("XWiki.John");
        Name name2 = new NameXW();
        name2.setGivenName("John");
        john.setName(name2);

        Set<UserId> userIds = buildUserIds("XWiki.Jane", "XWiki.John");
        GroupId groupId = new GroupId(GroupId.Type.self, null);
        SecurityToken token = new AnonymousSecurityToken();

        PersonServiceXW personService =
            (PersonServiceXW) getComponentManager().lookup(SocialServiceComponent.class, "PersonServiceXW");
        // set values for Jane
        mockDocumentAccessBridge.expects(once()).method("exists").with(new IsEqual("XWiki.Jane")).will(
            returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentName").with(new IsEqual("XWiki.Jane")).will(
            returnValue(new DocumentName("wiki", "XWiki", "Jane")));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("Jane"));
        // set values for John
        mockDocumentAccessBridge.expects(once()).method("exists").with(new IsEqual("XWiki.John")).will(
            returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentName").with(new IsEqual("XWiki.John")).will(
            returnValue(new DocumentName("wiki", "XWiki", "John")));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.John"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("John"));
        // unset values
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("last_name")).will(returnValue("Doe"));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("avatar")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blog")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blogfeed")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("city")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("country")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("comment")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("company")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("email")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imaccount")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imtype")).will(returnValue(null));
        personService.setDocumentAccessBridge((DocumentAccessBridge) mockDocumentAccessBridge.proxy());

        // get the actual result
        CollectionOptions options = new CollectionOptions();
        options.setSortBy("name");
        options.setFirst(0);
        options.setMax(20);
        Future<RestfulCollection<Person>> actual =
            personService.getPeople(userIds, groupId, options, Collections.<String> emptySet(), token);
        RestfulCollection<Person> peopleCollection = actual.get();

        // assert results
        assertEquals(2, peopleCollection.getTotalResults());
        List<Person> people = peopleCollection.getEntry();
        Person actualJane = people.get(0);
        assertEquals(jane.getId(), actualJane.getId());
        assertNotNull(actualJane.getName());
        assertEquals(jane.getName().getGivenName(), actualJane.getName().getGivenName());
        Person actualJohn = people.get(1);
        assertEquals(john.getId(), actualJohn.getId());
        assertNotNull(actualJohn.getName());
        assertEquals(john.getName().getGivenName(), actualJohn.getName().getGivenName());
    }

    /**
     * Test get Jane's friends: Bob and Alice
     * 
     * @throws Exception
     */
    public void testGetPeopleFriends() throws Exception
    {
        Set<UserId> userIds = buildUserIds("XWiki.Jane");
        GroupId groupId = new GroupId(GroupId.Type.friends, null);
        SecurityToken token = new AnonymousSecurityToken();

        PersonServiceXW personService =
            (PersonServiceXW) getComponentManager().lookup(SocialServiceComponent.class, "PersonServiceXW");
        // friend properties values
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.FriendClass"), new IsEqual(0), new IsEqual("friendName")).will(returnValue("XWiki.Bob"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.FriendClass"), new IsEqual(1), new IsEqual("friendName")).will(
            returnValue("XWiki.Alice"));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.FriendClass"), new IsEqual(2), new IsEqual("friendName")).will(returnValue(null));
        // set values for Bob
        mockDocumentAccessBridge.expects(once()).method("exists").with(new IsEqual("XWiki.Bob"))
            .will(returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentName").with(new IsEqual("XWiki.Bob")).will(
            returnValue(new DocumentName("wiki", "XWiki", "Bob")));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Bob"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("Bob"));
        // set values for Alice
        mockDocumentAccessBridge.expects(once()).method("exists").with(new IsEqual("XWiki.Alice")).will(
            returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentName").with(new IsEqual("XWiki.Alice")).will(
            returnValue(new DocumentName("wiki", "XWiki", "Alice")));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Alice"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("Alice"));
        // unset values
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("last_name")).will(returnValue("Doe"));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("avatar")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blog")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blogfeed")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("city")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("country")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("comment")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("company")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("email")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imaccount")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(2)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imtype")).will(returnValue(null));
        personService.setDocumentAccessBridge((DocumentAccessBridge) mockDocumentAccessBridge.proxy());

        // get the actual result
        CollectionOptions options = new CollectionOptions();
        options.setSortBy("name");
        options.setFirst(0);
        options.setMax(20);
        Future<RestfulCollection<Person>> actual =
            personService.getPeople(userIds, groupId, options, Collections.<String> emptySet(), token);
        RestfulCollection<Person> peopleCollection = actual.get();

        // assert results
        assertEquals(2, peopleCollection.getTotalResults());
        List<Person> actual_people = peopleCollection.getEntry();
        assertEquals("XWiki.Alice", actual_people.get(0).getId());
        assertEquals("XWiki.Bob", actual_people.get(1).getId());
    }

    /**
     * Test get sorted list by name
     * 
     * @throws Exception
     */
    public void testGetPeopleSelfSortedByName() throws Exception
    {
        Set<UserId> userIds = buildUserIds("XWiki.John", "XWiki.Jane", "XWiki.Alice");
        GroupId groupId = new GroupId(GroupId.Type.self, null);
        SecurityToken token = new AnonymousSecurityToken();

        PersonServiceXW personService =
            (PersonServiceXW) getComponentManager().lookup(SocialServiceComponent.class, "PersonServiceXW");
        // set values for Jane
        mockDocumentAccessBridge.expects(once()).method("exists").with(new IsEqual("XWiki.Jane")).will(
            returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentName").with(new IsEqual("XWiki.Jane")).will(
            returnValue(new DocumentName("wiki", "XWiki", "Jane")));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Jane"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("Jane"));
        // set values for John
        mockDocumentAccessBridge.expects(once()).method("exists").with(new IsEqual("XWiki.John")).will(
            returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentName").with(new IsEqual("XWiki.John")).will(
            returnValue(new DocumentName("wiki", "XWiki", "John")));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.John"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("John"));
        // set values for Alice
        mockDocumentAccessBridge.expects(once()).method("exists").with(new IsEqual("XWiki.Alice")).will(
            returnValue(true));
        mockDocumentAccessBridge.expects(once()).method("getDocumentName").with(new IsEqual("XWiki.Alice")).will(
            returnValue(new DocumentName("wiki", "XWiki", "Alice")));
        mockDocumentAccessBridge.expects(once()).method("getProperty").with(new IsEqual("XWiki.Alice"),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("first_name")).will(returnValue("Alice"));
        // unset values
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("last_name")).will(returnValue("Doe"));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("avatar")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blog")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("blogfeed")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("city")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("country")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("comment")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("company")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("email")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imaccount")).will(returnValue(null));
        mockDocumentAccessBridge.expects(exactly(3)).method("getProperty").with(new IsAnything(),
            new IsEqual("XWiki.XWikiUsers"), new IsEqual("imtype")).will(returnValue(null));
        personService.setDocumentAccessBridge((DocumentAccessBridge) mockDocumentAccessBridge.proxy());

        // get the actual result
        CollectionOptions options = new CollectionOptions();
        options.setSortBy("name");
        options.setFirst(0);
        options.setMax(20);
        Future<RestfulCollection<Person>> actual =
            personService.getPeople(userIds, groupId, options, Collections.<String> emptySet(), token);
        RestfulCollection<Person> peopleCollection = actual.get();

        // assert results
        assertEquals(3, peopleCollection.getTotalResults());
        List<Person> people = peopleCollection.getEntry();
        assertEquals("XWiki.Alice", people.get(0).getId());
        assertEquals("XWiki.Jane", people.get(1).getId());
        assertEquals("XWiki.John", people.get(2).getId());
    }

    /*
     * Build userId set
     */
    public static Set<UserId> buildUserIds(String... userIds)
    {
        Set<UserId> userIdSet = Sets.newHashSet();
        for (String userId : userIds) {
            userIdSet.add(new UserId(UserId.Type.userId, userId));
        }
        return userIdSet;
    }

    public static void assertPersonEquals(Person actual, Person expected)
    {
        assertEquals(actual.getAboutMe(), expected.getAboutMe());
        assertAddressEquals(actual.getCurrentLocation(), expected.getCurrentLocation());
        assertEquals(actual.getDisplayName(), expected.getDisplayName());
        assertCollectionSizeEquals(actual.getEmails(), expected.getEmails());
        for (int i = 0; i < actual.getEmails().size(); i++) {
            assertListFieldEquals(actual.getEmails().get(i), expected.getEmails().get(i));
        }
        assertEquals(actual.getId(), expected.getId());
        assertCollectionSizeEquals(actual.getOrganizations(), expected.getOrganizations());
        for (int i = 0; i < actual.getOrganizations().size(); i++) {
            assertOrganizationEquals(actual.getOrganizations().get(i), expected.getOrganizations().get(i));
        }
        assertNameEquals(actual.getName(), expected.getName());
        assertEquals(actual.getNickname(), expected.getNickname());
        assertEquals(actual.getThumbnailUrl(), expected.getThumbnailUrl());
    }

    private static void assertAddressEquals(Address actual, Address expected)
    {
        assertEquals(actual.getCountry(), expected.getCountry());
        assertEquals(actual.getLocality(), expected.getLocality());
    }

    private static void assertCollectionSizeEquals(Collection< ? > actual, Collection< ? > expected)
    {
        assertTrue(actual != null && expected != null);
        assertEquals(actual.size(), expected.size());
    }

    private static void assertListFieldEquals(ListField actual, ListField expected)
    {
        assertEquals(actual.getType(), expected.getType());
        assertEquals(actual.getValue(), expected.getValue());
    }

    private static void assertOrganizationEquals(Organization actual, Organization expected)
    {
        assertEquals(actual.getName(), expected.getName());
    }

    private static void assertNameEquals(Name actual, Name expected)
    {
        assertEquals(actual.getFamilyName(), expected.getFamilyName());
        assertEquals(actual.getGivenName(), expected.getGivenName());
        assertEquals(actual.getFormatted(), expected.getFormatted());
    }
}
