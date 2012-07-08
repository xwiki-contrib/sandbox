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
package org.xwiki.batchimport.log;

import java.util.List;

import org.slf4j.Logger;
import org.xwiki.batchimport.BatchImport;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

/**
 * Log for the batch import, to save results and be able to retrieve them after.
 * 
 * @version $Id$
 */
@ComponentRole
public interface BatchImportLog
{
    /**
     * Any message that needs to be output, like starting processing, etc.
     */
    void log(String messageKey, Object... parameters);

    /**
     * Any **problem** that prevents a document from being saved (e.g. invalid data, etc).
     */
    void logError(String messageKey, Object... parameters);

    /**
     * Any row that needs to be skipped: e.g. doc.name already used (and doc.name deduplication set to skip), empty page
     * name, document already exists and overwrite set to skip, etc.
     */
    void logSkip(String messageKey, Object... parameters);

    /**
     * All successful saves.
     */
    void logSave(String messageKey, Object... parameters);

    /**
     * Any unexpected exception when trying to save documents (XWikiException upon calling save, for example).
     */
    void logCritical(String messageKey, Object... parameters);

    /**
     * All document deletes (for replaces) or from the
     * {@link BatchImport#deleteExistingDocuments(String, String, String)} function.
     */
    void logDelete(String messageKey, Object... parameters);

    /**
     * If the logger is set, this log will also send messaged to the console logger, with the INFO level.
     * 
     * @param logger
     */
    void setConsoleLogger(Logger logger);

    /**
     * @return the log with the errors occurred during import
     */
    String getErrorLog();

    /**
     * @return the full log (errors and successes and other information)
     */
    String getFullLog();

    /**
     * @return an URL to the full log, if it's a file
     */
    String getFullLogURL();

    /**
     * @return the list of saved documents during this import
     */
    List<DocumentReference> getSavedDocuments();
}
