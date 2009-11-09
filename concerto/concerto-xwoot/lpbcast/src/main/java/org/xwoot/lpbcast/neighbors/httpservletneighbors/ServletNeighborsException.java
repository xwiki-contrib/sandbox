package org.xwoot.lpbcast.neighbors.httpservletneighbors;

import org.xwoot.lpbcast.neighbors.NeighborsException;

/**
 * Exception handling for ServletNeighbors.
 * 
 * @version $Id$
 */
public class ServletNeighborsException extends NeighborsException
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public ServletNeighborsException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(Throwable)
     */
    public ServletNeighborsException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public ServletNeighborsException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public ServletNeighborsException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
