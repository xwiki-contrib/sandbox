package net.jxta.jxtacast.input;

import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.jxtacast.AbstractWrangler;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.event.JxtaCastEvent;

/**
 * Common behavior for receiving entities.
 * 
 * @version $Id$
 * 
 */
public abstract class AbstractInputWrangler extends AbstractWrangler implements
		InputWrangler {

	/** Each slot is true when we've received the corresponding data block. */
	boolean blockIn[];

	/** Parallel to block array, ID of last peer to send an ACK for each block. */
	String lastAck[];

	/**
	 * Parallel to block array, true if we've asked the original sender for this
	 * block.
	 */
	boolean askedOrig[];

	/** Number of incoming blocks processed so far. */
	int blocksReceived;

	/**
	 * Method of requesting missing blocks: from original sender or from anyone.
	 */
	String reqLevel;

	/** Block to request if it's missing. */
	int currReqBlock;

	/** Timestamp when we last requested a missing block. */
	long lastReqTime;

	/** Timestamp when we received the first block message. */
	long firstBlockTime;

	/** Timestamp when we received the most recent block. */
	long latestBlockTime;

	/** Minimum time to wait with no activity before requesting a block. */
	long minTimeToWait;

	/** The type of messages this Input Wrangler handles. */
	String messageType;

	/**
	 * Constructor - Build a wrangler to process an incoming transfer.
	 * 
	 * The message used in the constructor doesn't have to be the first message
	 * in the sequence. Any will do. The message is not processed from the
	 * constructor, so be sure to call processMsg() as well.
	 */
	public AbstractInputWrangler(JxtaCast jc, Message msg) {

		this.jc = jc;
		lastActivity = System.currentTimeMillis();

		// Get some header data that we only need once.
		sender = JxtaCast.getMsgString(msg, JxtaCast.SENDERNAME);
		senderId = JxtaCast.getMsgString(msg, JxtaCast.SENDERID);
		key = JxtaCast.getMsgString(msg, JxtaCast.TRANSACTION_KEY);
		filename = JxtaCast.getMsgString(msg, JxtaCast.FILENAME);

		// Get info about the blocks.
		blocksReceived = 0;
		totalBlocks = Integer.parseInt(JxtaCast.getMsgString(msg,
				JxtaCast.TOTALBLOCKS));
		myBlockSize = Integer.parseInt(JxtaCast.getMsgString(msg,
				JxtaCast.BLOCKSIZE));

		// Allocate space to store the data. We also create an array
		// to check off the blocks as we process them, and a couple parallel
		// arrays
		// to track who to ask for missing blocks.
		//
		data = new byte[Integer.parseInt(JxtaCast.getMsgString(msg,
				JxtaCast.FILESIZE))];
		blockIn = new boolean[totalBlocks];
		lastAck = new String[totalBlocks];
		askedOrig = new boolean[totalBlocks];

		// Initialize tracking info for blocks to request.
		currReqBlock = 0;
		lastReqTime = System.currentTimeMillis();
		minTimeToWait = 4000;
	}

	/** {@inheritDoc} */
	public void processMsg(Message msg) {

		String msgType = JxtaCast.getMsgString(msg, JxtaCast.MESSAGETYPE);
		if (msgType.equals(this.messageType)
				|| msgType.equals(JxtaCast.MSG_REQ_RESP))
			processMsgTransfer(msg);
		else if (msgType.equals(JxtaCast.MSG_ACK))
			processMsgAck(msg);
		else if (msgType.equals(JxtaCast.MSG_REQ))
			processMsgReq(msg);
	}

	/** {@inheritDoc} */
	public void bossCheck() {

		// If this wrangler has been inactive for a long time, remove it from
		// JxtaCast's collection.
		if (System.currentTimeMillis() - lastActivity > jc.inWranglerLifetime) {
			jc.removeWrangler(key);
			JxtaCast.logMsg("Input Wrangler " + this.key + " removed for inactivity.");
		}

		// Calculate the time of inactivity that we'll endure before
		// requesting missing blocks. First determine the average amount
		// of time between blocks for the blocks we've received so far.
		// We'll wait either thrice that amount, or the time contained in
		// JxtaCast's timeTilReq member, whichever is shorter. Let's also
		// impose a minimum of a few seconds.
		//
		// This calculation should help us find an optimal time to wait,
		// based on current network conditions. We want to give any missing
		// blocks an adequate amount of time to reach us before we give up and
		// start requesting them. But this amount of time can be very different
		// depending on the network topology. It is very short when the sending
		// and receiving peers are on the same subnet. It can be long, over 30
		// seconds, if the peers are separated by an HTTP relay.
		//
		// The minimum time to wait starts out at a few seconds, and grows each
		// time we send a REQ, until a new file block comes in. Then it is
		// reset. This will keep a single peer from spewing out too many
		// requests.
		//
		long timeToWait = jc.timeTilReq;
		long avgTimeTweenBlocks;
		if (blocksReceived > 1) {
			avgTimeTweenBlocks = (latestBlockTime - firstBlockTime)
					/ blocksReceived;
			if ((avgTimeTweenBlocks * 3) < timeToWait)
				timeToWait = avgTimeTweenBlocks * 2;
			if (timeToWait < minTimeToWait)
				timeToWait = minTimeToWait;
		}

		// Are we missing any blocks? We'll request missing blocks, but don't
		// want to do it too often, or we'll queue up a bunch requests and then
		// receive a bunch of duplicate blocks.
		//
		if (blocksReceived < totalBlocks
				&& System.currentTimeMillis() - lastReqTime > timeToWait
				&& System.currentTimeMillis() - lastActivity > timeToWait) {
			requestNextMissingBlock();
		}
	}

	/**
	 * Process one incoming block of data.
	 */
	public void processMsgTransfer(Message msg) {

		lastActivity = System.currentTimeMillis();

		try {
			int blockNum = Integer.parseInt(JxtaCast.getMsgString(msg,
					JxtaCast.BLOCKNUM));
			// String msgSender = JxtaCast.getMsgString(msg,
			// JxtaCast.SENDERNAME);
			String msgSenderId = JxtaCast.getMsgString(msg, JxtaCast.SENDERID);

			// Have we already processed this block? If we've received a
			// duplicate message, ignore it.
			//
			if (blockIn[blockNum] == true) {
				// Log a msg, unless we were the sender.
				if (!msgSenderId.equals(jc.getMyPeer().getPeerID().toString()))
					;
				JxtaCast.logMsg("Duplicate block: " + filename + " block: "
						+ (blockNum + 1));
				return;
			}

			// Record some timestamps to be used later. bossCheck() uses these
			// to calculate an average time between blocks.
			latestBlockTime = lastActivity;
			if (blocksReceived == 0)
				firstBlockTime = lastActivity;
			minTimeToWait = 4000;

			// The caption is stored with the first block.
			if (blockNum == 0)
				caption = JxtaCast.getMsgString(msg, JxtaCast.CAPTION);

			JxtaCast.logMsg("From " + sender + " - " + " < " + filename
					+ " > block: " + (blockNum + 1) + " of " + totalBlocks);

			// Get the data block, place it in our data array.
			MessageElement elem = msg.getMessageElement(JxtaCast.DATABLOCK);
			if (elem == null)
				return;
			byte dataBlock[] = elem.getBytes(false);
			System.arraycopy(dataBlock, 0, data, blockNum * myBlockSize,
					dataBlock.length);

			// Record that we've processed this block.
			blockIn[blockNum] = true;
			blocksReceived++;

			// Acknowledge receipt of the block, so the sender will send the
			// next.
			// This also serves to notify other peers that we have received this
			// block;
			// they may request it from us.
			//
			sendAck(msg);

			// If this was a response to a missing block request, ask for the
			// next one.
			//
			if (JxtaCast.getMsgString(msg, JxtaCast.MESSAGETYPE).equals(
					JxtaCast.MSG_REQ_RESP)) {

				// The REQ_RESP may not have been in response to a request from
				// this peer. But assume that if one peer is already requesting
				// dropped block messages, we should be too.
				//
				requestNextMissingBlock();
			}

			// Notify listeners of transfer progress.
			JxtaCastEvent e = new JxtaCastEvent();
			e.transType = JxtaCastEvent.RECV;
			e.filename = new String(filename);
			e.filepath = new String(jc.fileSaveLoc);
			e.senderId = new String(senderId);

			if (sender == null)
				sender = "<anonymous>";
			e.sender = new String(sender);

			if (caption != null)
				e.caption = new String(caption);

			e.percentDone = ((float) blocksReceived / totalBlocks) * 100;

			// Are we done?
			if (blocksReceived == totalBlocks) {
				doTransferCompleted(e);
			}

			jc.sendJxtaCastEvent(e);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	public void sendAck(Message msg) {

		try {
			// Create and send an ACK message.
			Message ackMsg = new Message();
			JxtaCast.setMsgString(ackMsg, JxtaCast.MESSAGETYPE,
					JxtaCast.MSG_ACK);
			JxtaCast.setMsgString(ackMsg, JxtaCast.SENDERNAME, jc.getMyPeer()
					.getName());
			JxtaCast.setMsgString(ackMsg, JxtaCast.SENDERID, jc.getMyPeer()
					.getPeerID().toString());
			JxtaCast.setMsgString(ackMsg, JxtaCast.VERSION, JxtaCast.version);
			JxtaCast.setMsgString(ackMsg, JxtaCast.TRANSACTION_KEY, JxtaCast
					.getMsgString(msg, JxtaCast.TRANSACTION_KEY));
			JxtaCast.setMsgString(ackMsg, JxtaCast.FILENAME, filename);
			JxtaCast.setMsgString(ackMsg, JxtaCast.BLOCKNUM, JxtaCast
					.getMsgString(msg, JxtaCast.BLOCKNUM));

			// Send the ACK message.
			int blockNum = Integer.parseInt(JxtaCast.getMsgString(msg,
					JxtaCast.BLOCKNUM));
			JxtaCast.logMsg("Sending ACK: " + filename + "  block "
					+ (blockNum + 1));
			jc.sendMessage(ackMsg);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	public void sendReq(int blockNum) {

		// Increase the wait time every time we send a request. It's reset
		// when we get a response. The longer we go without getting a response,
		// the less often we'll send requests.
		minTimeToWait += 4000;

		try {
			// Create a message, fill it with key info, and the block number.
			Message reqMsg = new Message();
			JxtaCast.setMsgString(reqMsg, JxtaCast.MESSAGETYPE,
					JxtaCast.MSG_REQ);
			JxtaCast.setMsgString(reqMsg, JxtaCast.SENDERNAME, jc.getMyPeer()
					.getName());
			JxtaCast.setMsgString(reqMsg, JxtaCast.SENDERID, jc.getMyPeer()
					.getPeerID().toString());
			JxtaCast.setMsgString(reqMsg, JxtaCast.VERSION, JxtaCast.version);
			JxtaCast.setMsgString(reqMsg, JxtaCast.TRANSACTION_KEY, key);
			JxtaCast.setMsgString(reqMsg, JxtaCast.FILENAME, filename);
			JxtaCast.setMsgString(reqMsg, JxtaCast.BLOCKNUM, String
					.valueOf(blockNum));

			// Who are we requesting it from?
			// If we've gotten an ACK for this block, ask that peer. Then clear
			// him from the lastAck array, so we don't ask the same peer again.
			//
			String reqTo = "last ACK";
			if (lastAck[blockNum] != null) {
				JxtaCast.setMsgString(reqMsg, JxtaCast.REQTOPEER,
						lastAck[blockNum]);
				lastAck[blockNum] = null;

			} else if (!askedOrig[blockNum]) {

				// We haven't asked the original sender for this block yet, so
				// ask him now.
				JxtaCast.setMsgString(reqMsg, JxtaCast.REQTOPEER, senderId);
				askedOrig[blockNum] = true;
				reqTo = "orig sender";

			} else {

				// Ask any peer to respond.
				JxtaCast.setMsgString(reqMsg, JxtaCast.REQTOPEER,
						JxtaCast.REQ_ANYPEER);
				reqTo = "ANYONE!";
			}

			// Send the REQ message. It'd be nice to send this through a
			// "back channel" unicast pipe directly to the peer we're requesting
			// from. For now we'll send it out over the wire. Everyone else
			// can just ignore it.
			//
			JxtaCast.logMsg("Sending REQ to " + reqTo + ": " + filename
					+ "  block " + (blockNum + 1));
			jc.sendMessage(reqMsg);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	public void requestNextMissingBlock() {

		// Find and request the next missing block. We just request one block.
		// We'll request the next after it comes in, or when the TrailBoss
		// triggers
		// the next bossCheck().
		//
		while (currReqBlock < blockIn.length) {

			if (blockIn[currReqBlock] == false) {
				sendReq(currReqBlock);
				currReqBlock++;
				lastReqTime = System.currentTimeMillis();
				break;
			}

			currReqBlock++;
		}

		// If we've reached the end of the array, start over.
		//
		if (currReqBlock == blockIn.length)
			currReqBlock = 0;
	}

	/** {@inheritDoc} */
	public void processMsgAck(Message msg) {

		// Ignore the ACK if it's from us.
		String senderId = JxtaCast.getMsgString(msg, JxtaCast.SENDERID);
		if (senderId.equals(jc.getMyPeer().getPeerID().toString()))
			return;

		int blockNum = Integer.parseInt(JxtaCast.getMsgString(msg,
				JxtaCast.BLOCKNUM));
		lastAck[blockNum] = JxtaCast.getMsgString(msg, JxtaCast.SENDERID);

		JxtaCast.logMsg("Received ACK: " + filename + "  block "
				+ (blockNum + 1) + ", from "
				+ JxtaCast.getMsgString(msg, JxtaCast.SENDERNAME));
	}

	/** {@inheritDoc} */
	public void processMsgReq(Message msg) {

		// If it's not addressed to us, or to "any peer", bail out.
		String reqToPeer = JxtaCast.getMsgString(msg, JxtaCast.REQTOPEER);
		if (reqToPeer == null)
			return;
		if (!reqToPeer.equals(jc.getMyPeer().getPeerID().toString())
				&& !reqToPeer.equals(JxtaCast.REQ_ANYPEER))
			return;

		// If it's FROM us, bail out. (It's an any peer req that we sent.)
		String reqSender = JxtaCast.getMsgString(msg, JxtaCast.SENDERID);
		if (reqSender == null)
			return;
		if (reqSender.equals(jc.getMyPeer().getPeerID().toString()))
			return;

		int blockNum = Integer.parseInt(JxtaCast.getMsgString(msg,
				JxtaCast.BLOCKNUM));

		if (reqToPeer.equals(JxtaCast.REQ_ANYPEER))
			JxtaCast.logMsg("Received REQ_ANYPEER: " + filename + "  block "
					+ (blockNum + 1) + ", from "
					+ JxtaCast.getMsgString(msg, JxtaCast.SENDERNAME));
		else
			JxtaCast.logMsg("Received FILE_REQ: " + filename + "  block "
					+ (blockNum + 1) + ", from "
					+ JxtaCast.getMsgString(msg, JxtaCast.SENDERNAME));

		// Send the block, if we actually do have it.
		if (blockNum > 0 && blockNum < blockIn.length
				&& blockIn[blockNum] == true)
			sendBlock(blockNum, JxtaCast.MSG_REQ_RESP);
	}
}
