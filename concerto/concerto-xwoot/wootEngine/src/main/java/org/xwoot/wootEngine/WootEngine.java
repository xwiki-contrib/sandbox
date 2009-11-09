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

import org.apache.commons.logging.LogFactory;

import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;

import org.xwoot.wootEngine.core.ContentId;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootContent;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootDel;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;
import org.xwoot.xwootUtil.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Manages the internal Woot state, applies patches and Woot operations.
 * 
 * @version $Id$
 */
public class WootEngine extends LoggedWootExceptionThrower
{
    /** The name of the output file containing the zipped state. */
    public static final String STATE_FILE_NAME = "wootState.zip";
    
    /** The name prefix of the output file containing the zipped state. */
    public static final String STATE_FILE_NAME_PREFIX = "wootState";
    
    /** The file extension of the output file containing the zipped state. */
    public static final String STATE_FILE_EXTENSION = ".zip";

    /** The directory where the WootEngine stores it's data. */
    private String workingDirPath;

    /** An internal {@link Clock} engine required by the Woot algorithm. */
    private Clock opLocalClock;

    /**
     * A waiting queue for WootOp elements originating from patches and that were destined for another content id than
     * the content id of the patch.
     */
    private Pool waitingQueue;

    /** Handles WootContents for the internal WootEngine model. */
    private ContentManager contentManager;

    /**
     * Creates a new WootEngine object.
     * 
     * @param siteId Unique identifier of the wanted component.
     * @param workingDir Directory with read/write access to serialize content.
     * @param opClock {@link Clock} engine component instance.
     * @throws WootEngineException if problems related to directory access occur.
     */
    public WootEngine(String siteId, String workingDir, Clock opClock) throws WootEngineException
    {
        this.wootEngineId = siteId;
        this.logger = LogFactory.getLog(this.getClass());

        this.setWorkingDir(workingDir);
        this.createWorkingDir();

        this.setOpLocalClock(opClock);
        this.setWaitingQueue(new Pool(workingDir));
        this.setContentManager(new ContentManager(siteId, workingDir));

        this.logger.info(this.wootEngineId + " - WootEngine created.");
    }

