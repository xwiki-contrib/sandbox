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
     * Actually imports the data.
     * 
     * @param config
     * @param withFiles
     * @param overwrite
     * @param overwritefile
     * @param simulation
     * @param convertToUpperCase
     * @return
     * @throws IOException
     * @throws XWikiException
     */
    public String doImport(BatchImportConfiguration config, boolean withFiles, boolean overwrite,
        boolean overwritefile, boolean simulation) throws IOException, XWikiException;

    /**
     * Deletes existing documents with objects of class className from space space, besides the template document, built
     * with classNameTemplate or classTemplate.
     * 
     * @param className the name of the class
     * @param wiki the wiki on which to execute the operation. Note that since this will be done with the current user,
     *            he'd better have the right to.
     * @param space space to delete from. If missing, the documents from all the spaces will be deleted
     * @return the log of the delete
     * @throws XWikiException
     */
    public String deleteExistingDocuments(String className, String wiki, String space) throws XWikiException;
}
