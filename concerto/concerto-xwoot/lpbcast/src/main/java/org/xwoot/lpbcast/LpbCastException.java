package org.xwoot.lpbcast;

/**
 * Exception handling for LPBcast module.
 * 
 * @version $Id:$
 */
public class LpbCastException extends Exception
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public LpbCastException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(Throwable)
     */
    public LpbCastException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public LpbCastException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public LpbCastException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
