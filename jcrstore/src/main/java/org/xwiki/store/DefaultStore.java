package org.xwiki.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.xwiki.context.Execution;
import org.xwiki.query.QueryManager;
import org.xwiki.store.dao.DocumentDao;
import org.xwiki.store.dao.ObjectDao;
import org.xwiki.store.dao.WikiDao;
import org.xwiki.store.value.DocumentId;
import org.xwiki.store.value.DocumentValue;
import org.xwiki.store.value.ObjectId;
import org.xwiki.store.value.ObjectValue;
import org.xwiki.store.value.ValueConverter;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.doc.XWikiLink;
import com.xpn.xwiki.doc.XWikiLock;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.store.XWikiAttachmentStoreInterface;
import com.xpn.xwiki.store.XWikiHibernateStore;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.store.XWikiVersioningStoreInterface;

/**
 * Clean version of {@link XWikiHibernateStore} 
 */
public class DefaultStore implements XWikiStoreInterface
{
    /**
     * QueryManager for this store. Injected via component manager.
     */
    private QueryManager queryManager;

    private WikiDao wikiDao;

    private DocumentDao documentDao;

    private ObjectDao objectDao;

    private Execution execution;

    private ValueConverter valueConverter = new ValueConverter(execution);

    private XWikiVersioningStoreInterface versioningStore;

