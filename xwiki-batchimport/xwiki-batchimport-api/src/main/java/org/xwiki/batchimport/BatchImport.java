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
package org.xwiki.batchimport;

import java.io.IOException;
import java.util.List;

import org.xwiki.batchimport.log.BatchImportLog;
import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
@ComponentRole
public interface BatchImport
{
    /**
     * @return a list of all the columns in the source file, as specified in the configuration.
     * @throws IOException if something goes wrong reading the file
     */
    List<String> getColumnHeaders(BatchImportConfiguration config) throws IOException;

    /**
     * @param config the configuration which would be used for import afterwards. Overwrite settings don't need to be
     *            set since preview is not necessarily about document name but more about data types parsing and
     *            associations of columns with fields.
     * @param maxRows the max number of processed rows to return
     * @param logHint the hint of the log to be used for this preview
     * @return the result of the preview, as described in {@link MappingPreviewResult}
     */
    MappingPreviewResult getMappingPreview(BatchImportConfiguration config, int maxRows, String logHint)
        throws IOException, XWikiException;

    /**
     * Same as {@link #getMappingPreview(BatchImportConfiguration, int, String)} but using the default logger.
     */
    MappingPreviewResult getMappingPreview(BatchImportConfiguration config, int maxRows) throws IOException,
        XWikiException;

    /**
     * Actually imports the data.
     * 
     * @param config
     * @param withFiles
     * @param overwritefile
     * @param simulation
     * @param convertToUpperCase
     * @return
     * @throws IOException
     * @throws XWikiException
     */
    public BatchImportLog doImport(BatchImportConfiguration config, boolean withFiles, boolean overwritefile,
        boolean simulation) throws IOException, XWikiException;

    /**
     * @param config
     * @param withFiles
     * @param overwritefile
     * @param simulation
     * @param logHint
     * @return
     * @throws IOException
     * @throws XWikiException
     */
    public BatchImportLog doImport(BatchImportConfiguration config, boolean withFiles, boolean overwritefile,
        boolean simulation, String logHint) throws IOException, XWikiException;

    /**
     * Deletes existing documents with objects of class className from space space, besides the template document, built
     * with classNameTemplate or classTemplate.
     * 
     * @param className the name of the class
     * @param wiki the wiki on which to execute the operation. Note that since this will be done with the current user,
     *            he'd better have the right to.
     * @param space space to delete from. If missing, the documents from all the spaces will be deleted
     * @param logHint the hint for the reporting log to use for this delete
     * @return the log of the delete
     * @throws XWikiException
     */
    public BatchImportLog deleteExistingDocuments(String className, String wiki, String space, String logHint)
        throws XWikiException;

    /**
     * Same as {@link #deleteExistingDocuments(String, String, String, String)} but using the default log.
     * 
     * @see #deleteExistingDocuments(String, String, String, String)
     */
    public BatchImportLog deleteExistingDocuments(String className, String wiki, String space) throws XWikiException;
}
