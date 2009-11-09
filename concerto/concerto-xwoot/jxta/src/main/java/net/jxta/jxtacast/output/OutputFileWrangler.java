package net.jxta.jxtacast.output;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import net.jxta.jxtacast.JxtaCast;

/**
 * Class for sending a file.
 * 
 * Files are split up and sent in blocks of data. This class loads the file into
 * memory, and sends out data blocks. It re-send blocks in response to requests
 * from other peers.
 * 
 */
public class OutputFileWrangler extends AbstractOutputWrangler {

	File file; // The file we want to send.

	/**
	 * Constructor - Build a wrangler to process an outgoing file.
	 */
	public OutputFileWrangler(JxtaCast jc, File file, String caption) {
		super(jc);

		this.file = file;
		this.filename = file.getName();
		this.caption = caption;

		this.key = composeKey(senderId, filename);

		this.messageType = JxtaCast.MSG_FILE;
	}

	/** {@inheritDoc} */
	public void readData() {

		// Allocate space to store the file data.
		data = new byte[(int) file.length()];

		// Read the file into memory.
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(data, 0, data.length);
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	/** {@inheritDoc} */
	public String composeKey(String senderId, Object filename) {

		// The key is a combination of the sender's PeerId, the filename,
		// and a timestamp.
		String keyStr = senderId + "+" + filename + "+"
				+ String.valueOf(System.currentTimeMillis());

		return keyStr;
	}
}
