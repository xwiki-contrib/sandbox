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
 */
package org.xwiki.component.wiki.internal;

import java.util.Arrays;
import java.util.List;

import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.wiki.InvalidComponentDefinitionException;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentBuilder;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.component.wiki.WikiComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.DocumentDeleteEvent;
import org.xwiki.observation.event.DocumentSaveEvent;
import org.xwiki.observation.event.DocumentUpdateEvent;
import org.xwiki.observation.event.Event;

/**
 * An {@link EventListener} responsible for dynamically registering / unregistering / updating xwiki wiki components,
 * based on wiki component create / delete / update actions.
 * 
 * @version $Id$
 * @since 2.22
 */
@Component(WikiComponentEventListener.NAME)
public class WikiComponentEventListener extends AbstractLogEnabled implements EventListener
{

    /**
     * This event listener name. Also used as role hint for this component implementation.
     */
    public static final String NAME = "wikiComponentListener";

    /**
     * Wiki Component manager. Used to register/unregister wiki components.
     */
    @Requirement
    private WikiComponentManager wikiComponentManager;

    /**
     * Wiki Component builder. Used to create {@link WikiComponent} form document references.
     */
    @Requirement
    private WikiComponentBuilder wikiComponentBuilder;

    /**
     * {@inheritDoc}
     */
    public List<Event> getEvents()
    {
        return Arrays.<Event> asList(new DocumentSaveEvent(), new DocumentUpdateEvent(), new DocumentDeleteEvent());
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    public void onEvent(Event event, Object source, Object data)
    {
        DocumentModelBridge document = (DocumentModelBridge) source;
        DocumentReference documentReference = document.getDocumentReference();

        if (event instanceof DocumentSaveEvent || event instanceof DocumentUpdateEvent) {
            
            // Unregister any existing component registered under this document.
            if (unregisterComponentInternal(documentReference)) {

                // Check whether the given document has a wiki component defined in it.
                if (this.wikiComponentBuilder.containsWikiComponent(documentReference)) {

                    // Attempt to create a wiki component.
                    WikiComponent wikiComponent = null;
                    try {
                        wikiComponent = this.wikiComponentBuilder.build(documentReference);
                    } catch (InvalidComponentDefinitionException e) {
                        // An invalid component exception here can be an expected error, so we only log at debug level.
                        getLogger().debug(
                            String.format("Invalid component definition for document [%s]", documentReference), e);
                        return;
                    } catch (WikiComponentException e) {
                        getLogger().error(
                            String.format("Failed to create wiki component for document [%s]", documentReference), e);
                        return;
                    }

                    // Register the component.
                    registerComponentInternal(wikiComponent);
                }
            }
        } else if (event instanceof DocumentDeleteEvent) {
            unregisterComponentInternal(documentReference);
        }

    }

    /**
     * Helper method to register a wiki component.
     * 
     * @param wikiComponent the wikiComponent to register.
     */
    private void registerComponentInternal(WikiComponent wikiComponent)
    {
        try {
            this.wikiComponentManager.registerWikiComponent(wikiComponent);
        } catch (WikiComponentException e) {
            getLogger()
                .debug(
                    String
                        .format("Unable to register component in document [%s]", wikiComponent.getDocumentReference()),
                    e);
        }
    }

    /**
     * Helper method to unregister a wiki component.
     * 
     * @param documentReference the reference to the document for which to unregister the held wiki component.
     * @return true if successful, false otherwise
     */
    private boolean unregisterComponentInternal(DocumentReference documentReference)
    {
        boolean result = true;
        if (this.wikiComponentBuilder.containsWikiComponent(documentReference)) {
            try {
                this.wikiComponentManager.unregisterWikiComponent(documentReference);
            } catch (WikiComponentException e) {
                getLogger().debug(String.format("Unable to unregister component in document [%s]", documentReference),
                    e);
                result = false;
            }
        }
        return result;
    }

}
