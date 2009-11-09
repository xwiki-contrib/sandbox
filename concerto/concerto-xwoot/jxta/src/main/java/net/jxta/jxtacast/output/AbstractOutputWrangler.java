package net.jxta.jxtacast.output;

import net.jxta.endpoint.Message;
import net.jxta.jxtacast.AbstractWrangler;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.event.JxtaCastEvent;

/**
 * Abstract output wrangler implementation.
 * 
 * @version $Id$
 */
public abstract class AbstractOutputWrangler extends AbstractWrangler implements
		OutputWrangler {

	int blocksSent; // Number of outgoing blocks processed so far.
	String messageType; // Type of messages handled by this wrangler. Fill this

	// in constructor.

	public AbstractOutputWrangler(JxtaCast jc) {
		this.jc = jc;
		this.lastActivity = System.currentTimeMillis();

		// Get some header data that we only need once.
		sender = jc.getMyPeer().getName();
		senderId = jc.getMyPeer().getPeerID().toString();
	}

	/** {@inheritDoc} */
	public void processMsg(Message msg) {

		lastActivity = System.currentTimeMillis();

		// Since this is an output wrangler, we ignore messages of MSG_FILE and
		// MSG_OBJECT.
		// They came from us! Respond to ACK and REQ messages from peers
		// that are receiving this entity from us.
		//
		String msgType = JxtaCast.getMsgString(msg, JxtaCast.MESSAGETYPE);
		if (msgType.equals(JxtaCast.MSG_ACK))
			processMsgAck(msg);
		else if (msgType.equals(JxtaCast.MSG_REQ))
			processMsgReq(msg);
	}

	/** {@inheritDoc} **/
	public void bossCheck() {

		// If there's been no activity since our last check-in, and we still
		// have
		// blocks to send, send the next one now. (But make sure we've sent at
		// least one block. If we haven't, then the other thread hasn't gotten
		// thru the sendFile() function yet, and the wrangler is not
		// initialized.)
		//
		if (blocksSent > 0
				&& blocksSent < totalBlocks
				&& System.currentTimeMillis() - lastActivity > jc.trailBossPeriod + 500) {
			JxtaCast.logMsg("bossCheck sending block for wrangler " + this.key + "." );
			sendBlock(blocksSent++, this.messageType);
			updateProgress();
		}

		// If this wrangler has been inactive for a long time, remove it from
		// JxtaCast's collection.
		if (System.currentTimeMillis() - lastActivity > jc.outWranglerLifetime) {
			jc.removeWrangler(key);
			JxtaCast.logMsg("Output Wrangler " + this.key + " removed for inactivity.");
		}
	}

	/** {@inheritDoc} */
	public void processMsgAck(Message msg) {

		int blockNum = Integer.parseInt(JxtaCast.getMsgString(msg,
				JxtaCast.BLOCKNUM));

		JxtaCast.logMsg("Received ACK: " + filename + "  block "
				+ (blockNum + 1) + ", from "
				+ JxtaCast.getMsgString(msg, JxtaCast.SENDERNAME));

		// If there are more blocks to send, send the next one now.
		int nextBlock = blockNum + 1;
		if (nextBlock == blocksSent && nextBlock < totalBlocks) {
			blocksSent++;
			sendBlock(nextBlock, this.messageType);
			updateProgress();
		}
	}

	/** {@inheritDoc} */
	public void processMsgReq(Message msg) {

		int blockNum = Integer.parseInt(JxtaCast.getMsgString(msg,
				JxtaCast.BLOCKNUM));

		// If this is a request for a block we haven't sent yet, send the
		// next block as a normal MSG_FILE/MSG_OBJECT message, instead of as a
		// REQ_RESP
		// (request response). We want to keep to the "push" protocol of
		// MSG_FILE/MSG_OBJECT/MSG_FILE_ACK messages until we've sent all the
		// blocks
		// one time. The peers will use the "pull" REQ/REQ_RESP protocol
		// to fill in their missing blocks.
		//
		if (blockNum >= blocksSent) {
			JxtaCast.logMsg("Received "
					+ JxtaCast.getMsgString(msg, JxtaCast.MESSAGETYPE) + ": "
					+ filename + "  block " + (blockNum + 1) + ", from "
					+ JxtaCast.getMsgString(msg, JxtaCast.SENDERNAME));
			sendBlock(blocksSent++, this.messageType);
			updateProgress();
			return;
		}

		// Send out the block, but only if the request was addressed to us or
		// to "any peer".
		String reqToPeer = JxtaCast.getMsgString(msg, JxtaCast.REQTOPEER);
		if (reqToPeer.equals(jc.getMyPeer().getPeerID().toString())
				|| reqToPeer.equals(JxtaCast.REQ_ANYPEER)) {

			if (reqToPeer.equals(JxtaCast.REQ_ANYPEER))
				JxtaCast.logMsg("Received REQ_ANYPEER: " + filename
						+ "  block " + (blockNum + 1) + ", from "
						+ JxtaCast.getMsgString(msg, JxtaCast.SENDERNAME));
			else
				JxtaCast.logMsg("Received FILE_REQ: " + filename + "  block "
						+ (blockNum + 1) + ", from "
						+ JxtaCast.getMsgString(msg, JxtaCast.SENDERNAME));
			sendBlock(blockNum, JxtaCast.MSG_REQ_RESP);
		}
	}

	/** {@inheritDoc} */
	public void updateProgress() {

		// Notify listeners of transfer progress.
		JxtaCastEvent e = new JxtaCastEvent();
		e.transType = JxtaCastEvent.SEND;
		e.filename = new String(filename);
		e.filepath = new String(jc.fileSaveLoc);
		e.sender = new String(sender);
		e.senderId = new String(senderId);

		if (caption != null)
			e.caption = new String(caption);

		e.percentDone = ((float) blocksSent / totalBlocks) * 100;
		jc.sendJxtaCastEvent(e);
	}

	/** {@inheritDoc} */
	public void initTransferMetadata() {
		blocksSent = 0;

		myBlockSize = jc.outBlockSize;
		int lastBlockSize = (int) data.length % myBlockSize;

		totalBlocks = (int) data.length / myBlockSize;
		if (lastBlockSize != 0)
			totalBlocks++;
	}

	/** {@inheritDoc} */
	public synchronized void send() {
		// Read the data of the entity we want to send.
		try {
			readData();
		} catch (Exception e) {
			JxtaCast.logMsg("Unable to read the data to send. Send canceled.");
			e.printStackTrace();
			return;
		}

		// Initialize transfer metadata.
		initTransferMetadata();

		// Send out the first block;
		sendBlock(blocksSent++, this.messageType);
		updateProgress();
	}
}
