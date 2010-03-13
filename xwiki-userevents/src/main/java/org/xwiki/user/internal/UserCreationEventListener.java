package org.xwiki.user.internal;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.DocumentSaveEvent;
import org.xwiki.observation.event.DocumentUpdateEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.event.filter.RegexEventFilter;
import org.xwiki.user.event.UserCreationEvent;
import org.xwiki.user.event.UserValidationEvent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("usercreation")
public class UserCreationEventListener extends AbstractLogEnabled implements EventListener
{
    /**
     * The regular expression that matches only document names in the XWiki space.
     * Note: in the future we might want to allow registering users in other spaces as well.
     */
    private static final String USER_DOCUMENT_NAME_REGEX = "^[^:]+:XWiki\\..*$";

    @Requirement
    private ComponentManager componentManager;

    @Requirement
    private Execution execution;

    /**
     * The observation manager that will be use to fire user creation events. Note: We can't have the OM as a
     * requirement, since it would create an infinite initialization loop, causing a stack overflow error (this event
     * listener would require an initialized OM and the OM requires a list of initialized event listeners)
     */
    private ObservationManager observationManager;

    /**
     * Used to convert a proper Document Reference to a string but without the wiki name.
     */
    @Requirement("local")
    private EntityReferenceSerializer<String> localEntityReferenceSerializer;

    /**
     * {@inheritDoc}
     */
    public List<Event> getEvents()
    {
        return Arrays. <Event> asList(
            new DocumentSaveEvent(new RegexEventFilter(USER_DOCUMENT_NAME_REGEX)),
            new DocumentUpdateEvent(new RegexEventFilter(USER_DOCUMENT_NAME_REGEX))
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return "usercreation";
    }

    /**
     * {@inheritDoc}
     */
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument document = (XWikiDocument) source;
        String wikiName = document.getDocumentReference().getWikiReference().getName();
        DocumentReference userClass = new DocumentReference(wikiName, "XWiki", "XWikiUsers");
        String documentName = localEntityReferenceSerializer.serialize(document.getDocumentReference());

        if (event instanceof DocumentSaveEvent && document.getXObject(userClass) != null) {
            // Fire the user created event
            UserCreationEvent newEvent = new UserCreationEvent(documentName);
            getObservationManager().notify(newEvent, source, this.getUserDataMap(document.getXObject(userClass)));
        }

        else if (document.getXObject(userClass) != null) {
            // FIXME why don't we have #getBooleanValue ?
            // Here we have a document update event on a user document
            if (document.getXObject(userClass).getIntValue("active") == 1) {
                try {
                    XWikiDocument previousRevision = getPreviousRevision(document);
                    if (previousRevision.getXObject(userClass) != null
                        && previousRevision.getXObject(userClass).getIntValue("active") != 1) {
                        // User validated his account during this document update
                        UserValidationEvent validationEvent = new UserValidationEvent(documentName);
                        getObservationManager().notify(validationEvent, source,
                            this.getUserDataMap(document.getXObject(userClass)));
                    }
                } catch (XWikiException e) {
                    this.getLogger().error(
                        MessageFormat.format("Error while retrieveing previous version of document with name [{0}]",
                            documentName), e);
                }
            }
        }
    }

    private Map<String, String> getUserDataMap(BaseObject dataObject)
    {
        Map<String, String> userData = new HashMap<String, String>();
        userData.put("firstName", dataObject.getStringValue("first_name"));
        userData.put("lastName", dataObject.getStringValue("last_name"));
        userData.put("email", dataObject.getStringValue("email"));

        return userData;
    }

    private XWikiDocument getPreviousRevision(XWikiDocument document) throws XWikiException
    {
        return getXWikiContext().getWiki().getDocument(document, document.getPreviousVersion(), getXWikiContext());
    }

    private ObservationManager getObservationManager()
    {
        if (this.observationManager == null) {
            try {
                this.observationManager = componentManager.lookup(ObservationManager.class);

            } catch (ComponentLookupException e) {
                throw new RuntimeException("Cound not retrieve an Observation Manager against the component manager");
            }
        }
        return this.observationManager;
    }

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }

}
