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
package org.xwiki.batchimport.internal.iterators;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.xwiki.batchimport.BatchImportConfiguration;
import org.xwiki.batchimport.ImportFileIterator;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.AttachmentReference;

import au.com.bytecode.opencsv.CSVReader;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * CSV implementation of the import file iterator, to read CSV files.
 * 
 * @version $Id$
 */
@Component("csv")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class CSVImportFileIterator implements ImportFileIterator
{
    @Inject
    private Execution execution;

    private CSVReader reader;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.ImportFileIterator#readNextLine()
     */
    @Override
    public List<String> readNextLine() throws IOException
    {
        String[] nextLine = this.reader.readNext();
        if (nextLine == null) {
            return null;
        }
        return Arrays.asList(nextLine);
    }

    @Override
    public void resetFile(BatchImportConfiguration config) throws IOException
    {
        // if a previous reader is open, close it first and then open the new one
        this.close();

        // prepare the new reader from the passed config and store it
        InputStreamReader sourceInputStream = getSourceInputStream(config, getXWikiContext());
        this.reader = new CSVReader(sourceInputStream, config.getCsvSeparator(), config.getCsvTextDelimiter());
    }

    @Override
    public void close() throws IOException
    {
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
        }
    }

    /**
     * @param config the import configuration, to read input stream && file from it
     * @param xcontext the xwiki context
     * @return the input stream for the file to import
     * @throws IOException if we fail to read properly the source from the config (e.g. no source configured or the
     *             attachment configured does not exist).
     */
    protected InputStreamReader getSourceInputStream(BatchImportConfiguration config, XWikiContext xcontext)
        throws IOException
    {
        InputStream inputStream = null;

        // if some config input stream is passed, use it, otherwise go look at the attachment reference
        if (config.getFileInputStream() != null) {
            inputStream = config.getFileInputStream();
        } else if (config.getAttachmentReference() != null) {
            AttachmentReference attachmentRef = config.getAttachmentReference();
            XWikiAttachment attach = null;
            try {
                XWikiDocument attachmentDoc =
                    xcontext.getWiki().getDocument(attachmentRef.getDocumentReference(), xcontext);
                attach = attachmentDoc.getAttachment(attachmentRef.getName());
                if (attach != null) {
                    inputStream = attach.getContentInputStream(xcontext);
                } else {
                    throw new IOException("Could not read csv file, attachment not found " + attachmentRef.getName()
                        + " for document " + attachmentDoc.getDocumentReference() + " for config " + config.toString());
                }
            } catch (XWikiException e) {
                throw new IOException("Could not read csv file, attachment not found " + attachmentRef.getName()
                    + " for document " + attachmentRef.getDocumentReference() + " for config " + config.toString());
            }

        } else {
            throw new IOException("Could not read csv file, no source file configured in the batch import config: "
                + config.toString());
        }

        return new InputStreamReader(inputStream, config.getEncoding());
    }

    protected XWikiContext getXWikiContext()
    {
        ExecutionContext ec = execution.getContext();
        XWikiContext xwikicontext = (XWikiContext) ec.getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        return xwikicontext;
    }
}
