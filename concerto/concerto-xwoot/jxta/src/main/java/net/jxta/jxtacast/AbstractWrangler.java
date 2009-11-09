package net.jxta.jxtacast;

import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;

/**
 * Base class implementation.
 * 
 * @version $Id:$
 * @see Wrangler
 */
public abstract class AbstractWrangler implements Wrangler {

	protected String key; // Unique identifier for this file transfer.

	protected JxtaCast jc; // The parent JxtaCast obj.

	protected String sender; // Sender's peer name.
	protected String senderId; // Sender's peer ID.
	protected String filename;
	protected String caption = "";

	protected byte data[]; // The transferred data.
	protected int myBlockSize; // This data transfer block size.
	protected int totalBlocks; // Number of total blocks to transfer.

	protected long lastActivity; // Timestamp when the most recent message was processed.

	/**
	 * Process a file transfer message.
	 */
	public abstract void processMsg(Message msg);

	/** Receive a regular 'maintainence' check-in from the TrailBoss thread. */
	public abstract void bossCheck();

	/**
	 * Send the specified block of data out over the wire.
	 * 
	 * @param blockNum
	 *            The block to send.
	 * @param msgType
	 *            Message type: MSG_OBJECT or MSG_FILE_REQ_RESP.
	 */
	synchronized public void sendBlock(int blockNum, String msgType) {

		// Make sure it's a valid block.
		if (blockNum < 0 || blockNum >= totalBlocks)
			return;

		try {
			lastActivity = System.currentTimeMillis();

			// Create a message, fill it with our standard headers.
			Message msg = new Message();
			JxtaCast.setMsgString(msg, JxtaCast.MESSAGETYPE, msgType);
			JxtaCast
					.setMsgString(msg, JxtaCast.SENDERNAME, jc.myPeer.getName());
			JxtaCast.setMsgString(msg, JxtaCast.SENDERID, jc.myPeer.getPeerID()
					.toString());
			JxtaCast.setMsgString(msg, JxtaCast.VERSION, JxtaCast.version);
			JxtaCast.setMsgString(msg, JxtaCast.TRANSACTION_KEY, key);
			JxtaCast.setMsgString(msg, JxtaCast.FILENAME, filename);
			JxtaCast.setMsgString(msg, JxtaCast.FILESIZE, String
					.valueOf(data.length));

			// If we've got a caption, store it in the first message.
			if (blockNum == 0 && caption != null)
				JxtaCast.setMsgString(msg, JxtaCast.CAPTION, caption);

			// Place the block info in the message.
			JxtaCast.setMsgString(msg, JxtaCast.BLOCKNUM, String
					.valueOf(blockNum));
			JxtaCast.setMsgString(msg, JxtaCast.TOTALBLOCKS, String
					.valueOf(totalBlocks));
			JxtaCast.setMsgString(msg, JxtaCast.BLOCKSIZE, String
					.valueOf(myBlockSize));

			// Place the block of file data in the message.
			// If this is the last block, it's probably smaller than a full
			// block.
			//
			int bSize = myBlockSize;
			if (blockNum == totalBlocks - 1)
				bSize = data.length - (blockNum * myBlockSize);
			ByteArrayMessageElement elem = new ByteArrayMessageElement(
					JxtaCast.DATABLOCK, null, data, blockNum * myBlockSize,
					bSize, null);
			msg.replaceMessageElement(elem);

			// Send the message.
			JxtaCast.logMsg("Sending: " + filename + "  block: "
					+ (blockNum + 1) + "  of: " + totalBlocks);
			jc.sendMessage(msg);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getKey() {
		return this.key;
	}
}
