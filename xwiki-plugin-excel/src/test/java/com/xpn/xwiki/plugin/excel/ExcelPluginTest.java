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
import com.xpn.xwiki.test.AbstractBridgedXWikiComponentTestCase;
import jxl.write.WritableWorkbook;
import jxl.write.WritableSheet;
import jxl.write.WritableCell;
import jxl.Cell;
import jxl.Workbook;

public class ExcelPluginTest extends AbstractBridgedXWikiComponentTestCase
{
    public void testWritableExcel() throws Exception
    {
        XWikiContext context = new XWikiContext();
        ExcelPlugin ep = new ExcelPlugin("", "", context);
        ExcelPluginAPI epa = (ExcelPluginAPI) ep.getPluginApi(ep, context);
        WritableWorkbook writableWorkbook = epa.getWritableWorkbook();
        WritableSheet sheet = writableWorkbook.createSheet("test", 0);
        Cell cell = sheet.getWritableCell(0, 0);
        WritableCell newcell = epa.getNumberCell(cell);
        sheet.addCell(newcell);
        String table1 = epa.getTableFromWorkbook("", "1", "A1-A1", writableWorkbook);
        byte[] data = epa.getWritableWorkbookAsBytes(writableWorkbook);
        Workbook workbook = epa.getWorkbook("", data);
        String table2 = epa.getTableFromWorkbook("", "1", "A1-A1", workbook);
        assertEquals("Tables are different", table1, table2);
    }
}
