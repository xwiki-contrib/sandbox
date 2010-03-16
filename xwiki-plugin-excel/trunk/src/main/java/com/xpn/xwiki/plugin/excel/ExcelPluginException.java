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

import com.xpn.xwiki.plugin.PluginException;

public class ExcelPluginException extends PluginException
{
    static String plugName = "ExcelPlugin";

    public static final int ERROR_EXCELPLUGIN_UNKNOWN = 1;

    public static final int ERROR_EXCELPLUGIN_UNKNOWN_ATTACHMENT = 2;

    public static final int ERROR_EXCELPLUGIN_INVALID_ATTACHMENT = 3;

    public static final int ERROR_EXCELPLUGIN_INVALID_SPREADSHEET = 4;

    public static final int ERROR_EXCELPLUGIN_INVALID_SHEET = 5;

    public static final int ERROR_EXCELPLUGIN_INVALID_RANGE = 6;

    public static final int ERROR_EXCELPLUGIN_CANNOT_CREATE_SPREADSHEET = 7;

    public ExcelPluginException(int code, String message, Throwable e, Object[] args)
    {
        super(plugName, code, message, e, args);
    }

    public ExcelPluginException(int code, String message, Throwable e)
    {
        super(plugName, code, message, e);
    }

    public ExcelPluginException(int code, String message)
    {
        super(plugName, code, message);
    }

    public ExcelPluginException()
    {
        super();
    }
}
