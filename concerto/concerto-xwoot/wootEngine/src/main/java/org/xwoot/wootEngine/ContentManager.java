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
package org.xwoot.wootEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.LogFactory;
import org.xwoot.wootEngine.core.ContentId;
import org.xwoot.wootEngine.core.WootContent;
import org.xwoot.wootEngine.core.WootFile;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.xwootUtil.FileUtil;
import org.xwoot.xwootUtil.PersistencyUtil;

/**
 * Handles WootContents serialization for the internal WootEngine model.
 * 
 * @version $Id$
 */
public class ContentManager extends LoggedWootExceptionThrower
{
    /** The name of the directory inside the workingDir where to serialize {@link WootFile} objects. */
    public static final String CONTENTS_DIRECTORY_NAME = "contents";

    /** The directory path where to store contents. */
    private String contentsDirPath;

    /**
     * Creates a new ContentManager instance to be used by the WootEngine. The ContentManager will store it's contents
     * in a sub-directory of the owning WootEngine's working directory. Contents are linked with an associated
     * {@link contentId} and a pageName to get the correct file. There is one {@link WootFile} by pageName.
     * 
     * @param wootEngineId the Id of the WootEngine instance this page manager belongs to.
     * @param wootEngineWorkingDirPath the workingDir for the WootEngine this page manager belongs to.
     * @throws WootEngineException if the WootEngine's working directory is not accessible.
     * @see WootEngine#getWootEngineId()
     * @see WootEngine#getWorkingDir()
     * @see FileUtil#checkDirectoryPath(String)
     */
    public ContentManager(String wootEngineId, String wootEngineWorkingDirPath) throws WootEngineException
    {
        String newContentsDirPath = wootEngineWorkingDirPath + File.separator + CONTENTS_DIRECTORY_NAME;

        this.contentsDirPath = newContentsDirPath;

        this.createWorkingDir();

        this.wootEngineId = wootEngineId;
        this.logger = LogFactory.getLog(this.getClass());
    }

    /**
     * Creates the directory structure for the ContentManager.
     * 
     * @throws WootEngineException if file access problems occur.
     * @see FileUtil#checkDirectoryPath(String)
     */
    public void createWorkingDir() throws WootEngineException
    {
        try {
            FileUtil.checkDirectoryPath(this.getContentsDirPath());
        } catch (Exception e) {
            this.throwLoggedException("Problems creating the ContentManager's working directory.", e);
        }
    }

    /**
     * Deletes and reinitializes the contents of the working dir.
     * 
     * @throws WootEngineException if problems occur while recreating the working directory's structure.
     * @see #createWorkingDir()
     */
    public void clearWorkingDir() throws WootEngineException
    {
        File workingDir = new File(this.getContentsDirPath());

        if (workingDir.exists()) {
            FileUtil.deleteDirectory(workingDir);
        }

        createWorkingDir();
    }

    /**
     * Creates a new empty file and stores it in the model.
     * 
     * @param pageName the name of the associated page.
     * @return the newly created {@link WootContent} object.
     * @throws WootEngineException if the page already exists or if problems occurred while serializing.
     * @throws IllegalArgumentException if the pageName is a null or empty String.
     * @see WootContent#WootPage(String)
     * @see #storePage(WootContent)
     */
    private WootFile createFile(String pageName) throws WootEngineException, IllegalArgumentException
    {
        if (pageName == null || pageName.trim().equals("")) {
            throw new IllegalArgumentException(pageName);
        }
        if (this.fileExists(pageName)) {
            this.throwLoggedException("The page named " + pageName + " already exits.");
        }

        WootFile wootFile = new WootFile(pageName);

        this.logger.debug(this.getWootEngineId() + " - Create woot page : " + wootFile.getFileName());

        this.storeFile(wootFile);

        return wootFile;
    }

    /**
     * Loads a WootContent previously stored.
     * 
     * @param pageName the name of the container page of the wanted content.
     * @param objectId the id of the container object of the wanted content in the associated page.
     * @param fieldId the id of the wanted content in the object in the page.
     * @return the requested WootContent or if it does not exist, a new WootContent with the same id that is
     *         automatically added to the model.
     * @throws WootEngineException if the pageName causes encoding problems or if serializing/deserializing problems
     *             occur.
     * @throws IllegalArgumentException if the pageName is a null or empty String.
     */
    public WootContent loadWootContent(String pageName, String objectId, String fieldId) throws WootEngineException
    {
        if (pageName == null || pageName.trim().equals("") || objectId == null || objectId.trim().equals("")) {
            throw new IllegalArgumentException(pageName + " " + objectId + " " + fieldId);
        }
        WootFile wootFile = this.loadFile(pageName);
        ContentId id = new ContentId(pageName, objectId, fieldId, false);
        WootContent result = wootFile.getContents().get(id);
        if (result == null) {
            result = new WootContent(id);
            wootFile.addContent(result);
            this.storeFile(wootFile);
        }
        return result;
    }

