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

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Document;
import jxl.*;
import jxl.write.*;
import jxl.write.Number;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class ExcelPluginAPI extends Api
{
    private static ExcelPlugin plugin;

    private Map workbookstreams = new HashMap();

    public ExcelPluginAPI(ExcelPlugin plugin, XWikiContext context)
    {
        super(context);
        setPlugin(plugin);
    }

    public ExcelPlugin getPlugin()
    {
        return plugin;
    }

    public void setPlugin(ExcelPlugin plugin)
    {
        ExcelPluginAPI.plugin = plugin;
    }

    public void clearCache()
    {
        workbookstreams.clear();
    }

    public WritableCell getNumberCell(Cell cell)
    {
        return new Number(cell.getRow(), cell.getColumn(), 0);
    }

    public WritableCell getDateTimeCell(Cell cell)
    {
        return new DateTime(cell.getRow(), cell.getColumn(), new Date());
    }

    public WritableCell getFormulaCell(Cell cell, String formula)
    {
        return new Formula(cell.getRow(), cell.getColumn(), formula);
    }

    public WritableCell getLabelCell(Cell cell, String text)
    {
        return new Label(cell.getRow(), cell.getColumn(), text);
    }

    public boolean setNumberCell(WritableSheet sheet, int i, int j, double nb)
    {
        Cell cell = sheet.getWritableCell(i, j);
        Number newcell = (Number) getNumberCell(cell);
        newcell.setValue(nb);
        try {
            sheet.addCell(newcell);
        } catch (WriteException e) {
            e.printStackTrace();
            context.put("exception", e);
            return false;
        }
        return true;
    }

    public boolean setNumberCell(WritableSheet sheet, String name, double nb)
    {
        Cell cell = sheet.getWritableCell(name);
        Number newcell = (Number) getNumberCell(cell);
        newcell.setValue(nb);
        try {
            sheet.addCell(newcell);
        } catch (WriteException e) {
            e.printStackTrace();
            context.put("exception", e);
            return false;
        }
        return true;
    }

    public boolean setNumberCell(WritableWorkbook wb, String name, double nb)
    {
        Range[] ranges = wb.findByName(name);
        if (ranges.length == 0) {
            return false;
        }

        Range range = ranges[0];
        WritableSheet sheet = wb.getSheet(range.getFirstSheetIndex());
        Cell cell = range.getTopLeft();
        return setNumberCell(sheet, cell.getRow(), cell.getColumn(), nb);
    }

    public boolean setDateTimeCell(WritableSheet sheet, int i, int j, Date date)
    {
        Cell cell = sheet.getWritableCell(i, j);
        DateTime newcell = (DateTime) getDateTimeCell(cell);
        newcell.setDate(date);
        try {
            sheet.addCell(newcell);
        } catch (WriteException e) {
            e.printStackTrace();
            context.put("exception", e);
            return false;
        }
        return true;
    }

    public boolean setDateTimeCell(WritableSheet sheet, String name, Date date)
    {
        Cell cell = sheet.getWritableCell(name);
        DateTime newcell = (DateTime) getDateTimeCell(cell);
        newcell.setDate(date);
        try {
            sheet.addCell(newcell);
        } catch (WriteException e) {
            e.printStackTrace();
            context.put("exception", e);
            return false;
        }
        return true;
    }

    public boolean setDateTimeCell(WritableWorkbook wb, String name, Date date)
    {
        Range[] ranges = wb.findByName(name);
        if (ranges.length == 0) {
            return false;
        }

        Range range = ranges[0];
        WritableSheet sheet = wb.getSheet(range.getFirstSheetIndex());
        Cell cell = range.getTopLeft();
        return setDateTimeCell(sheet, cell.getRow(), cell.getColumn(), date);
    }

    public boolean setFormulaCell(WritableSheet sheet, int i, int j, String formula)
    {
        Cell cell = sheet.getWritableCell(i, j);
        Formula newcell = (Formula) getFormulaCell(cell, formula);
        try {
            sheet.addCell(newcell);
        } catch (WriteException e) {
            e.printStackTrace();
            context.put("exception", e);
            return false;
        }
        return true;
    }

    public boolean setFormulaCell(WritableSheet sheet, String name, String formula)
    {
        Cell cell = sheet.getWritableCell(name);
        Formula newcell = (Formula) getFormulaCell(cell, formula);
        try {
            sheet.addCell(newcell);
        } catch (WriteException e) {
            e.printStackTrace();
            context.put("exception", e);
            return false;
        }
        return true;
    }

    public boolean setFormulaCell(WritableWorkbook wb, String name, String formula)
    {
        Range[] ranges = wb.findByName(name);
        if (ranges.length == 0) {
            return false;
        }

        Range range = ranges[0];
        WritableSheet sheet = wb.getSheet(range.getFirstSheetIndex());
        Cell cell = range.getTopLeft();
        return setFormulaCell(sheet, cell.getRow(), cell.getColumn(), formula);
    }

    public boolean setLabelCell(WritableSheet sheet, int i, int j, String text)
    {
        Cell cell = sheet.getWritableCell(i, j);
        Label newcell = (Label) getLabelCell(cell, text);
        try {
            sheet.addCell(newcell);
        } catch (WriteException e) {
            e.printStackTrace();
            context.put("exception", e);
            return false;
        }
        return true;
    }

    public boolean setLabelCell(WritableSheet sheet, String name, String text)
    {
        Cell cell = sheet.getWritableCell(name);
        Label newcell = (Label) getLabelCell(cell, text);
        try {
            sheet.addCell(newcell);
        } catch (WriteException e) {
            e.printStackTrace();
            context.put("exception", e);
            return false;
        }
        return true;
    }

    public boolean setLabelCell(WritableWorkbook wb, String name, String text)
    {
        Range[] ranges = wb.findByName(name);
        if (ranges.length == 0) {
            return false;
        }

        Range range = ranges[0];
        WritableSheet sheet = wb.getSheet(range.getFirstSheetIndex());
        Cell cell = range.getTopLeft();
        return setLabelCell(sheet, cell.getRow(), cell.getColumn(), text);
    }

    public WritableWorkbook getWritableWorkbook()
    {
        return getWritableWorkbook((Workbook) null);
    }

    public WritableWorkbook getWritableWorkbook(Workbook wb)
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            WritableWorkbook wwb = plugin.getWritableWorkbook(wb, baos);
            workbookstreams.put(wwb, baos);
            return wwb;
        } catch (ExcelPluginException e) {
            e.printStackTrace();
            context.put("exception", e);
            return null;
        }
    }

    public byte[] getWritableWorkbookAsBytes(WritableWorkbook wwb)
    {
        try {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) workbookstreams.get(wwb);
            if (baos == null) {
                return null;
            }
            wwb.write();
            wwb.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            context.put("exception", e);
            return null;
        }
    }

    public Workbook getWorkbook(String attachname)
    {
        try {
            return plugin.getWorkbook(attachname, context);
        } catch (Exception e) {
            e.printStackTrace();
            context.put("exception", e);
            return null;
        }
    }

    public WritableWorkbook getWritableWorkbook(String attachname)
    {
        try {
            return getWritableWorkbook(getWorkbook(attachname));
        } catch (Exception e) {
            e.printStackTrace();
            context.put("exception", e);
            return null;
        }
    }

    public Workbook getWorkbook(Document doc, String attachname)
    {
        try {
            return plugin.getWorkbook(doc.getDocument(), attachname, context);
        } catch (Exception e) {
            e.printStackTrace();
            context.put("exception", e);
            return null;
        }
    }

    public WritableWorkbook getWritableWorkbook(Document doc, String attachname)
    {
        try {
            return getWritableWorkbook(getWorkbook(doc, attachname));
        } catch (Exception e) {
            e.printStackTrace();
            context.put("exception", e);
            return null;
        }
    }

    public Workbook getWorkbook(String spreadsheetname, byte[] array) throws ExcelPluginException
    {
        try {
            return plugin.getWorkbook(spreadsheetname, array);
        } catch (Exception e) {
            e.printStackTrace();
            context.put("exception", e);
            return null;
        }
    }

    public String getTable(String attachname, String sheetname, String range)
    {
        try {
            return plugin.getTable(attachname, sheetname, range, getXWikiContext());
        } catch (Exception e) {
            e.printStackTrace();
            context.put("exception", e);
            return e.getMessage();
        }
    }

    public String getTableFromWorkbook(String spreadsheetname, String sheetname, String range, Object workbook)
    {
        try {
            if (workbook instanceof Workbook) {
                return plugin.getTableFromWorkbook(spreadsheetname, sheetname, range, (Workbook) workbook);
            } else {
                return plugin.getTableFromWritableWorkbook(spreadsheetname, sheetname, range,
                    (WritableWorkbook) workbook);
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.put("exception", e);
            return e.getMessage();
        }
    }
}
