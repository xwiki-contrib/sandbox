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
package org.xwiki.eclipse.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.swizzle.confluence.Attachment;
import org.codehaus.swizzle.confluence.ServerInfo;
import org.codehaus.swizzle.confluence.Space;
import org.codehaus.swizzle.confluence.SpaceSummary;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.xwiki.eclipse.core.model.XWikiEclipseAttachmentSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseClass;
import org.xwiki.eclipse.core.model.XWikiEclipseClassSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseObject;
import org.xwiki.eclipse.core.model.XWikiEclipseObjectSummary;
import org.xwiki.eclipse.core.model.XWikiEclipsePage;
import org.xwiki.eclipse.core.model.XWikiEclipsePageHistorySummary;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.core.notifications.CoreEvent;
import org.xwiki.eclipse.core.notifications.NotificationManager;
import org.xwiki.eclipse.core.storage.IDataStorage;
import org.xwiki.eclipse.core.storage.LocalXWikiDataStorage;
import org.xwiki.eclipse.core.storage.RemoteXWikiDataStorage;
import org.xwiki.eclipse.core.utils.PersistentMap;
import org.xwiki.xmlrpc.model.XWikiClass;
import org.xwiki.xmlrpc.model.XWikiClassSummary;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiObjectSummary;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwiki.xmlrpc.model.XWikiPageHistorySummary;
import org.xwiki.xmlrpc.model.XWikiPageSummary;

/**
 * A class that implements a controller for handling data and the connection towards an XWiki server. It takes care of
 * synchronizing pages, objects, handling local copies, conflicts, etc.
 */
public class DataManager
{
    private static final IPath DATA_MANAGER_DIRECTORY = new Path(".xwikieclipse"); //$NON-NLS-1$

    private static final IPath LOCAL_STORAGE_DIRECTORY = DATA_MANAGER_DIRECTORY.append("local_storage"); //$NON-NLS-1$

    private static final IPath LAST_RETRIEVED_PAGE_DIRECTORY = DATA_MANAGER_DIRECTORY.append("last_retrieved_pages"); //$NON-NLS-1$

    private static final IPath CONFLICTING_PAGES_DIRECTORY = DATA_MANAGER_DIRECTORY.append("conflicting_pages"); //$NON-NLS-1$

    private static final String PAGES_STATUS = "pagesStatus.index"; //$NON-NLS-1$

    private static final String OBJECTS_STATUS = "objectsStatus.index"; //$NON-NLS-1$

    /**
     * The project associated to this data manager.
     */
    private IProject project;

    /**
     * The remote XWiki
     */
    private RemoteXWikiDataStorage remoteXWikiDataStorage;

    /**
     * A local XWiki data storage for caching XWiki elements.
     */
    private LocalXWikiDataStorage localXWikiDataStorage;

    private LocalXWikiDataStorage lastRetrievedPagesDataStorage;

    private LocalXWikiDataStorage conflictingPagesDataStorage;

    private static final String DIRTY_STATUS = "dirty"; //$NON-NLS-1$

    private static final String CONFLICTING_STATUS = "conflicting"; //$NON-NLS-1$

    private PersistentMap pageToStatusMap;

    private PersistentMap objectToStatusMap;

    private Set<Functionality> supportedFunctionalities;

    /* Properties for projects associated to data managers */
    public static final QualifiedName AUTO_CONNECT = new QualifiedName("xwiki.eclipse", "auto_connect"); //$NON-NLS-1$ //$NON-NLS-2$

    public static final QualifiedName PASSWORD = new QualifiedName("xwiki.eclipse", "password"); //$NON-NLS-1$ //$NON-NLS-2$

    public static final QualifiedName USERNAME = new QualifiedName("xwiki.eclipse", "username"); //$NON-NLS-1$ //$NON-NLS-2$

