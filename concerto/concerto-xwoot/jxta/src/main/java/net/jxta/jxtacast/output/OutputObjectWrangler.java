package net.jxta.jxtacast.output;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import net.jxta.jxtacast.JxtaCast;

/**
 * Class for sending a file.
 * 
 * Files are split up and sent in blocks of data. This class loads the file into
 * memory, and sends out data blocks. It re-send blocks in response to requests
 * from other peers.
 * 
 */
public class OutputObjectWrangler extends AbstractOutputWrangler {

	Object object;
	int blocksSent; // Number of outgoing blocks processed so far.

	/**
	 * Constructor - Build a wrangler to process an outgoing file.
	 * 
	 */
	public OutputObjectWrangler(JxtaCast jc, Object object, String caption) {
		super(jc);

		this.object = object;
		filename = object.getClass().getName();
		this.caption = caption;

		key = composeKey(senderId, this.object);

		this.messageType = JxtaCast.MSG_OBJECT;
	}

	/** {@inheritDoc} */
	public void readData() throws Exception {
		this.data = dissasembleObject(object);

		// if dissasembleObject failed, cancel object transfer.
		if (data == null) {
			throw new Exception("Failed to read data.");
		}
	}

	private byte[] dissasembleObject(Object object) {
		byte[] result = null;

		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;

		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			result = baos.toByteArray();
			
			JxtaCast.logMsg("Successfuly dissassembled an object of type " + result.getClass() + " into " + result.length + " bytes.");
		} catch (Exception e) {
			JxtaCast
					.logMsg("Unable to disassemble an object into a byte array.");
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
				if (baos != null) {
					baos.close();
				}
			} catch (Exception e) {
				JxtaCast
						.logMsg("Unable to close output streams after disassembling an object into a byte array.");
				e.printStackTrace();
			}
		}

		return result;
	}

	/** {@inheritDoc} */
	public String composeKey(String senderId, Object object) {
		// The key is a combination of the sender's PeerId, the object's class name,
		// the object's hashcode and a timestamp.
	    // FIXME: switch to a simple uuid?
		String keyStr = senderId + "+" + object.getClass().getName() + "+" + object.hashCode() + "+"
				+ String.valueOf(System.currentTimeMillis());

		return keyStr;
	}
}
