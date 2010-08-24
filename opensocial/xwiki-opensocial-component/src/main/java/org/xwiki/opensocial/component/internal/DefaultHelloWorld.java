package org.xwiki.opensocial.component.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

//import javax.servlet.http.HttpServletResponse;

import org.xwiki.opensocial.component.HelloWorld;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.bridge.DocumentAccessBridge;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.social.core.model.PersonImpl;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.Message;
import org.apache.shindig.social.opensocial.model.MessageCollection;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.MessageService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;

/**
 * Concrete implementation of a <tt>HelloWorld</tt> component. The component is configured via the Plexus container.
 * 
 * @version $Id$
 */
@Component("default")
public class DefaultHelloWorld extends AbstractLogEnabled implements HelloWorld, ActivityService, AppDataService,
    MessageService, PersonService, Initializable
{
    /** The greeting that was specified in the configuration. */
    private String greeting = "Hello World!";

    /** Provides access to documents. Injected by the Component Manager. */
    @Requirement
    private DocumentAccessBridge documentAccessBridge;

    /**
     * Says hello by returning a greeting to the caller.
     * 
     * @return A greeting.
     */
    public String sayHello()
    {
        return greeting + " " + documentAccessBridge.isDocumentViewable("bla");
    }

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
    public void initialize() throws InitializationException
    {
        // TODO: initialize component
        getLogger().debug("DefaultHelloWorld initialized");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.shindig.social.opensocial.spi.PersonService#getPeople(Set, GroupId, CollectionOptions, Set,
     *      SecurityToken)
     */
    public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds, GroupId groupId, CollectionOptions arg2,
        Set<String> arg3, SecurityToken arg4) throws ProtocolException
    {
        UserId JOHN = new UserId(UserId.Type.userId, "john.doe");
        UserId JANE = new UserId(UserId.Type.userId, "jane.doe");
        UserId[] FRIENDS = {JOHN, JANE};

        try {
            List<Person> people = new ArrayList<Person>();
            switch (groupId.getType()) {
                case self:
                    for (UserId userId : userIds) {
                        Person person = new PersonImpl();
                        person.setId(userId.getUserId());
                        people.add(person);
                    }
                    break;
                case friends:
                    for (UserId userId : FRIENDS) {
                        Person person = new PersonImpl();
                        person.setId(userId.getUserId());
                        people.add(person);
                    }
                    break;
                case all:
                    // throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not yet implemented",null);
                    throw new ProtocolException(501, "Not yet implemented", null);
                case groupId:
                    // throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not yet implemented",null);
                    throw new ProtocolException(501, "Not yet implemented", null);
                case deleted:
                    // throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not yet implemented",null);
                    throw new ProtocolException(501, "Not yet implemented", null);
                default:
                    // throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Group ID not recognized",null);
                    throw new ProtocolException(501, "Not yet implemented", null);
            }
            return ImmediateFuture.newInstance(new RestfulCollection<Person>(people, 0, people.size()));
        } catch (Exception e) {
            // throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception occurred", e);
            throw new ProtocolException(501, "Not yet implemented", null);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.shindig.social.opensocial.spi.PersonService#getPerson(UserId, Set, SecurityToken)
     */
    public Future<Person> getPerson(UserId userId, Set<String> fields, SecurityToken token) throws ProtocolException
    {
        String userDocumentName = userId.getUserId(token);
        String userClassName = "XWiki.XWikiUsers";
        String userFirstNamePropertyName = "first_name";
        String userLastNamePropertyName = "last_name";

        try {
            String firstName =
                (String) documentAccessBridge.getProperty(userDocumentName, userClassName, userFirstNamePropertyName);
            String lastName =
                (String) documentAccessBridge.getProperty(userDocumentName, userClassName, userLastNamePropertyName);

            Person person = new PersonImpl();
            person.setId(userDocumentName);
            Name name = new NameImpl();
            name.setGivenName(firstName);
            name.setFamilyName(lastName);
            person.setName(name);

            return ImmediateFuture.newInstance(person);

        } catch (Exception e) {
            // throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception occurred", e);
            throw new ProtocolException(501, "Not yet implemented", null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> createActivity(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, Activity arg4,
        SecurityToken arg5) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> deleteActivities(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, SecurityToken arg4)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<RestfulCollection<Activity>> getActivities(Set<UserId> arg0, GroupId arg1, String arg2,
        Set<String> arg3, CollectionOptions arg4, SecurityToken arg5) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<RestfulCollection<Activity>> getActivities(UserId arg0, GroupId arg1, String arg2, Set<String> arg3,
        CollectionOptions arg4, Set<String> arg5, SecurityToken arg6) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<Activity> getActivity(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, String arg4,
        SecurityToken arg5) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> deletePersonData(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, SecurityToken arg4)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<DataCollection> getPersonData(Set<UserId> arg0, GroupId arg1, String arg2, Set<String> arg3,
        SecurityToken arg4) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> updatePersonData(UserId arg0, GroupId arg1, String arg2, Set<String> arg3,
        Map<String, String> arg4, SecurityToken arg5) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> createMessage(UserId arg0, String arg1, String arg2, Message arg3, SecurityToken arg4)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<MessageCollection> createMessageCollection(UserId arg0, MessageCollection arg1, SecurityToken arg2)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> deleteMessageCollection(UserId arg0, String arg1, SecurityToken arg2) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> deleteMessages(UserId arg0, String arg1, List<String> arg2, SecurityToken arg3)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<RestfulCollection<MessageCollection>> getMessageCollections(UserId arg0, Set<String> arg1,
        CollectionOptions arg2, SecurityToken arg3) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<RestfulCollection<Message>> getMessages(UserId arg0, String arg1, Set<String> arg2,
        List<String> arg3, CollectionOptions arg4, SecurityToken arg5) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> modifyMessage(UserId arg0, String arg1, String arg2, Message arg3, SecurityToken arg4)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> modifyMessageCollection(UserId arg0, MessageCollection arg1, SecurityToken arg2)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
