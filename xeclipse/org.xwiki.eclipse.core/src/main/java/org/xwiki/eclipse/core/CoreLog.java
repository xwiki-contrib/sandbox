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
 *
 */
package org.xwiki.eclipse.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class CoreLog
{
    public static void logInfo(String message)
    {
        log(IStatus.INFO, IStatus.OK, message, null);
    }

    public static void logWarning(String message)
    {
        log(IStatus.WARNING, IStatus.OK, message, null);
    }

    public static void logError(String message)
    {
        log(IStatus.ERROR, IStatus.OK, message, null);
    }

    public static void logError(String message, Throwable exception)
    {
        log(IStatus.ERROR, IStatus.OK, message, exception);
    }

    private static void log(int severity, int code, String message, Throwable exception)
    {
        IStatus status = new Status(severity, CorePlugin.PLUGIN_ID, code, message, exception);
        CorePlugin.getDefault().getLog().log(status);
    }
}
