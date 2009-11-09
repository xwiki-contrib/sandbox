/*
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id$
 *
 */

/*****************************************************************************
*
* JxtaCast release history
*
* Version numbers below correspond to the JxtaCast.version string, not to the
* CVS check-in ID.
*
* 1.00  03/18/02  Beta release.  The class was named "FileCast", and was a
*                 part of the PicShare demo.  There was an unversioned alpha
*                 on 03/09/02.
* 1.01  03/20/02  Don't show "duplicate block" message if we were the
*                 ones that sent the block.  Create pipes AFTER advs have
*                 been published.
* 1.02  03/23/02  Use "average time between blocks" calculation to determine
*                 wait time before requesting missing blocks.
*                 Shorten inactive lifetime of input wranglers to 5 minutes.
*                 Put sending peer name in more log messages.
* 1.03  04/07/02  Change name to JxtaCast, change package location.
* 1.04  10/11/02  Migrate from deprecated Message and PipeService methods.
* 2.00  04/07/03  Tested with JXTA 2.0 platform: JXTA_2_0_Stable_20030301.
*                 Bumped default block size to 12kb.  Tightened time between
*                 BossCheck checks, from 2 seconds to 1.
*
*****************************************************************************/

package net.jxta.jxtacast;

import java.io.*;
import java.util.*;

import net.jxta.discovery.*;
import net.jxta.document.*;
import net.jxta.endpoint.*;
import net.jxta.id.*;
import net.jxta.jxtacast.event.JxtaCastEvent;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.jxtacast.input.InputFileWrangler;
import net.jxta.jxtacast.input.InputObjectWrangler;
import net.jxta.jxtacast.output.OutputFileWrangler;
import net.jxta.jxtacast.output.OutputObjectWrangler;
import net.jxta.jxtacast.output.OutputWrangler;
import net.jxta.peergroup.*;
import net.jxta.pipe.*;
import net.jxta.protocol.*;


/*
 * JxtaCast: Sends data files to all peers in a peer group (those that are
 *           listening for them with JxtaCast).  Receives data files sent
 *           by other JxtaCast users.
 *
 *           Large files are broken up and sent in blocks.  Since blocks may
 *           arrive out of order, the receivers re-assemble all the blocks in
 *           memory before writing the file. (JxtaCast is therefore a memory hog
 *           if used with very large files...  Should change it to read/write
 *           blocks directly from disk files.)
 *
 *           The default block size is set in the public outBlockSize variable.
 *           Client applications can change this size if they wish.  The default
 *           size is 12kb.  The maximum message size that can be sent using IP
 *           multicasting is 16kb, so we want to stay under that.  You can use
 *           bigger blocks if you are always using a rendezvous.
 *
 *           This class was originally named "FileCast".  After finding that the
 *           name had already been used (oops), we changed it to JxtaCast.
 *           The string "FileCast" is still used in some places, to maintain
 *           backwards compatibility with earlier versions.
 */
public class JxtaCast implements PipeMsgListener, Runnable {

    // JxtaCast version number.  The version number is placed in the messages that
    // JxtaCast sends.  Hopefully this will help us orchestrate communication
    // between newer and older versions of JxtaCast.
    //
    public static String version = "2.10";


    // JxtaCast supports two types of messages: FILE and CHAT.  File messages are
    // used to broadcast files to peers through the wire protocol.  Chat messages
    // can be sent through the same pipes.  They allow the peers to carry on a side
    // chat while sending and receiving files.
    //
    // Most of the following message elements are used for file messages.  Chat
    // messages require only the MESSAGETYPE, SENDERNAME, and CAPTION elements.
    // The chat text is contained in the CAPTION element.
    //
    // All of the element's data values are stored in the message as strings, except
    // for the DATABLOCK element, containing binary image file data.  Numeric values
    // such as FILESIZE are converted to strings for storage.

    // Message element names.
    public static final String MESSAGETYPE = "MessageType";         // See list of types below.
    public static final String SENDERNAME  = "JxtaTalkSenderName";  // The sending peer name.
    public static final String SENDERID    = "SenderID";            // Peer ID of the sender.
    public static final String VERSION     = "FileCastVersion";     // JxtaCast version number.
    public static final String CAPTION     = "Caption";             // Description of the file.

