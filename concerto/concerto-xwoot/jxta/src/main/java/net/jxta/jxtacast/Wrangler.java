package net.jxta.jxtacast;

import net.jxta.endpoint.Message;

/**
 * Interface for processing a transfered entity.
 * 
 * The data is split up and sent in blocks. Superclasses gather the data
 * blocks for a transfered entity as they come in, and notify the listener when
 * it is complete.
 * 
 * It's ok for blocks to arrive out of order, and ok for duplicate blocks to
 * arrive.
 */
public interface Wrangler {

	/**
	 * Process a transfer message.
	 */
	void processMsg(Message msg);

	/** Receive a regular 'maintenance' check-in from the TrailBoss thread. */
	void bossCheck();

	/**
	 * Send the specified block of data out over the wire.
	 * 
	 * @param blockNum
	 *            The block to send.
	 * @param msgType
	 *            Message type: MSG_FILE or MSG_OBJECT or MSG_FILE_REQ_RESP.
	 */
	void sendBlock(int blockNum, String msgType);

	/**
	 * @return the wrangler's unique key.
	 */
	String getKey();
}