    /**
     * Loads a copy of WootContent previously stored (if not, creates the both new empty wootContent and his copy).
     * 
     * @param pageName the name of the container page of the wanted content copy.
     * @param objectId the id of the container object of the wanted content copy in the associated page.
     * @param fieldId the id of the wanted content copy in the object in the page.
     * @return the requested WootContent copy or if it does not exist, a new WootContent copy with the same id that is
     *         automatically added to the model (wootContent copy and wootContent main).
     * @throws WootEngineException if the pageName causes encoding problems or if serializing/deserializing problems
     *             occur.
     * @throws IllegalArgumentException if the pageName is a null or empty String.
     */
    public WootContent loadWootContentCopy(String pageName, String objectId, String fieldId) throws WootEngineException
    {
        if (pageName == null || pageName.trim().equals("") || objectId == null || objectId.trim().equals("")) {
            throw new IllegalArgumentException(pageName + " " + objectId + " " + fieldId);
        }
        WootFile wootFile = this.loadFile(pageName);
        ContentId id = new ContentId(pageName, objectId, fieldId, true);
        WootContent resultCopy = wootFile.getContents().get(id);
        if (resultCopy == null) {
            resultCopy = new WootContent(id);
            wootFile.addContent(resultCopy);

            ContentId idMain = new ContentId(pageName, objectId, fieldId, false);
            if (wootFile.getContents().get(idMain) == null) {
                WootContent resultMain = new WootContent(idMain);
                wootFile.addContent(resultMain);
            }
            this.storeFile(wootFile);
        }
        return resultCopy;
    }

