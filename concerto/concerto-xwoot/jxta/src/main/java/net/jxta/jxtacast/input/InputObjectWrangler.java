package net.jxta.jxtacast.input;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import net.jxta.endpoint.Message;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.event.JxtaCastEvent;

/**
 * Class for receiving an object.
 *
 * @version $Id$
 * @see InputWrangler
 */
public class InputObjectWrangler extends AbstractInputWrangler {

    /**
     * Constructor - Build a wrangler to process an incoming object.
     *
     * The message used in the constructor doesn't have to be the first
     * message in the sequence.  Any will do.  The message is not
     * processed from the constructor, so be sure to call processMsg() as
     * well.
     */
    public InputObjectWrangler(JxtaCast jc, Message msg) {
    	super(jc, msg);
    	
    	this.messageType = JxtaCast.MSG_OBJECT;
    }
    
    
    public void doTransferCompleted(JxtaCastEvent event) {
    	event.transferedData = reassembleObject(data);
    }


    /**
     * Reassemble a received object.
     * 
     * @param objectData the received raw object data.
     * @return the received object ready to use by listeners.
     */
    private Object reassembleObject(byte[] objectData) {
    	Object result = null;
    	
    	ByteArrayInputStream bais = null;
    	ObjectInputStream ois = null;
    	
    	try {
	    	bais = new ByteArrayInputStream(objectData);
	    	ois = new ObjectInputStream(bais);
	    	
	    	result = ois.readObject();
	    	
	    	JxtaCast.logMsg("Successfuly reassembled " + objectData.length + " bytes into an object of type " + result.getClass());
    	} catch (Exception e) {
    		JxtaCast.logMsg("Failed to reassemble the received message.");
    		e.printStackTrace();
    	} finally {
    		try {
	    		if (ois != null) {
	    			ois.close();
	    		}
	    		if (bais != null) {
	    			bais.close();
	    		}
    		} catch (Exception e) {
    			JxtaCast.logMsg("Failed to close input streams after reassembling the received message.");
    			e.printStackTrace();
    		}
    	}
    	
    	return result;
    }
}
