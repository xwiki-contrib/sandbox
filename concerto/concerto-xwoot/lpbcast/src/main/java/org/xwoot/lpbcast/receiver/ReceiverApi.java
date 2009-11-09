package org.xwoot.lpbcast.receiver;

import org.xwoot.lpbcast.message.Message;

/**
 * Defines the functionality of a Receiver in the P2P Network.
 * 
 * @version $Id:$
 */
public interface ReceiverApi
{
    /**
     * (Re)Connects this receiver to the P2P network.
     * 
     * @throws Exception if problems occur.
     */
    void connectReceiver() throws Exception;

    /**
     * Disconnects this receiver from the P2P network.
     * 
     * @throws Exception if problems occur.
     */
    void disconnectReceiver() throws Exception;

    /**
     * @return the siteId of this receiver.
     */
    Object getPeerId();

    /**
     * @return true if the receiver is connected to the P2P network.
     */
    boolean isReceiverConnected();

    /**
     * Receive a message from the P2P network.
     * 
     * @param message the message to receive.
     * @throws ReceiverException if problems receiving the message occur.
     */
    void receive(Message message) throws ReceiverException;
}
