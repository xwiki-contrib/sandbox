/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwoot.xwootApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import jlibdiff.Diff;
import jlibdiff.Hunk;
import jlibdiff.HunkAdd;
import jlibdiff.HunkChange;
import jlibdiff.HunkDel;
import net.jxta.document.AdvertisementFactory;
import net.jxta.jxtacast.event.JxtaCastEvent;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.contentprovider.XWootContentProviderException;
import org.xwoot.contentprovider.XWootContentProviderInterface;
import org.xwoot.contentprovider.XWootId;
import org.xwoot.contentprovider.XWootObject;
import org.xwoot.contentprovider.XWootObjectField;
import org.xwoot.antiEntropy.AntiEntropy;
import org.xwoot.antiEntropy.AntiEntropyException;
import org.xwoot.clockEngine.ClockException;
import org.xwoot.jxta.DirectMessageReceiver;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.jxta.message.Message;
import org.xwoot.jxta.message.MessageFactory;
import org.xwoot.thomasRuleEngine.ThomasRuleEngine;
import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;
import org.xwoot.thomasRuleEngine.core.Entry;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Value;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOp;
import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.WootEngine;
import org.xwoot.wootEngine.WootEngineException;
import org.xwoot.wootEngine.core.WootContent;
import org.xwoot.wootEngine.op.WootOp;
import org.xwoot.xwootApp.core.LastPatchAndXWikiXWootId;
import org.xwoot.xwootApp.core.tre.XWootObjectIdentifier;
import org.xwoot.xwootApp.core.tre.XWootObjectValue;
import org.xwoot.xwootUtil.FileUtil;

/**
 * DOCUMENT ME!
 * 
 * @version $Id$
 */
public class XWoot3 implements XWootAPI, JxtaCastEventListener, DirectMessageReceiver
{
    /** When the last synchronization took place */
    private Date lastSynchronizationDate;
    
    /** The last synchronization failure */
    private String lastSynchronizationFailure;
    
    /** The content manager providing the connection with the wiki. */
    private XWootContentProviderInterface contentManager;

    /** The woot engine used for merging objects' content. */
    private WootEngine wootEngine;

    /** The P2P module. */
    private Peer peer;

    /** The Last-Writer-Win engine used to manage replicated data and objects. */ 
    private ThomasRuleEngine tre;

    /** The anti-entropy module used to keep track of patches. */
    private AntiEntropy antiEntropy;

    /** Logging component. */
    private final Log logger = LogFactory.getLog(this.getClass());    

    private String contentManagerURL;

    private String stateDirPath;

    public static final String STATEFILENAME = "state.zip";
    
    public static final String STATE_FILE_NAME_PREFIX = "xwootState";
    
    public static final String STATE_FILE_EXTENSION = ".zip";
    
    public static final String STATE_FILE_NAME_SEPARATOR = "-";

    /** The working directory where to store data. */
    private String workingDir;
    
    /** If this peer created the group he currently is member of. */
    private boolean createdCurrentGroup;

    /**
     * A content id list. ContentManager adds an id when a content change occurs. (see {@link LastPatchAndXWikiXWootId}
     * )
     */
    private LastPatchAndXWikiXWootId lastModifiedContentIdMap;

    private String contentProviderLogin;

    private String contentProviderPassword;

    /**
     * Creates a new XWoot object.
     * 
     * @param contentManager DOCUMENT ME!
     * @param wootEngine DOCUMENT ME!
     * @param peer DOCUMENT ME!
     * @param clock DOCUMENT ME!
     * @param siteUrl DOCUMENT ME!
     * @param siteId DOCUMENT ME!
     * @param tre DOCUMENT ME!
     * @param ae TODO
     * @param WORKINGDIR DOCUMENT ME!
     * @throws WootEngineException
     * @throws XWootContentProviderException
     */
    public XWoot3(XWootContentProviderInterface contentManager, WootEngine wootEngine, Peer peer, String workingDir,
        ThomasRuleEngine tre, AntiEntropy ae, String contentProviderLogin, String contentProviderPassword) throws XWootException
        {
        // TODO: Remove parameter peerId and siteId. They will be replaced with jxta values.
        
        this.lastModifiedContentIdMap = new LastPatchAndXWikiXWootId(workingDir);
        this.workingDir = workingDir;
        this.stateDirPath = workingDir + File.separator + "stateDir";
        this.createWorkingDir();

        //String endpoint="http://concerto1:8080/xwiki/xmlrpc";

        this.contentManager = contentManager;
        this.contentProviderLogin = contentProviderLogin;
        this.contentProviderPassword = contentProviderPassword;

        //TODO
        this.contentManagerURL = "";
        this.wootEngine = wootEngine;
        this.peer = peer;
        this.tre = tre;
        // FIXME: Focus on replacing siteId with the peer's id (UUID). Modify TreEngine and WootEngine and provide UUID operations (subtraction and/or comparison).
        //this.siteId = siteId;
        //this.peerId = peerId;
        this.logger.info(this.getXWootName() + " : AntiEntropy component created.");
        this.antiEntropy = ae;
        this.logger.info(this.getXWootName() + " : XWoot engine created. XWoot working directory : " + workingDir + "\n\n");
        //this.p2Pconnected = false;        
        if (this.peer.isConnectedToNetwork()) {
            this.peer.stopNetwork();
        }
        
        this.setGroupCreator(false);
    }

    public void clearWorkingDir()
    {
        File stateDirFile = new File(this.stateDirPath);

        if (stateDirFile.exists()) {
            FileUtil.deleteDirectory(stateDirFile);
        }

    }

    public void clearBaseDir() throws XWootException
    {
        File f = new File(this.workingDir);
        if (f.exists()) {
            this.logger.info(this.getXWootName() + " Delete working dir xwoot : " + f.toString());
            FileUtil.deleteDirectory(f);
        }
        this.createWorkingDir();
    }

