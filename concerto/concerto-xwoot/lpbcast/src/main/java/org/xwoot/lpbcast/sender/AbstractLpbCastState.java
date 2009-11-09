package org.xwoot.lpbcast.sender;

import java.security.InvalidParameterException;
import java.util.Collection;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.neighbors.Neighbors;
import org.xwoot.lpbcast.sender.httpservletlpbcast.HttpServletLpbCast;
import org.xwoot.lpbcast.sender.httpservletlpbcast.HttpServletLpbCastException;

/**
 * Abstract implementation of the state of an HttpServletLpbCast sender. Part of the methods described by
 * {@link LpbCastAPI} are delegated to the owning {@link HttpServletLpbCast} and the rest are up to the subclasses to
 * implement, for they are sensible to the state of the sender.
 * 
 * @version $Id$
 */
public abstract class AbstractLpbCastState implements LpbCastAPI
{
    /** The connection instance that this state belongs to. */
    protected final HttpServletLpbCast connection;

    /**
     * Creates a new state instance.
     * 
     * @param connection the connection instance that this state belongs to.
     * @throws InvalidParameterException if the connection parameter is null.
     */
    public AbstractLpbCastState(HttpServletLpbCast connection)
    {
        if (connection == null) {
            throw new InvalidParameterException("The provided parameter must not be null.");
        }
        this.connection = connection;
    }

    /** {@inheritDoc} */
    public Neighbors getNeighbors()
    {
        return this.connection.getNeighbors();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Collection getNeighborsList() throws HttpServletLpbCastException
    {
        return this.connection.getNeighborsList();
    }

    /** {@inheritDoc} */
    public void removeNeighbor(Object neighbor) throws HttpServletLpbCastException
    {
        this.connection.removeNeighbor(neighbor);
    }

    /** {@inheritDoc} */
    public Message getNewMessage(Object creatorPeerId, Object content, int action, int round)
    {
        return this.connection.getNewMessage(creatorPeerId, content, action, round);
    }

    /** {@inheritDoc} */
    public int getRound()
    {
        return this.connection.getRound();
    }

    /** {@inheritDoc} */
    public void clearWorkingDir()
    {
        this.connection.clearWorkingDir();
    }
}
