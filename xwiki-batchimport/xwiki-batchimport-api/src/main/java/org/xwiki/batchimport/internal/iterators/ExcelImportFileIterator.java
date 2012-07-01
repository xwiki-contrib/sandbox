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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.io.IOUtils;
import org.xwiki.batchimport.BatchImportConfiguration;
import org.xwiki.batchimport.ImportFileIterator;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.plugin.excel.ExcelPlugin;
import com.xpn.xwiki.plugin.excel.ExcelPluginException;

/**
 * Excel import file iterator.
 * 
 * @version $Id$
 */
@Component("xls")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class ExcelImportFileIterator implements ImportFileIterator
{
    @Inject
    private Execution execution;

    /**
     * The excel sheet to read.
     */
    private Sheet sheet;

    /**
     * The workbook being read, stored here to be closed at the end.
     */
    private Workbook workbook;

    /**
     * The current row.
     */
    private int currentRow = 0;
    
    /**
     * The encoding of this file, to read cells with it
     */
    private String encoding = DEFAULT_ENCODING;
    
    /**
     * TODO: Maybe we should read this encoding from xwiki default 
     */
    private static final String DEFAULT_ENCODING = "UTF-8"; 

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.ImportFileIterator#resetFile(org.xwiki.batchimport.BatchImportConfiguration)
     */
    @Override
    public void resetFile(BatchImportConfiguration config) throws IOException
    {
        // close the workbook if any is open
        this.close();

        // now read the config and create a new workbook and sheet from it
        XWikiContext xcontext = getXWikiContext();
        ExcelPlugin xlsPlugin = (ExcelPlugin) xcontext.getWiki().getPlugin("calc", xcontext);

        if (xlsPlugin == null) {
            throw new IOException("Canot read excel file, no plugin found. "
                + "Check that the excel plugin is installed in your wiki and properly configured.");
        }

        // create the workbook from the settings in the configuration
        try {
            if (config.getFileInputStream() != null) {
                this.workbook = xlsPlugin.getWorkbook("", IOUtils.toByteArray(config.getFileInputStream()));
            } else if (config.getAttachmentReference() != null) {
                this.workbook =
                    xlsPlugin.getWorkbook(
                        xcontext.getWiki()
                            .getDocument(config.getAttachmentReference().getDocumentReference(), xcontext), config
                            .getAttachmentReference().getName(), xcontext);
            }
        } catch (ExcelPluginException e) {
            throw new IOException("Canot read excel file, excel plugin exception encountered for config " + config
                + ": ", e);
        } catch (XWikiException e) {
            throw new IOException(
                "Canot read excel file, exception encountered when reading document attachment for config " + config
                    + ": ", e);
        }

        // setup the sheet from the read workbook
        this.sheet = this.workbook.getSheet(0);
        this.encoding = config.getEncoding();
        // reset the current row
        this.currentRow = 0;
    }

    @Override
    public void close() throws IOException
    {
        if (this.workbook != null) {
            this.workbook.close();
            this.workbook = null;
            this.sheet = null;
            this.currentRow = 0;
            this.encoding = DEFAULT_ENCODING;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.ImportFileIterator#readNextLine()
     */
    @Override
    public List<String> readNextLine() throws IOException
    {
        List<String> line = new ArrayList<String>();
        if (this.currentRow < this.sheet.getRows()) {
            for (Cell cell : this.sheet.getRow(this.currentRow)) {
                line.add(new String(cell.getContents().trim().getBytes(), this.encoding));
            }
            this.currentRow++;
            return line;
        } else {
            // no more rows
            return null;
        }
    }

    protected XWikiContext getXWikiContext()
    {
        ExecutionContext ec = execution.getContext();
        XWikiContext xwikicontext = (XWikiContext) ec.getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        return xwikicontext;
    }
}
