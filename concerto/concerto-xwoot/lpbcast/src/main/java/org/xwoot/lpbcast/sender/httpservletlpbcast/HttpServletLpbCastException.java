package org.xwoot.lpbcast.sender.httpservletlpbcast;

import org.xwoot.lpbcast.sender.SenderException;

/**
 * Exception handling for HttpServletLpbCast.
 * 
 * @version $Id:$
 */
public class HttpServletLpbCastException extends SenderException
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public HttpServletLpbCastException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(Throwable)
     */
    public HttpServletLpbCastException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public HttpServletLpbCastException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public HttpServletLpbCastException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
