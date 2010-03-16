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
package com.xpn.xwiki.plugin.excel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jxl.Cell;
import jxl.Hyperlink;
import jxl.Range;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.biff.EmptyCell;
import jxl.format.RGB;
import jxl.write.WritableWorkbook;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;

public class ExcelPlugin extends XWikiDefaultPlugin
{
    public ExcelPlugin(String name, String className, XWikiContext context)
    {
        super(name, className, context);
    }

    public String getName()
    {
        return "calc";
    }

    public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context)
    {
        return new ExcelPluginAPI((ExcelPlugin) plugin, context);
    }

    public void flushCache()
    {
    }

    public void init(XWikiContext context)
    {
        super.init(context);
    }

    public WritableWorkbook getWritableWorkbook(OutputStream os) throws ExcelPluginException
    {
        return getWritableWorkbook(os);
    }

    public WritableWorkbook getWritableWorkbook(Workbook workbook, OutputStream os) throws ExcelPluginException
    {
        try {
            WritableWorkbook wwb =
                (workbook == null) ? Workbook.createWorkbook(os) : Workbook.createWorkbook(os, workbook);
            return wwb;
        } catch (IOException e) {
            throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_CANNOT_CREATE_SPREADSHEET,
                "Cannot create spreadsheet", e);
        }
    }

    public Workbook getWorkbook(String attachname, XWikiContext context) throws ExcelPluginException
    {
        return getWorkbook(context.getDoc(), attachname, context);
    }

    public Workbook getWorkbook(XWikiDocument doc, String attachname, XWikiContext context) throws ExcelPluginException
    {
        XWikiAttachment attachment = doc.getAttachment(attachname);
        if (attachment == null) {
            throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_UNKNOWN_ATTACHMENT,
                "Couldn't find the requested attachment: " + attachname);
        }
        byte[] array;
        try {
            array = attachment.getContent(context);
        } catch (XWikiException e2) {
            throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_INVALID_ATTACHMENT,
                "Couldn't load the requested attachment: " + attachname, e2);
        }
        return getWorkbook(attachname, array);
    }

    public Workbook getWorkbook(String spreadsheetname, byte[] array) throws ExcelPluginException
    {
        InputStream stream = new ByteArrayInputStream(array);
        Workbook workbook;
        WorkbookSettings ws = new WorkbookSettings();
        ws.setEncoding("windows-1252");

        try {
            workbook = Workbook.getWorkbook(stream, ws);
        } catch (Exception e1) {
            throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_INVALID_SPREADSHEET,
                "Couldn't load the spreadsheet: " + spreadsheetname, e1);
        }
        return workbook;
    }

    public String getTable(String attachname, String sheetname, String range, XWikiContext context)
        throws ExcelPluginException
    {
        Workbook workbook = getWorkbook(attachname, context);
        return getTableFromWorkbook(attachname, sheetname, range, workbook);
    }

    public String getTableFromWritableWorkbook(String spreadsheetname, String sheetname, String range,
        WritableWorkbook workbook) throws ExcelPluginException
    {
        Sheet sheet;
        if (isNumeric(sheetname)) {
            try {
                int num = Integer.valueOf(sheetname).intValue();
                sheet = workbook.getSheet(num - 1);
            } catch (Exception e) {
                throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_INVALID_SHEET,
                    "Couldn't load sheet in spreadsheet: " + spreadsheetname, e);
            }
        } else {
            sheet = workbook.getSheet(sheetname);
        }
        if (sheet == null) {
            throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_INVALID_SHEET,
                "Couldn't load sheet in spreadsheet: " + spreadsheetname);
        }
        Range[] ranges = workbook.findByName(range);
        return getTableFromSheet(spreadsheetname, ranges, range, sheet);
    }

    public String getTableFromWorkbook(String spreadsheetname, String sheetname, String range, Workbook workbook)
        throws ExcelPluginException
    {
        Sheet sheet;
        if (isNumeric(sheetname)) {
            try {
                int num = Integer.valueOf(sheetname).intValue();
                sheet = workbook.getSheet(num - 1);
            } catch (Exception e) {
                throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_INVALID_SHEET,
                    "Couldn't load sheet in spreadsheet: " + spreadsheetname, e);
            }
        } else {
            sheet = workbook.getSheet(sheetname);
        }
        if (sheet == null) {
            throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_INVALID_SHEET,
                "Couldn't load sheet in spreadsheet: " + spreadsheetname);
        }
        Range[] ranges = workbook.findByName(range);
        return getTableFromSheet(spreadsheetname, ranges, range, sheet);
    }

    public List getTableFromSheetAsCells(String spreadsheetname, Range[] ranges, String range, Sheet sheet)
        throws ExcelPluginException
    {
        List cellList = new ArrayList();
        int fromColIndex;
        int toRowIndex;
        int fromRowIndex;
        int toColIndex;
        if ((ranges == null) || (ranges.length == 0)) {
            String fromCell;
            String toCell;
            StringTokenizer tokenizer = new StringTokenizer(range, "-");
            if (tokenizer.countTokens() != 2) {
                throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_INVALID_RANGE,
                    "Couldn't load range in spreadsheet: " + spreadsheetname);
            } else {
                fromCell = tokenizer.nextToken();
                toCell = tokenizer.nextToken();
            }
            fromColIndex = 0;
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

            try {
                fromRowIndex = Integer.valueOf(fromCell.substring(i)).intValue() - 1;
            } catch (Exception e) {
                throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_INVALID_RANGE,
                    "Couldn't load range in spreadsheet: " + spreadsheetname);
            }

            toColIndex = 0;
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

            try {
                toRowIndex = Integer.valueOf(toCell.substring(i)).intValue() - 2;
            } catch (Exception e) {
                throw new ExcelPluginException(ExcelPluginException.ERROR_EXCELPLUGIN_INVALID_RANGE,
                    "Couldn't load range in requested attachment: " + spreadsheetname);
            }
        } else {
            Cell fromCell = ranges[0].getTopLeft();
            Cell toCell = ranges[0].getBottomRight();
            fromRowIndex = fromCell.getRow();
            fromColIndex = fromCell.getColumn();
            toRowIndex = toCell.getRow() - 1;
            toColIndex = toCell.getColumn();
        }

        int columnCount = toColIndex - fromColIndex + 1;
        int rowCount = toRowIndex - fromRowIndex + 1;

        for (int row = 0; row <= rowCount; row++) {
            List rowCells = new ArrayList();
            for (int i = 0; i < columnCount; i++) {
                try {
                    Cell cell = sheet.getCell(fromColIndex + i, fromRowIndex + row);
                    rowCells.add(cell);
                } catch (Exception e) {
                    rowCells.add(new EmptyCell(fromColIndex + i, fromRowIndex + row));
                }
            }
            cellList.add(rowCells);
        }
        return cellList;
    }

    public String getTableFromSheet(String spreadsheetname, Range[] ranges, String range, Sheet sheet)
        throws ExcelPluginException
    {

        return getTableFromCells(getTableFromSheetAsCells(spreadsheetname, ranges, range, sheet), sheet);
    }

    public String getTableFromCells(List cellList, Sheet sheet)
    {
        StringBuffer result = new StringBuffer();
        result.append("<table class=\"wiki-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n");
        result.append("<tr>\n");
        if (cellList.size() > 0) {
            List headerCells = (List) cellList.get(0);
            for (int c = 0; c < headerCells.size(); c++) {
                try {
                    Cell cell = (Cell) headerCells.get(c);
                    result.append(showCell(cell, "th", "", sheet));
                } catch (Exception e) {
                    result.append("<th></th>");
                }
            }
        }
        result.append("</tr>\n");
        String oddRowClass = "class=\"table-odd\"";
        String evenRowClass = "class=\"table-even\"";
        for (int row = 0; row < cellList.size() - 1; row++) {
            result.append("<tr>\n");
            List cells = (List) cellList.get(row + 1);
            for (int i = 0; i < cells.size(); i++) {
                String rowclass;
                if (row % 2 == 1) {
                    rowclass = oddRowClass;
                } else {
                    rowclass = evenRowClass;
                }
                try {
                    Cell cell = (Cell) cells.get(i);
                    result.append(showCell(cell, "td", rowclass, sheet));
                } catch (Exception e) {
                    result.append("<td " + rowclass + "></td>");
                }
            }
            result.append("\n</tr>\n");
        }
        result.append("</table>");
        return result.toString();
    }

    private URL findHyperlink(Cell cell, Sheet sheet)
    {
        int row = cell.getRow();
        int col = cell.getColumn();
        Hyperlink[] links = sheet.getHyperlinks();
        if (links == null) {
            return null;
        }
        for (int i = 0; i < links.length; i++) {
            int hrow = links[i].getRow();
            int hrow2 = links[i].getLastRow();
            int hcol = links[i].getColumn();
            int hcol2 = links[i].getLastColumn();
            if ((row >= hrow) && (row <= hrow2) && (col >= hcol) && (col <= hcol2)) {
                return links[i].getURL();
            }
        }
        return null;
    }

    private String showCell(Cell cell, String element, String classmod, Sheet sheet)
    {
        String bgcolor = null;
        String textcolor = null;

        if ((cell.getCellFormat() != null) && (cell.getCellFormat().getBackgroundColour() != null)) {
            RGB color = cell.getCellFormat().getBackgroundColour().getDefaultRGB();
            bgcolor =
                "background-color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "); ";
        }

        if ((cell.getCellFormat() != null) && (cell.getCellFormat().getFont() != null)
            && (cell.getCellFormat().getFont().getColour() != null))
        {
            RGB color = cell.getCellFormat().getFont().getColour().getDefaultRGB();
            textcolor = "color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "); ";
        }

        URL url = findHyperlink(cell, sheet);
        String content =
            (url == null) ? cell.getContents() : "<a href=\"" + url.toString() + "\" target=\"_blank\" />"
                + cell.getContents() + "</a>";

        if ((bgcolor == null) && (textcolor == null)) {
            return "<" + element + " " + classmod + ">" + content + "</" + element + ">";
        } else {
            return "<" + element + " " + classmod + " style=\"" + textcolor + bgcolor + "\">" + content + "</"
                + element + ">";
        }
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
