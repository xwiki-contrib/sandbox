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
package org.xwiki.wikiimporter.internal.importer;

import java.util.LinkedList;
import java.util.List;

/**
 * WikiImporter custom logging class to generate import process log summary. Uses singleton design pattern.
 * 
 * @version $Id$
 */
public class WikiImporterLogger
{

    private static WikiImporterLogger logger;

    // Log levels
    public static final int INFO = 1;

    public static final int ERROR = 2;

    public static final int WARNING = 3;

    private static PageLog pageLog;

    private static final String START_TAG = "{{velocity}}\n $xwiki.ssx.use(\"WikiImporter.SSX\")\n{{html}}";

    private static final String END_TAG = "{{/html}}{{/velocity}}";

    private static final String NEW_LINE = "\n";

    private static final String LI_START_TAG = "<li>";

    private static final String LI_START_TAG_INFO = "<li class=\"info\">";

    private static final String LI_START_TAG_ERROR = "<li class=\"error\">";

    private static final String LI_START_TAG_WARNING = "<li class=\"warning\">";

    private static final String LI_END_TAG = "</li>";

    private static final String UL_START_TAG = "<ul class=\"importer\">";

    private static final String UL_END_TAG = "</ul>";

    private List<Log> logs = new LinkedList<Log>();

    // Private Constructor
    private WikiImporterLogger()
    {
    }

    // Log class to report any information.
    public class Log
    {
        protected String log = "";

        public void setLog(String log)
        {
            this.log = log;
        }

        public String getLog()
        {
            return this.log;
        }

    }

    // Page log class to report page specific information.
    public class PageLog extends Log
    {
        private StringBuilder pageLog = new StringBuilder();

        public PageLog()
        {
            pageLog.append(UL_START_TAG);
        }

        public void setLog(String log)
        {
            this.log = LI_START_TAG + log + LI_END_TAG;
        }

        public StringBuilder getPageLog()
        {
            return pageLog;
        }
    }

    /**
     * @param infoStr Log Content.
     * @param isPage if the log is page specific log.
     * @param logLevel Error, Info or Warning.
     */
    public void info(String infoStr, boolean isPage, int logLevel)
    {

        switch (logLevel) {
            case INFO:
                infoStr = LI_START_TAG_INFO + infoStr + LI_END_TAG;
                break;
            case ERROR:
                infoStr = LI_START_TAG_ERROR + infoStr + LI_END_TAG;
                break;
            case WARNING:
                infoStr = LI_START_TAG_WARNING + infoStr + LI_END_TAG;
                break;
            default:
                infoStr = LI_START_TAG + infoStr + LI_END_TAG;

        }

        if (isPage) {
            if (pageLog == null) {
                nextPage();
            }
            pageLog.getPageLog().append(infoStr);
        } else {
            Log logTmp = new Log();
            logs.add(logTmp);
            logTmp.setLog(infoStr);
        }
    }

    /**
     * @return the page logger in use.
     */
    public static PageLog getPageLog()
    {
        return pageLog;
    }

    /**
     * Creates page log object for a new page.
     */
    public void nextPage()
    {
        PageLog newPageLog = new PageLog();
        logs.add(newPageLog);
        pageLog = newPageLog;
    }

    /**
     * @return the list of all log objects.
     */
    public List<Log> getAllLogs()
    {
        return logs;
    }

    /**
     *Clear all the logs.
     */
    public void clearAllLogs()
    {
        logs.clear();
    }

    /**
     * @return the log content as String.
     */
    public String getAllLogsAsString()
    {
        StringBuilder logStr = new StringBuilder();
        logStr.append(START_TAG + "<ol \"importer\">");
        for (Log log : logs) {
            logStr.append(log.getLog() + NEW_LINE);
            if (log instanceof PageLog) {
                logStr.append(((PageLog) log).getPageLog() + UL_END_TAG + NEW_LINE);
            }
        }
        logStr.append("</ol>" + END_TAG);
        return logStr.toString();
    }

    /**
     * @return instance of the logger.
     */
    public static WikiImporterLogger getLogger()
    {
        if (logger == null) {
            logger = new WikiImporterLogger();
        }
        return logger;
    }
}
