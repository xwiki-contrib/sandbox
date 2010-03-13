package org.xwiki.user.event;

import org.xwiki.observation.event.AbstractDocumentEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.event.filter.EventFilter;

/**
 * {@link Event} generated when a new user is validates his account.
 */
public class UserValidationEvent extends AbstractDocumentEvent
{
    /**
     * The version identifier for this Serializable class. Increment only if the <i>serialized</i> form of the class
     * changes.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor initializing the event filter with an
     * {@link org.xwiki.observation.event.filter.AlwaysMatchingEventFilter}, meaning that this event will match any
     * other document update event.
     */
    public UserValidationEvent()
    {
        super();
    }

    /**
     * Constructor initializing the event filter with a {@link org.xwiki.observation.event.filter.FixedNameEventFilter},
     * meaning that this event will match only update events affecting the document matching the passed document name.
     * 
     * @param documentName the name of the updated document to match
     */
    public UserValidationEvent(String documentName)
    {
        super(documentName);
    }

    /**
     * Constructor using a custom {@link EventFilter}.
     * 
     * @param eventFilter the filter to use for matching events
     */
    public UserValidationEvent(EventFilter eventFilter)
    {
        super(eventFilter);
    }
}
