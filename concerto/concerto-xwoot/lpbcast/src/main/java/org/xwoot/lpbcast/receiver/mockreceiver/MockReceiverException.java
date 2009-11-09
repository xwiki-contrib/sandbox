package org.xwoot.lpbcast.receiver.mockreceiver;

import org.xwoot.lpbcast.receiver.ReceiverException;

/**
 * Exception handling for MockReceiver.
 * 
 * @version $Id:$
 */
public class MockReceiverException extends ReceiverException
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public MockReceiverException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(Throwable)
     */
    public MockReceiverException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public MockReceiverException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public MockReceiverException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