    public static final String TRANSACTION_KEY     = "FileKey";      // Unique key for this file transaction.
    public static final String FILENAME    = "FileName";     // File name (no path).
    public static final String FILESIZE    = "FileSize";     // File size.
    public static final String BLOCKNUM    = "BlockNum";     // Large files are sent in blocks.
    public static final String TOTALBLOCKS = "TotalBlocks";  // Total number of blocks in the file.
    public static final String BLOCKSIZE   = "BlockSize";    // The size of one block.
    public static final String DATABLOCK   = "DataBlock";    // One block of file data.

    // REQTOPEER is a message element name, the value will usually be a peer ID.
    // REQ_ANYPEER is a value for the REQTOPEER element, requesting from any peer.
    //
    public static final String REQTOPEER   = "ReqToPeer";    // Peer ID to which we're addressing a FILE_REQ message.
    public static final String REQ_ANYPEER = "ReqAnyPeer";   // Addressing the FILE_REQ message to any peer.


    // MESSAGETYPE element data values for message content.
    public static final String MSG_FILE    = "FILE";          // File transfer message.
    public static final String MSG_OBJECT  = "OBJECT";		 // Object transfer message.
    public static final String MSG_CHAT    = "CHAT";          // Chat message.

    // MESSAGETYPE element data values for communication.
    public static final String MSG_ACK      = "FILE_ACK";      // Block received acknowledgement.
    public static final String MSG_REQ      = "FILE_REQ";      // Request a block from another peer.
    public static final String MSG_REQ_RESP = "FILE_REQ_RESP"; // Respond to a block request.

    public final static String DELIM      = "]--,',--[";     // Delimiter for some pipe name sections.

    public static boolean logEnabled;        // Log debug messages if true.
    public int outBlockSize        =  12288; // Size of the data block to send with each message, in bytes.
    public int outWranglerLifetime = 600000; // 10 mins: time to store inactive output wranglers, in millis.
    public int inWranglerLifetime  = 300000; //  5 mins: time to store inactive input wranglers, in millis.
    public int timeTilReq          =  60000; // 60 secs: max time that we'll wait before requesting missing file blocks.
    public int trailBossPeriod     =   1000; //  1  sec: worker thread sleep time between wrangler checks.
    public String fileSaveLoc;               // Destination directory for saved files.


    // We may be receiving several files at once, from multiple peers.  Since
    // the files are sent in chunks, we need objects that can hold on to what
    // we've got so far, while we process a piece of a different file.  We'll
    // use a hash table of FileWrangler objects.  Each FileWrangler will handle
    // the incoming messages for one file.  The wrangler's composeKey() func will
    // supply us with a unique hash key for each file transfer.
    //
    protected Hashtable<String, Wrangler> wranglers = new Hashtable<String, Wrangler>(40);

    // When sending, requests are temporarily queued here.  Another thread
    // reads the queue, loads the data, and starts the transmission out through
    // the pipes.  This helps keep the GUI thread cleared for action.  The vector
    // will contain OutputWrangler objects.
    //
    protected Vector<OutputWrangler> sendQueue = new Vector<OutputWrangler>(10);

    protected DiscoveryService  disco;
    protected PeerAdvertisement myPeer;
    protected PeerGroup   group;
    protected String      castName;
    protected PipeService pipeServ;
    protected InputPipe   inputPipe;     // Public propagation pipe, the "broadcast channel".
    protected OutputPipe  outputPipe;    // Public propagation pipe, paired with the above.
    protected InputPipe   privInputPipe; // Private unicast pipe, the "back channel".
    
    protected Vector<JxtaCastEventListener> jcListeners;   // Registered JxtaCastEventListener objects.

    public static final String TRAIL_BOSS_LOCK = "sleeping";

    /** Constructor
     *
     *  @param group - peergroup that we've joined.
     *  @param castName - name to use in the pipe advertisement ID , such as an
     *                    application name.  This permits the creation of
     *                    multiple JxtaCast channels within a single group.
     */
    public JxtaCast(PeerAdvertisement myPeer, PeerGroup group, String castName) {

        this.myPeer = myPeer;
        this.castName = new String(castName);
        

        // Default destination for saved files is the current directory.
        fileSaveLoc = "." + File.separator;

        // Create collection to hold JxtaCastEventListener objects.
        jcListeners = new Vector<JxtaCastEventListener>(10);

//        // Create a worker thread to handle file loading and message output.
//        // Also checks thru the list of FileWranglers to give any stalled
//        // file transactions kick in the pants.
//        //
//        Thread trailBossThread = new Thread(this, "JxtaCast:TrailBoss");
//        trailBossThread.start();
        
        setPeerGroup(group);
    }


