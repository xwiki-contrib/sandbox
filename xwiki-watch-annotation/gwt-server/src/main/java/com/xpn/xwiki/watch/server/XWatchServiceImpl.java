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
package com.xpn.xwiki.watch.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.annotation.AnnotationService;
import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.annotation.reference.IndexedObjectReference;
import org.xwiki.annotation.reference.TypedStringEntityReferenceSerializer;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.gwt.api.client.Document;
import com.xpn.xwiki.gwt.api.client.XObject;
import com.xpn.xwiki.gwt.api.client.XWikiGWTException;
import com.xpn.xwiki.gwt.api.server.XWikiServiceImpl;
import com.xpn.xwiki.watch.client.XWatchService;
import com.xpn.xwiki.watch.client.annotation.Annotation;
import com.xpn.xwiki.watch.client.annotation.AnnotationState;
import com.xpn.xwiki.watch.client.data.FeedArticle;
import com.xpn.xwiki.web.Utils;

/**
 * Implementation of the {@link XWatchService} interface, to provide Watch specific functions to the Watch application.
 */
public class XWatchServiceImpl extends XWikiServiceImpl implements XWatchService
{
    protected static final Log WATCHLOG = LogFactory.getLog(XWatchServiceImpl.class);

    protected static final String FEED_ENTRY_CLASS = "XWiki.FeedEntryClass";

    protected static final String FEED_ENTRY_CONTENT_FIELD = "content";

    public List<FeedArticle> getArticles(String sql, int nb, int start) throws XWikiGWTException
    {
        try {
            XWikiContext context = getXWikiContext();
            List<Document> documents = getDocumentsFromObjects(sql, nb, start, context);
            List<FeedArticle> articles = new ArrayList<FeedArticle>();
            for(Document doc : documents) {
                FeedArticle current = new FeedArticle(doc);
                // now set the content to the annotated content and the annotations in the annotations list
                current.setContent(getAnnotatedEntryFeed(doc.getFullName()));
                current.setAnnotations(getAnnotations(doc.getFullName()));
                articles.add(current);
            }
            return articles;
        } catch (Exception e) {
            throw getXWikiGWTException(e);
        }
    }
    
    public FeedArticle getArticle(String documentName) throws XWikiGWTException
    {
        Document apiDoc = getDocument(documentName, true, true, false);
        FeedArticle article = new FeedArticle(apiDoc);
        // now set the content to the annotated content and the annotations to the annotations list
        article.setContent(getAnnotatedEntryFeed(apiDoc.getFullName()));
        article.setAnnotations(getAnnotations(apiDoc.getFullName()));
        return article;
    }

    private List<Document> getDocumentsFromObjects(String sql, int nb, int start, XWikiContext context) throws XWikiGWTException
    {
        List<Document> docList = new ArrayList<Document>();
        try {
            // removed distinct to have faster querying and because it's useless
            String objectsSql = "select obj.name from BaseObject as obj " + sql;
            List list = context.getWiki().search(objectsSql, nb, start, context);
            if ((list == null) && (list.size() == 0)) {
                return docList;
            }
            for (int i = 0; i < list.size(); i++) {
                if (context.getWiki().getRightService().hasAccessLevel("view", context.getUser(), (String) list.get(i),
                    context) == true) {
                    XWikiDocument doc = context.getWiki().getDocument((String) list.get(i), context);
                    Document apidoc = newDocument(new Document(), doc, true, true, false, false, context);
                    docList.add(apidoc);
                }
            }
            return docList;
        } catch (Exception e) {
            throw getXWikiGWTException(e);
        }
    }
    
    public List getConfigDocuments(String watchSpace) throws XWikiGWTException
    {
        try {
            String query =
                " where obj.className in "
                    + "('XWiki.AggregatorURLClass','XWiki.AggregatorGroupClass', 'XWiki.KeywordClass') and obj.name like '"
                    + watchSpace + ".%'";
            XWikiContext context = getXWikiContext();
            return getDocumentsFromObjects(query, 0, 0, context);
        } catch (Exception e) {
            throw getXWikiGWTException(e);
        }
    }

