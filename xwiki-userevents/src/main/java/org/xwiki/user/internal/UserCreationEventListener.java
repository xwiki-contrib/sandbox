package org.xwiki.user.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.DocumentSaveEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.user.event.UserCreationEvent;

import com.xpn.xwiki.doc.XWikiDocument;

@Component("usercreation")
public class UserCreationEventListener implements EventListener
{

    @Requirement
    private ComponentManager componentManager;

    /**
     * The observation manager that will be use to fire user creation events. Note: We can't have the OM as a
     * requirement, since it would create an infinite initialization loop, causing a stack overflow error (this event
     * listener would require an initialized OM and the OM requires a list of initialized event listeners)
     */
    private ObservationManager observationManager;

    /**
     * {@inheritDoc}
     */
    public List<Event> getEvents()
    {
        return Arrays.<Event>asList(new DocumentSaveEvent());
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

        if (document.getXObject(userClass) != null) {
            // Create a map to hold our new event data
            Map<String,String> userData = new HashMap<String,String>();
            userData.put("firstName", document.getXObject(userClass).getStringValue("firstName"));
            userData.put("lastName", document.getXObject(userClass).getStringValue("lastName"));
            userData.put("email", document.getXObject(userClass).getStringValue("email"));
            // Fire the user created event
            UserCreationEvent newEvent = new UserCreationEvent();
            getObservationManager().notify(newEvent, source, userData);
        }
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

}
