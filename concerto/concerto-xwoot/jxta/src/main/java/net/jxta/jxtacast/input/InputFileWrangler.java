package net.jxta.jxtacast.input;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import net.jxta.endpoint.Message;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.event.JxtaCastEvent;

/**
 * Class for receiving a file.
 * 
 * @version $Id:$
 * @see InputWrangler
 */
public class InputFileWrangler extends AbstractInputWrangler {

	/**
	 * Calls super and sets the messageType.
	 * 
	 * @see AbstractInputWrangler
	 */
	public InputFileWrangler(JxtaCast jc, Message msg) {
		super(jc, msg);

		this.messageType = JxtaCast.MSG_FILE;
	}

	/** {@inheritDoc} */
	public void doTransferCompleted(JxtaCastEvent event) {
		writeFile();
	}

	/**
	 * Write the file data to a disk file.
	 */
	private void writeFile() {

		JxtaCast.logMsg("*** WRITING FILE ***   " + jc.fileSaveLoc + filename);

		try {
			FileOutputStream fos = new FileOutputStream(jc.fileSaveLoc
					+ filename);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(data, 0, data.length);
			bos.flush();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