    public int getArticlesCount(String watchSpace) throws XWikiGWTException
    {
        try {
            XWikiContext context = getXWikiContext();
            String articleCountQuery =
                "select count(*) from XWiki.FeedEntryClass as entry, BaseObject as obj "
                    + "where obj.id = entry.id and obj.name like '" + watchSpace + ".%'";
            List resultList = context.getWiki().search(articleCountQuery, context);
            if (resultList == null || resultList.size() < 1) {
                return 0; // TODO: or throw exception? We didn't really expect
                // that...
            } else {
                return ((Number) resultList.get(0)).intValue();
            }
        } catch (Exception e) {
            throw getXWikiGWTException(e);
        }
    }

    public List getNewArticlesCountPerFeeds(String watchSpace) throws XWikiGWTException
    {
        try {
            String newArticlesPerFeedQuery =
                "select entry.feedname, sum(1 - coalesce(entry.read, 0)), count(*) "
                    + "from XWiki.FeedEntryClass as entry, BaseObject as obj "
                    + "where obj.id = entry.id and obj.name like '" + watchSpace + ".%' group by entry.feedname";
            XWikiContext context = getXWikiContext();
            List resultList = context.getWiki().search(newArticlesPerFeedQuery, context);
            return prepareResultList(resultList);
        } catch (XWikiException e) {
            throw getXWikiGWTException(e);
        }
    }

    public List getTagsList(String watchSpace, String like) throws XWikiGWTException
    {
        try {
            String tagsListQuery =
                "select elements(entry.tags), count(*) " + " from XWiki.FeedEntryClass as entry, BaseObject as obj "
                    + " where obj.id = entry.id and obj.name like '" + watchSpace + ".%'";
            if (like != null && like.length() != 0) {
                tagsListQuery += " and col_0_0_ like '" + like + "%' ";
            }
            String orderGroupQuery = " group by col_0_0_ order by lower(col_0_0_) asc";

            XWikiContext context = getXWikiContext();
            List resultList = context.getWiki().search(tagsListQuery + orderGroupQuery, context);
            return prepareResultList(resultList);
        } catch (XWikiException e) {
            throw getXWikiGWTException(e);
        }
    }

    /**
     * Transforms a list of arrays (Object[]) into a list of lists, since we cannot send Object[]'s through GWT. To be
     * used to prepare the results returned by database search functions.
     * 
     * @param results the List of arrays to be wrapped
     * @return the list, with all arrays transformed into lists
     */
    private List prepareResultList(List results)
    {
        ArrayList newList = new ArrayList();

        for (int i = 0; i < results.size(); i++) {
            List currentList = new ArrayList();
            Object[] array = (Object[]) results.get(i);
            for (int j = 0; j < array.length; j++) {
                currentList.add(array[j]);
            }
            newList.add(currentList);
        }
        return newList;
    }

    public Map getAccessLevels(List rights, String docname) throws XWikiGWTException
    {
        Map rightsMap = new HashMap();
        for (int i = 0; i < rights.size(); i++) {
            rightsMap.put(rights.get(i), this.hasAccessLevel((String) rights.get(i), docname));
        }
        return rightsMap;
    }

    /**
     * {@inheritDoc}
     */
    public boolean addFeed(String spaceName, String feedName, XObject feedObject) throws XWikiGWTException
    {
        String uniquePageName = getUniquePageName(spaceName, feedName);
        String pageName = spaceName + "." + uniquePageName;
        // complete the doc name in the feed object
        feedObject.setName(pageName);
        boolean objectSaveResult = saveObject(feedObject);
        if (!objectSaveResult) {
            // something went wrong with object save, probably no rights
            return false;
        }
        String feedDefaultContent = "#includeForm(\"WatchSheets.FeedSheet\")";
        boolean docSaveResponse = saveDocumentContent(pageName, feedDefaultContent);
        if (!docSaveResponse) {
            // something went wrong with document save, lack of rights
            return false;
        }
        // everything is fine
        return true;
    }

    /**
     * {@inheritDoc}. Overwrite to set the syntax to xwiki 1.0 syntax.
     */
    public Boolean saveDocumentContent(String fullName, String content, String comment) throws XWikiGWTException
    {
        try {
            XWikiContext context = getXWikiContext();
            if (context.getWiki().getRightService().hasAccessLevel("edit", context.getUser(), fullName, context)) {
                XWikiDocument doc = context.getWiki().getDocument(fullName, context);
                doc.setContent(content);
                doc.setSyntaxId(XWikiDocument.XWIKI10_SYNTAXID);
                doc.setAuthor(context.getUser());
                if (doc.isNew())
                    doc.setCreator(context.getUser());
                context.getWiki().saveDocument(doc,
                    (comment == null) ? context.getMessageTool().get("core.comment.updateContent") : comment, context);
                return Boolean.valueOf(true);
            } else {
                return Boolean.valueOf(false);
            }
        } catch (Exception e) {
            throw getXWikiGWTException(e);
        }
    }

