package org.xwiki.pdf.multipageexport;
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

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

/**
 * This pdf exporter exports multiple pages as a single pdf, the generated pdf is sent directly on the response.
 * 
 * @version $Id$
 */
@ComponentRole
public interface MultipagePdfExporter
{
    /**
     * @param name the title of the document to export, the name of the file will be computed from this title replacing
     *            whitespace with underscores
     * @param docs the list of pages to export
     * @throws Exception if something goes wrong during the export
     */
    void export(String name, List<String> docs) throws Exception;

    /**
     * @param name the title of the document to export, the name of the file will be computed from this title replacing
     *            whitespace with underscores
     * @param docs the list of pages to export
     * @param multiPageSequence whether each page should be exported in its own page sequence, with its own header and
     *            footer. If this parameter is true, all new documents will start on recto). If you need a different
     *            behaviour, use the 4 parameters version of this function (
     *            {@link #export(String, List, boolean, boolean)}).
     * @throws Exception if something goes wrong during the export
     */
    void export(String name, List<String> docs, boolean multiPageSequence) throws Exception;

    /**
     * @param name the title of the document to export, the name of the file will be computed from this title replacing
     *            whitespace with underscores
     * @param docs the list of pages to export
     * @param multiPageSequence whether each page should be exported in its own page sequence, with its own header and
     *            footer. For the moment, this function needs a custom xhtml2fo.xsl, which can be passed by passing a
     *            the pdftemplate parameter
     * @param alwaysStartOnRecto used in conjunction with multiPageSequence, whether each page sequence (each wiki
     *            document) should always start on recto. If {@code multiPageSequence} is false, this parameter is
     *            ignored.
     * @throws Exception if something goes wrong during the export
     */
    void export(String name, List<String> docs, boolean multiPageSequence, boolean alwaysStartOnRecto) throws Exception;
}
