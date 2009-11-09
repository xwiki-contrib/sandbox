package net.jxta.jxtacast.input;

import net.jxta.endpoint.Message;
import net.jxta.jxtacast.Wrangler;
import net.jxta.jxtacast.event.JxtaCastEvent;

/**
 * Interface for receiving an entity.
 * 
 * The data is  split up and sent in blocks. This class gathers the data
 * blocks as they come in, and calls {@link #doTransferCompleted(JxtaCastEvent)} it is
 * complete.
 * 
 * It's ok for blocks to arrive out of order, and ok for duplicate blocks to
 * arrive. The wrangler can send out requests for missing blocks, and also
 * provide blocks for other peers that are missing them.
 * 
 * @version $Id:$
 *
 */
public interface InputWrangler extends Wrangler {

	/** Process a file transfer message. */
	void processMsg(Message msg);

	/** Receive a regular 'maintainence' check-in from the TrailBoss. */
	void bossCheck();

	/** Process one incoming block of data. */
	void processMsgTransfer(Message msg);

	/**
	 * Do any operation required to delivering the received data to who
	 * requested it.
	 * 
	 * @param event
	 *            the event that will be sent to listeners after this method
	 *            returns signaling that this transfer has finished
	 *            (event.percentDone == 100). Implementer could embed the
	 *            transfered data here or point to where it was saved.
	 **/
	void doTransferCompleted(JxtaCastEvent event);

	/** Send an acknowledgment that we've received a data block. */
	void sendAck(Message msg);

	/**
	 * Send a request for specific data block.
	 * 
	 * @param blockNum
	 *            the block to request.
	 */
	void sendReq(int blockNum);

	/**
	 * Request the next missing block of data. We request the block from
	 * the latest peer known to have received that block. If none are known, or
	 * we've already requested from that peer and not gotten a response, we
	 * request from the original sender. If we've already done that, then we 
	 * request from anyone. (We hope not to have to do that, since it may result
	 * in many peers responding at once with the same data block.)
	 */
	void requestNextMissingBlock();

	/**
	 * Process a transfer ACK message.
	 * 
	 * Peers will send out an ACK message when they've received a block. We'll
	 * keep track of the latest peer that sent an ACK for each block. If we
	 * don't get that block ourselves, we can request it from a peer that has
	 * it.
	 */
	void processMsgAck(Message msg);

	/**
	 * Process a transfer REQ message.
	 * 
	 * A peer has requested a data block. If the request is addressed
	 * to us, or to any peer, send the block out as a REQ_RESP 'request
	 * response' message.
	 */
	void processMsgReq(Message msg);
}
