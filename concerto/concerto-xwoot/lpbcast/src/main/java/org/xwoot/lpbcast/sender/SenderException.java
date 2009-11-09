package org.xwoot.lpbcast.sender;

import org.xwoot.lpbcast.LpbCastException;

/**
 * Exception handling for Sender.
 * 
 * @version $Id:$
 */
public class SenderException extends LpbCastException
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public SenderException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(Throwable)
     */
    public SenderException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public SenderException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public SenderException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