    private XWikiAttachmentStoreInterface attachmentStore;

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.store.XWikiStoreInterface#isWikiNameAvailable(java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    public boolean isWikiNameAvailable(String wikiName, XWikiContext context) throws XWikiException
    {
        return !wikiDao.exist(wikiName);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.store.XWikiStoreInterface#createWiki(java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    public void createWiki(String wikiName, XWikiContext context) throws XWikiException
    {
        wikiDao.create(wikiName);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.store.XWikiStoreInterface#deleteWiki(java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    public void deleteWiki(String wikiName, XWikiContext context) throws XWikiException
    {
        wikiDao.delete(wikiName);
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists(XWikiDocument doc, XWikiContext context) throws XWikiException
    {
        try {
            return documentDao.load(valueConverter.getId(doc)) != null;
        } catch (Exception e) {
            throw new XWikiException(0, 0, "", e);
        }
    }

    public void saveXWikiDoc(XWikiDocument doc, XWikiContext context, boolean bTransaction) throws XWikiException
    {
        try {
            DocumentId docId = valueConverter.getId(doc);
            doc.setStore(this);
            // Make sure the database name is stored
            doc.setDatabase(context.getDatabase());

            // These informations will allow to not look for attachments and objects on loading
            doc.setElement(XWikiDocument.HAS_ATTACHMENTS, (doc.getAttachmentList().size() != 0));
            doc.setElement(XWikiDocument.HAS_OBJECTS, (doc.getxWikiObjects().size() != 0));

            // Let's update the class XML since this is the new way to store it
            BaseClass bclass = doc.getxWikiClass();
            if ((bclass != null) && (bclass.getFieldList().size() > 0)) {
                doc.setxWikiClassXML(bclass.toXMLString());
            } else {
                doc.setxWikiClassXML(null);
            }

            // Handle the latest text file
            if (doc.isContentDirty() || doc.isMetaDataDirty()) {
                Date ndate = new Date();
                doc.setDate(ndate);
                if (doc.isContentDirty()) {
                    doc.setContentUpdateDate(ndate);
                    doc.setContentAuthor(doc.getAuthor());
                }
                doc.incrementVersion();
                if (versioningStore != null) {
                    versioningStore.updateXWikiDocArchive(doc, false, context);
                }

                doc.setContentDirty(false);
                doc.setMetaDataDirty(false);
            } else {
                if (doc.getDocumentArchive() != null) {
                    // Let's make sure we save the archive if we have one
                    // This is especially needed if we load a document from XML
                    if (versioningStore != null) {
                        versioningStore.saveXWikiDocArchive(doc.getDocumentArchive(), false, context);
                    }
                } else {
                    // Make sure the getArchive call has been made once
                    // with a valid context
                    try {
                        if (versioningStore != null) {
                            doc.getDocumentArchive(context);
                        }
                    } catch (XWikiException e) {
                        // this is a non critical error
                    }
                }
            }

            DocumentValue docValue = valueConverter.toValue(doc);
            documentDao.save(docValue);

            // Remove properties planned for removal
            for (BaseObject bobj : doc.getObjectsToRemove()) {
                objectDao.delete(new ObjectId(docId, bobj.getNumber()));                
            }
            doc.setObjectsToRemove(new ArrayList<BaseObject>());

            if (doc.hasElement(XWikiDocument.HAS_OBJECTS)) {
                for (Vector<BaseObject> objects : doc.getxWikiObjects().values()) {
                    for (BaseObject obj : objects) {
                        obj.setName(doc.getFullName());
                        objectDao.save(valueConverter.toValue(obj));
                    }
                }
            }

            if (context.getWiki().hasBacklinks(context)) {
                saveLinks(doc, context, true);
            }

            doc.setNew(false);

            // We need to ensure that the saved document becomes the original document
            doc.setOriginalDocument((XWikiDocument) doc.clone());
        } catch (Exception e) {
            Object[] args = {doc.getFullName()};
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                XWikiException.ERROR_XWIKI_STORE_HIBERNATE_SAVING_DOC, "Exception while saving document {0}", e, args);
        }
    }

    public void saveXWikiDoc(XWikiDocument doc, XWikiContext context) throws XWikiException
    {
        saveXWikiDoc(doc, context, true);
    }

    public XWikiDocument loadXWikiDoc(XWikiDocument doc, XWikiContext context) throws XWikiException
    {
        try {
            doc.setStore(this);

            DocumentId docId = valueConverter.getId(doc);
            DocumentValue docValue = documentDao.load(docId);
            docValue.database = context.getDatabase();
            doc = valueConverter.fromValue(docValue, doc);
            doc.setNew(false);
            doc.setMostRecent(true);
            // Fix for XWIKI-1651
            doc.setDate(new Date(doc.getDate().getTime()));
            doc.setCreationDate(new Date(doc.getCreationDate().getTime()));
            doc.setContentUpdateDate(new Date(doc.getContentUpdateDate().getTime()));

            BaseClass bclass = new BaseClass();
            String cxml = doc.getxWikiClassXML();
            if (cxml != null) {
                bclass.fromXML(cxml);
                bclass.setName(doc.getFullName());
                doc.setxWikiClass(bclass);
            }

            // Store this XWikiClass in the context so that we can use it in case of recursive usage
            // of classes
            context.addBaseClass(bclass);

            Collection<ObjectId> lst = objectDao.list(docId);
            for (ObjectId oid : lst) {
                ObjectValue ovalue = objectDao.load(oid);
                BaseObject obj = valueConverter.fromValue(ovalue);
                doc.setObject(obj.getClassName(), obj.getNumber(), obj);
                obj.setWiki(context.getDatabase());
            }

            // We need to ensure that the loaded document becomes the original document
            doc.setOriginalDocument((XWikiDocument) doc.clone());
        } catch (Exception e) {
            Object[] args = {doc.getFullName()};
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                XWikiException.ERROR_XWIKI_STORE_HIBERNATE_READING_DOC, "Exception while reading document {0}", e, args);
        }
        return doc;
    }

    public void deleteXWikiDoc(XWikiDocument doc, XWikiContext context) throws XWikiException
    {
        try {
            if (doc.getStore() == null) {
                Object[] args = {doc.getFullName()};
                throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                    XWikiException.ERROR_XWIKI_STORE_HIBERNATE_CANNOT_DELETE_UNLOADED_DOC,
                    "Impossible to delete document {0} if it is not loaded", null, args);
            }

            DocumentId docId = valueConverter.getId(doc);
            documentDao.delete(docId);

            // Let's delete any attachment this document might have
            for (XWikiAttachment attachment : doc.getAttachmentList()) {
                if (attachmentStore != null) {
                    attachmentStore.deleteXWikiAttachment(attachment, false, context, false);
                }
            }

            // deleting XWikiLinks
            if (context.getWiki().hasBacklinks(context)) {
                deleteLinks(doc.getId(), context, true);
            }

            // Find the list of classes for which we have an object
            // Remove properties planned for removal
            for (BaseObject obj : doc.getObjectsToRemove()) {
                objectDao.delete(new ObjectId(docId, obj.getNumber()));
            }
            doc.setObjectsToRemove(new ArrayList<BaseObject>());
            for (Vector<BaseObject> objects : doc.getxWikiObjects().values()) {
                for (BaseObject obj : objects) {
                    objectDao.delete(new ObjectId(docId, obj.getNumber()));
                }
            }
            if (versioningStore != null) {
                versioningStore.deleteArchive(doc, false, context);
            }

            documentDao.delete(docId);

            // We need to ensure that the deleted document becomes the original document
            doc.setOriginalDocument((XWikiDocument) doc.clone());
        } catch (Exception e) {
            Object[] args = {doc.getFullName()};
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                XWikiException.ERROR_XWIKI_STORE_HIBERNATE_DELETING_DOC, "Exception while deleting document {0}", e,
                args);
        }
    }

    public XWikiLock loadLock(long docId, XWikiContext context, boolean bTransaction) throws XWikiException
    {
        // TODO:
        return null;
    }

    public void saveLock(XWikiLock lock, XWikiContext context, boolean bTransaction) throws XWikiException
    {
        // TODO:
    }

    public void deleteLock(XWikiLock lock, XWikiContext context, boolean bTransaction) throws XWikiException
    {
        // TODO:
    }

    public List<XWikiLink> loadLinks(long docId, XWikiContext context, boolean bTransaction) throws XWikiException
    {
        List<XWikiLink> links = new ArrayList<XWikiLink>();
        // TODO:
        return links;
    }

    public List loadBacklinks(String fullName, XWikiContext context, boolean bTransaction) throws XWikiException
    {
        List backlinks = new ArrayList();
        // TODO:
        return backlinks;
    }

    public void saveLinks(XWikiDocument doc, XWikiContext context, boolean bTransaction) throws XWikiException
    {
        // TODO:
    }

    public void deleteLinks(long docId, XWikiContext context, boolean bTransaction) throws XWikiException
    {
        // TODO:
    }

    public List<String> getClassList(XWikiContext context) throws XWikiException
    {
        // TODO:
        return Collections.emptyList();
    }

    @Deprecated
    public List<String> searchDocumentsNames(String parametrizedSqlClause, List parameterValues, XWikiContext context)
        throws XWikiException
    {
        return searchDocumentsNames(parametrizedSqlClause, 0, 0, parameterValues, context);
    }

    @Deprecated
    public List<String> searchDocumentsNames(String parametrizedSqlClause, int nb, int start, List parameterValues,
        XWikiContext context) throws XWikiException
    {
        return Collections.emptyList();
    }

    @Deprecated
    public List search(String sql, int nb, int start, XWikiContext context) throws XWikiException
    {
        return search(sql, nb, start, (List) null, context);
    }

    @Deprecated
    public List search(String sql, int nb, int start, List parameterValues, XWikiContext context) throws XWikiException
    {
        return search(sql, nb, start, null, parameterValues, context);
    }

    @Deprecated
    public List search(String sql, int nb, int start, Object[][] whereParams, XWikiContext context)
        throws XWikiException
    {
        return search(sql, nb, start, whereParams, null, context);
    }

    @Deprecated    
    public List search(String sql, int nb, int start, Object[][] whereParams, List parameterValues, XWikiContext context)
        throws XWikiException
    {
        // unsupported
        return Collections.emptyList();
    }

    @Deprecated
    public List<String> searchDocumentsNames(String wheresql, int nb, int start, String selectColumns,
        XWikiContext context) throws XWikiException
    {
        // unsupported
        return Collections.emptyList();
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, boolean customMapping,
        boolean checkRight, int nb, int start, XWikiContext context) throws XWikiException
    {
        return searchDocuments(wheresql, distinctbylanguage, customMapping, checkRight, nb, start, null, context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, boolean customMapping,
        boolean checkRight, int nb, int start, List parameterValues, XWikiContext context) throws XWikiException
    {
        // unsupported
        return Collections.emptyList();
    }

    @Deprecated
    public boolean isCustomMappingValid(BaseClass bclass, String custommapping1, XWikiContext context)
    {
        // unsupported
        return false;
    }

    @Deprecated
    public void injectCustomMappings(XWikiContext context) throws XWikiException
    {
        // unsupported
    }

    @Deprecated
    public void injectUpdatedCustomMappings(XWikiContext context) throws XWikiException
    {
        // unsupported
    }

    @Deprecated
    public boolean injectCustomMappings(XWikiDocument doc, XWikiContext context) throws XWikiException
    {
        // unsupported
        return false;
    }

    @Deprecated
    public boolean injectCustomMapping(BaseClass doc1class, XWikiContext context) throws XWikiException
    {
        // unsupported
        return false;
    }

    @Deprecated
    public List getCustomMappingPropertyList(BaseClass bclass)
    {
        // unsupported
        return Collections.emptyList();
    }

    @Deprecated
    public List<String> searchDocumentsNames(String wheresql, XWikiContext context) throws XWikiException
    {
        return searchDocumentsNames(wheresql, 0, 0, "", context);
    }

    @Deprecated
    public List<String> searchDocumentsNames(String wheresql, int nb, int start, XWikiContext context)
        throws XWikiException
    {
        return searchDocumentsNames(wheresql, nb, start, "", context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, XWikiContext context) throws XWikiException
    {
        return searchDocuments(wheresql, null, context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, List parameterValues, XWikiContext context)
        throws XWikiException
    {
        return searchDocuments(wheresql, 0, 0, parameterValues, context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, XWikiContext context)
        throws XWikiException
    {
        return searchDocuments(wheresql, distinctbylanguage, 0, 0, context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, boolean customMapping,
        XWikiContext context) throws XWikiException
    {
        return searchDocuments(wheresql, distinctbylanguage, customMapping, 0, 0, context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, int nb, int start, XWikiContext context)
        throws XWikiException
    {
        return searchDocuments(wheresql, nb, start, null, context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, int nb, int start, List parameterValues,
        XWikiContext context) throws XWikiException
    {
        return searchDocuments(wheresql, true, nb, start, parameterValues, context);
    }


    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, int nb, int start,
        List parameterValues, XWikiContext context) throws XWikiException
    {
        return searchDocuments(wheresql, distinctbylanguage, false, nb, start, parameterValues, context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, int nb, int start,
        XWikiContext context) throws XWikiException
    {
        return searchDocuments(wheresql, distinctbylanguage, nb, start, null, context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, boolean customMapping,
        int nb, int start, XWikiContext context) throws XWikiException
    {
        return searchDocuments(wheresql, distinctbylanguage, customMapping, nb, start, null, context);
    }

    @Deprecated
    public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, boolean customMapping,
        int nb, int start, List parameterValues, XWikiContext context) throws XWikiException
    {
        return searchDocuments(wheresql, distinctbylanguage, customMapping, true, nb, start, parameterValues, context);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getTranslationList(XWikiDocument doc, XWikiContext context) throws XWikiException
    {
        // TODO:
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    public QueryManager getQueryManager()
    {
        return queryManager;
    }

    public void cleanUp(XWikiContext context)
    {
        // TODO:
    }
}
