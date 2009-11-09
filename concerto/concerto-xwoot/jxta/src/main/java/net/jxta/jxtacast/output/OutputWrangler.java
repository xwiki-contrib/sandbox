package net.jxta.jxtacast.output;

import net.jxta.endpoint.Message;
import net.jxta.jxtacast.Wrangler;

public interface OutputWrangler extends Wrangler {

	/**
	 * Process a transfer ACK message.
	 * 
	 * Peers will send us an ACK message when they've received a block. When we
	 * get one (from any peer), for the last block we've sent, then we can send
	 * the next block.
	 */
	void processMsgAck(Message msg);

	/**
	 * Process a transfer REQ message.
	 * 
	 * A peer has requested a block of this entity. Send it out as a REQ_RESP
	 * 'request response' message.
	 */
	void processMsgReq(Message msg);

	/**
	 * Start the transfer process.
	 * 
	 * We read the entity into memory here, instead of in the constructor, so
	 * that the operation will take place in the desired thread. (See the
	 * JxtaCast.send*() methods.)
	 * 
	 * We'll send out the entity's first data block. Additional blocks will be
	 * sent in response to acknowledgment messages from the peers, or in
	 * response to bossCheck() calls from the TrailBoss (whichever comes
	 * faster).
	 * 
	 * Why? Because if we tried to send all the blocks at once, we'd overload
	 * the capabilities of the propagate pipes, and lots of messages would be
	 * dropped. So we use the ACK in order to send blocks at a rate that can be
	 * managed.
	 */
	void send();

	/** Notify listeners of our transmission progress. */
	void updateProgress();

	/**
	 * Read the entity's data into memory.
	 * 
	 * @throws Exception
	 *             if problems occurred while reading the data.
	 **/
	void readData() throws Exception;

	/** Initialize info about blocks by looking at the data to send. */
	void initTransferMetadata();
	
	/** Create a unique key for this transfer. */
	String composeKey(String senderId, Object extraData);
}
