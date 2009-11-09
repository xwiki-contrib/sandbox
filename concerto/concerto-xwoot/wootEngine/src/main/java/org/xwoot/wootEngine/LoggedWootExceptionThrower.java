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
package org.xwoot.wootEngine;

import org.apache.commons.logging.Log;

/**
 * Contains methods to log an error event and throw a {@link WootEngineException} at the same time.
 * 
 * @version $Id$
 */
public class LoggedWootExceptionThrower
{
    /** Unique ID for the WootEngine. */
    protected String wootEngineId;

    /** Object for logging and debugging events. */
    protected Log logger;

    /**
     * Used to log exceptions as they are thrown.
     * 
     * @param errorMessage the message of this exception.
     * @throws WootEngineException after logging, the exception is thrown.
     */
    public void throwLoggedException(String errorMessage) throws WootEngineException
    {
        this.throwLoggedException(errorMessage, null);
    }

    /**
     * Used to log exceptions as they are thrown.
     * 
     * @param errorMessage the message of this exception.
     * @param wrappedException the cause of this exception.
     * @throws WootEngineException after logging, the exception is thrown.
     */
    public void throwLoggedException(String errorMessage, Throwable wrappedException) throws WootEngineException
    {
        String prefixedErrorMessage = this.wootEngineId + " - " + errorMessage;
        this.logger.error(prefixedErrorMessage, wrappedException);
        throw new WootEngineException(prefixedErrorMessage + "\n", wrappedException);
    }

    /**
     * @return the wootEngineId of the WootEngine object. Primarily used for logging.
     */
    public String getWootEngineId()
    {
        return this.wootEngineId;
    }

    /**
     * @param wootEngineId the wootEngineId to set
     */
    public void setWootEngineId(String wootEngineId)
    {
        this.wootEngineId = wootEngineId;
    }
}
