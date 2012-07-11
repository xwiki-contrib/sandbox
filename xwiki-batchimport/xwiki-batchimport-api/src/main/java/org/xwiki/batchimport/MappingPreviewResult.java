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
package org.xwiki.batchimport;

import java.util.List;
import java.util.Map;

import org.xwiki.batchimport.log.BatchImportLog;

/**
 * Encapsulates the result of a mapping preview, that is the actual previewed parsed data (where a row is given by a
 * list of pairs with an xwiki field on the first position and an object on the second position which represents the
 * validated and parsed data: if the object is null, there was a validation error) and the log of the preview, which
 * will contain all the validation errors encountered during data parsing.
 * 
 * @version $Id$
 */
public class MappingPreviewResult
{
    private List<Map<String, Object>> parsedData;

    private BatchImportLog log;

    public MappingPreviewResult(List<Map<String, Object>> parsedData, BatchImportLog log)
    {
        super();
        this.parsedData = parsedData;
        this.log = log;
    }

    /**
     * @return the parsedData
     */
    public List<Map<String, Object>> getParsedData()
    {
        return parsedData;
    }

    /**
     * @param parsedData the parsedData to set
     */
    public void setParsedData(List<Map<String, Object>> parsedData)
    {
        this.parsedData = parsedData;
    }

    /**
     * @return the log
     */
    public BatchImportLog getLog()
    {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog(BatchImportLog log)
    {
        this.log = log;
    }
}
