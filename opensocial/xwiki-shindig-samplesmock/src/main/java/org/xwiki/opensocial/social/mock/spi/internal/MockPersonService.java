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
package org.xwiki.opensocial.social.mock.spi.internal;

import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.opensocial.social.mock.model.PersonXW;
import org.xwiki.opensocial.social.mock.spi.MockXWikiComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

@Component("MockPersonService")
public class MockPersonService extends AbstractLogEnabled implements PersonService, MockXWikiComponent, Initializable
{

    /** Provides access to documents */
    @SuppressWarnings("unused")
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
        UserId ALICE = new UserId(UserId.Type.userId, "XWiki.Alice");
        UserId BOB = new UserId(UserId.Type.userId, "XWiki.Bob");
        UserId[] FRIENDS = {ALICE, BOB};

        try {
            List<Person> people = new ArrayList<Person>();
            switch (groupId.getType()) {
                case self:
                    for (UserId userId : userIds) {
                        Person person = new PersonXW();
                        person.setId(userId.getUserId());
                        person.setDisplayName(userId.getUserId());
                        people.add(person);
                    }
                    break;
                case friends:
                    for (UserId userId : FRIENDS) {
                        Person person = new PersonXW();
                        person.setId(userId.getUserId());
                        person.setDisplayName(userId.getUserId());
                        people.add(person);
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
            return ImmediateFuture.newInstance(new RestfulCollection<Person>(people, 0, people.size()));
        } catch (Exception e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception occurred", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Future<Person> getPerson(UserId userId, Set<String> fields, SecurityToken token) throws ProtocolException
    {
        Person person = new PersonXW();
        person.setId("XWiki.Julia");

        Name name = new NameImpl();
        name.setGivenName("Julia");
        name.setFamilyName("Doe");
        name.setFormatted("Julia D");
        person.setName(name);
        person.setDisplayName(name.getFormatted());
        person.setNickname("Julia");

        return ImmediateFuture.newInstance(person);
    }

    /**
     * {@inheritDoc}
     */
    public void initialize() throws InitializationException
    {

    }

}
