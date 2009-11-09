package org.xwoot.lpbcast.receiver;

import org.xwoot.lpbcast.LpbCastException;

/**
 * Exception handling for Receiver.
 * 
 * @version $Id:$
 */
public class ReceiverException extends LpbCastException
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public ReceiverException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(Throwable)
     */
    public ReceiverException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public ReceiverException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public ReceiverException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
