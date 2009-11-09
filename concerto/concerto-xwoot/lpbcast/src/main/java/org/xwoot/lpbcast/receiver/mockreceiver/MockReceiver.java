package org.xwoot.lpbcast.receiver.mockreceiver;

import java.io.File;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.receiver.ReceiverApi;

/**
 * A Mockup for the ReceiverAPI.
 * 
 * @version $Id$
 */
public class MockReceiver implements ReceiverApi
{

    /** {@inheritDoc} */
    public void connectReceiver()
    {
        //void
    }

    /** {@inheritDoc} */
    public void disconnectReceiver()
    {
      //void
    }

    /** {@inheritDoc} */
    public Object getPeerId()
    {
        return null;
    }

    /** {@inheritDoc} */
    public boolean isReceiverConnected()
    {
        return false;
    }

    /** {@inheritDoc} */
    public void receive(Message message)
    {
      //void
    }

    /** {@inheritDoc} */
    public File askState()
    {
        return null;
    }
}
