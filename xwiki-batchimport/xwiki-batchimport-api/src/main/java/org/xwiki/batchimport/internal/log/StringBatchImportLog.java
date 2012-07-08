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
package org.xwiki.batchimport.internal.log;

import org.slf4j.Logger;
import org.xwiki.batchimport.log.AbstractSavedDocumentsBatchImportLog;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

/**
 * @version $Id$
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class StringBatchImportLog extends AbstractSavedDocumentsBatchImportLog
{
    protected StringBuffer fullLog = new StringBuffer();

    protected StringBuffer errorLog = new StringBuffer();

    protected Logger consoleLogger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.log.BatchImportLog#log()
     */
    @Override
    public void log(String messageKey, Object... parameters)
    {
        String prettyMessage = getPrettyMessage(messageKey, parameters);
        internalLog(prettyMessage, false);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.log.BatchImportLog#logError()
     */
    @Override
    public void logError(String messageKey, Object... parameters)
    {
        String prettyMessage = getPrettyMessage(messageKey, parameters);
        internalLog(prettyMessage, true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.log.BatchImportLog#logSave(java.lang.String, java.lang.Object[])
     */
    @Override
    public void logSave(String messageKey, Object... parameters)
    {
        log(messageKey, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.log.BatchImportLog#logCritical(java.lang.String, java.lang.Object[])
     */
    @Override
    public void logCritical(String messageKey, Object... parameters)
    {
        String prettyMessage = getPrettyMessage(messageKey, parameters);
        internalLog(prettyMessage, true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.log.BatchImportLog#logDelete(java.lang.String, java.lang.Object[])
     */
    @Override
    public void logDelete(String messageKey, Object... parameters)
    {
        log(messageKey, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.log.BatchImportLog#logSkip()
     */
    @Override
    public void logSkip(String messageKey, Object... parameters)
    {
        log(messageKey, parameters);
    }

    @Override
    public void setConsoleLogger(Logger logger)
    {
        this.consoleLogger = logger;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.log.BatchImportLog#getErrorLog()
     */
    @Override
    public String getErrorLog()
    {
        return this.errorLog.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.log.BatchImportLog#getFullLog()
     */
    @Override
    public String getFullLog()
    {
        return this.fullLog.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.log.BatchImportLog#getFullLogURL()
     */
    @Override
    public String getFullLogURL()
    {
        return null;
    }

    protected void internalLog(String message, boolean isError)
    {
        fullLog.append(message);
        fullLog.append("\n");
        if (isError) {
            errorLog.append(message);
            errorLog.append("\n");
        }
        if (this.consoleLogger != null) {
            this.consoleLogger.info(message);
        }
    }

    protected String getPrettyMessage(String messageKey, Object... parameters)
    {
        return DefaultBatchImportLogMessages.getPrettyMessage(messageKey, parameters);
    }
}
