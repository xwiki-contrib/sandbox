package org.xwoot.lpbcast.sender.httpservletlpbcast;

import java.io.File;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.sender.AbstractLpbCastState;

/**
 * Implements the disconnected state of a sender.
 * <p>
 * Almost, if not all, methods do nothing because these operations can not be performed while disconnected from the P2P
 * network but instead they are logged.
 * 
 * @version $Id:$
 */
public class HttpServletLpbCastStateDisconnected extends AbstractLpbCastState
{

    /**
     * Creates a new instance.
     * 
     * @param connection the connection instance that this state belongs to.
     */
    public HttpServletLpbCastStateDisconnected(HttpServletLpbCast connection)
    {
        super(connection);
    }

    /** {@inheritDoc} */
    public void connectSender()
    {
        // connect somehow ...

        // finally set connected state of the connection instance
        this.connection.setState(this.connection.connectedState);
    }

    /** {@inheritDoc} */
    public void disconnectSender()
    {
        throw new IllegalStateException(this.connection.getSiteId() + " - Already disconnected");
    }

    /** {@inheritDoc} */
    public boolean isSenderConnected()
    {
        return false;
    }

    /** {@inheritDoc} */
    public boolean addNeighbor(Object from, Object neighbor)
    {
        this.connection.logger.info(from + " Add neighbor when sender is disconnected -- return false.");
        return false;
    }

    /** {@inheritDoc} */
    public void gossip(Message message)
    {
        this.connection.logger.info(this.connection.getSiteId()
            + " - Try to send message when sender is disconnected -- send nothing.");
    }

    /** {@inheritDoc} */
    public void processSendState(HttpServletResponse response, File state)
    {
        this.connection.logger.info(this.connection.getSiteId()
            + " - Try to send state when sender is disconnected -- send nothing.");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void processSendAE(HttpServletResponse response, Collection diff)
    {
        this.connection.logger.info(this.connection.getSiteId()
            + " - Try to send anti entropy diff when sender is disconnected -- send nothing.");
    }

    /** {@inheritDoc} */
    public void sendTo(Object peerId, Object toSend)
    {
        this.connection.logger.info(this.connection.getSiteId() + " - Try to send message to -- " + peerId
            + " -- when sender is disconnected -- send nothing.");
    }
}
