package org.xwoot.lpbcast.neighbors;

import org.xwoot.lpbcast.LpbCastException;

/**
 * Exception handling for Neighbors.
 * 
 * @version $Id:$
 */
public class NeighborsException extends LpbCastException
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public NeighborsException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(Throwable)
     */
    public NeighborsException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public NeighborsException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public NeighborsException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
