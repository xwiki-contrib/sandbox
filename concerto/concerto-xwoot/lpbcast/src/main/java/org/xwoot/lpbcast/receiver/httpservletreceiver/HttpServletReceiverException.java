package org.xwoot.lpbcast.receiver.httpservletreceiver;

import org.xwoot.lpbcast.receiver.ReceiverException;

/**
 * Exception handling for HttpServletReceiver.
 * 
 * @version $Id:$
 */
public class HttpServletReceiverException extends ReceiverException
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public HttpServletReceiverException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public HttpServletReceiverException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public HttpServletReceiverException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public HttpServletReceiverException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