    public static final QualifiedName ENDPOINT = new QualifiedName("xwiki.eclipse", "endpoint"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Constructor.
     * 
     * @param project The project this data manager is associated with.
     * @throws CoreException
     */
    public DataManager(IProject project) throws CoreException
    {
        Assert.isNotNull(project); //$NON-NLS-1$
        this.project = project;

        remoteXWikiDataStorage = null;

        localXWikiDataStorage = new LocalXWikiDataStorage(project.getFolder(LOCAL_STORAGE_DIRECTORY));

        lastRetrievedPagesDataStorage = new LocalXWikiDataStorage(project.getFolder(LAST_RETRIEVED_PAGE_DIRECTORY));

        conflictingPagesDataStorage = new LocalXWikiDataStorage(project.getFolder(CONFLICTING_PAGES_DIRECTORY));

        pageToStatusMap = new PersistentMap(project.getFolder(DATA_MANAGER_DIRECTORY).getFile(PAGES_STATUS));

        objectToStatusMap = new PersistentMap(project.getFolder(DATA_MANAGER_DIRECTORY).getFile(OBJECTS_STATUS));

        /*
         * At the beginning we operate locally, and the local storage always support all extended functionalities, i.e.,
         * objects, etc.
         */
        supportedFunctionalities = new HashSet<Functionality>();
        supportedFunctionalities.add(Functionality.OBJECTS);
        supportedFunctionalities.add(Functionality.RENAME);
        supportedFunctionalities.add(Functionality.TRANSLATIONS);
        supportedFunctionalities.add(Functionality.ALL_PAGES_RETRIEVAL);
        supportedFunctionalities.add(Functionality.EFFICIENT_RETRIEVAL);
        supportedFunctionalities.add(Functionality.EFFICIENT_RENAME);
        supportedFunctionalities.add(Functionality.LAZY_RETRIEVAL);
    }

    /*
     * Property getters and setters
     */
    public IProject getProject()
    {
        return project;
    }

    public Set<Functionality> getSupportedFunctionalities()
    {
        return supportedFunctionalities;
    }

    public String getName()
    {
        return project.getName();
    }

    public String getEndpoint() throws CoreException
    {
        return project.getPersistentProperty(ENDPOINT);
    }

    public void setEndpoint(final String endpoint) throws CoreException
    {
        project.setPersistentProperty(ENDPOINT, endpoint);
    }

    public String getUserName() throws CoreException
    {
        return project.getPersistentProperty(USERNAME);
    }

    public void setUserName(final String userName) throws CoreException
    {
        project.setPersistentProperty(USERNAME, userName);
    }

    public String getPassword() throws CoreException
    {
        return project.getPersistentProperty(PASSWORD);
    }

    public void setPassword(final String password) throws CoreException
    {
        project.setPersistentProperty(PASSWORD, password);
    }

    public boolean isAutoConnect() throws CoreException
    {
        return project.getPersistentProperty(AUTO_CONNECT) != null;
    }

    public void setAutoConnect(final boolean autoConnect) throws CoreException
    {
        project.setPersistentProperty(AUTO_CONNECT, autoConnect ? "true" : null); //$NON-NLS-1$
    }

    /*
     * Connection management
     */
    public boolean isConnected()
    {
        return remoteXWikiDataStorage != null;
    }

    public void connect() throws XWikiEclipseException, CoreException
    {
        if (isConnected()) {
            return;
        }

        remoteXWikiDataStorage = new RemoteXWikiDataStorage(getEndpoint(), getUserName(), getPassword());
        try {
            ServerInfo serverInfo = remoteXWikiDataStorage.getServerInfo();

            if (serverInfo.getBaseUrl().contains("xwiki")) {
                if (serverInfo.getMajorVersion() == 1) {
                    // FIXME: Which version exactly? Apparently gsoc.myxwiki.org is running 2.0M1, hence fails.
                    supportedFunctionalities.remove(Functionality.EFFICIENT_RETRIEVAL);
                    supportedFunctionalities.remove(Functionality.EFFICIENT_RENAME);
                    supportedFunctionalities.remove(Functionality.LAZY_RETRIEVAL);
                    if (serverInfo.getMinorVersion() < 5) {
                        supportedFunctionalities.remove(Functionality.RENAME);
                    }

                    if (serverInfo.getMinorVersion() < 4) {
                        supportedFunctionalities.remove(Functionality.TRANSLATIONS);
                        supportedFunctionalities.remove(Functionality.ALL_PAGES_RETRIEVAL);
                    }
                }
            } else {
                /* We are talking to a confluence server */
                supportedFunctionalities.remove(Functionality.EFFICIENT_RETRIEVAL);
                supportedFunctionalities.remove(Functionality.EFFICIENT_RENAME);
                supportedFunctionalities.remove(Functionality.TRANSLATIONS);
                supportedFunctionalities.remove(Functionality.OBJECTS);
                supportedFunctionalities.remove(Functionality.ALL_PAGES_RETRIEVAL);
                supportedFunctionalities.remove(Functionality.LAZY_RETRIEVAL);
            }
        } catch (Exception e) {
            /* Here we are talking to an XWiki < 1.4. In this case we only support basic functionalities. */
            supportedFunctionalities.clear();
        }

        /* When connected synchronize all the pages and objects */
        synchronizePages(new HashSet<String>(pageToStatusMap.keySet()));
        synchronizeObjects(new HashSet<String>(objectToStatusMap.keySet()));

        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.DATA_MANAGER_CONNECTED, this, this);
    }

