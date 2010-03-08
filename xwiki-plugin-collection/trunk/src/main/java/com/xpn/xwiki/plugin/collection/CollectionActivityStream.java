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
package com.xpn.xwiki.plugin.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.notify.XWikiDocChangeNotificationInterface;
import com.xpn.xwiki.notify.XWikiNotificationRule;
import com.xpn.xwiki.plugin.activitystream.api.ActivityStreamException;
import com.xpn.xwiki.plugin.activitystream.impl.ActivityStreamImpl;

/**
 * Collection Activity Stream allowing to record changes inside collections
 * @author ludovic
 */
public class CollectionActivityStream extends ActivityStreamImpl implements XWikiDocChangeNotificationInterface {

	public static final String EVENT_CREATE = "create";

	public static final String EVENT_UPDATE = "update";

	public static final String EVENT_DELETE = "delete";

	public static final String EVENT_ADD = "added";

	public static final String EVENT_REMOVE = "removed";

	/**
	 * Log4J logger object to log messages in this class.
	 */
	private static final Logger LOG = Logger.getLogger(CollectionActivityStream.class);

	public void notify(XWikiNotificationRule rule, XWikiDocument newdoc,
			XWikiDocument olddoc, int event, XWikiContext context) {

		if (event==XWikiDocChangeNotificationInterface.EVENT_CHANGE
				||event==XWikiDocChangeNotificationInterface.EVENT_NEW
				||event==XWikiDocChangeNotificationInterface.EVENT_DELETE) {

			CollectionPlugin collPlugin = (CollectionPlugin) context.getWiki().getPlugin("collection", context);

			List<String> collections = collPlugin.getCollections(newdoc.getFullName(), context);

			// if this document is not part of any collection we should not continue
			if (collections==null || collections.size()==0)
				return;


			// handle linked pages
			List<String> oldlinkedPages = (olddoc==null) ? new ArrayList<String>() : olddoc.getLinkedPages(context);
			List<String> newlinkedPages = (olddoc==null) ? new ArrayList<String>() : newdoc.getLinkedPages(context);

			// added pages 
			for (Iterator<String> iterator = newlinkedPages.iterator(); iterator.hasNext();) {
				String page = (String) iterator.next();
				if (!oldlinkedPages.contains(page)) {
					for (Iterator<String> iterator2 = collections.iterator(); iterator2.hasNext();) {
						String collName = (String) iterator2.next();
						XWikiDocument pageDoc = null;
						XWikiDocument collDoc = null;
						try {
							pageDoc = context.getWiki().getDocument(page, context);
							collDoc = context.getWiki().getDocument(collName, context);
						} catch (XWikiException e) {
						}
						if (pageDoc!=null && !pageDoc.isNew()) {
							ArrayList<String> params = new ArrayList<String>();
							params.add(0, page);
							params.add(1, pageDoc.getDisplayTitle(context));
							params.add(2, collName);
							params.add(3, collDoc.getDisplayTitle(context));

							try {
								addDocumentActivityEvent(collName, newdoc, EVENT_ADD,
										"cas_document_has_been_added", params, context);
							} catch (ActivityStreamException e) {
								LOG.error("Failed to store event: page " + page + " has been added to page " + newdoc.getFullName() + " in collection " + collName);
							}

							if (LOG.isDebugEnabled())
								LOG.debug("[COLL] Page " + page + " has been added to page " + newdoc.getFullName() + " in collection " + collName);
						}
					}
				}
			}

			// removed pages 
			for (Iterator<String> iterator = oldlinkedPages.iterator(); iterator.hasNext();) {
				String page = (String) iterator.next();
				if (!newlinkedPages.contains(page)) {
					for (Iterator<String> iterator2 = collections.iterator(); iterator2.hasNext();) {
						String collName = (String) iterator2.next();

						XWikiDocument pageDoc = null;
						XWikiDocument collDoc = null;
						try {
							pageDoc = context.getWiki().getDocument(page, context);
							collDoc = context.getWiki().getDocument(collName, context);
						} catch (XWikiException e) {
						}
						if (pageDoc!=null && !pageDoc.isNew()) {
							ArrayList<String> params = new ArrayList<String>();
							params.add(0, page);
							params.add(1, pageDoc.getDisplayTitle(context));
							params.add(2, collName);
							params.add(3, collDoc.getDisplayTitle(context));
							try {
								addDocumentActivityEvent(collName, newdoc, EVENT_REMOVE,
										"cas_document_has_been_removed", params, context);
							} catch (ActivityStreamException e) {
								LOG.error("Failed to store event: page " + page + " has been removed to page " + newdoc.getFullName() + " in collection " + collName);
							}

							if (LOG.isDebugEnabled())
								LOG.debug("[COLL] Page " + page + " has been removed from page " + newdoc.getFullName() + " in collection " + collName);
						}
					}
				}
			}


			// handle current page event
			for (Iterator<String> iterator2 = collections.iterator(); iterator2.hasNext();) {
				String collName = (String) iterator2.next();

				XWikiDocument collDoc = null;
				try {
					collDoc = context.getWiki().getDocument(collName, context);
				} catch (XWikiException e) {
				}

				ArrayList<String> params = new ArrayList<String>();
				params.add(0, newdoc.getFullName());
				params.add(1, newdoc.getDisplayTitle(context));
				params.add(2, collName);
				params.add(3, collDoc.getDisplayTitle(context));

				if (event==XWikiDocChangeNotificationInterface.EVENT_CHANGE) {
					if (olddoc==null || olddoc.isNew()) {
						try {
							addDocumentActivityEvent(collName, newdoc, EVENT_CREATE,
									"cas_document_has_been_created", params, context);
						} catch (ActivityStreamException e) {
							LOG.debug("Failed to store event: page " + newdoc.getFullName() + " has been created in collection " + collName);					
						}

						if (LOG.isDebugEnabled())
							LOG.debug("[COLL] Page " + newdoc.getFullName() + " has been created in collection " + collName);					
					} else if (newdoc==null || newdoc.isNew()) {
							try {
								addDocumentActivityEvent(collName, newdoc, EVENT_DELETE,
										"cas_document_has_been_deleted", params, context);
							} catch (ActivityStreamException e) {
								LOG.debug("Failed to store event: page " + newdoc.getFullName() + " has been deleted in collection " + collName);					
							}

							if (LOG.isDebugEnabled())
								LOG.debug("[COLL] Page " + newdoc.getFullName() + " has been deleted in collection " + collName);					
				    } else {
						try {
							addDocumentActivityEvent(collName, newdoc, EVENT_UPDATE,
									"cas_document_has_been_modified", params, context);
						} catch (ActivityStreamException e) {
							LOG.debug("Failed to store event: page " + newdoc.getFullName() + " has been modified in collection " + collName);					
						}

						if (LOG.isDebugEnabled())
							LOG.debug("[COLL] Page " + newdoc.getFullName() + " has been modified in collection " + collName);
					}
				}

				if (event==XWikiDocChangeNotificationInterface.EVENT_NEW) {
					try {
						addDocumentActivityEvent(collName, newdoc, EVENT_CREATE,
								"cas_document_has_been_created", params, context);
					} catch (ActivityStreamException e) {
						LOG.debug("Failed to store event: page " + newdoc.getFullName() + " has been created in collection " + collName);					
					}

					if (LOG.isDebugEnabled())
						LOG.debug("[COLL] Page " + newdoc.getFullName() + " has been created in collection " + collName);			
				}

				if (event==XWikiDocChangeNotificationInterface.EVENT_DELETE) {
					try {
						addDocumentActivityEvent(collName, newdoc, EVENT_DELETE,
								"cas_document_has_been_deleted", params, context);
					} catch (ActivityStreamException e) {
						LOG.debug("Failed to store event: page " + newdoc.getFullName() + " has been deleted in collection " + collName);					
					}

					if (LOG.isDebugEnabled())
						LOG.debug("[COLL] Page " + newdoc.getFullName() + " has been deleted in collection " + collName);			
				}
			}
		}
	}

}