    /**
     * Create or set a copy of a {@link WootContent}.
     * 
     * @param pageName the name of the container page of the content to copy.
     * @param objectId the id of the container object of the content to copy in the associated page.
     * @param fieldId the id of the content to copy in the object in the page.
     * @throws WootEngineException if the pageName causes encoding problems or if serializing/deserializing problems
     *             occur.
     * @throws IllegalArgumentException if the pageName is a null or empty String.
     */
    public void copyWootContent(String pageName, String objectId, String fieldId) throws WootEngineException
    {
        if (pageName == null || pageName.trim().equals("") || objectId == null || objectId.trim().equals("")) {
            throw new IllegalArgumentException(pageName + " " + objectId + " " + fieldId);
        }
        WootFile wootFile = this.loadFile(pageName);
        ContentId mainId = new ContentId(pageName, objectId, fieldId, false);
        ContentId copyId = new ContentId(pageName, objectId, fieldId, true);
        WootContent mainContent = wootFile.getContents().get(mainId);
        WootContent deepCopy = null;
        if (mainContent == null) {
            deepCopy = new WootContent(copyId);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream(baos);
                oos.writeObject(mainContent);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                deepCopy = (WootContent) ois.readObject();
                deepCopy.setContentId(copyId);
            } catch (IOException e) {
                throw new WootEngineException(e);
            } catch (ClassNotFoundException e) {
                throw new WootEngineException(e);
            }
        }
        wootFile.addContent(deepCopy);
        this.storeFile(wootFile);
    }

    /**
     * Loads a WootContent previously stored. see{@link ContentManager#loadWootContent(String, String, String)}
     * 
     * @param contentId the id of the wanted content
     * @return the requested WootContent or if it does not exist, a new WootContent with the same id that is
     *         automatically added to the model.
     * @throws WootEngineException if the pageName causes encoding problems or if serializing/deserializing problems
     *             occur.
     * @throws IllegalArgumentException if the pageName is a null or empty String.
     */
    public WootContent loadWootContent(ContentId contentId) throws WootEngineException
    {
        return this.loadWootContent(contentId.getPageName(), contentId.getObjectName(), contentId.getFieldName());
    }

    /**
     * Serializes the given {@link WootContent}. This method loads the associated page (using the WoootContent
     * pagename), add the content to the {@link WootFile} and stores it.
     * 
     * @param wootContent the content to serialize
     * @throws WootEngineException if serialization or file access problems occur.
     */
    public void storeWootContent(WootContent wootContent) throws WootEngineException
    {
        WootFile wootFile = this.loadFile(wootContent.getContentId().getPageName());

        wootFile.addContent(wootContent);
        this.storeFile(wootFile);
    }

    /**
     * Serializes the {@link WootContent}.
     * 
     * @param wootFile the file to serialize.
     * @throws WootEngineException if serialization or file access problems occur.
     */
    private void storeFile(WootFile wootFile) throws WootEngineException
    {
        String pageFilePath = this.getContentsDirPath() + File.separator + wootFile.getFileName();

        try {
            PersistencyUtil.saveObjectToXml(wootFile, pageFilePath);
        } catch (Exception e) {
            this.throwLoggedException("Problems storing page " + wootFile.getPageName());
        }
        System.runFinalization();
        System.gc();
    }

    /**
     * Check if a page name already exists in the model.
     * 
     * @param pageName the name of the page to check.
     * @return true if the page exists, false if it does not or if the page name is empty or null.
     * @throws WootEngineException if the name of the page caused encoding problems.
     * @see FileUtil#getEncodedFileName(String)
     */
    private boolean fileExists(String pageName) throws WootEngineException
    {
        if (pageName == null || pageName.length() == 0) {
            return false;
        }

        String pageFileName = null;
        try {
            pageFileName = FileUtil.getEncodedFileName(pageName);
        } catch (UnsupportedEncodingException e) {
            this.throwLoggedException("Problem with filename encoding for page " + pageName, e);
        }

        File pageFile = new File(this.getContentsDirPath(), pageFileName);

        return pageFile.exists();
    }

    /**
     * Loads a WootFile previously stored.
     * 
     * @param pageName the name of the wanted page (i.e the wanted file)
     * @return the requested WootFile or if it does not exist, a new WootFile with the same name that is automatically
     *         added to the model.
     * @throws WootEngineException if the pageName causes encoding problems or if serializing/deserializing problems
     *             occur.
     * @throws IllegalArgumentException if the pageName is a null or empty String.
     */
    private synchronized WootFile loadFile(String pageName) throws WootEngineException
    {
        if (pageName == null || pageName.trim().equals("")) {
            throw new IllegalArgumentException(pageName);
        }

        if (!this.fileExists(pageName)) {
            return this.createFile(pageName);
        }

        String filename = null;
        try {
            filename = FileUtil.getEncodedFileName(pageName);
        } catch (UnsupportedEncodingException e) {
            this.throwLoggedException("Problem with filename encoding of page : " + pageName, e);
        }

        String filePath = this.getContentsDirPath() + File.separator + filename;

        try {
            return (WootFile) PersistencyUtil.loadObjectFromXml(filePath);
        } catch (Exception e) {
            this.throwLoggedException("Problems while loading page " + pageName, e);
        }

        // never reachable.
        return null;
    }

    /**
     * @param pageName the name of the page.
     * @param objectId the id of the container object of the wanted content in the associated page.
     * @param fieldId the id of the wanted content in the object in the page.
     * @param getCopy if true, loads the copied content in place of the "normal" content.
     * @return the visible content or empty string if the page does not exist.
     * @throws WootEngineException if problems occur while accessing the page.
     * @see WootContent#toHumanString()
     * @see #loadPage(String)
     * @see WootRow#isVisible()
     */
    private String getContent(String pageName, String objectId, String fieldId, boolean getCopy)
        throws WootEngineException
    {
        if (!this.fileExists(pageName)) {
            return "";
        }

        WootContent content =
            this.loadFile(pageName).getContents().get(new ContentId(pageName, objectId, fieldId, getCopy));
        if (content == null) {
            return "";
        }
        return content.toHumanString();
    }

    /**
     * @param pageName the name of the page.
     * @param objectId the id of the container object of the wanted content in the associated page.
     * @param fieldId the id of the wanted content in the object in the page.
     * @return the visible content or empty string if the page does not exist.
     * @throws WootEngineException if problems occur while accessing the page.
     * @see WootContent#toHumanString()
     * @see #loadPage(String)
     * @see WootRow#isVisible()
     */
    public String getContent(String pageName, String objectId, String fieldId) throws WootEngineException
    {
        return this.getContent(pageName, objectId, fieldId, false);
    }

    /**
     * @param pageName the name of the page.
     * @param objectId the id of the container object of the wanted copied content in the associated page.
     * @param fieldId the id of the wanted copied content in the object in the page.
     * @return the visible copied content or empty string if the page does not exist.
     * @throws WootEngineException if problems occur while accessing the page.
     * @see WootContent#toHumanString()
     * @see #loadPage(String)
     * @see WootRow#isVisible()
     */
    public String getCopyContent(String pageName, String objectId, String fieldId) throws WootEngineException
    {
        return this.getContent(pageName, objectId, fieldId, true);
    }

    /**
     * @param pageName the name of the page.
     * @param objectId the id of the container object of the wanted content in the associated page.
     * @param fieldId the id of the wanted content in the object in the page.
     * @return the full content, as stored internally, or null if the page does not exist.
     * @throws WootEngineException if problems occur while accessing the page.
     * @see WootContent#toHumanString()
     * @see #loadPage(String)
     */
    public String getContentInternal(String pageName, String objectId, String fieldId) throws WootEngineException
    {
        if (!this.fileExists(pageName)) {
            return null;
        }

        WootContent content =
            this.loadFile(pageName).getContents().get(new ContentId(pageName, objectId, fieldId, false));
        if (content == null) {
            return null;
        }

        return content.toString();
    }

    /**
     * @param pageName the name of the page.
     * @param objectId the id of the container object of the wanted content in the associated page.
     * @param fieldId the id of the wanted content in the object in the page.
     * @return only the visible content, as stored internally or null if the page does not exist.
     * @throws WootEngineException if problems occur while accessing the page.
     * @see #loadPage(String)
     * @see WootContent#toVisibleString()
     */
    public String getContentInternalVisible(String pageName, String objectId, String fieldId)
        throws WootEngineException
    {
        if (!this.fileExists(pageName)) {
            return null;
        }

        WootContent content =
            this.loadFile(pageName).getContents().get(new ContentId(pageName, objectId, fieldId, false));
        if (content == null) {
            return null;
        }

        return content.toVisibleString();
    }

    /**
     * Serializes a WootContent object and requests finalization and garbage collection to free the used resources.
     * 
     * @param wootContent the object to serialize and unload.
     * @throws WootEngineException if problems occur while serializing the object.
     * @see #storeWootContent(WootContent)
     * @see System#runFinalization()
     * @see System#gc()
     */
    public synchronized void unloadWootContent(WootContent wootContent) throws WootEngineException
    {
        this.storeWootContent(wootContent);
        System.runFinalization();
        System.gc();
    }

    /**
     * @return An array of all pages names in the model in decoded format or null if there are no pages in the model.
     * @throws WootEngineException if filename decoding problems occur.
     * @see FileUtil#getDecodedFileName(String)
     */
    public String[] listPages() throws WootEngineException
    {
        File dir = new File(this.getContentsDirPath());
        String[] pageNames = dir.list();

        // FIXME: watch out for directories not supposed to be in the pagesDir. They will show up as page names.

        if (pageNames != null) {

            for (int i = 0; i < pageNames.length; i++) {
                try {
                    pageNames[i] = FileUtil.getDecodedFileName(pageNames[i]);
                } catch (UnsupportedEncodingException e) {
                    this.throwLoggedException("Problems decoding file name " + pageNames[i], e);
                }
            }

        }

        return pageNames;
    }

    /**
     * @param pageName the name of the page.
     * @param objectId the id of the container object of the wanted content in the associated page.
     * @param fieldId the id of the wanted content in the object in the page.
     * @return the content with each line wrapped as a paragraph with the class "visible" or "invisible", depending on
     *         the status of a {@link WootRow} (page line). If the page has no content, then an empty string is
     *         returned.
     * @throws WootEngineException if problems loading the page occur.
     * @see #loadPage(String)
     * @see WootRow#isVisible()
     */
    public String getContentModifications(String pageName, String objectId, String fieldId) throws WootEngineException
    {
        if (!this.fileExists(pageName)) {
            return "";
        }

        WootContent content =
            this.loadFile(pageName).getContents().get(new ContentId(pageName, objectId, fieldId, false));
        if (content == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer("");
        String paragraphEnd = "</p>\n";
        for (WootRow row : content.getRows()) {
            if (row.isVisible()) {
                sb.append("<p class=\"visibleLine\">" + row.getContent() + paragraphEnd);
            } else {
                sb.append("<p class=\"invisibleLine\">" + row.getContent() + paragraphEnd);
            }
        }

        return sb.toString();
    }

    /**
     * @return the directory path where the WootPages of the internal model are stored.
     */
    public String getContentsDirPath()
    {
        return this.contentsDirPath;
    }
}