    public void disconnect()
    {
        remoteXWikiDataStorage.dispose();
        remoteXWikiDataStorage = null;

        /* Set this to true, because the local storage always support extended features */
        supportedFunctionalities.clear();
        supportedFunctionalities.add(Functionality.OBJECTS);
        supportedFunctionalities.add(Functionality.RENAME);

        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.DATA_MANAGER_DISCONNECTED, this, this);
    }

    /*
     * Space retrieval
     */
    public XWikiEclipseSpaceSummary getSpaceSummary(String spaceKey) throws XWikiEclipseException
    {
        SpaceSummary space = null;

        if (isConnected()) {
            space = remoteXWikiDataStorage.getSpaceSumary(spaceKey);
        } else {
            space = localXWikiDataStorage.getSpaceSumary(spaceKey);
        }

        return new XWikiEclipseSpaceSummary(this, space);
    }

    public List<XWikiEclipseSpaceSummary> getSpaces() throws XWikiEclipseException
    {
        List<SpaceSummary> spaceSummaries;

        if (isConnected()) {
            spaceSummaries = remoteXWikiDataStorage.getSpaces();
        } else {
            spaceSummaries = localXWikiDataStorage.getSpaces();
        }

        List<XWikiEclipseSpaceSummary> result = new ArrayList<XWikiEclipseSpaceSummary>();
        for (SpaceSummary spaceSummary : spaceSummaries) {
            result.add(new XWikiEclipseSpaceSummary(this, spaceSummary));
        }

        return result;
    }

    public List<XWikiEclipseSpaceSummary> getSpaces(int start, int limit) throws XWikiEclipseException
    {

        // If called unknowingly.
        if (!supportedFunctionalities.contains(Functionality.LAZY_RETRIEVAL)) {
            return this.getSpaces();
        }

        List<SpaceSummary> spaceSummaries;

        if (isConnected()) {
            spaceSummaries = remoteXWikiDataStorage.getSpaces(start, limit);

            List<XWikiEclipseSpaceSummary> result = new ArrayList<XWikiEclipseSpaceSummary>();
            for (SpaceSummary spaceSummary : spaceSummaries) {
                result.add(new XWikiEclipseSpaceSummary(this, spaceSummary));
            }

            return result;

        } else {
            // spaceSummaries = localXWikiDataStorage.getSpaces();
        }

        // List<XWikiEclipseSpaceSummary> result = new ArrayList<XWikiEclipseSpaceSummary>();
        // for (SpaceSummary spaceSummary : spaceSummaries) {
        // result.add(new XWikiEclipseSpaceSummary(this, spaceSummary));
        // }

        return null;
    }

    /*
     * Page retrieval
     */
    public List<XWikiEclipsePageSummary> getPages(final String spaceKey) throws XWikiEclipseException
    {
        List<XWikiPageSummary> pageSummaries;

        if (isConnected()) {
            pageSummaries = remoteXWikiDataStorage.getPages(spaceKey);
        } else {
            pageSummaries = localXWikiDataStorage.getPages(spaceKey);
        }

        List<XWikiEclipsePageSummary> result = new ArrayList<XWikiEclipsePageSummary>();
        for (XWikiPageSummary pageSummary : pageSummaries) {
            result.add(new XWikiEclipsePageSummary(this, pageSummary));
        }

        return result;
    }

    /*
     * Page retrieval
     */
    public List<XWikiEclipsePageSummary> getPages(final String spaceKey, int start, int limit)
        throws XWikiEclipseException
    {

        // If called unknowingly.
        if (!supportedFunctionalities.contains(Functionality.LAZY_RETRIEVAL)) {
            return this.getPages(spaceKey);
        }

        List<XWikiPageSummary> pageSummaries;

        if (isConnected()) {
            pageSummaries = remoteXWikiDataStorage.getPages(spaceKey, start, limit);
            List<XWikiEclipsePageSummary> result = new ArrayList<XWikiEclipsePageSummary>();
            for (XWikiPageSummary pageSummary : pageSummaries) {
                result.add(new XWikiEclipsePageSummary(this, pageSummary));
            }
            return result;
        } else {
            // FIXME: for fix local storage here.
            // pageSummaries = localXWikiDataStorage.getPages(spaceKey);
        }

        // List<XWikiEclipsePageSummary> result = new ArrayList<XWikiEclipsePageSummary>();
        // for (XWikiPageSummary pageSummary : pageSummaries) {
        // result.add(new XWikiEclipsePageSummary(this, pageSummary));
        // }

        // return result;
        return null;

    }

    public XWikiEclipsePage getPage(String pageId) throws XWikiEclipseException
    {
        XWikiPage page = null;

        page = localXWikiDataStorage.getPage(pageId);
        if (page != null) {
            String pageStatus = pageToStatusMap.get(pageId);
            /* If our local page is either dirty or in conflict then return it */
            if (pageStatus != null) {
                return new XWikiEclipsePage(this, page);
            }
        }

        /*
         * If we are here either there is no cached page, or the cached page is not dirty and not in conflict, so we can
         * grab the latest version of the page and store it in the local storage.
         */
        if (isConnected()) {
            page = remoteXWikiDataStorage.getPage(pageId);

            localXWikiDataStorage.storePage(page);

            /* Write an additional copy of the page that can be useful for performing 3-way diffs */
            lastRetrievedPagesDataStorage.storePage(page);

            XWikiEclipsePage result = new XWikiEclipsePage(this, page);

            /* Fire the stored notification to communicate that the page has been stored in the local storage */
            NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.PAGE_STORED, this, result);

            return result;
        }

        /* If we are here we are not connected so we can return the page that we have retrieved at the beginning */
        return new XWikiEclipsePage(this, page);
    }

    public XWikiEclipsePage storePage(XWikiEclipsePage page) throws XWikiEclipseException
    {
        Assert.isNotNull(page);

        XWikiPage storedPage = localXWikiDataStorage.storePage(page.getData());

        /*
         * Set the dirty flag only if the page has no status. In fact it might be already dirty (should not be possible
         * though) or in conflict
         */
        if (pageToStatusMap.get(page.getData().getId()) == null) {
            pageToStatusMap.put(page.getData().getId(), DIRTY_STATUS);
        }

        page = new XWikiEclipsePage(this, synchronize(storedPage));

        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.PAGE_STORED, this, page);

        return page;
    }

    public Boolean addSpace(Space space) throws XWikiEclipseException
    {
        Assert.isNotNull(space);

        if (isConnected()) {
            remoteXWikiDataStorage.addSpace(space);
        }

        XWikiEclipseSpaceSummary spaceSummary = getSpaceSummary(space.getKey());

        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.SPACE_ADDED, this, spaceSummary);

        return true;
    }

    private XWikiPage synchronize(XWikiPage page) throws XWikiEclipseException
    {
        /* If we are not connected then do nothing */
        if (!isConnected()) {
            return page;
        }

        /*
         * If the page is not dirty (i.e., is in conflict or has no status associated) then do nothing
         */
        if (!DIRTY_STATUS.equals(pageToStatusMap.get(page.getId()))) {
            return page;
        }

        Assert.isTrue(isConnected());
        Assert.isTrue(DIRTY_STATUS.equals(pageToStatusMap.get(page.getId())));

        XWikiPage remotePage = null;
        try {
            remotePage = remoteXWikiDataStorage.getPage(page.getId());

            if (remotePage.getLanguage() != null && !remotePage.getLanguage().equals(page.getLanguage())) {
                /*
                 * The requested translation has not been found, so we are creating a new translation. We set the
                 * remotePage to null in order to force the page creation
                 */
                remotePage = null;
            }
        } catch (XWikiEclipseException e) {
            /*
             * This can fail if the remote page does not yet exist. So ignore the exception here and handle the
             * condition later: remotePage will be null if we are here.
             */
        }

        if (remotePage == null) {
            /* If we are here the page or its translation don't exist. Create it! */
            page = remoteXWikiDataStorage.storePage(page);

            localXWikiDataStorage.storePage(page);

            clearPageStatus(page.getId());
        } else if (page.getVersion() == remotePage.getVersion()) {
            /* This might be a rename */
            if (remotePage.getTitle().equals(page.getTitle())) {
                /* If the local and remote content are equals, no need to re-store remotely the page. */
                if (remotePage.getContent().equals(page.getContent())) {
                    page = remotePage;
                } else {
                    page = remoteXWikiDataStorage.storePage(page);
                }
            } else {
                page = remoteXWikiDataStorage.storePage(page);
            }

            localXWikiDataStorage.storePage(page);

            clearPageStatus(page.getId());
        } else {
            pageToStatusMap.put(page.getId(), CONFLICTING_STATUS);
            conflictingPagesDataStorage.storePage(remotePage);
        }

        return page;
    }

    public void clearConflictingStatus(String pageId) throws XWikiEclipseException
    {
        conflictingPagesDataStorage.removePage(pageId);
        pageToStatusMap.put(pageId, DIRTY_STATUS);
    }

    public void clearPageStatus(String pageId) throws XWikiEclipseException
    {
        conflictingPagesDataStorage.removePage(pageId);
        pageToStatusMap.remove(pageId);
    }

    public boolean isInConflict(String pageId)
    {
        return CONFLICTING_STATUS.equals(pageToStatusMap.get(pageId));
    }

    public List<XWikiEclipseAttachmentSummary> getAttachments(String pageId) throws XWikiEclipseException
    {
        List<XWikiEclipseAttachmentSummary> result = new ArrayList<XWikiEclipseAttachmentSummary>();

        XWikiEclipsePageSummary xwikiPageSummary = getPageSummary(pageId);

        if (isConnected()) {
            List<Attachment> attachments = remoteXWikiDataStorage.getAttachments(pageId);

            for (Attachment attachment : attachments) {
                result.add(new XWikiEclipseAttachmentSummary(this, attachment, xwikiPageSummary.getData()));
            }
        } else {
            /* FIXME: Check if works */
            List<Attachment> attachments = localXWikiDataStorage.getAttachments(pageId);

            for (Attachment attachment : attachments) {
                result.add(new XWikiEclipseAttachmentSummary(this, attachment, xwikiPageSummary.getData()));
            }
        }

        return result;
    }

    public XWikiEclipsePage getConflictingPage(String pageId) throws XWikiEclipseException
    {
        return new XWikiEclipsePage(this, conflictingPagesDataStorage.getPage(pageId));
    }

    public XWikiEclipsePage getConflictAncestorPage(String pageId) throws XWikiEclipseException
    {
        XWikiPage ancestorPage = lastRetrievedPagesDataStorage.getPage(pageId);
        return ancestorPage != null ? new XWikiEclipsePage(this, ancestorPage) : null;
    }

    /*
     * Objects
     */

    public List<XWikiEclipseObjectSummary> getObjects(String pageId) throws XWikiEclipseException
    {
        List<XWikiEclipseObjectSummary> result = new ArrayList<XWikiEclipseObjectSummary>();

        if (!supportedFunctionalities.contains(Functionality.OBJECTS)) {
            return result;
        }

        XWikiEclipsePageSummary xwikiPageSummary = getPageSummary(pageId);

        if (isConnected()) {
            List<XWikiObjectSummary> objects = remoteXWikiDataStorage.getObjects(pageId);

            for (XWikiObjectSummary object : objects) {
                result.add(new XWikiEclipseObjectSummary(this, object, xwikiPageSummary.getData()));
            }
        } else {
            List<XWikiObjectSummary> objects = localXWikiDataStorage.getObjects(pageId);

            for (XWikiObjectSummary object : objects) {
                result.add(new XWikiEclipseObjectSummary(this, object, xwikiPageSummary.getData()));
            }
        }

        return result;
    }

    public XWikiEclipseObject getObject(String pageId, String className, int id) throws XWikiEclipseException
    {
        if (isConnected()) {
            XWikiClass xwikiClass = remoteXWikiDataStorage.getClass(className);
            XWikiObject xwikiObject = remoteXWikiDataStorage.getObject(pageId, className, id);
            XWikiPageSummary xwikiPageSummary = remoteXWikiDataStorage.getPageSummary(pageId);

            localXWikiDataStorage.storeObject(xwikiObject);
            localXWikiDataStorage.storeClass(xwikiClass);

            XWikiEclipseObject result = new XWikiEclipseObject(this, xwikiObject, xwikiClass, xwikiPageSummary);

            /* Fire the stored notification to communicate that the object has been stored in the local storage */
            NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.OBJECT_STORED, this, result);

            return result;
        } else {
            XWikiClass xwikiClass = localXWikiDataStorage.getClass(className);
            XWikiPageSummary xwikiPageSummary = localXWikiDataStorage.getPageSummary(pageId);

            return new XWikiEclipseObject(this, localXWikiDataStorage.getObject(pageId, className, id), xwikiClass,
                xwikiPageSummary);
        }
    }

    public XWikiEclipseClass getClass(String classId) throws XWikiEclipseException
    {
        if (isConnected()) {
            return new XWikiEclipseClass(this, remoteXWikiDataStorage.getClass(classId));

        } else {
            return new XWikiEclipseClass(this, localXWikiDataStorage.getClass(classId));
        }
    }

    public XWikiEclipsePageSummary getPageSummary(String pageId) throws XWikiEclipseException
    {
        if (isConnected()) {
            return new XWikiEclipsePageSummary(this, remoteXWikiDataStorage.getPageSummary(pageId));

        } else {
            return new XWikiEclipsePageSummary(this, localXWikiDataStorage.getPageSummary(pageId));
        }
    }

    public XWikiEclipseObject storeObject(XWikiEclipseObject object) throws XWikiEclipseException
    {
        localXWikiDataStorage.storeObject(object.getData());

        objectToStatusMap.put(getCompactIdForObject(object.getData()), DIRTY_STATUS);

        object =
            new XWikiEclipseObject(this, synchronize(object.getData()), object.getXWikiClass(), object.getPageSummary());

        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.OBJECT_STORED, this, object);

        return object;
    }

    private XWikiObject synchronize(XWikiObject object) throws XWikiEclipseException
    {
        /* If we are not connected then do nothing */
        if (!isConnected()) {
            return object;
        }

        /*
         * If the page is not dirty (i.e., is in conflict or has no status associated) then do nothing
         */
        if (!DIRTY_STATUS.equals(objectToStatusMap.get(getCompactIdForObject(object)))) {
            return object;
        }

        Assert.isTrue(isConnected());
        Assert.isTrue(DIRTY_STATUS.equals(objectToStatusMap.get(getCompactIdForObject(object))));

        if (object.getId() == -1) {
            /*
             * If we are here we are synchronizing an object that has been created locally and does not exist remotely.
             */

            /*
             * We save the current object because its id will be assigned when the object is stored remotely. In this
             * way, we will be able to cleanup all the references to the locally created object with the -1 id from the
             * status map, index and local storage.
             */
            XWikiObject previousObject = object;

            object = remoteXWikiDataStorage.storeObject(object);
            localXWikiDataStorage.storeObject(object);
            objectToStatusMap.remove(getCompactIdForObject(object));

            /* Cleanup */
            localXWikiDataStorage.removeObject(previousObject.getPageId(), previousObject.getClassName(),
                previousObject.getId());
            objectToStatusMap.remove(getCompactIdForObject(previousObject));
        } else {
            object = remoteXWikiDataStorage.storeObject(object);
            localXWikiDataStorage.storeObject(object);

            objectToStatusMap.remove(getCompactIdForObject(object));
        }

        return object;
    }

    private void synchronizePages(Set<String> pageIds) throws XWikiEclipseException
    {
        for (String pageId : pageIds) {
            XWikiPage page = localXWikiDataStorage.getPage(pageId);
            if (page != null) {
                synchronize(page);
            }
        }
    }

    private void synchronizeObjects(Set<String> objectCompactIds) throws XWikiEclipseException
    {
        for (String objectCompactId : objectCompactIds) {
            XWikiObject object = getObjectByCompactId(localXWikiDataStorage, objectCompactId);
            if (object != null) {
                synchronize(object);
            }
        }
    }

    public boolean isLocallyAvailable(XWikiEclipsePageSummary pageSummary)
    {
        return localXWikiDataStorage.exists(pageSummary.getData().getId());
    }

    public boolean isLocallyAvailable(XWikiEclipseObjectSummary objectSummary)
    {
        return localXWikiDataStorage.exists(objectSummary.getData().getPageId(),
            objectSummary.getData().getClassName(), objectSummary.getData().getId());
    }

    private String getCompactIdForObject(XWikiObject object)
    {
        return String.format("%s/%s/%d", object.getPageId(), object.getClassName(), object.getId());
    }

    private XWikiObject getObjectByCompactId(IDataStorage storage, String compactId) throws NumberFormatException,
        XWikiEclipseException
    {
        String[] components = compactId.split("/");
        return storage.getObject(components[0], components[1], Integer.parseInt(components[2]));
    }

    public XWikiEclipsePage createPage(String spaceKey, String name, String title, String content)
        throws XWikiEclipseException
    {
        return createPage(spaceKey, name, title, null, content);
    }

    public XWikiEclipsePage createPage(String spaceKey, String name, String title, String language, String content)
        throws XWikiEclipseException
    {
        XWikiPage xwikiPage = new XWikiPage();
        xwikiPage.setSpace(spaceKey);
        xwikiPage.setTitle(title);
        if (language != null) {
            xwikiPage.setId(String.format("%s.%s?language=%s", spaceKey, name, language));
        } else {
            xwikiPage.setId(String.format("%s.%s", spaceKey, name));
        }
        xwikiPage.setContent(content);
        xwikiPage.setVersion(1);
        xwikiPage.setMinorVersion(1);
        xwikiPage.setContentStatus("");
        xwikiPage.setCreated(new Date());
        xwikiPage.setCreator("");
        if (language != null) {
            xwikiPage.setLanguage(language);
        } else {
            xwikiPage.setLanguage("");
        }
        xwikiPage.setModified(new Date());
        xwikiPage.setModifier("");
        xwikiPage.setParentId("");
        xwikiPage.setTranslations(new ArrayList<String>());
        xwikiPage.setUrl("");

        XWikiEclipsePage page = new XWikiEclipsePage(this, xwikiPage);

        return storePage(page);
    }

    public XWikiEclipseObject createObject(String pageId, String className) throws XWikiEclipseException
    {
        XWikiObject xwikiObject = new XWikiObject();
        xwikiObject.setClassName(className);
        xwikiObject.setPageId(pageId);
        xwikiObject.setId(-1);
        xwikiObject.setPrettyName(String.format("%s[NEW]", className));

        XWikiEclipseClass xwikiClass = getClass(className);
        XWikiEclipsePageSummary xwikiPageSummary = getPageSummary(pageId);

        XWikiEclipseObject object =
            new XWikiEclipseObject(this, xwikiObject, xwikiClass.getData(), xwikiPageSummary.getData());

        object = storeObject(object);

        return object;
    }

    public List<XWikiEclipseClassSummary> getClasses() throws XWikiEclipseException
    {
        List<XWikiClassSummary> classSummaries;

        if (isConnected()) {
            classSummaries = remoteXWikiDataStorage.getClasses();
        } else {
            classSummaries = localXWikiDataStorage.getClasses();
        }

        List<XWikiEclipseClassSummary> result = new ArrayList<XWikiEclipseClassSummary>();
        for (XWikiClassSummary classSummary : classSummaries) {
            result.add(new XWikiEclipseClassSummary(this, classSummary));
        }

        return result;
    }

    public void removePage(String pageId) throws XWikiEclipseException
    {
        XWikiPage page = null;

        if (isConnected()) {
            page = remoteXWikiDataStorage.getPage(pageId);
            remoteXWikiDataStorage.removePage(pageId);
        } else {
            page = localXWikiDataStorage.getPage(pageId);
        }

        localXWikiDataStorage.removePage(pageId);

        String spaceKey = page.getSpace();

        List<XWikiEclipsePageSummary> pages = null;
        try {
            pages = getPages(spaceKey);
        } catch (XWikiEclipseException e) {
            CoreLog.logError("Unable to get space pages: " + e.getMessage());
        }

        if (pages != null && pages.size() == 0) {
            // The space is left with no pages so it has to be removed.
            localXWikiDataStorage.removeSpace(spaceKey);
        }

        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.PAGE_REMOVED, this,
            new XWikiEclipsePage(this, page));
    }

    public void removeSpace(String spaceKey) throws XWikiEclipseException
    {
        XWikiEclipseSpaceSummary space = getSpaceSummary(spaceKey);

        if (space != null) {
            remoteXWikiDataStorage.removeSpace(spaceKey);
            localXWikiDataStorage.removeSpace(spaceKey);

            NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.SPACE_REMOVED, this, space);
        }
    }

    public void removeAttachment(XWikiEclipseAttachmentSummary attachmentSummary) throws XWikiEclipseException
    {
        if (isConnected()) {
            remoteXWikiDataStorage.removeAttachment(attachmentSummary.getData());
        }
        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.ATTACHMENT_REMOVED, this, attachmentSummary);
        // FIXME LocalXWikiDataStorage.
    }

    public void removeObject(String pageId, String className, int objectId) throws XWikiEclipseException
    {
        XWikiObject object = null;

        if (isConnected()) {
            object = remoteXWikiDataStorage.getObject(pageId, className, objectId);
            remoteXWikiDataStorage.removeObject(pageId, className, objectId);
        } else {
            object = localXWikiDataStorage.getObject(pageId, className, objectId);
        }

        localXWikiDataStorage.removeObject(pageId, className, objectId);

        NotificationManager.getDefault().fireCoreEvent(
            CoreEvent.Type.OBJECT_REMOVED,
            this,
            new XWikiEclipseObject(this, object, getClass(object.getClassName()).getData(), getPageSummary(
                object.getPageId()).getData()));
    }

    public boolean renamePage(String pageId, String newSpace, String newPageName) throws XWikiEclipseException
    {
        if (supportedFunctionalities.contains(Functionality.EFFICIENT_RENAME)) {
            clearPageStatus(pageId);
            XWikiEclipsePage page = getPage(pageId);
            localXWikiDataStorage.removePage(pageId);
            Boolean result = remoteXWikiDataStorage.renamePage(pageId, newSpace + "." + newPageName);
            XWikiEclipsePage newPage = getPage(String.format("%s.%s", newSpace, newPageName));
            XWikiEclipsePage pages[] = new XWikiEclipsePage[] {page, newPage};
            NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.PAGE_RENAMED, this, pages);
            return result;
        }
        if (!supportedFunctionalities.contains(Functionality.RENAME)) {
            return false;

        }
        XWikiEclipsePage page = getPage(pageId);
        page.getData().setSpace(newSpace);
        page.getData().setTitle(newPageName);
        storePage(page);

        /* Remove the old page from the cache */
        clearPageStatus(pageId);
        localXWikiDataStorage.removePage(pageId);

        /* Retrieve the new page for caching it */
        XWikiEclipsePage newPage;
        if (pageId.indexOf('.') != -1) {
            newPage = getPage(String.format("%s.%s", newSpace, newPageName));
        } else {
            newPage = getPage(pageId);
        }

        XWikiEclipsePage pages[] = new XWikiEclipsePage[] {page, newPage};
        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.PAGE_RENAMED, this, pages);

        return true;
    }

    public List<XWikiEclipsePageHistorySummary> getPageHistory(String pageId) throws XWikiEclipseException
    {
        List<XWikiEclipsePageHistorySummary> result = new ArrayList<XWikiEclipsePageHistorySummary>();
        List<XWikiPageHistorySummary> pageHistory = null;

        if (isConnected()) {
            pageHistory = remoteXWikiDataStorage.getPageHistory(pageId);

        } else {
            pageHistory = localXWikiDataStorage.getPageHistory(pageId);
        }

        for (XWikiPageHistorySummary pageHistorySummary : pageHistory) {
            result.add(new XWikiEclipsePageHistorySummary(this, pageHistorySummary));
        }

        return result;
    }

    public String getXWikiEclipseId()
    {
        return String.format("xwikieclipse://%s", getName()); //$NON-NLS-1$
    }

    public List<XWikiEclipsePageSummary> getAllPageIds(String linkPrefix) throws XWikiEclipseException
    {
        List<XWikiEclipsePageSummary> result = new ArrayList<XWikiEclipsePageSummary>();

        if (isConnected()) {
            if (supportedFunctionalities.contains(Functionality.EFFICIENT_RETRIEVAL)) {
                java.util.HashMap map = new java.util.HashMap(6);
                if (linkPrefix.equals("")) {
                    linkPrefix = "__ALL_PAGES__";
                }
                map.put("searchedField", "fullname");
                map.put("startIndex", "0");
                map.put("maxResults", "25");
                List<XWikiPageSummary> pageSummaries = remoteXWikiDataStorage.getConstrainedPageIds(linkPrefix, map);
                for (XWikiPageSummary pageSummary : pageSummaries) {
                    result.add(new XWikiEclipsePageSummary(this, pageSummary));
                }
            } else if (supportedFunctionalities.contains(Functionality.ALL_PAGES_RETRIEVAL)) {
                List<XWikiPageSummary> pageSummaries = remoteXWikiDataStorage.getAllPageIds();
                for (XWikiPageSummary pageSummary : pageSummaries) {
                    result.add(new XWikiEclipsePageSummary(this, pageSummary));
                }
            } else {
                List<SpaceSummary> spaces = remoteXWikiDataStorage.getSpaces();
                for (SpaceSummary spaceSummary : spaces) {
                    List<XWikiPageSummary> pages = remoteXWikiDataStorage.getPages(spaceSummary.getKey());
                    for (XWikiPageSummary pageSummary : pages) {
                        result.add(new XWikiEclipsePageSummary(this, pageSummary));
                    }
                }
            }
        } else {
            List<XWikiPageSummary> pageSummaries = localXWikiDataStorage.getAllPageIds();
            for (XWikiPageSummary pageSummary : pageSummaries) {
                result.add(new XWikiEclipsePageSummary(this, pageSummary));
            }
        }

        return result;
    }

    public List<XWikiEclipsePageSummary> getAllPageIds() throws XWikiEclipseException
    {
        return this.getAllPageIds("");
    }

    public boolean exists(String pageId)
    {
        if (isConnected()) {
            return remoteXWikiDataStorage.exists(pageId);
        }

        return localXWikiDataStorage.exists(pageId);
    }

    public boolean existsSpace(String spaceKey)
    {
        if (isConnected()) {
            return remoteXWikiDataStorage.existsSpace(spaceKey);
        }
        return false;
    }

    public Attachment addAttachment(Attachment attachment, byte[] data, XWikiEclipsePageSummary pageSummary)
        throws XWikiEclipseException
    {
        if (isConnected()) {
            XWikiEclipseAttachmentSummary attachmentSummary =
                new XWikiEclipseAttachmentSummary(this, attachment, pageSummary.getData());
            Attachment newAttachment = remoteXWikiDataStorage.addAttachment(attachment, data);
            NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.ATTACHMENT_STORED, this, attachmentSummary);
            return newAttachment;
        }
        return null;
    }

    public Boolean renameAttachment(XWikiEclipseAttachmentSummary attachmentSummary, String toFileName,
        String toPageName, String toSpace) throws XWikiEclipseException
    {
        if (!supportedFunctionalities.contains(Functionality.EFFICIENT_RENAME)) {
            return false;
        }

        Boolean result = false;
        try {
            result =
                remoteXWikiDataStorage.moveAttachment(attachmentSummary.getPageSummary().getId(), attachmentSummary
                    .getData().getFileName(), toSpace + "." + toPageName, toFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        XWikiEclipsePageSummary pageSummaries[] =
            new XWikiEclipsePageSummary[] {
            attachmentSummary.getDataManager().getPageSummary(attachmentSummary.getData().getPageId()),
            attachmentSummary.getDataManager().getPageSummary(toSpace + "." + toPageName)};
        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.ATTACHMENT_RENAMED, this, pageSummaries);
        return result;
    }

    public Boolean copyAttachment(XWikiEclipseAttachmentSummary attachmentSummary, String newpageId)
    {
        if (!supportedFunctionalities.contains(Functionality.EFFICIENT_RENAME)) {
            return false;
        }

        Boolean result = false;
        try {
            result = remoteXWikiDataStorage.copyAttachment(attachmentSummary.getData(), newpageId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.ATTACHMENT_COPIED, this,
                attachmentSummary.getDataManager().getPageSummary(newpageId));
        } catch (XWikiEclipseException e) {
            e.printStackTrace();
        }
        return result;

    }

    public Boolean copyAttachment(XWikiEclipseAttachmentSummary attachmentSummary, String newpageId, String newfileName)
    {
        if (!supportedFunctionalities.contains(Functionality.EFFICIENT_RENAME)) {
            return false;
        }

        Boolean result = false;
        try {
            result = remoteXWikiDataStorage.copyAttachment(attachmentSummary.getData(), newpageId, newfileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.ATTACHMENT_COPIED, this,
                attachmentSummary.getDataManager().getPageSummary(newpageId));
        } catch (XWikiEclipseException e) {
            e.printStackTrace();
        }
        return result;

    }

    public Boolean copyPage(XWikiEclipsePageSummary oldpageSummary, String newpageId)
    {
        if (!supportedFunctionalities.contains(Functionality.EFFICIENT_RENAME)) {
            return false;
        }

        Boolean result = false;
        try {
            result = remoteXWikiDataStorage.copyPage(oldpageSummary.getData().getId(), newpageId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.PAGE_COPIED, this,
                oldpageSummary.getDataManager().getSpaceSummary(newpageId.split("\\.")[0]));
        } catch (XWikiEclipseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Integer getNumberOfPages(String spaceKey)
    {
        if (!supportedFunctionalities.contains(Functionality.LAZY_RETRIEVAL)) {
            return 0;
        }
        try {
            return remoteXWikiDataStorage.getNumberOfPages(spaceKey);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Integer getNumberOfSpaces()
    {
        if (!supportedFunctionalities.contains(Functionality.LAZY_RETRIEVAL)) {
            return 0;
        }
        try {
            return remoteXWikiDataStorage.getNumberOfSpaces();
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