    /** Return the currently joined peer group. */
    public PeerGroup getPeerGroup() {
        return group;
    }


    /** Change to a new peer group.
     *  @return true if we successfully created the pipes in the new group.
     */
    public synchronized boolean setPeerGroup(PeerGroup group) {

        boolean rc = false;
        
        if (group == null) {
            
            // Close any existing pipes.
            if (inputPipe != null)
                inputPipe.close();
            if (outputPipe != null)
                outputPipe.close();
            if (privInputPipe != null)
                privInputPipe.close();
            
            this.group = null;
            
            // Notify trail boss thread to close.
            synchronized (TRAIL_BOSS_LOCK) {
                TRAIL_BOSS_LOCK.notifyAll();
            }
            
            return true;
        }
        
        // If the new group is the same group we already have, it's a no-op.
        if (group == this.group) {
            return true;
        }

        // By synchronizing on the wranglers object, we ensure that the
        // trailboss thread is not trying to use the current pipes while
        // we create new ones.
        //
        synchronized (wranglers) {
        	JxtaCast.logMsg("Changing group...");
        	
            this.group = group;
            disco = group.getDiscoveryService();
            pipeServ = group.getPipeService();
            rc = createPipes(castName);
        }
        
        // Create a worker thread to handle file loading and message output.
        // Also checks thru the list of FileWranglers to give any stalled
        // file transactions kick in the pants.
        //
        Thread trailBossThread = new Thread(this, "JxtaCast:TrailBoss");
        trailBossThread.start();
        
        JxtaCast.logMsg("Set peer group to: " + group);

        return rc;
    }


    /** Log a debug message to the console.  Should maybe use Log4J?
     *  Have to figure out whether we can use Log4J to show our application
     *  debug messages, but suppress all the JXTA platform messages.
     */
    public static void logMsg(String msg) {
        if (logEnabled)
            System.out.println("[JxtaCast " + new Date() + "] " + msg);
    }

    /**
     * Close any existing pipes.
     */
    protected void closePipes()
    {
        if (inputPipe != null)
            inputPipe.close();
        if (outputPipe != null)
            outputPipe.close();
        if (privInputPipe != null)
            privInputPipe.close();
    }

