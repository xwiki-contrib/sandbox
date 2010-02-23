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
package com.xpn.xwiki.plugin.charts.source;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.charts.exceptions.DataSourceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelDataSource extends DefaultDataSource implements DataSource
{
    public static final String FILENAME = "file";

    public static final String SHEET = "sheet";

    public static final String RANGE = "range";

    public static final String HAS_HEADER_COLUMN = "has_header_column";

    public static final boolean DEFAULT_HAS_HEADER_COLUMN = true;

    private static final Log LOG = LogFactory.getLog(ExcelDataSource.class);

    public ExcelDataSource()
    {

    }

    public ExcelDataSource(BaseObject defObject, XWikiContext context) throws DataSourceException
    {
        init(defObject.getStringValue(FILENAME), defObject.getStringValue(SHEET), defObject.getStringValue(RANGE),
            defObject.getIntValue(HAS_HEADER_COLUMN) == 1, context);
    }

    public ExcelDataSource(Map params, XWikiContext context) throws DataSourceException
    {
        String fileName = (String) params.get(FILENAME);
        String sheetName = (String) params.get(SHEET);
        if (sheetName == null) {
            sheetName = "1";
        }
        String range = (String) params.get(RANGE);

        String hhc = (String) params.get(HAS_HEADER_COLUMN);
        boolean hasHeaderColumn;
        if (hhc != null) {
            hasHeaderColumn = hhc.equalsIgnoreCase("true");
        } else {
            hasHeaderColumn = DEFAULT_HAS_HEADER_COLUMN;
        }
        init(fileName, sheetName, range, hasHeaderColumn, context);
    }

    public ExcelDataSource(String filename, String sheetname, String range, boolean hasHeaderColumn,
        XWikiContext context) throws DataSourceException
    {
        init(filename, sheetname, range, hasHeaderColumn, context);
    }

    private void fillDate(InputStream stream, String sheetname, String range, boolean hasHeaderCol)
    {

        Workbook workbook;
        try {
            workbook = Workbook.getWorkbook(stream);
        } catch (BiffException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        Sheet sheet;
        if (isNumeric(sheetname)) {
            try {
                int num = Integer.valueOf(sheetname).intValue();
                sheet = workbook.getSheet(num - 1);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else {
            sheet = workbook.getSheet(sheetname);
        }
        if (sheet == null) {
            LOG.error("Invalid sheetname: " + sheetname);
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(range, "-");
        if (tokenizer.countTokens() != 2) {
            LOG.error("Invalid range: " + range);
            return;
        }
        String fromCell = tokenizer.nextToken();
        String toCell = tokenizer.nextToken();

        int fromColIndex = 0;
        int i;
        for (i = 0; i < fromCell.length(); i++) {
            char ch = fromCell.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (i > 0) {
                    fromColIndex += 26;
                }
                fromColIndex += ((int) ch - (int) 'A');
            } else {
                break;
            }
        }

        int fromRowIndex;
        try {
            fromRowIndex = Integer.valueOf(fromCell.substring(i)).intValue() - 1;
        } catch (Exception e) {
            LOG.error("Invalid from cell: " + fromCell, e);
            return;
        }

        int toColIndex = 0;
        for (i = 0; i < toCell.length(); i++) {
            char ch = toCell.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (i > 0) {
                    toColIndex += 26;
                }
                toColIndex += ((int) ch - (int) 'A');
            } else {
                break;
            }
        }

        int toRowIndex;
        try {
            toRowIndex = Integer.valueOf(toCell.substring(i)).intValue() - 1;
        } catch (Exception e) {
            LOG.error("Invalid to cell: " + toCell, e);
            return;
        }

        int columnCount = toColIndex - fromColIndex + 1;
        int rowCount = toRowIndex - fromRowIndex + 1;

        if (hasHeaderCol) {
            headerColumn = new String[rowCount - 1];
            headerRow = new String[columnCount - 1];
            for (i = 1; i < columnCount; i++) {
                headerRow[i - 1] = sheet.getCell(fromColIndex + i, fromRowIndex).getContents();
            }
        } else {
            headerRow = new String[columnCount];
            for (i = 0; i < columnCount; i++) {
                headerRow[i] = sheet.getCell(fromColIndex + i, fromRowIndex).getContents();
            }
        }

        int rowNumber = 0;
        if (hasHeaderCol) {
            data = new Number[rowCount - 1][columnCount - 1];
        } else {
            data = new Number[rowCount - 1][columnCount];
        }
        for (int r = fromRowIndex + 1; r <= toRowIndex; r++) {
            if (hasHeaderCol) {
                headerColumn[rowNumber] = sheet.getCell(fromColIndex, r).getContents();
                for (int c = 1; c < columnCount; c++) {
                    try {
                        data[rowNumber][c - 1] = Double.valueOf(sheet.getCell(fromColIndex + c, r).getContents());
                    } catch (Exception e) {
                        data[rowNumber][c - 1] = null;
                    }
                }
            } else {
                for (int c = 0; c < columnCount; c++) {
                    try {
                        data[rowNumber][c] = Double.valueOf(sheet.getCell(fromColIndex + c, r).getContents());
                    } catch (Exception e) {
                        data[rowNumber][c] = null;
                    }
                }
            }
            rowNumber++;
        }
    }

    private void init(String attachname, String sheetname, String range, boolean hasHeaderCol, XWikiContext context)
        throws DataSourceException
    {
        XWikiAttachment attachment = context.getDoc().getAttachment(attachname);
        if (attachment == null) {
            LOG.error("Couldn't find the requested attachment: " + attachname);
            return;
        }
        byte[] array;
        try {
            array = attachment.getContent(context);
        } catch (XWikiException e2) {
            e2.printStackTrace();
            return;
        }
        fillDate(new ByteArrayInputStream(array), sheetname, range, hasHeaderCol);
    }

    private boolean isNumeric(String str)
    {
        char[] chars = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {
            int ch = (int) (chars[i]);
            if (!Character.isDigit(chars[i]) && (ch < 1776 || ch > 1785)) {
                return false;
            }
        }
        return true;
    }
}