    public String getAnnotatedEntryFeed(String documentName) throws XWikiGWTException
    {
        try {
            // get the annotation service and ask for the annotated field
            AnnotationService service = Utils.getComponent(AnnotationService.class);
            // force html syntax on the content of the feed
            return service.getAnnotatedRenderedContent(getFeedEntryContentReference(documentName), "html/4.01",
                "xhtml/1.0");
        } catch (AnnotationServiceException e) {
            throw getXWikiGWTException(e);
        }
    }

    /**
     * {@inheritDoc} <br />
     * TODO: why is this function getting a String parameter?
     * 
     * @see com.xpn.xwiki.watch.client.XWatchService#removeAnnotation(java.lang.String, java.lang.String)
     */
    public void removeAnnotation(String documentName, String annotationID) throws XWikiGWTException
    {
        try {
            // get the annotation service
            AnnotationService service = (AnnotationService) Utils.getComponent(AnnotationService.class);
            service.removeAnnotation(getFeedEntryContentReference(documentName), annotationID);
        } catch (AnnotationServiceException e) {
            throw getXWikiGWTException(e);
        }
    }

    public void addAnnotation(String selection, String metadata, String documentName) throws XWikiGWTException
    {
        AnnotationService service = (AnnotationService) Utils.getComponent(AnnotationService.class);
        String userID = "XWiki.XWikiGuest";
        try {
            userID = getUser().getFullName();
        } catch (XWikiGWTException e) {
            // some exception occurred while getting the current user
            WATCHLOG.warn("Couldn't get the current user in the context, saving annotation as XWiki.XWikiGuest");
        }
        try {
            service
                .addAnnotation(getFeedEntryContentReference(documentName), selection, selection, 0, userID, metadata);
        } catch (AnnotationServiceException e) {
            throw getXWikiGWTException(e);
        }
    }

    public List<Annotation> getAnnotations(String documentName) throws XWikiGWTException
    {
        try {
            AnnotationService service = (AnnotationService) Utils.getComponent(AnnotationService.class);
            return prepareAnnotations(service.getValidAnnotations(getFeedEntryContentReference(documentName)));
        } catch (AnnotationServiceException e) {
            throw getXWikiGWTException(e);
        }
    }
    
    /**
     * Helper function to marshal a set of annotations to gwt serializable annotations to send to the watch client.
     * @param annotations the annotations collection to convert
     * @return the list of gwt serializable annotations
     */
    protected List<Annotation> prepareAnnotations(Collection<org.xwiki.annotation.Annotation> annotations)
    {
        List<Annotation> result = new ArrayList<Annotation>();
        for (org.xwiki.annotation.Annotation annotation : annotations) {
            Annotation watchAnnotation =
                new Annotation(annotation.getTarget(), annotation.getAuthor(), annotation.getAnnotation(), annotation
                    .getSelection(), annotation.getSelectionContext(), annotation.getId());
            watchAnnotation.setOriginalSelection(annotation.getOriginalSelection());
            watchAnnotation.setState(AnnotationState.valueOf(annotation.getState().toString()));
            watchAnnotation.setDisplayDate(annotation.getDisplayDate());
            result.add(watchAnnotation);
        }
        
        return result;
    }

    protected String getFeedEntryContentReference(String documentFullName)
    {
        // parse the document full name to an entity reference
        // use current resolver to get it all relative to current document, although no current document might be around
        EntityReferenceResolver<String> resolver = Utils.getComponent(EntityReferenceResolver.class, "current");
        EntityReference docRef = resolver.resolve(documentFullName, EntityType.DOCUMENT);
        // create an indexed object reference pointing to the default feed entry object
        IndexedObjectReference objectRef = new IndexedObjectReference(FEED_ENTRY_CLASS, null, docRef);
        // create a property reference pointing to the content property
        EntityReference propRef = new EntityReference(FEED_ENTRY_CONTENT_FIELD, EntityType.OBJECT_PROPERTY, objectRef);
        // serialize it all with a typed serializer and return it
        TypedStringEntityReferenceSerializer serializer =
            Utils.getComponent(TypedStringEntityReferenceSerializer.class);
        return serializer.serialize(propRef);
    }
}