    /**
     *  Create an input and output pipe to handle the file transfers.
     *  Publish their advertisements so that other peers will find them.
     *
     *  @param castName - name to use in the pipe advertisement ID.
     *  @return true if successful.
     */
    protected boolean createPipes(String castName) {

        // Close any existing pipes.
        this.closePipes();
        
        // Create the input and output pipes for the many-to-many "broadcast channel",
        // using propagation pipes.  The broadcast channel is used to send the
        // file data out to all listening peers.  First we cook up an
        // advertisement, and then create the pipes using the adv.
        //
        PipeAdvertisement pipeAdvt;
        pipeAdvt = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(
            PipeAdvertisement.getAdvertisementType());

        // This is a pre-defined ID for the propagate pipes used for file transfers.
        // Using this known ID allows us to start using the pipes immediately,
        // without having to discover other peers pipe advertisements first.
        // There is, however, a potential for collision with another app using the
        // same ID.  We use a prefix given for this JxtaCast object (castName), and
        // then append a string and byte array that should be unique to JxtaCast.
        //
        byte jxtaCastID[] = {
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB,
            (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA };

        String idStr = castName + "-[FileCast Pipe ID]-" + new String(jxtaCastID);
        PipeID id = (PipeID)IDFactory.newPipeID(group.getPeerGroupID(), idStr.getBytes());
        pipeAdvt.setPipeID(id);
        pipeAdvt.setName("JxtaTalkSenderName." + castName);
        pipeAdvt.setType(PipeService.PropagateType);

        try {
            disco.publish(pipeAdvt);
            inputPipe = pipeServ.createInputPipe(pipeAdvt, this);
            outputPipe = pipeServ.createOutputPipe(pipeAdvt, 2000);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Create the input pipe for the "back channel", using a unicast pipe.
        // Peers use this pipe for one-to-one communication, such as requesting
        // a file block from a specific peer.
        //
        // FIXME - The back channel concept isn't fully implemented yet.  We're
        // creating the pipe and adv here, but not using the pipe anywhere.
        // We want to leave this code active now, even though the pipes aren't
        // used, because the advs are useful.  JxtaCast apps can do a filtered
        // adv discovery to detect other peers running the same JxtaCast app.
        //
        // TODO - Include this adv in outgoing messages, so that receivers
        // can respond thru the back channel pipe.  
        //
        pipeAdvt = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(
            PipeAdvertisement.getAdvertisementType());

        id = (PipeID)IDFactory.newPipeID(group.getPeerGroupID());
        pipeAdvt.setPipeID(id);
        pipeAdvt.setName(getBackChannelPipeName());
        pipeAdvt.setType(PipeService.UnicastType);

        try {
            disco.publish(pipeAdvt);
            privInputPipe = pipeServ.createInputPipe(pipeAdvt, this);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /** Return the name used in advertisement for our "back channel" input pipe.
     *  The string contains a known prefix that can be used for discovery,
     *  plus our peer name and ID.
     */
    public String getBackChannelPipeName() {

        // Use a complex delimiter to mark off the peer name and ID.
        // We need to parse this string later, so we need something that's
        // unlikely to appear in a peer name.  (A simple period is too risky.)
        //
        String name = getBackChannelPipePrefix() + DELIM +
                      myPeer.getName()           + DELIM +
                      myPeer.getPeerID().toString();

        return name;
    }


    /** Return the prefix used in the name of our "back channel" input pipe.
     *  This prefix can be used with advertisement discovery to narrow the
     *  discovery results to peers using JxtaCast with your application.
     */
    public String getBackChannelPipePrefix() {

        return "FileCastBackChannel." + castName;
    }
    
    
    /** @return the pipe advertisement used for the "back channel" input pipe. */
    public PipeAdvertisement getBackChannelPipeAdvertisement() {
        return privInputPipe.getAdvertisement();
    }


    /** Extract the peer name from the given pipe advertisement name.
     */
    public static String getPeerNameFromBackChannelPipeName(String pipeName) {

        // The peer name is located between the first and second delimiters.
        int start = pipeName.indexOf(DELIM);
        if (start < 0)
            return null;

        int end = pipeName.indexOf(DELIM, start + 1);
        if (end < 0)
            return null;

        // Extract the peer name.
        start += DELIM.length();
        if (start > end)
            return null;
        return pipeName.substring(start, end);
    }


    /** Extract the peer ID from the given pipe advertisement name.
     */
    public static String getPeerIdFromBackChannelPipeName(String pipeName) {

        // The peer ID is located after the second delimiter.
        int pos = pipeName.indexOf(DELIM);
        if (pos < 0)
            return null;
        pos = pipeName.indexOf(DELIM, ++pos);
        if (pos < 0)
            return null;

        return pipeName.substring(pos + DELIM.length());
    }


    /**
     *  Send a Message down the output pipe.
     */
    public synchronized void sendMessage(Message msg) {

        try {
            outputPipe.send(msg);
        }
        catch (Exception e) {
        	JxtaCast.logMsg("Failed to send message: " + msg);
            e.printStackTrace();
        }
    }


    /**
     * Receive messages from the input pipe.
     *
     * @param event PipeMsgEvent the event that contains our message.
     */
    public /*synchronized*/ void pipeMsgEvent(PipeMsgEvent event) {
       
        Message msg = event.getMessage();
        
        String msgSenderId = JxtaCast.getMsgString(msg, JxtaCast.SENDERID);
        if (msgSenderId.equals(this.getMyPeer().getPeerID().toString())) {
            JxtaCast.logMsg("INFO: Dropping echo message from ourselves.");
            return;
        }
        
        try {
            // logMsg("Message received: " + getMsgString(msg, MESSAGETYPE));

            // Determine the message type, and dispatch it.
            String msgType = getMsgString(msg, MESSAGETYPE);
            if (msgType == null) {
                logMsg("Error: message received with no MESSAGETYPE.");
                return;
            }
            if (msgType.equals(MSG_CHAT)) {
                receiveChatMsg(msg);
            }
            else {
                receiveMsg(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();   
        }
    }

    
	/**
     * Receive a transfer message.
     *
     * @param msg an object transfer message.
     */
	public /*synchronized*/ void receiveMsg(Message msg) {

        try {
            String msgType = getMsgString(msg, MESSAGETYPE);
            if (msgType == null)
                return;

            // Check for a wrangler in the hash table, to see if we've
            // already started processing this object.  If not, create a
            // new Wrangler to handle it.  (But only if it's a
            // MSG_OBJECT/MSG_FILE message from the original sender.)
            //
            synchronized (wranglers) {
                Wrangler wrangler = (Wrangler)wranglers.get(getMsgString(msg, TRANSACTION_KEY));
                if (wrangler == null) {
                	if (msgType.equals(MSG_FILE)) {
                		wrangler = new InputFileWrangler(this, msg);
                	} else if (msgType.equals(MSG_OBJECT)) {
                		wrangler = new InputObjectWrangler(this, msg);
                	}
                	
                	if (wrangler != null) {
    	                wranglers.put(wrangler.getKey(), wrangler);
                	}
                }
                
                if (wrangler == null) {
                    logMsg("Unable to obtain wrangler for message. This is either a late or an early ACK. Ignoring.");
                    logMsg(" Msg type: " + msgType + "  key: " + getMsgString(msg, TRANSACTION_KEY));
                } else {
                    wrangler.processMsg(msg);
                }
            }
        } catch (Exception e) {
            JxtaCast.logMsg("ERROR: Failed to receive message: " + e.getMessage());
            e.printStackTrace();   
        }
    }


    /**
     * Receive a chat message.
     *
     * @param msg a chat message.
     */
    public /*synchronized*/ void receiveChatMsg(Message msg) {

        try {
            String sender   = getMsgString(msg, SENDERNAME);
            String caption  = getMsgString(msg, CAPTION);

            logMsg(sender + " : " + caption);

            // FIXME - Send the chat message to any registered listeners.
        } catch (Exception e) {
            e.printStackTrace();   
        }
    }


    /**
     *  Send a chat message out to the group members.
     *
     *  @param  text   the message text.
     */
    public /*synchronized*/ void sendChatMsg(String text) {
        try {
            // Create a message, fill it with our standard headers.
            Message msg = new Message();
            setMsgString(msg, MESSAGETYPE, MSG_CHAT);
            setMsgString(msg, SENDERNAME,  myPeer.getName());
            setMsgString(msg, SENDERID,    myPeer.getPeerID().toString());
            setMsgString(msg, VERSION,     version);
            setMsgString(msg, CAPTION,     text);

            sendMessage(msg);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     *  Send a file out to the group members.
     *
     *  @param  file       the file to send.
     *  @param  caption    description of the file (optional)
     */
    public /*synchronized*/ void sendFile(File file, String caption) {

        // Create a wrangler to handle the file transfer, and then queue it
        // in our sendFileQueue Vector.  Another thread will retrieve it
        // from the queue and start the send process.  (We don't want to
        // hang up the GUI while the file is loading.)
        //
        OutputFileWrangler wrangler = new OutputFileWrangler(this, file, caption);
        sendQueue.add(wrangler);
        
        JxtaCast.logMsg("OutputFileWrangler created and added to send queue.");
    }
    
    
    /**
     *  Send an Object out to the group members.
     *
     *  @param  object     the object to send. It must not be null or a {@link NullPointerException} will be thrown on the spot.
     *  @param  caption    description of the object (optional)
     */
    public /*synchronized*/ void sendObject(Object object, String caption) {

    	if (object == null) {
    		throw new NullPointerException("Object must not be null.");
    	}
    	
        // Create a wrangler to handle the object transfer, and then queue it
        // in our sendQueue Vector.  Another thread will retrieve it
        // from the queue and start the send process.  (We don't want to
        // hang up the GUI while the data is loaded.)
        //
        OutputObjectWrangler wrangler = new OutputObjectWrangler(this, object, caption);
        sendQueue.add(wrangler);
        
        JxtaCast.logMsg("OutputObjectWrangler created and added to send queue.");
    }


    /** Worker thread.  Call functions to perform time-intensive operations that
     *  would bog down the main thread.
     */
    public void run() {

        // While we are connected to a group.
        while (this.group != null) {
            synchronized (TRAIL_BOSS_LOCK) {
                try {
                    TRAIL_BOSS_LOCK.wait(trailBossPeriod);
                } catch (InterruptedException e) {}
            }

            // If we wake up and find that we are no longer connected to a group, stop the thread.
            if (this.group == null) {
                JxtaCast.logMsg("Stopping Trail Boss thread named: " + Thread.currentThread().getName());
                return;
            }
            
            // Check the work in progress.
            synchronized (wranglers) {

                checkWranglers();
                checkSendQueue();
            }
        }
    }


    /** Loop thru our current collection of Wrangler objects, and call
     *  bossCheck() for each one.  This gives them a chance to perform any
     *  needed tasks.
     *
     *  We keep wranglers stored in our Hashtable for awhile after we've finished
     *  sending or receiving the file. They're available to respond to requests
     *  from other peers for file blocks that they are missing.
     *
     *  In response to the bossCheck() call, wranglers that have been inactive
     *  for a long time will remove themselves from the collection.  Input
     *  wranglers that are missing data blocks will request them.
     */
    protected void checkWranglers() {

        Enumeration<Wrangler> elements = wranglers.elements();
        Wrangler wrangler;

        while (elements.hasMoreElements()) {
            wrangler = (Wrangler)elements.nextElement();
            wrangler.bossCheck();
        }
    }


    /**
     * Outgoing send operations are queued by the main thread.  This function
     * is called by the worker thread.  It reads them from the queue, and triggers
     * the data load and send process.
     */
    protected void checkSendQueue() {

        OutputWrangler wrangler = null;
      
        // Yank the first wrangler from the queue, if there is one.
        // Put it in our collection of active wranglers, and start the
        // file load and send process.
        //
        if (sendQueue.isEmpty())
            return;
        wrangler = sendQueue.remove(0);
        if (wrangler != null) {
            JxtaCast.logMsg("Processing wrangler " + wrangler.getKey() + " in send queue...");
            
            wranglers.put(wrangler.getKey(), wrangler);
            wrangler.send();
            
            JxtaCast.logMsg("Wrangler " + wrangler.getKey() + " processed and transfer started.");
            
            wrangler = null;
        }
    }
    

    /**
     * Register a JxtaCastEventListener.  Listeners are sent progress events while
     * sending and receiving files.
     */
    public synchronized void addJxtaCastEventListener(JxtaCastEventListener listener) {

        // Add the listener to our collection, unless we already have it.
        if (!jcListeners.contains(listener))
            jcListeners.addElement(listener);
    }


    /**
     * Un-register a JxtaCastEventListener.
     */
    public synchronized void removeJxtaCastEventListener(JxtaCastEventListener listener) {

        jcListeners.removeElement(listener);
    }


    /**
     * Send a JxtaCastEvent to all registered listeners in a new thread for each.
     */
    public void sendJxtaCastEvent(final JxtaCastEvent e) {

        Enumeration<JxtaCastEventListener> elements = jcListeners.elements();
        while (elements.hasMoreElements()) {
            final JxtaCastEventListener listener = elements.nextElement();
            
            new Thread(new Runnable() {
                public void run()
                {
                    listener.jxtaCastProgress(e);
                }
            }, "JxtaCast:ProgressEventDispatcher").start();
        }
    }


    public static void setMsgString(Message msg, String name, String str) {

        msg.replaceMessageElement(new StringMessageElement(name, str, null));
    }


    public static String getMsgString(Message msg, String name) {

        MessageElement elem = msg.getMessageElement(name);
        if (elem == null)
            return null;

        return elem.toString();
    }


	/**
	 * @return the PeerAdvertisemet that describes this peer. Peer information can be extracted from it. 
	 */
	public PeerAdvertisement getMyPeer() {
		return myPeer;
	}


	/**
	 * @param myPeer the PeerAdvertisemet to set that describes this peer.
	 */
	public void setMyPeer(PeerAdvertisement myPeer) {
		this.myPeer = myPeer;
	}
	
	
	public Wrangler removeWrangler(String key) {
		if (wranglers != null) {
			return this.wranglers.remove(key);
		}
		
		return null;
	}
	
}