    /**
     * Create the working directories and init the contentsDir field.
     * 
     * @throws WootEngineException if file access problems occur.
     * @see FileUtil#checkDirectoryPath(String)
     */
    private void createWorkingDir() throws WootEngineException
    {
        try {
            FileUtil.checkDirectoryPath(this.workingDirPath);
        } catch (Exception e) {
            this.throwLoggedException("Problems creating workingDir.", e);
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
        File dir = new File(this.workingDirPath);
        if (dir.exists()) {
            FileUtil.deleteDirectory(dir);
        }

        this.createWorkingDir();

        this.getContentManager().clearWorkingDir();
        this.getWaitingQueue().initializePool(true);
    }

    /**
     * Delete from the WootContent an atomic value ({@link WootRow}) at a given visible position.
     * 
     * @param content the content from which to delete.
     * @param position the position (of the WootRow) to delete relative to the visible WootRows. (starting from 0)
     * @return the corresponding Woot operation that has been applied.
     * @throws WootEngineException if the given position is invalid or if problems occurred with the operations Clock.
     */
    public WootDel delete(WootContent content, int position) throws WootEngineException
    {
        if ((position >= 0) && (position < content.sizeOfVisible())) {
            // skip the default first row.
            WootRow deleteRow = content.visibleElementAt(position + 1);

            if (!deleteRow.equals(WootRow.LAST_WOOT_ROW)) {
                WootDel deleteOperation = new WootDel(deleteRow.getWootId());

                try {
                    deleteOperation.setOpId(new WootId(this.wootEngineId, this.getOpLocalClock().tick()));
                } catch (ClockException e) {
                    this.throwLoggedException("Problem with the clock.", e);
                }

                deleteOperation.setContentId(content.getContentId());
                deleteOperation.execute(content);

                this.logger.debug(this.wootEngineId + " Operation executed : " + deleteOperation.toString());

                // in copy returned operation must be in the corresponding main content
                // FIXME more cleaner copy management
                if (deleteOperation.getContentId().isCopy()) {
                    ContentId cid =
                        new ContentId(deleteOperation.getContentId().getPageName(), deleteOperation.getContentId()
                            .getObjectName(), deleteOperation.getContentId().getFieldName(), false);
                    deleteOperation.setContentId(cid);
                }

                return deleteOperation;
            }
        }

        this.throwLoggedException("Invalid delete position " + position + " for the content " + content.getContentId());

        // never reachable
        return null;
    }

    /**
     * Inserts in the WootContent a String value at a given position.
     * 
     * @param content the content in which to insert.
     * @param value the value to insert.
     * @param position the position (WootRow) where to insert. (starting from 0)
     * @return the corresponding Woot operation that has been applied.
     * @throws WootEngineException if the given position is invalid or if problems occurred with the operations Clock.
     */
    public WootIns insert(WootContent content, String value, int position) throws WootEngineException
    {
        this.logger.debug(this.wootEngineId + " - Direct insertion in " + content.getContentId() + ", value : " + value
            + ", position : " + position);

        if ((position >= 0) && (position <= content.size())) {
            int insertIndex = content.indexOfVisible(position);
            WootRow rowBeforeInsert = (insertIndex != -1) ? content.elementAt(insertIndex) : content.elementAt(0);

            if (!rowBeforeInsert.equals(WootRow.LAST_WOOT_ROW)) {
                int indexAfterInsert = content.indexOfVisibleNext(insertIndex);

                WootRow rowAfterInsert =
                    (indexAfterInsert != -1) ? content.elementAt(indexAfterInsert) : content
                        .elementAt(content.size() + 1);

                int degreeC = 1;
                degreeC +=
                    ((rowBeforeInsert.getDegree() >= rowAfterInsert.getDegree()) ? rowBeforeInsert.getDegree()
                        : rowAfterInsert.getDegree());

                try {
                    WootId insertionId = new WootId(this.wootEngineId, this.getOpLocalClock().tick());

                    WootRow newRowToInsert = new WootRow(insertionId, value, degreeC);

                    WootIns insertOperation =
                        new WootIns(newRowToInsert, rowBeforeInsert.getWootId(), rowAfterInsert.getWootId());
                    insertOperation.setOpId(insertionId);
                    insertOperation.setContentId(content.getContentId());

                    insertOperation.execute(content);
                    this.logger.debug(this.wootEngineId + " - Operation executed :   " + insertOperation.toString());

                    // in copy returned operation must be in the corresponding main content
                    // FIXME more cleaner copy management
                    if (insertOperation.getContentId().isCopy()) {
                        ContentId cid =
                            new ContentId(insertOperation.getContentId().getPageName(), insertOperation.getContentId()
                                .getObjectName(), insertOperation.getContentId().getFieldName(), false);
                        insertOperation.setContentId(cid);
                    }
                    return insertOperation;
                } catch (ClockException e) {
                    this.throwLoggedException("Problems with the clock.", e);
                }
            }
        }

        this.throwLoggedException("Invalid insert position " + position + " for content " + content.getContentId());

        // never reachable.
        return null;
    }

    /**
     * Applies a given Woot Operation on a content.
     * 
     * @param operation the operation to apply.
     * @param content the content on which to apply the operation.
     * @return true if the operation has been applied, false otherwise or if the operation was not indented for the
     *         specified content or this operation is not yet applicable for this content.
     */
    private boolean executeOp(WootOp operation, WootContent content)
    {
        if (!operation.getContentId().equals(content.getContentId())) {
            return false;
        }

        synchronized (content) {
            // If the operation can not yet be applied because the targeted content does not exist.
            if (operation.getAffectedRowIndexes(content) == null) {
                return false;
            }

            if (operation instanceof WootIns) {

                WootIns insertOperation = (WootIns) operation;

                // In case of an op reception after a setState containing this op
                if (content.indexOfId(insertOperation.getRowToInsert().getWootId()) >= 0) {

                    this.logger.debug(this.wootEngineId
                        + " - Operation not executed because it was already executed during a state transfert. -- "
                        + insertOperation.getRowToInsert().getWootId());

                    return true;
                }
            }

            operation.execute(content);

            this.logger.debug(this.wootEngineId + " - Operation executed :  " + operation.toString());

        }
        /*
         * synchronized (page) { if (operation instanceof WootIns) { WootIns insertOperation = (WootIns) operation;
         * int[] indexs = new int[2]; indexs = insertOperation.getAffectedRowIndexes(page); // In case of an op
         * reception after a setState containing this op if (page.indexOfId(insertOperation.getNewRow().getWootId()) >=
         * 0) { this.logger.debug(this.wootEngineId +
         * " - Operation not executed because it was already executed during a state transfert. -- " +
         * insertOperation.getNewRow().getWootId()); return true; } else if (indexs != null) { // execute the operation
         * insertOperation.execute(indexs[0], indexs[1], page); this.logger.debug(this.wootEngineId +
         * " - Operation executed  (" + operation.getPageName() + "  -  " + page.getPageName() + ")  : " +
         * operation.toString()); return true; } } else if (operation instanceof WootDel) { WootDel deleteOperation =
         * (WootDel) operation; int deleteRowIndex = deleteOperation.getAffectedRowIndexes(page); if (deleteRowIndex >=
         * 0) { deleteOperation.execute(page); this.logger.debug(this.wootEngineId + " - Operation executed (" +
         * operation.getPageName() + " - " + page.getPageName() + ") : " + operation.toString()); return true; } } }
         * return false;
         */
        return true;
    }

    /**
     * Applies a received patch.
     * <p>
     * If the content this patch for does not exist in the model, it will be created and added.
     * <p>
     * If operations in this patch can not be applied, they will be added to a waiting queue.
     * 
     * @param patch the patch to apply.
     * @throws WootEngineException if problems accessing or creating the content or working with the waiting queue
     *             occur.
     */
    public synchronized void deliverPatch(Patch patch) throws WootEngineException
    {
        this.logger.info(this.wootEngineId + " - Reception of a new patch for content : " + patch.getPageId() + " - "
            + patch.globalId());
        this.logger.debug(this.wootEngineId + " - Patch contents : " + patch.toString());

        WootContent content = null;
        this.logger.debug(this.wootEngineId + " - Execution of patch operations...");

        if (patch.getData() == null || !(patch.getData().iterator().hasNext())) {
            return;
        }
        
        ContentId cid = ((WootOp) patch.getData().iterator().next()).getContentId();
        content = this.contentManager.loadWootContent(cid);
       
        this.getWaitingQueue().loadPool();
        for (Object obj : patch.getData()) {
            WootOp op = (WootOp) obj;

            if (!this.executeOp(op, content)) {
                this.logger.debug(this.wootEngineId + " - apending to waiting queue : " + op.toString());
                this.getWaitingQueue().getContent().add(op);
            }
        }
        this.waitingQueueExec(content);
        this.getWaitingQueue().unLoadPool();
        this.getContentManager().unloadWootContent(content);
      
    }

    /**
     * This method is called to check if waiting operations can be applied.
     * <p>
     * The operations that do get executed will get removed from the waiting queue.
     * 
     * @param content the WootContent on which the operations have to be applied.
     * @throws WootEngineException if problems saving the pool's state occur.
     */
    private void waitingQueueExec(WootContent content) throws WootEngineException
    {
        this.logger.debug(this.wootEngineId + " - Waiting queue execution.");

        int i = 0;

        while (i < this.getWaitingQueue().getContent().size()) {
            WootOp operation = this.getWaitingQueue().get(i);

            if (this.executeOp(operation, content)) {
                this.getWaitingQueue().remove(i);
                // rewind
                i = 0;
                this.getWaitingQueue().storePool();

                this.logger.debug(this.wootEngineId + " - Operation executed : " + operation.toString());
            } else {
                i++;

                this.logger.debug(this.wootEngineId + " - Operation not executed :" + operation.toString());
            }
        }
    }

    /**
     * Computes the current state of the WootEngine as a zip archieve containing WootContents.
     * 
     * @return the location of the zip file generated. Note: The file is temporary and it is stored in the temporary
     *         directory.
     * @throws WootEngineException if problems occur in the zipping process.
     * @see FileUtil#zipDirectory(String)
     */
    public synchronized File getState() throws WootEngineException
    {
        File contentsDir = new File(this.getContentManager().getContentsDirPath());

        String stateFilePath = null;
        try {
            stateFilePath = FileUtil.zipDirectory(contentsDir.getAbsolutePath());
        } catch (IOException e) {
            this.throwLoggedException("Problems zipping the current state.", e);
        }

        if (stateFilePath == null) {
            return null;
        }

        return new File(stateFilePath);
    }

    /**
     * Replaces the current state with the one provided.
     * 
     * @param zippedStateFile the location of a zipped state that will replace the current one.
     * @return true if the process was successfully executed, false otherwise.
     * @throws WootEngineException if unzipping or I/O problems occur.
     * @see #getState()
     */
    public synchronized boolean setState(File zippedStateFile) throws WootEngineException
    {
        // FIXME: Implement fail-safe method, transaction style. If setState fails for the new state, setState should be
        // called for a backup state, previously saved.
        if (zippedStateFile != null) {

            try {
                // delete all existing pages
                File contentsDir = new File(this.getContentManager().getContentsDirPath());
                /*
                 * FIXME: actually remove current content and remake directory structure.
                 * FileUtil.deleteDirectory(pagesDir); createWorkingDir();
                 */

                FileUtil.unzipInDirectory(zippedStateFile.toString(), contentsDir.getAbsolutePath());

                this.logger.info(this.getWootEngineId() + " - Received WootEngine state.");

                return true;
            } catch (Exception e) {
                this.throwLoggedException("Problems unziping the state file " + zippedStateFile.toString(), e);
            }
        }

        return false;
    }

    /**
     * @return a waiting queue for WootOp elements originating from patches and that were destined for another content
     *         id than the content id of the patch.
     */
    private Pool getWaitingQueue()
    {
        return this.waitingQueue;
    }

    /**
     * @param waitingQueue the Pool object to set.
     */
    private void setWaitingQueue(Pool waitingQueue)
    {
        this.waitingQueue = waitingQueue;
    }

    /**
     * @return the directory where the WootEngine stores it's data.
     */
    public String getWorkingDir()
    {
        return this.workingDirPath;
    }

    /**
     * @param workDirectory the workDirectory to set.
     */
    private void setWorkingDir(String workDirectory)
    {
        this.workingDirPath = workDirectory;
    }

    /**
     * @return the associated {@link Clock} object.
     */
    private Clock getOpLocalClock()
    {
        return this.opLocalClock;
    }

    /**
     * @param opLocalClock the {@link Clock} object that will become associated to this instance and will be used with
     *            the {@link WootOp} operations.
     */
    private void setOpLocalClock(Clock opLocalClock)
    {
        this.opLocalClock = opLocalClock;
    }

    /**
     * Loads the clock from file.
     * 
     * @throws ClockException if problems occur.
     * @see Clock#load()
     */
    public synchronized void loadClock() throws ClockException
    {
        this.opLocalClock = this.opLocalClock.load();
    }

    /**
     * Serializes the clock to file.
     * 
     * @throws ClockException if problems occur.
     * @see Clock#store()
     */
    public synchronized void unloadClock() throws ClockException
    {
        this.opLocalClock.store();
    }

    /**
     * @return the contentManager instance responsible for WootContents handling.
     * @see WootContent
     */
    public ContentManager getContentManager()
    {
        return this.contentManager;
    }

    /**
     * @param contentManager the contentManager to set
     */
    public void setContentManager(ContentManager contentManager)
    {
        this.contentManager = contentManager;
    }

}
