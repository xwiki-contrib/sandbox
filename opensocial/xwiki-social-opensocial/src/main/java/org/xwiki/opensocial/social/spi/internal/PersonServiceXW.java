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
package org.xwiki.opensocial.social.spi.internal;

import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.model.FilterOperation;
import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.social.core.model.AddressImpl;
import org.apache.shindig.social.core.model.ListFieldImpl;
import org.apache.shindig.social.core.model.OrganizationImpl;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.apache.shindig.social.opensocial.model.Address;
import org.apache.shindig.social.opensocial.model.ListField;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Organization;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentName;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.opensocial.social.model.NameXW;
import org.xwiki.opensocial.social.model.PersonXW;
import org.xwiki.opensocial.social.model.PersonXWComparator;
import org.xwiki.opensocial.social.spi.SocialServiceComponent;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

@Component("PersonServiceXW")
public class PersonServiceXW extends AbstractLogEnabled implements PersonService, SocialServiceComponent, Initializable
{

    /** Provides access to documents */
    @Requirement
    private DocumentAccessBridge documentAccessBridge;

    /**
     * Allows overriding the Document Access Bridge used (useful for unit tests).
     * 
     * @param documentAccessBridge the new Document Access Bridge to use
     */
    public void setDocumentAccessBridge(DocumentAccessBridge documentAccessBridge)
    {
        this.documentAccessBridge = documentAccessBridge;
    }

    /**
     * {@inheritDoc}
     */
    public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds, GroupId groupId,
        CollectionOptions collectionOptions, Set<String> fields, SecurityToken token) throws ProtocolException
    {
        try {
            List<Person> people = new ArrayList<Person>();
            switch (groupId.getType()) {
                case self:
                    for (UserId userId : userIds) {
                        Future<Person> person = getPerson(userId, fields, token);
                        people.add(person.get());
                    }
                    break;
                case friends:
                    for (UserId userId : userIds) {
                        String uid = userId.getUserId(token);
                        String friendUid = null;
                        int index = 0;

                        // get friends for user uid
                        while ((friendUid =
                            (String) documentAccessBridge.getProperty(uid, PersonXW.XWIKI_FRIEND_CLASS_NAME, index,
                                PersonXW.XWIKI_FRIEND_NAME_PROPERTY_NAME)) != null) {
                            // get friend with friendUid
                            Future<Person> person = getPerson(new UserId(UserId.Type.userId, friendUid), fields, token);
                            people.add(person.get());
                            index = index + 1;
                        }
                    }
                    break;
                case all:
                    throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not yet implemented", null);
                case groupId:
                    throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not yet implemented", null);
                case deleted:
                    throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not yet implemented", null);
                default:
                    throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Group ID not recognized", null);
            }

            if (GroupId.Type.self.equals(groupId.getType()) && people.isEmpty()) {
                throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Person not found");
            }

            // Collection Options
            // Filter Collection
            String filter = collectionOptions.getFilter();
            // check if special filter
            if (GroupId.Type.friends.equals(groupId.getType()) && filter != null) {
                if (PersonService.ALL_FILTER.equals(filter)) {
                    // default value: do nothing
                } else if (PersonService.TOP_FRIENDS_FILTER.equals(filter)) {
                    // TODO: implement
                } else if (PersonService.HAS_APP_FILTER.equals(filter)) {
                    // TODO: implement
                } else {
                    // field filters
                    FilterOperation filterOperation = collectionOptions.getFilterOperation();
                    String filterValue = collectionOptions.getFilterValue();
                    // TODO: implement
                }
            }

            // Sort Collection
            String sortBy = collectionOptions.getSortBy();
            SortOrder sortOrder = collectionOptions.getSortOrder();
            if (sortBy != null) {
                if (PersonService.TOP_FRIENDS_SORT.equals(sortBy)) {
                    // do nothing - assume already sorted by PersonService.TOP_FRIENDS_SORT
                    // TODO: implement
                } else {
                    // sort by field value
                    Person.Field sortByField = Person.Field.getField(sortBy);
                    Collections.sort(people, new PersonXWComparator(sortByField, sortOrder));
                }
            }

            // First index & max elements
            int first = collectionOptions.getFirst();
            int max = collectionOptions.getMax();
            if (first < 0 || first >= people.size())
                first = 0;
            if (max < 0 || max > people.size() - first)
                max = people.size() - first;
            people = people.subList(first, first + max);

            return ImmediateFuture.newInstance(new RestfulCollection<Person>(people, 0, people.size()));
        } catch (InterruptedException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Future<Person> getPerson(UserId userId, Set<String> fields, SecurityToken token) throws ProtocolException
    {
        String uid = userId.getUserId(token);
        Person person = new PersonXW();

        if (!documentAccessBridge.exists(uid))
            throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Person not found");

        // set id - REQUIRED
        person.setId(uid);

        DocumentName docName = documentAccessBridge.getDocumentName(uid);

        Name name = new NameXW();
        Address address = null;
        ListField im = null;

        for (PersonXW.XWikiField prop : PersonXW.XWikiField.values()) {
            String propValue =
                (String) documentAccessBridge.getProperty(uid, PersonXW.XWIKI_USER_CLASS_NAME, prop.toString());

            if (propValue == null)
                continue;

            switch (prop) {
                case AVATAR:
                    String avatarUrl = documentAccessBridge.getAttachmentURL(uid, propValue);
                    person.setThumbnailUrl(avatarUrl);
                    break;
                case BLOG:
                    ((PersonXW) person).setBlogUrl(propValue);
                    break;
                case BLOGFEED:
                    ((PersonXW) person).setBlogfeedUrl(propValue);
                    break;
                case CITY:
                    if (address == null)
                        address = new AddressImpl();
                    address.setLocality(propValue);
                    break;
                case COMMENT:
                    person.setAboutMe(propValue);
                    break;
                case COMPANY:
                    Organization org = new OrganizationImpl();
                    org.setName(propValue);
                    person.setOrganizations(Lists.<Organization> newArrayList(org));
                    break;
                case COUNTRY:
                    if (address == null)
                        address = new AddressImpl();
                    address.setCountry(propValue);
                    break;
                case EMAIL:
                    person.setEmails(Lists.<ListField> newArrayList(new ListFieldImpl("work", propValue)));
                    break;
                case FIRST_NAME:
                    name.setGivenName(propValue);
                    break;
                case IMACCOUNT:
                    if (im == null)
                        im = new ListFieldImpl();
                    im.setValue(propValue);
                    break;
                case IMTYPE:
                    if (im == null)
                        im = new ListFieldImpl();
                    im.setType(propValue);
                    break;
                case LAST_NAME:
                    name.setFamilyName(propValue);
                    break;
            }
        }

        // set name - REQUIRED
        person.setName(name);
        // set displayName - REQUIRED
        person.setDisplayName(name.getFormatted());
        // set nickname - REQUIRED
        person.setNickname(docName.getPage());
        // set current location
        if (address != null)
            person.setCurrentLocation(address);
        // set im
        if (im != null)
            person.setIms(Lists.<ListField> newArrayList(im));

        return ImmediateFuture.newInstance(person);
    }

    /**
     * {@inheritDoc}
     */
    public void initialize() throws InitializationException
    {

    }

}