    private void createWorkingDir() throws XWootException
    {
        File working = new File(this.workingDir);

        if (!working.exists() && !working.mkdir()) {
            throw new XWootException("Can't create xwoot directory: " + working);
        }

        if (!working.isDirectory()) {
            throw new RuntimeException(working + " is not a directory");
        } else if (!working.canWrite()) {
            throw new XWootException("Can't write in directory: " + working);
        }

        File stateDirFile = new File(this.stateDirPath);

        if (!stateDirFile.exists() && !stateDirFile.mkdir()) {
            throw new XWootException("Can't create pages directory: " + stateDirFile);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param receivedMessage DOCUMENT ME!
     * @throws XWootException
     */
    public Object receiveMessage(Object aMessage) throws XWootException
    {
        if (!(aMessage instanceof Message)) {
            logger.warn("Not and instance of org.xwoot.jxta.Message. Dropping message.");
            return null;
        }
        
        if (!this.isConnectedToP2PGroup()) {
            logger.warn("Not conencted to any group. Dropping message.");
            return null;
        }
        
        Message message = (Message) aMessage;
        
        this.logger.info(this.getXWootName() + " : received message...");
        this.logger.info(this.getXWootName() + " : message type : " + message.getAction());
        
        if (!this.isStateComputed() && !message.getAction().equals(Message.Action.STATE_REPLY)) {
            logger.warn("This XWoot node does not have a state for the group yet. Also, the message is not a state reply. Dropping message.");
            return null;
        }
        
        this.logger.info(this.getXWootName() + " : processing message...");
        
        if (message.getAction().equals(Message.Action.BROADCAST_PATCH)) {
            this.processPatchBroadcast(message);
        } else if (message.getAction().equals(Message.Action.ANTI_ENTROPY_REQUEST)) {
            this.processAntiEntropyRequest(message);
        } else if (message.getAction().equals(Message.Action.ANTI_ENTROPY_REPLY)) {
            this.processAntiEntropyReply(message);
        } else if (message.getAction().equals(Message.Action.STATE_REQUEST)) {
            return this.processStateRequest(message);
        }
        
        return null;
    }
    
    /**
     * A broadcasted patch from the network.
     * <p>
     * This peer will log an apply the operations described by the patch.
     * 
     * @param message the received message containing the disseminated patch.
     * @throws XWootException if problems occur integrating the reply in the anti-entropy log or processing the patch.
     */
    private synchronized void processPatchBroadcast(Message message) throws XWootException
    {
        // content == Patch.
        
        try {
            if (!this.antiEntropy.getLog().existInLog(message.getId())) {
                this.logger.info(this.getXWootName() + " : received message : integration and logging.");
                this.antiEntropy.logMessage(message.getId(), message);
                this.treatePatch((Patch) message.getContent());
            } else {
                this.logger.info(this.getXWootName() + " : received message : already in log.");
            }
        } catch (AntiEntropyException e) {
            throw new XWootException(this.getXWootName() + " : Problem logging the message.", e);
        }
    }
    
    /**
     * A reply to an anti-entropy requested by another peer. 
     * 
     * @param message the received message containing the requesting peer's anti-entropy log and a channel by which to reply back to him the diff.
     * @throws XWootException if the message does not contain information for contacting the requester, anti-entropy failed or the reply could not be set back.
     */
    @SuppressWarnings("unchecked")
    private void processAntiEntropyRequest(Message message) throws XWootException
    {
        // send diff with local log
        this.logger.info(this.getXWootName() + " : Message asks antientropy diff -- sending it.");
    
        // Check the pipeAdv of the sender in order to be able to reply.
//        if (!(message.getOriginalPeerId() instanceof PipeAdvertisement)) {
//            throw new XWootException(this.getXWootName() + " : The message contained invalid sender identification: " + message.getOriginalPeerId() + ". Can not reply.");
//        }
        /*
        
        PipeAdvertisement pipeAdv = null;
        try {
            pipeAdv = (PipeAdvertisement) message.getOriginalPeerId();
        } catch (ClassCastException cce) {
            throw new XWootException(this.siteId + " : The message contained invalid sender identification. Can not reply.\n", cce);
        }*/

        // Process the sender's log and send reply.
        // content == messageId[].
        try {
            // TODO: modify answerAntiEntropy or process it's result. (as stated in the todo of Message.Action.ANTI_ENTROPY_REPLY)
            Collection replyContent = this.antiEntropy.answerAntiEntropy(message.getContent());
            
            this.logger
            .debug(this.getXWootName()
                + " : New message -- content : patches : result of diff beetween given log and local log -- Action : ANTI_ENTROPY_REPLY");
            
            if (!replyContent.isEmpty()) {
                // We do not expect any reply for this message.
                this.sendMessage(replyContent, Message.Action.ANTI_ENTROPY_REPLY, message.getOriginalPeerId());
            } else {
                this.logger.debug(this.getXWootName() + " : No anti entropy reply needed because the requesting peer has all our messages. Reply dropped.");
            }
        } catch (AntiEntropyException aee) {
            // TODO: can we tolerate this exception and just warn about it?
            throw new XWootException(this.getXWootName() + " : Problem with antiEntropy\n", aee);
        } catch (XWootException xe) {
            // just log it.
            this.logger.warn(this.getXWootName() + " : Failed to answer anti-entropy request to neighbor " + message.getOriginalPeerId() + "\n", xe);
        }
        
        try {
            Object[] missingIdsFromLocalLog = this.antiEntropy.getMessageIdsMissingFromLocalLog(message.getContent());
            
            // If we have missing messages, get them from the peer that has them.
            if (missingIdsFromLocalLog != null && missingIdsFromLocalLog.length != 0) {
//                Message missingMessages = this.sendMessage(missingIdsFromLocalLog, Message.Action.MESSAGES_REQUEST, message.getOriginalPeerId());
//                this.processAntiEntropyReply(missingMessages);
                this.logger.debug(this.getXWootName() + " : The remote peer has messages we don't have. Sending anti-entropy request.");
                //this.doAntiEntropy(message.getOriginalPeerId());
                
                // If we missed some messages, they may be more.
                // TODO: any issues with this vs doAntiEntropy(neighbor)?
                this.doAntiEntropyWithAllNeighbors();
            }
        } catch (AntiEntropyException aee) {
            // just log it.
            this.logger.warn(this.getXWootName() + " : Failed to compute the missing messages from the local log.\n", aee);
        } catch (XWootException xe) {
            // just log it. It's quite bad, but let`s hope we`ll get them next time.
            this.logger.warn(this.getXWootName() + " : Failed to send anti-entropy request to get the missing messages from the remote peer.\n", xe);
        }
    }
    
    /**
     * Send a message to a single peer by using a specified channel.
     * 
     * @param content the content to send.
     * @param action the action that creates the message to send. Must not be null.
     * @param channel the channel to use to send the message. If this is null, the message will be sent to a random neighbor in the current group.
     * @throws XWootException if failed to send the message an invalid channel was supplied or an invalid reply was received.
     */
    private Message sendMessage(Object content, Message.Action action, Object channel) throws XWootException
    {
        this.logger.debug("Sending a " + action + " message.");

        Message toSend = this.createMessage(content, action);
        
        Object reply = null;
        
        if (channel == null) {
        // Send to random peer in group.
            
            try {
                reply = this.peer.sendObjectToRandomPeerInGroup(toSend, true);
            } catch (Exception e) {
                this.logger.error("Failed to send message to random peer.\n", e);
                throw new XWootException("Failed to send message to random peer.\n", e);
            }
        } else {
        // Send to specified peer in group.
            
            PipeAdvertisement pipeAdv = null;
            try {
                if (channel instanceof String) {
                    pipeAdv = createPipeAdvFromStringID((String)channel);
                } else {
                    pipeAdv = (PipeAdvertisement) channel;                
                }
            } catch (Exception e) {
                throw new XWootException(this.getXWootName() + " : Invalid or not supported channel specified.\n", e);
            }
            
            try {
                reply = this.peer.sendObject(toSend, pipeAdv);
            } catch (Exception e) {
                this.logger.error("Failed to send message to specified peer.\n", e);
                throw new XWootException("Failed to send message to specified peer.\n", e);
            }
        }
        
        // If a reply is received, it must be of type Message.
        if (reply != null && !(reply instanceof Message)) {
            throw new XWootException(this.getXWootName() + " : Received an invalid or not supported reply type (" + reply.getClass() + ").");
        }
        
        return (Message) reply;
    }
    
    
    private Message createMessage(Object content, Message.Action action)
    {
        String originalPeerID = null;
        
        if (this.isConnectedToP2PGroup()) {
            originalPeerID = this.peer.getMyDirectCommunicationPipeIDAsString();
        } else {
            originalPeerID = this.peer.getMyDirectCommunicationPipeName();
        }
        
        return MessageFactory.createMessage(originalPeerID, content, action);
    }
    
    private PipeAdvertisement createPipeAdvFromStringID(String pipeIDAsString) throws URISyntaxException
    {
        PipeAdvertisement pipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
        pipeAdv.setType(PipeService.UnicastType);
        PipeID pipeID = PipeID.create(new URI(pipeIDAsString));
        pipeAdv.setPipeID(pipeID);
        
        return pipeAdv;
    }
    
    /**
     * A reply to an anti-entropy request this peer sent earlier.
     * 
     * @param message the received message containing the reply.
     * @throws XWootException if problems occur integrating the reply in the anti-entropy log or processing the patches.
     */
    @SuppressWarnings("unchecked")
    private synchronized void processAntiEntropyReply(Message message) throws XWootException
    {
        this.logger.info(this.getXWootName() + " : Integrate antientropy messages\n\n");

     // TODO: Have a list of requests and if the reply comes without being requested, drop it?
        
        // TODO: content == should not be List<Message> but Patch[].
        //       For now, we'll keep List<Message> for backwards compatibility. 
        Collection contents = (Collection) message.getContent();

        for (Iterator iter = contents.iterator(); iter.hasNext();) {
            Message mess = (Message) iter.next();
            try {
                if (!this.antiEntropy.getLog().existInLog(mess.getId())) {
                    this.antiEntropy.logMessage(mess.getId(), mess);
                    this.treatePatch((Patch) mess.getContent());
                }
            } catch (AntiEntropyException e) {
                throw new XWootException(this.getXWootName() + " : Problems integrating the message", e);
            }
        }
    }

    /**
     * Process a request to get the state of this XWoot node.
     * 
     * @param message the received message containing a channel by which to reply back the state.
     * @return a message containing a {@code byte[]} of the state file's data.
     * @throws XWootException if problems occur getting the state or sending back the reply.
     */
    private Message processStateRequest(Message message) throws XWootException
    {
        this.logger.debug(this.getXWootName() + " : Processing a state request.");
        
        if (!this.isStateComputed()) {
            this.logger.warn(this.getXWootName() + " : This peer does not have a state. Can not answer the state request. Dropping request.");
            return null;
        }
        
        // Update the local zipped state before sending it so we don't send an old state.
        this.updateState();
        
        byte[] stateFileData = null;
        try {
            stateFileData = FileUtils.readFileToByteArray(this.getState());
        } catch (Exception e) {
            this.logger.error(this.getXWootName() + " : Failed to read the state file. Can not answer the state request. Dropping request.");
            return null;
        }
        
        Message stateReply = this.createMessage(stateFileData, Message.Action.STATE_REPLY);
        
        return stateReply;
    }
    

    private void treatePatch(Patch patch) throws XWootException
    { 
        try {
            if (patch.getMDelements() != null) {

                for (Object tre_op : patch.getMDelements()) {
                    this.getTre().applyOp((ThomasRuleOp) tre_op);
                }
            }

            XWootId xWootId =this.lastModifiedContentIdMap.getXwikiId(patch.getPageId());
            if (xWootId==null){
                xWootId=new XWootId(patch.getPageId(), patch.getTimestamp(), patch.getVersion(), patch.getMinorVersion());
            }
            System.out.println(this.getXWootName()+" : New XWootID in treatePatch : "+xWootId);

            this.lastModifiedContentIdMap.add2PatchIdMap(xWootId, patch.getObjectId());

            this.getWootEngine().deliverPatch(patch);
        } catch (WootEngineException e) {
            throw new XWootException("Problem with WootEngine");
        }catch (ThomasRuleEngineException e) {
            throw new XWootException("Problem with ThomasRuleEngine");
        }
        
        this.synchronize();
        /*this.synchronizeFromXWikiToModel(true);
        this.synchronizeFromModelToXWiki();*/
    }

    /**
     * Broadcast a new patch to the P2P network.
     * 
     * @param newPatch the patch to send.
     * @throws XWootException if problems occur while logging the patch or sending it.
     */
    private void sendNewPatch(Patch newPatch) throws XWootException
    {
        this.logger.debug(this.getXWootName() + " : Senging new patch");
        
        Message message = this.createMessage(newPatch, Message.Action.BROADCAST_PATCH);
        
        try {
            // the message must be logged before we send it 
            this.getAntiEntropy().logMessage(message.getId(), message); 
            this.logger.debug(this.getXWootName() + " : Message logged in the local log. Sending it to the group.");
            
            if (this.isConnectedToP2PGroup()) {
                this.peer.sendObject(message, message.getAction().toString());
                this.logger.debug(this.getXWootName() + " : Message(Patch) sent to the group.");
            } else {
                this.logger.warn(this.getXWootName() + " : [OFFLINE] Message(Patch) not sent. This peer is currently not conencted to a group.");
            }
        } catch (Exception e) {
            this.logger.error("Can't send the new Patch.\n", e);
            throw new XWootException("Can't send the new Patch.\n", e);
        }
    }
    
    private synchronized void synchronizeFromXWikiToModel(boolean inCopy, boolean generatePatches) throws XWootException
    {
        this.logger.info(this.getXWootName() + " : synchronize From XWiki To Model ("+inCopy+", " +  generatePatches + ")");
        
        try {
            Set<XWootId> xwootIds = this.contentManager.getModifiedPagesIds();
            System.out.println(this.getXWootName()+" : xwootIds => "+xwootIds);

            while (xwootIds != null && xwootIds.size() > 0) {
                Object[] objArray=xwootIds.toArray();
                for (Object o : objArray) {
                    XWootId id=(XWootId)o;


                    List<XWootObject> objects = this.contentManager.getModifiedEntities(id);
                    System.out.println(this.getXWootName()+" - "+id+" : entites => "+objects);

                    // need some security : the id is cleared server side but 
                    // all the modifiedEntities are not already consumed...
                    // It's important to remove the id before modifiedEntites treatment. 
                    System.out.println(this.getXWootName()+" : remove in xwiki list : "+id);

                    this.contentManager.clearModification(id);
                    
                    System.out.println(this.getXWootName()+" : save version : "+id.getPageId()+" -> "+id);
                   
                    this.getLastModifiedContentIdMap().add2XWikiIdMap(id.getPageId(), id);
                    //this.contentManager.clearModification(id);
                    for (XWootObject newObject : objects) {
                        Patch newPatch = this.synchronizeObjectFromXWikiToModel(newObject, id, inCopy);
                        if (inCopy) {
                            this.wootEngine.deliverPatch(newPatch);

                            if (newPatch.getMDelements() != null) {
                                try {
                                    for (Object tre_op : newPatch.getMDelements()) {
                                        this.getTre().applyOp((ThomasRuleOp) tre_op);
                                    }
                                } catch (ThomasRuleEngineException e) {
                                    throw new XWootException("Problem with ThomasRuleEngine");
                                }
                            }
                        }
                        
                        // If this patch is not relevant for the network, send it.
                        if (generatePatches) {
                            this.sendNewPatch(newPatch);
                        }
                    }
                }
                xwootIds = this.contentManager.getModifiedPagesIds();
            }
        } catch (XWootContentProviderException e) {
            throw new XWootException(e);

        } catch (WootEngineException e) {
            throw new XWootException(e);
        }
        this.logger.info(this.getXWootName() + " end of synchronize From XWiki To Model ("+inCopy+")");
    }

    private synchronized Patch synchronizeObjectFromXWikiToModel(XWootObject newObject, XWootId id, boolean inCopy)
    throws XWootException, WootEngineException
    {
        this.logger.info(this.getXWootName() + " : synchronize Object From XWiki To Model -- id : "+id+" -- Object : "+newObject);
        List<ThomasRuleOp> treOps = new ArrayList<ThomasRuleOp>();
        String objectId = newObject.getGuid();
        // TRE content
        ThomasRuleOp tre_op = this.synchronizeWithTRE(newObject);
        if (tre_op == null) {
            throw new XWootException("Synchronization problem !");
        }
        treOps.add(tre_op);
        List<WootOp> wootOps = new ArrayList<WootOp>();

        if (newObject.hasWootableFields()) {
            String pageName = newObject.getPageId();
            for (XWootObjectField f : newObject.getFields()) {
                String fieldId = f.getName();
                if (f.isWootable()) {
                    String oldContent = "";
                    if (inCopy) {
                        oldContent =
                            this.getWootEngine().getContentManager().getCopyContent(pageName, objectId, fieldId);
                    } else {
                        oldContent = this.getWootEngine().getContentManager().getContent(pageName, objectId, fieldId);
                    }
                    wootOps.addAll(this.synchronizeWithWootEngine(pageName, objectId, fieldId, oldContent, (String) f.getValue(),
                        inCopy));
                }
            }
        }
        Patch newPatch =
            new Patch(wootOps, treOps, id.getPageId(), objectId, id.getTimestamp(), id.getVersion(), id
                .getMinorVersion());
        this.logger.info(this.getXWootName() + " end of synchronize Object From XWiki To Model -- return : "+newPatch);
        return newPatch;
    }

    private Value loadObjectFromModel(String pageName,String objectId) throws XWootException{
        this.logger.info(this.getXWootName() + " : load Object From Model -- pagename : "+pageName+" -- id : "+objectId );
        XWootObjectValue obj_tre = null;
        XWootObject result =null;
        XWootObjectIdentifier id_tre = new XWootObjectIdentifier(objectId);
        try {
            obj_tre = (XWootObjectValue) this.tre.getValue(id_tre);
            if (obj_tre == null) {
                throw new XWootException(
                "Problem with last modified content id list -- An id is in the list but not in the Thomas Rule Engine model.");
            }
            result = (XWootObject) obj_tre.get();
            for (XWootObjectField f : result.getFields()) {
                String fieldId = f.getName();
                if (f.isWootable()) {
                    String content =
                        this.wootEngine.getContentManager().getContent(pageName, objectId, fieldId);
                    obj_tre.setObjectField(new XWootObjectField(fieldId, content, true));
                }
            }
        } catch (WootEngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ThomasRuleEngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.logger.info(this.getXWootName() + " : end of load Object From Model -- return : "+obj_tre); 
        return obj_tre;

    }

    private void synchronizeFromModelToXWiki() throws XWootException
    {
        this.logger.info(this.getXWootName() + " : synchronize From Model To XWiki");
        Map<XWootId, Set<String>> currentList = this.lastModifiedContentIdMap.getCurrentPatchIdMap();
        System.out.println(this.getXWootName()+" : current Pach id list : "+currentList);
        for (XWootId xwid : currentList.keySet()) {
            for (String objectId : currentList.get(xwid)) {
                Value objectValue=this.loadObjectFromModel(xwid.getPageId(), objectId);
                XWootObject xwootObject=(XWootObject) objectValue.get();
                try {
                    if (this.synchronizeObjectFromModelToXWiki(xwootObject))
                    {
                        for (XWootObjectField f : xwootObject.getFields()) {
                            if (f.isWootable()) {
                                this.getWootEngine().getContentManager().copyWootContent(xwootObject.getPageId(), xwootObject.getGuid(), f.getName());
                            }
                        }
                        System.out.println(this.getXWootName()+" : remove in xwoot2 list : "+xwid);
                        this.lastModifiedContentIdMap.removePatchId(xwid,objectId);
                    }
                } catch (WootEngineException e) {
                    throw new XWootException(e);
                }

            }
        }
        
        this.logger.info(this.getXWootName() + " end of synchronize From Model To XWiki");

    }


    private synchronized boolean synchronizeObjectFromModelToXWiki(XWootObject o2) throws XWootException
    {
        this.logger.info(this.getXWootName() + " : synchronize Object From Model To XWiki -- object : "+o2);
        if (this.isContentManagerConnected()) {
            try {

                XWootId id=this.getLastModifiedContentIdMap().getXwikiId(o2.getPageId());
                System.out.println(this.getXWootName()+" last xwiki id saved : "+id +" -- try to store ...");
                
                XWootId newXWootId=this.contentManager.store(o2,id);
                System.out.println(this.getXWootName()+" result of store : "+newXWootId);
                if (newXWootId!=null) {
                    this.getLastModifiedContentIdMap().add2XWikiIdMap(o2.getPageId(), newXWootId);
                    System.out.println(this.getXWootName()+" save xwiki id : "+newXWootId);
                    System.out.println("verif : "+ this.getLastModifiedContentIdMap().getXwikiId(o2.getPageId()));
                }else{
                    this.logger.info(this.getXWootName() + " : some no consummed datas for id : " + o2.getPageId() + "."
                        + o2.getGuid());
                   // this.synchronize();
                    return false;
                }
            } catch (XWootContentProviderException e) {
                throw new XWootException(e);
            }
        }
        this.logger.info(this.getXWootName() + " end of synchronize Object From Model To XWiki");
        return true;

    }

    @SuppressWarnings("unchecked")
    private synchronized List<WootOp> synchronizeWithWootEngine(String pageName, String objectId, String fieldId,
        String oldPage, String newPage, boolean inCopy) throws XWootException
        {
        BufferedReader oldContent = new BufferedReader(new StringReader(oldPage));
        BufferedReader newContent = new BufferedReader(new StringReader(newPage));

        Diff d = new Diff();
        try {
            d.diff(oldContent, newContent);
        } catch (IOException e) {
            this.logger.error(this.getXWootName() + " : Problem with diff when synchronizing content", e);
        }

        List l = d.getHunks();
        ListIterator lIt = l.listIterator();
        List<WootOp> data = new ArrayList<WootOp>();

        if (lIt.hasNext()) {
            try {
                this.wootEngine.loadClock();
            } catch (ClockException e) {
                throw new XWootException(this.getXWootName() + " : Problem when synchronizing content", e);
            }
            WootContent page = null;
            try {
                if (inCopy) {
                    page = this.wootEngine.getContentManager().loadWootContentCopy(pageName, objectId, fieldId);
                } else {
                    page = this.wootEngine.getContentManager().loadWootContent(pageName, objectId, fieldId);
                }
            } catch (WootEngineException e) {
                throw new XWootException(this.getXWootName() + " : Problem when synchronizing content", e);

            }
            do {
                Hunk hunk = (Hunk) lIt.next();

                if (hunk instanceof HunkAdd) {
                    HunkAdd ha = (HunkAdd) hunk;
                    Iterator it = ha.getNewContent().iterator();
                    int pos = ha.getLD2();
                    int i = -1;

                    while (it.hasNext()) {
                        String line = (String) it.next();
                        WootOp ins = null;
                        try {
                            ins = this.getWootEngine().insert(page, line, (pos + i));
                        } catch (WootEngineException e) {
                            throw new XWootException(this.getXWootName() + " : Problem when synchronizing content", e);
                        }
                        data.add(ins);
                        i++;
                    }
                } else if (hunk instanceof HunkDel) {
                    HunkDel hDel = ((HunkDel) hunk);
                    int nbOfLine = hDel.getLF1() - hDel.getLD1() + 1;
                    int pos = hDel.getLD2() - 1;

                    for (int i = 0; i < nbOfLine; i++) {
                        WootOp del = null;
                        try {
                            del = this.getWootEngine().delete(page, pos);
                        } catch (WootEngineException e) {
                            throw new XWootException(this.getXWootName() + " : Problem when synchronizing content", e);
                        }
                        data.add(del);
                    }
                } else if (hunk instanceof HunkChange) {
                    throw new XWootException("HunkChange might not be detected, check the jlibdiff configuration");
                }
            } while (lIt.hasNext());
            try {
                this.wootEngine.unloadClock();                
            } catch (ClockException e) {
                this.logger.error(this.getXWootName() + " : Problem when synchronizing content", e);
            }
            try {
                this.wootEngine.getContentManager().unloadWootContent(page);
            } catch (WootEngineException e) {
                this.logger.error(this.getXWootName() + " : Problem when synchronizing content", e);
            }
        }

        if (!data.isEmpty()) {
            this.logger.info(this.getXWootName() + " : " + data.size() + " operation(s) applicated to content model\n\n");
        } else {
            this.logger.info(this.getXWootName() + " : Synchronize page content :" + pageName + " -- no diff.\n\n");
        }

        return data;
        }

    private ThomasRuleOp synchronizeWithTRE(XWootObject o) throws XWootException
    {
        XWootObjectIdentifier tre_id = new XWootObjectIdentifier(o.getGuid()); 
        try {
            XWootObjectValue tre_value = (XWootObjectValue) this.tre.getValue(tre_id);

            if (tre_value == null) {
                tre_value = new XWootObjectValue();
            }

            if (tre_value.get()==null) {
                tre_value.setObject(o);

            } else {
                for (XWootObjectField f : o.getFields()) {
                    tre_value.setObjectField(f);
                }
            }

            ThomasRuleOp op = this.tre.getOp(tre_id, tre_value);
            this.tre.applyOp(op);
            return op;

        } catch (ThomasRuleEngineException e) {
            throw new XWootException(e);
        }
    }

    
    public synchronized void synchronize() throws XWootException
    {        
        try {
        this.synchronize(true);
        }
        catch(XWootException e) {
            lastSynchronizationFailure = e.getMessage();
            throw e;
        }
        finally {
            lastSynchronizationDate = new Date(System.currentTimeMillis());
        }
        
        lastSynchronizationFailure = "Synch OK";
    }
    
    /**
     * DOCUMENT ME!
     * 
     * @throws XWootException
     * @throws WootEngineException
     */
    public synchronized void synchronize(boolean generatePatches) throws XWootException
    {
        this.logger.info(this.getXWootName() + " : Starting the synchronisation of each managed pages");
        
        if (!this.isContentManagerConnected()) {
            this.logger.warn("Content manager not connected. Synchronization aborded.");
            return;
        }
       
        try {
            if (!this.getContentProvider().getModifiedPagesIds().isEmpty()){
                this.synchronizeFromXWikiToModel(!this.lastModifiedContentIdMap.getCurrentPatchIdMap().isEmpty(), generatePatches);
            } else {
                this.logger.info("No changes in xwiki => No changes done to the model.");
            }
        } catch (XWootContentProviderException e) {
            throw new XWootException(e);
        }
        
        if (!this.getLastModifiedContentIdMap().getCurrentPatchIdMap().isEmpty()){
            this.synchronizeFromModelToXWiki();
        } else {
            this.logger.info("No changes in the model => No changes done in the xwiki.");
        }
        
        this.logger.info(this.getXWootName() + " : Synchronising OK.");                
    }
    
    public boolean createNetwork() throws XWootException
    {
        // TODO: change return type to void.
        // TODO: Read from properties file.
        try {
            // clear any seeds/seedingUris.
            NetworkConfigurator networkConfig = this.peer.getManager().getConfigurator();
            networkConfig.clearRelaySeedingURIs();
            networkConfig.clearRelaySeeds();
            networkConfig.clearRendezvousSeedingURIs();
            networkConfig.clearRendezvousSeeds();
            
            this.peer.getManager().setUseDefaultSeeds(false);
            this.peer.getManager().setMode(ConfigMode.RENDEZVOUS_RELAY);
        } catch (Exception e) {
            throw new XWootException(this.getXWootName() + " : Failed to initialize network.", e);
        }
        
        try {
            this.peer.startNetworkAndConnect(this, this);
        } catch (Exception e) {
            throw new XWootException(this.getXWootName() + " : Failed to start P2P network.", e);
        }
        
        return true;
        
        /*
        this.clearWorkingDir();
        try {
            this.wootEngine.clearWorkingDir();
        } catch (WootEngineException e) {
            this.logger.error(this.peerId + " : Problem when clearing wootEngine dir\n", e);
            throw new XWootException(this.peerId + " : Problem when clearing wootEngine dir\n", e);
        }
        this.tre.clearWorkingDir();
        this.antiEntropy.clearWorkingDir();
        // try {
        this.peer.clearWorkingDir();
        // } catch (SenderException e) {
        // this.logger.error(this.peerId+" : Problem when clearing sender dir\n",e);
        // throw new XWootException(this.peerId+" : Problem when clearing  sender dir\n",e);
        // }

        this.logger.info(this.siteId + " : all datas clears");
        if (!this.isContentManagerConnected()) {
            this.connectToContentManager();
        }
        return true;
        */
    }

    public boolean joinNetwork(String neighborURL) throws XWootException
    {
        // TODO: change return type to void.
        // TODO: remove parameter. Config should be read from file.
        // TODO: read from properties file.
        
        // using public network for now.
        //this.peer.getManager().setUseDefaultSeeds(true);
        try {
            this.peer.startNetworkAndConnect(this, this);
        } catch (Exception e) {
            throw new XWootException(this.getXWootName() + " : Failed to join network.", e);
        }
        
        // FIXME: Rejoin group by reading from a properties file or configuration object if this is an XWoot restart.
        // BIG PRIORITY.
        
        return true;
        
        
        //throw new IllegalStateException("NOT IMPLEMENTED");
        /*File s = null;
        if (this.isStateComputed()) {
            s = new File(this.getStateFilePath());
        }
        if (!this.isConnectedToP2PNetwork()) {
            this.p2Pconnected = true;
        }
        if (!this.isContentManagerConnected()) {
            this.connectToContentManager();
        }

        if (this.getNeighborsList().contains(neighborURL) || this.addNeighbour(neighborURL)) {
            if (s == null) {
                s = this.askState(this.getXWootPeerId(), neighborURL);
                if (s == null) {
                    this.logger.warn(this.getXWootName() + " : problem to get state of neighbor : " + neighborURL);
                    return false;
                }
            }
            this.importState(s);
            return true;
        }
        return false;*/
    }

    /** {@inheritDoc} */
    public PeerGroupAdvertisement createNewGroup(String name, String description, char[] keystorePassword, char[] groupPassword) throws XWootException
    {
        if (!this.isConnectedToP2PNetwork()) {
            throw new XWootException(this.getXWootName() + " : Not connected to network.");
        }
        
        PeerGroup newGroup = null;
        try {
            newGroup = this.peer.createNewGroup(name, description, keystorePassword, groupPassword);
        } catch (Exception e) {
            this.logger.error(this.getXWootName() + " : Failed to create new group.", e);
            throw new XWootException("Failed to create new group.", e);
        }
        
        // We created this group that we just joined. We can create a state.`
        this.setGroupCreator(true);
        
        return newGroup.getPeerGroupAdvertisement();
        
     // FIXME: store the currentlyJoinedGroup in a properties file or somewhere on drive in order to automatically rejoin (with proper password) the group on a reboot.
    }
    
    /** {@inheritDoc} */
    public void joinGroup(PeerGroupAdvertisement groupAdvertisement, char[] keystorePassword, char[] groupPassword) throws XWootException
    {
        this.joinGroup(groupAdvertisement, keystorePassword, groupPassword, false);
    }
    
    /** {@inheritDoc} */
    public void joinGroup(PeerGroupAdvertisement groupAdvertisement, char[] keystorePassword, char[] groupPassword, boolean beRendezVous) throws XWootException
    {
        if (!this.isConnectedToP2PNetwork()) {
            throw new XWootException(this.getXWootName() + " : Not connected to network.");
        }
        
        try {
            this.peer.joinPeerGroup(groupAdvertisement, keystorePassword, groupPassword, beRendezVous);
        } catch (Exception e) {
            this.logger.error(this.getXWootName() + " : Failed to join the group.", e);
            throw new XWootException("Failed to join the group.", e);
        }
        
        // We joined a group so we could not have created it.
        // FIXME: should we keep track of the groups we created to determine if we can create a state even if we did not the first time?
        this.setGroupCreator(false);
        
    }
    
    public void leaveGroup() throws XWootException 
    {        
        try {
            this.peer.leavePeerGroup();
        } catch (Exception e) {
            this.logger.error(this.getXWootName() + " : Failed to leave the group.", e);
            throw new XWootException("Failed to leave the group.", e);
        }
        
        // Forget that we created any group.
        // FIXME: should we keep track of the groups we created to determine if we can create a state even if we did not the first time?
        this.setGroupCreator(false);
        
     // FIXME: un-store the currentlyJoinedGroup in a properties file or somewhere on drive in order to disable automatic rejoin (with proper password) the of the group on a reboot.
    }
    
    public File getState()
    {
        if (this.isStateComputed()) {
            return new File(this.getStateFilePath());
        }
        
        return null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param state DOCUMENT ME!
     * @throws XWootException
     */
    private void setState(File newState) throws XWootException
    { 
        this.logger.debug(this.getXWootName() + " : receive state and apply");

        if (newState == null) {
            this.logger.warn(this.getXWootName() + " : null state file received. Aborting.");
            return;
        }
        
        this.logger.debug(this.getXWootName() + " : Importing file " + newState);
        ZipFile state=null;
        try {
        	state = new ZipFile(newState);
        }catch(Exception e){
            this.logger.error(this.getXWootName() + " : Invalid state file. Aborting.", e);
            throw new XWootException("Invalid state file. Set state aborted.", e);
        }
        
        File wootState = null;
        File treState = null;
        File antiEntropyState = null;
        try{
            FileUtil.unzipInDirectory(state, this.stateDirPath);
            
            wootState = new File(this.stateDirPath, this.getWootEngineStateFileName());
            treState = new File(this.stateDirPath, this.getTreStateFileName());
            antiEntropyState = new File(this.stateDirPath, this.getAntiEntropyStateFileName());
            
            if (!wootState.exists() || !treState.exists()) {
                throw new WootEngineException("Expected " + wootState + " and " + treState + " files were not found after unpacking an xwoot state.");
            }

            // Clear WootEngine data.
            this.getWootEngine().clearWorkingDir();
            // Update WootEngine data.
            this.getWootEngine().setState(wootState);

            // Clear ThomasRuleEngine data.
            //this.getTre().clearWorkingDir();
            String treWorkingDir = this.getTre().getWorkingDir();
            FileUtil.deleteDirectory(treWorkingDir);
            FileUtil.checkDirectoryPath(treWorkingDir);
            // Update ThomasRuleEngine data.
            FileUtil.unzipInDirectory(treState.toString(), this.tre.getWorkingDir());
            
            // Clean Anti-Entropy data.
            this.antiEntropy.clearWorkingDir();
            if (antiEntropyState.exists()) {
	            // Update Anti-Entropy data.
	            FileUtil.unzipInDirectory(antiEntropyState.toString(), this.antiEntropy.getLog().getWorkingDirectory());
            }

            return;
        } catch (WootEngineException e) {
            this.logger.error(this.getXWootName() + " : Problems setting woot engine state \n", e);
            throw new XWootException(this.getXWootName() + " : Problems setting woot engine state \n", e);
        } catch (Exception e) {
            this.logger.error(this.getXWootName() + " : Problems setting the XWoot state \n", e);
            throw new XWootException(this.getXWootName() + " : Problems setting the XWoot state \n", e);
        } finally {
            // delete unzipped states.
            if (wootState != null && wootState.exists()) {
                wootState.delete();
            }
            if (treState != null && treState.exists()) {
                treState.delete();
            }
            if (antiEntropyState != null && antiEntropyState.exists()) {
            	antiEntropyState.delete();
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public File computeState() throws XWootException
    {
        // TODO: make return type void.
        
        if (!this.isGroupCreator()) {
            throw new XWootException("Can not create a state for an existing group. There can be only one state creation per group. Create a new group istead.");
        }
        
        this.updateState();

        return new File(this.getStateFilePath());
    }
    
    public synchronized void updateState() throws XWootException
    {
        if (!this.hasJoinedAP2PGroup()) {
            throw new XWootException("Not joined any group.");
        }
        
        if (!contentManager.isConnected()) {
            throw new XWootException("Can't initialize woot Storage : contentManager is not connected.");
        }

        // initialization of the state directory
        /*File stateDir = new File(this.stateDirPath);
        FileUtil.deleteDirectory(stateDir);
        stateDir.mkdir();*/
        try {
            FileUtil.checkDirectoryPath(this.stateDirPath);
        } catch (Exception e) {
            throw new XWootException("The state directory can not be used to store newly created states.", e);
        }

        // Make sure the internal model is up to date and generate patches only when we are updating the state and not creating it.
        this.logger.debug(this.getXWootName() + " : Synchronizing with wiki.");
        this.synchronize(this.isStateComputed());
        
        this.logger.debug(this.getXWootName() + " : " + (this.isStateComputed() ? "Updating" : "Creating") + " state.");

        // TODO: could be better handled and more consistent.
        
        List<File> xwootStateFiles = new ArrayList<File>();
        
        // get WOOT state
        File wootState;
        try {
            wootState = this.getWootEngine().getState();
        } catch (WootEngineException e) {
            this.logger.error(this.getXWootName() + " : Problem to get woot engine state \n", e);
            throw new XWootException(this.getXWootName() + " : Problem to get woot engine state \n", e);
        }
        
        if (wootState!=null && wootState.exists()){
            File copy0 = new File(this.stateDirPath, this.getWootEngineStateFileName());
            try {
                wootState = FileUtil.moveFile(wootState, copy0);
            } catch (Exception e) {
                this.logger.error(this.getXWootName() + " : Failed to replace new state file with old one. State creation process failed.\n", e);
                throw new XWootException(this.getXWootName() + " : Failed to replace new state file with old one. State creation process failed.\n", e);
            }
        } else {
            this.logger.error(this.getXWootName() + " : The Woot state did not compute successfuly.\n");
            throw new XWootException(this.getXWootName() + " : The Woot state did not compute successfuly.");
        }
        
        xwootStateFiles.add(wootState);

        // get TRE state
        File treState = new File(this.stateDirPath, this.getTreStateFileName());
        try {
            FileUtil.zipDirectory(this.tre.getWorkingDir(), treState.toString());
        } catch (Exception e) {
            this.logger.error(this.getXWootName() + " : The TRE state did not compute successfuly.\n", e);
            throw new XWootException(this.getXWootName() + " : The TRE state did not compute successfuly.\n", e);
        }
        
        xwootStateFiles.add(treState);
        
        File antiEntropyState = null;
        int logSize = 0;
        try {
        	logSize = this.antiEntropy.getLog().logSize();
        } catch (Exception e) {
        	this.logger.error(this.getXWootName() + " : The Anti-entropy state did not compute successfuly.\n", e);
            throw new XWootException(this.getXWootName() + " : The Anti-entropy state did not compute successfuly.\n", e);
        }
        
        if (logSize != 0) {
	        // get Anti-Entropy log's state
        	antiEntropyState = new File(this.stateDirPath, this.getAntiEntropyStateFileName());
	        try {
	            FileUtil.zipDirectory(this.antiEntropy.getLog().getWorkingDirectory(), antiEntropyState.toString());
	        } catch (Exception e) {
	            this.logger.error(this.getXWootName() + " : The Anti-entropy state did not compute successfuly.\n", e);
	            throw new XWootException(this.getXWootName() + " : The Anti-entropy state did not compute successfuly.\n", e);
	        }
	        
	        xwootStateFiles.add(antiEntropyState);
        }
        
        // package the WOOT state, TRE state and Anti-Entropy log together.
        try {
            //String zip=FileUtil.zipDirectory(this.stateDirPath/*, File.createTempFile("state", ".zip").getPath()*/);
            FileUtil.zipFiles(xwootStateFiles.toArray(new File[0]), this.getStateFilePath());
           
            /* I think we should not make isStateComputed() return true if we failed.
            File result = new File(this.getStateFilePath());
            result.createNewFile();
            return null;
            */
            
        } catch (IOException e) {
            this.logger.error(this.getXWootName() + " : Problems creating the XWoot state.\n", e);
            throw new XWootException(this.getXWootName() + " : Problems creating the XWoot state.\n", e);
        }
        
        // delete the state files because they are now packed into the xwoot state.
        for (File componentStateFile : xwootStateFiles) {
        	if (componentStateFile != null && componentStateFile.exists()) {
        		componentStateFile.delete();
        	}
        }
        
        this.logger.debug(this.getXWootName() + " : Finished processing state.");
    }
    
    /** {@inheritDoc} */
    public boolean isGroupCreator() {
        return createdCurrentGroup;
    }
    
    /**
     * @param createdCurrentGroup if this peer created the group this peer is a member of.
     * @see #isGroupCreator()
     */
    public void setGroupCreator(boolean createdCurrentGroup) {
        this.createdCurrentGroup = createdCurrentGroup;
    }
    
    private String getWootEngineStateFileName() 
    {
        /*StringBuilder result = new StringBuilder();
        result.append(WootEngine.STATE_FILE_NAME_PREFIX);
        result.append(STATE_FILE_NAME_SEPARATOR);
        result.append(this.peer.getCurrentJoinedPeerGroup().getPeerGroupID().toString());
        result.append(WootEngine.STATE_FILE_EXTENSION);
        
        return result.toString();*/
        return WootEngine.STATE_FILE_NAME;
    }
    
    private String getTreStateFileName()
    {
        /*StringBuilder result = new StringBuilder();
        result.append(ThomasRuleEngine.STATE_FILE_NAME_PREFIX);
        result.append(STATE_FILE_NAME_SEPARATOR);
        result.append(this.peer.getCurrentJoinedPeerGroup().getPeerGroupID().toString());
        result.append(ThomasRuleEngine.STATE_FILE_EXTENSION);
        
        return result.toString();*/
        return ThomasRuleEngine.TRE_STATE_FILE_NAME;
    } 
    
    private String getAntiEntropyStateFileName()
    {
        return "antiEntropyState.zip";
    }

    public boolean isStateComputed()
    {
        File result = new File(this.getStateFilePath());
        return result.exists();
    }

    public boolean importState(File stateFileToImport) throws XWootException
    {
        // TODO: make return type void.

        if (stateFileToImport == null) {
            this.logger.warn(this.getXWootName() + " : Tried to import a null state. Operation ignored.");
            return false;
        }

        if (!this.isContentManagerConnected()) {
            this.connectToContentManager();
        }

        File currentState = new File(this.getStateFilePath());

        // set the state
        this.setState(stateFileToImport);

        // Mark the wiki as not modified because we just imported a state and have to override the wiki with the state's model.
        try {
            this.contentManager.getModifiedPagesIds();
            this.contentManager.clearAllModifications();
        } catch (Exception e) {
            this.logger.error(this.getXWootPeerId() + " : Failed to mark the xwiki as not modified before synchronizing with it.", e);
            throw new XWootException(this.getXWootPeerId() + " : Failed to mark the xwiki as not modified before synchronizing with it.", e);
        }        

        // FIXME: do some preprocessing of the entries and align them in a map from pageID to list of page objects.
        // to reduce complexity

        try {
            List<Entry> entries = this.tre.getAllEntries();
            for (Entry pageEntry : entries) {
                Identifier id = pageEntry.getId();
                String objectId = id.getId();
                if (!objectId.startsWith("page:")) {
                    // Skip objects that don't describe an XWiki page.
                    continue;
                }

                this.logger.debug("Synchronizing page from model: " + objectId);
                String pageId = objectId.substring(objectId.indexOf(":") + 1);

                // Update/Create the page first before doing the same for page objects.
                Value pageObjectValue = pageEntry.getValue();
                XWootObject pageObject = (XWootObject) pageObjectValue.get();
                XWootId pageXWootID = this.contentManager.store(pageObject);

                // Update/Create page objects.

                for(Entry objectEntry : entries) {
                    Identifier idOfObjectEntry = objectEntry.getId();
                    if (idOfObjectEntry.getId().startsWith("page:")) {
                        // Skip page-entries.
                        continue;
                    }

                    XWootObject objectInPage = (XWootObject) objectEntry.getValue().get();
                    String pageIdOfObjectEntry = objectInPage.getPageId(); 
                    if (!pageIdOfObjectEntry.equals(pageId)) {
                        // Skip entries that are not for our current page.
                        continue;
                    }

                    this.logger.debug("Synchronizing object from model: " + objectId);

                    // Store the object.
                    pageXWootID = this.contentManager.store(objectInPage);
                }
                
                // Save it's last known version as fix for http://jira.xwiki.org/jira/browse/CONCERTO-21
                this.getLastModifiedContentIdMap().add2XWikiIdMap(pageId, pageXWootID);

            }
        } catch (ThomasRuleEngineException tree) {
            this.logger.error(this.getXWootPeerId() + " : Failed to get objects from the XWoot model.\n", tree);
            throw new XWootException(this.getXWootPeerId() + " : Failed to get objects from the XWoot model.\n", tree);
        } catch (XWootContentProviderException xwcpe) {
            this.logger.error(this.getXWootPeerId() + " : Failed to update XWiki with objects from the XWoot model.\n", xwcpe);
            throw new XWootException(this.getXWootPeerId() + " : Failed to update XWiki with objects from the XWoot model.\n", xwcpe);
        }

        // If it is not a re-import of an existing state, then copy it and make it the current state.
        if (/*!stateFileToImport.equals(currentState) || */!stateFileToImport.getParent().toString().equals(this.workingDir)) {
            try {
                FileUtil.copyFile(stateFileToImport.toString(), currentState.toString());
            } catch (IOException e) {
                this.logger.error(this.getXWootName() + " : Problem when copying state file ", e);
                throw new XWootException(this.getXWootName() + " : Problem when copying state file ", e);
            }
        }

        return true;
    }
    
    public File askStateToGroup() throws XWootException
    {
        if (!this.isConnectedToP2PGroup()) {
            throw new XWootException(this.getXWootName() + " : Failed to ask the state bacause there is no other group member connected.");
        }
        
        this.logger.debug(this.getXWootName() + " : Asking the XWoot state to the current group.");
        
        Message stateReply = null;
        
        try {
            stateReply = this.sendMessage(null, Message.Action.STATE_REQUEST, null);
            if (stateReply == null) {
                throw new XWootException("A peer was contacted but it did not send back a reply.");
            }
        } catch (XWootException e) {
            this.logger.error(this.getXWootName() + " : Failed to get the state from the current group.\n", e);
            throw new XWootException(this.getXWootName() + " : Failed to get the state from the current group.\n", e);
        }
        
        if (!(stateReply.getAction().equals(Message.Action.STATE_REPLY))) {
            this.logger.error(this.getXWootName() + " : Invalid state reply message action (" + stateReply.getAction() + ").");
            throw new XWootException(this.getXWootName() + " : Invalid state reply message action (" + stateReply.getAction() + ").");
        }
        
        byte[] stateFileData = null;
        try {
            stateFileData = (byte[]) stateReply.getContent();
        } catch (ClassCastException cce) {
            throw new XWootException(this.getXWootName() + " : Invalid state reply message content.\n", cce);
        }
        
        File tempStateFile = null;
        try {
            tempStateFile = File.createTempFile("receivedXWootState", ".zip");            
            FileUtils.writeByteArrayToFile(tempStateFile, stateFileData);
        } catch (Exception e) {
            throw new XWootException("Failed to write the received state file to drive.\n", e);
        }
        
        return tempStateFile;
    }

    public void doAntiEntropyWithAllNeighbors() throws XWootException
    {
        /*if (this.isConnectedToP2PNetwork()) {
            Collection c = this.getNeighborsList();
            if ((c == null) || c.isEmpty()) {
                return;
            }

            Iterator i = c.iterator();
            while (i.hasNext()) {
                this.doAntiEntropy((String) i.next());
            }
        }*/        
        
        if (!this.isConnectedToP2PGroup()) {
            this.logger.warn(this.getXWootName() + " : Not successfuly joined or connected to a P2P group yet.");
            return;
        }
        
        this.logger.info(this.getXWootName() + " : Asking antiEntropy with all neighbors.");

        Object content = null;
        try {
            content = this.antiEntropy.getMessageIdsForAskAntiEntropy();
        } catch (AntiEntropyException e) {
            this.logger.error(this.getXWootName() + " : Problems getting content for antiEntropy.\n", e);
            throw new XWootException(this.getXWootName() + " : Problems getting content for antiEntropy.\n", e);
        }
        
        Message message = this.createMessage(content, Message.Action.ANTI_ENTROPY_REQUEST);
        
        this.logger
        .debug(this.getXWootName() + " : New message -- content : log patches -- Action : " + message.getAction());
        
        try {
            this.peer.sendObject(message, message.getAction().toString());
        } catch (Exception e) {
            throw new XWootException(this.getXWootName() + " : Can't do anti-entropy with all neighbors.\n", e);
        }
    }

    /**
     * Request anti-entropy from a specific neighbor.
     * 
     * @param neighbor the neighbor to contact.
     * @throws XWootException if problems occur getting the local message log or while sending the message.
     * @throws InvalidParameterException if the given neighbor is not of the correct type.
     * @see #sendMessage(Object, org.xwoot.jxta.message.Message.Action, Object)
     */
    public void doAntiEntropy(Object neighbor) throws XWootException
    {        
        if (!this.isConnectedToP2PGroup()) {
            this.logger.warn(this.getXWootName() + " : Not successfuly joined or connected to a P2P group yet.");
            return;
        }
        
        /*if (!(neighbor instanceof PipeAdvertisement)) {
            throw new InvalidParameterException("Parameter must be of type " + PipeAdvertisement.class + ". Provided: " + neighbor);
        }*/
        
        this.logger.info(this.getXWootName() + " : Asking antiEntropy with a specific neighbor.");
        this.logger.info(this.getXWootName() + " : PipeID of neighbor: " + neighbor);
        
        String neighborPipeId = (String) neighbor;
        PipeAdvertisement neighborPipeAdvertisement = null;
        
        try {
            neighborPipeAdvertisement = this.createPipeAdvFromStringID(neighborPipeId);
        } catch (Exception e) {
            this.logger.error("Invalid neighbor. Could not create communication channel.");
            throw new XWootException("Invalid neighbor. Could not create communication channel.\n", e);
        }

        Object content = null;
        try {
            content = this.antiEntropy.getMessageIdsForAskAntiEntropy();
        } catch (AntiEntropyException e) {
            this.logger.error(this.getXWootName() + " : Problems getting content for antiEntropy.\n", e);
            throw new XWootException(this.getXWootName() + " : Problems getting content for antiEntropy.\n", e);
        }
        
        this.logger.debug(this.getXWootName() + " : New message -- content : log patches -- Action : " + Message.Action.ANTI_ENTROPY_REQUEST.toString());
        
        // The reply will come as a separate message, initiated by the destination peer. This is caused by the broadcast nature of ANTI_ENTROPY_REQUEST messages.
        this.sendMessage(content, Message.Action.ANTI_ENTROPY_REQUEST, neighborPipeAdvertisement);
    }

    public void connectToContentManager() throws XWootException
    {
        this.logger.info("Connect to content manager.");
        
        if (!this.isContentManagerConnected()) {
            try {
                this.logger.info(this.getXWootName() + " : Connect to content provider ");
                
                this.contentManager.login(this.contentProviderLogin, this.contentProviderPassword);                
            } catch (XWootContentProviderException e) {
                throw new XWootException("Problem with login",e);
            }            
            this.logger.info("Content manager connected.");
        } else {
            this.logger.debug("Content manager already connected.");
        }
    }

    public boolean isContentManagerConnected()
    {
        return contentManager.isConnected();
    }

    public void disconnectFromContentManager() throws XWootException
    {
        this.logger.info(this.getXWootName() + " : Disconnect from content provider ");
        this.contentManager.logout();       
        
        this.logger.info(this.getXWootName() + " : Disconnected from content provider ");
    }

    public void reconnectToP2PNetwork() throws XWootException
    {
        this.logger.info(this.getXWootName() + " : (Re)Connect to P2P Network.");
        if (!this.isConnectedToP2PNetwork()) {
            
            // TODO: rejoining of the group will be done in joinNetwork().
            this.joinNetwork(null);            

        } else {
            this.logger.warn(this.getXWootName() + " : Already connected to P2P Network.");
        }
        
        this.logger.info(this.getXWootName() + " : (Re)Connected to P2P Network.");
    }

    public boolean isConnectedToP2PGroup()
    {
        return this.peer.isConnectedToGroup();
    }
    
    /** {@inheritDoc} */
    public boolean hasJoinedAP2PGroup()
    {
        return this.peer.hasJoinedAGroup();
    }
    
    public boolean isConnectedToP2PNetwork()
    {
        //return this.p2Pconnected;
        return this.peer.isConnectedToNetwork();
    }

    public void disconnectFromP2PNetwork() throws XWootException
    {
        this.logger.info(this.getXWootName() + " : Disconnect from P2P Network.");
        
        if (this.isConnectedToP2PNetwork()) {
            if (this.isContentManagerConnected()) {
                try {
                    this.synchronize();
                } catch (Exception e) {
                    // just log it.
                    this.logger.warn(this.getXWootName() + " : Failed to synchronize before disconnecting from network.", e);
                }
            }
            
            // FIXME: anti-entropy with all neighbors is quite useless if we immediately disconnect because it's asynchronously designed.
            try {
                this.doAntiEntropyWithAllNeighbors();
            } catch (Exception e) {
                // just log it.
                this.logger.warn(this.getXWootName() + " : Failed to do anti-entropy with all neighbors before disconnecting from network.", e);
            }
            
            // We could wait a fixed amount of time before disconnecting so that we get up to date, but the users might not like it.
            // Another idea would be to do the actual stopNetwork() call in a thread and immediately return, so that the users don't get blocked,
            //  but this thread would have to check before actually stopping the network if another reconnectToNetwork() call was made in between
            //  and cancel stopping the network if this is the case.
            this.peer.stopNetwork();
        } else {
            this.logger.warn(this.getXWootName() + " : Already disconnected from P2P network.");
        }
        
        this.logger.info(this.getXWootName() + " : Disconnected from P2P Network.");
    }

    /*public void removeNeighbor(String neighborURL) throws XWootException
    {
        try {
            this.peer.removeNeighbor(neighborURL);
        } catch (SenderException e) {
            this.logger.error(this.peerId + " : Problem to remove neighbor \n", e);
            throw new XWootException(this.peerId + " : Problem to remove neighbor \n", e);
        }
    }

    public boolean addNeighbour(String neighborURL)
    {
        return this.getSender().addNeighbor(this.getXWootPeerId(), neighborURL);

    }

    public boolean forceAddNeighbour(String neighborURL)
    {
        return this.getSender().addNeighbor(null, neighborURL);

    }*/

    @SuppressWarnings("unchecked")
    public Collection getNeighborsList()
    {      
        return Collections.list(this.peer.getKnownDirectCommunicationPipeAdvertisements());
    }
    
    @SuppressWarnings("unchecked")
    public Collection getGroups()
    {
        return Collections.list(this.peer.getKnownGroups());
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public AntiEntropy getAntiEntropy()
    {
        return this.antiEntropy;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Peer getPeer()
    {
        return this.peer;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public ThomasRuleEngine getTre()
    {
        return this.tre;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public WootEngine getWootEngine()
    {
        return this.wootEngine;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public XWootContentProviderInterface getContentProvider()
    {
        return this.contentManager;
    }

    public String getContentManagerURL()
    {
        return this.contentManagerURL;
    }

    public String getXWootPeerId()
    {
        return this.peer.getMyPeerID().toString();
    }
    
    public String getXWootName()
    {
        return this.peer.getMyPeerName();
    }

    public String getStateFilePath()
    {        
        return this.stateDirPath + File.separatorChar + getStateFileName();
    }
    
    public String getStateFileName(PeerGroup group)
    {
        if (group == null) {
            throw new NullPointerException("Null peer group provided.");
        }
        
        return STATE_FILE_NAME_PREFIX + STATE_FILE_NAME_SEPARATOR + group.getPeerGroupID().toString() + STATE_FILE_EXTENSION;
    }
    
    public String getStateFileName()
    {
        if (!this.hasJoinedAP2PGroup()) {
            throw new IllegalStateException("Unable to get the state for the currently joined group because this peer has not joined any group yet.");
        }
        
        return this.getStateFileName(this.peer.getCurrentJoinedPeerGroup());
    }

    /**
     * @return the lastModifiedContentIdMap
     */
    public LastPatchAndXWikiXWootId getLastModifiedContentIdMap()
    {
        return this.lastModifiedContentIdMap;
    }

    public List<String> getLastPages(String id) throws XWootException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} **/
    public void jxtaCastProgress(JxtaCastEvent event)
    {        
        if (event.percentDone == 100) {
            if (event.transType == JxtaCastEvent.RECV) {
                this.logger.debug(this.getXWootName() + " : Received a broadcasted message.");
                
                if (!(event.transferedData instanceof Message)) {
                    this.logger.warn(this.getXWootName() + " : Discarding unexpected broadcasted object of type " + event.transferedData.getClass() + " from " + event.sender + "(" + event.senderId + ")");
                    return;
                }
                
                Message message = (Message) event.transferedData;
                try {
                    // Not interested in any reply.
                    this.receiveMessage(message);
                } catch (XWootException e) {
                    this.logger.error(this.getXWootName() + " : Failed to process received message.", e);
                    return;
                }
            } else if (event.transType == JxtaCastEvent.RECV) {
                this.logger.debug(this.getXWootName() + " : Successfuly broadcasted the message.");
            }
        }        
    }

    /** {@inheritDoc} **/
    public void receiveDirectMessage(Object aMessage, ObjectOutputStream oos)
    {
        // TODO: create the class directMessageEvent and include sender.
        this.logger.debug("Directly received a message.");
        if (!(aMessage instanceof Message)) {
            this.logger.warn(this.getXWootName() + " : Discarding unexpected directly sent object of type " + aMessage.getClass() + ".");
            return;
        }
        
        Message message = (Message) aMessage;
        Object reply = null;
        try {
            reply = this.receiveMessage(message);
        } catch (XWootException e) {
            this.logger.error(this.getXWootName() + " : Failed to process received message.", e);
            return;
        }
        
        try {
            oos.writeObject(reply);
        } catch (Exception e) {
            this.logger.error(this.getXWootName() + " : Failed to send back the reply.", e);
            return;
        }
    }

    /** {@inheritDoc} **/
    public Log getLog()
    {
        return this.logger;
    }

    /** {@inheritDoc} */
    public String getWorkingDir()
    {
        return this.workingDir;
    }

    public Date getLastSynchronizationDate()
    {
        return lastSynchronizationDate;
    }

    public String getLastSynchronizationFailure()
    {        
        return lastSynchronizationFailure;
    }
    
}
