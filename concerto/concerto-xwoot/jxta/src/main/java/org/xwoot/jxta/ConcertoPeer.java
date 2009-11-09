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
package org.xwoot.jxta;

import net.jxta.document.Advertisement;
import net.jxta.exception.JxtaException;
import net.jxta.exception.PeerGroupException;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.jxtacast.event.JxtaCastEvent;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.jxta.message.Message;

/**
 * Proof of Concept TUI application using the JXTA platform trough the implementation
 * {@link JxtaPeer} of the interface {@link Peer}.
 * 
 * @version $Id$
 */
public class ConcertoPeer implements JxtaCastEventListener, DirectMessageReceiver {

	Peer jxta;
	Log logger;

	public ConcertoPeer() {
		jxta = new JxtaPeer();
		logger = LogFactory.getLog(this.getClass());
	}
	
	public void startNetwork() throws Exception {
		jxta.configureNetwork("ConcertoPeer"+UUID.randomUUID().toString(), null, ConfigMode.EDGE);
		//jxta.getManager().setUseDefaultSeeds(true);
		
		
		jxta.getManager().getConfigurator().addSeedRendezvous(new URI("tcp://localhost:9701"));
		jxta.getManager().getConfigurator().addSeedRendezvous(new URI("http://localhost:9700"));
		
		jxta.getManager().getConfigurator().addSeedRelay(new URI("tcp://localhost:9701"));
		jxta.getManager().getConfigurator().addSeedRelay(new URI("http://localhost:9700"));
		
		
		jxta.startNetworkAndConnect(this, this);
	}

	/**
	 * main
	 * 
	 * @param args
	 *            command line args
	 */
	public static void main(String args[]) {
		ConcertoPeer peer = new ConcertoPeer();

		try {
			peer.startNetwork();
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		while (true) {
			int option = 0;
			while (option <= 0 || option > 4 ) {
				System.out.println("> ");
	
				System.out.println("Please select an action for this peer:");
				System.out.println("1. Join a group.");
				System.out.println("2. Create a public group.");
				System.out.println("3. Create a private group.");
				System.out.println("4. List known peers in the current group.");
				System.out.println("5. Send a small object to a group.");
				System.out.println("6. Show jxta status.");
				System.out.println("7. Send a small object directly to a single group member.");
				System.out.println("8. Send a large object directly to a single group member.");
				System.out.println("9. Send my direct communication pipe adv directly to a single group member.");
				System.out.println("10. Send my direct communication pipe adv directly to a single random group member.");
				System.out.println("11. Combine 1 with 10 and get a normal get-state request when joining a group for the first time.");
				System.out.println("12. Leave the current peer group.");
	
				BufferedReader bis = new BufferedReader(new InputStreamReader(
						System.in));
	
				try {
					option = Integer.parseInt(bis.readLine());
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				if (option == 1) {
					// Start looking for messages and receiving them.
					peer.joinAGroup();
				} else if (option == 2) {
					peer.createAPublicGroup();
				} else if (option == 3) {
					peer.createAPrivateGroup();
				} else if (option == 4) {
					peer.listKnownPeersInCurrentGroup();
				} else if (option == 5) {
					// Send a small message.
					peer.sendSmallObjectToCurrentGroup();
				} else if (option == 6) {
					peer.getStatus();
				} else if (option == 7) {
                    peer.sendSmallObjectThroughDirectCommunication();
                } else if (option == 8) {
                    peer.sendLargeObjectThroughDirectCommunication();
                } else if (option == 9) {
                    peer.sendMyPipeAdvertisementThroughDirectCommunication();
                } else if (option == 10) {
                    peer.sendMyPipeAdvertisementThroughDirectCommunicationWithRandomPeer();
                } else if (option == 11) {
                    peer.joinAGroupAndSendMyPipeAdvertisementThroughDirectCommunicationWithRandomPeer();
                } else if (option == 12) {
                    peer.leaveCurrentGroup();
                } else {
					option = 0;
					System.out.println("Invalid option. Try again.");
				}
			}
		}
	}
	
	public void joinAGroup() {
		Enumeration<PeerGroupAdvertisement> en = this.jxta.getKnownGroups();
		
		List<PeerGroupAdvertisement> list = new ArrayList<PeerGroupAdvertisement>();
		
		System.out.println("Available groups:");
		while (en.hasMoreElements()) {
			PeerGroupAdvertisement adv = en.nextElement();
			list.add(adv);
			System.out.println(list.size() - 1 + ": " + adv.getName());
		}
		
		int selection = -1;
		System.out.println("Select: ");
		BufferedReader bis = new BufferedReader(new InputStreamReader(
				System.in));

		try {
			selection = Integer.parseInt(bis.readLine());
		} catch (Exception e) {
			System.out.println("Bad Value, canceled.");
			selection = -1;
			return;
		}
		
		try {
			char[] keystorePassword = null;
			char[] groupPassword = null;
			
			PeerGroup selectedGroup = jxta.getDefaultGroup().newGroup(list.get(selection));
			if (selectedGroup.getMembershipService() instanceof PSEMembershipService) {
				System.out.println("This group is a private group and requires authentication.");
				
				System.out.println("Please enter the keystore password: ");
				keystorePassword = bis.readLine().toCharArray();
				
				System.out.println("Please enter the group's password: ");
				groupPassword = bis.readLine().toCharArray();
			}
			
			jxta.joinPeerGroup(list.get(selection), keystorePassword, groupPassword, false);
		} catch (Exception e) {
			System.out.println("Failed to join the group.");
			e.printStackTrace();
			return;
		}
	}
	
	public void createAPublicGroup() {
		System.out.println("Please enter a group name: ");
		BufferedReader bis = new BufferedReader(new InputStreamReader(
				System.in));
		
		String groupName = null;
		try {
			groupName = bis.readLine();
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		try {
			jxta.createNewGroup(groupName, "A new test group", null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void createAPrivateGroup() {
		String groupName = null;
		char[] keystorePassword = null;
		char[] groupPassword = null;

		try {
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					System.in));

			System.out.println("Please enter a group name: ");
			groupName = bis.readLine();

			System.out.println("Please enter the keystore password: ");
			keystorePassword = bis.readLine().toCharArray();

			System.out.println("Please enter the group's password: ");
			groupPassword = bis.readLine().toCharArray();
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}

		try {
			jxta.createNewGroup(groupName, "A new test group", keystorePassword, groupPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void listKnownPeersInCurrentGroup() {
		System.out.println("Current group: " + jxta.getCurrentJoinedPeerGroup());
		
		System.out.println("Peer advertisements:");
		Enumeration<PeerAdvertisement> peers = jxta.getKnownPeers();
		if (peers != null) {
			while(peers.hasMoreElements()) {
				PeerAdvertisement peer = peers.nextElement();
				System.out.println(" " + peer.getName() + " (PeerID" + peer.getID() + ")");
			}
		}
		
		System.out.println("Pipe advertisements:");
		Enumeration<Advertisement> pipeAdvs = jxta.getKnownDirectCommunicationPipeAdvertisements();
		if (pipeAdvs != null) {
		    while(pipeAdvs.hasMoreElements()) {
		        PipeAdvertisement pipeAdv = (PipeAdvertisement) pipeAdvs.nextElement();
		        System.out.println(" " + pipeAdv.getName() + " (PipeID: " + pipeAdv.getID() + ")");
		    }
		}
	}
	
	
	public void getStatus() {
		
		System.out.println("Status: ");
		System.out.println("jxta.isNetworkConfigured() : " + jxta.isNetworkConfigured());
		System.out.println("jxta.isJxtaStarted() : " + jxta.isJxtaStarted()); 
		System.out.println("jxta.isConnectedToNetworkRendezVous() : " + jxta.isConnectedToNetworkRendezVous());
		System.out.println("jxta.isConnectedToNetwork() : " + jxta.isConnectedToNetwork());
		System.out.println("jxta.hasJoinedAGroup() : " + jxta.hasJoinedAGroup());
		System.out.println("jxta.isGroupRendezVous() : " + jxta.isGroupRendezVous());
		System.out.println("jxta.isConnectedToGroupRendezVous() : " + jxta.isConnectedToGroupRendezVous());
		System.out.println("jxta.isConnectedToGroup() : " + jxta.isConnectedToGroup());

	}
	
	
	public void sendSmallObjectToCurrentGroup() {
		System.out.println("Current group: " + jxta.getCurrentJoinedPeerGroup().getPeerGroupName());
		
		String smallObject = "Small object transfer";
		
		try {
			jxta.sendObject(smallObject, "small object");
		} catch (PeerGroupException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void sendSmallObjectThroughDirectCommunication() {
	    System.out.println("Current group: " + jxta.getCurrentJoinedPeerGroup());
	    
	    Enumeration<Advertisement> en = this.jxta.getKnownAdvertisements(PipeAdvertisement.NameTag, jxta.getDirectCommunicationPipeNamePrefix() + "*");
        
        List<PipeAdvertisement> list = new ArrayList<PipeAdvertisement>();
        
        System.out.println("Available group members' pipes:");
        while (en.hasMoreElements()) {
            Advertisement adv = en.nextElement();
            if (!(adv instanceof PipeAdvertisement) || adv.equals(jxta.getMyDirectCommunicationPipeAdvertisement())) {
                continue;
            }
            
            PipeAdvertisement pipeAdv = (PipeAdvertisement) adv;
            list.add(pipeAdv);
            System.out.println(list.size() - 1 + ": " + pipeAdv.getName());
        }
        
        int selection = -1;
        System.out.println("Select: ");
        BufferedReader bis = new BufferedReader(new InputStreamReader(
                System.in));

        try {
            selection = Integer.parseInt(bis.readLine());
        } catch (Exception e) {
            System.out.println("Bad Value, canceled.");
            selection = -1;
            return;
        }
        
        String[] messageIds = {"first message id", "second message id"};
        Object reply = null;
        try {
            reply = jxta.sendObject(messageIds, list.get(selection));
        } catch (JxtaException je) {
            System.out.println("Problem sending the message: ");
            je.printStackTrace();
            return;
        }
        System.out.println("Received reply: " + reply);    
	}
	
	public void sendLargeObjectThroughDirectCommunication() {
        System.out.println("Current group: " + jxta.getCurrentJoinedPeerGroup());
        
        Enumeration<Advertisement> en = this.jxta.getKnownAdvertisements(PipeAdvertisement.NameTag, jxta.getDirectCommunicationPipeNamePrefix() + "*");
        
        List<PipeAdvertisement> list = new ArrayList<PipeAdvertisement>();
        
        System.out.println("Available group members' pipes:");
        while (en.hasMoreElements()) {
            Advertisement adv = en.nextElement();
            if (!(adv instanceof PipeAdvertisement) || adv.equals(jxta.getMyDirectCommunicationPipeAdvertisement())) {
                continue;
            }
            
            PipeAdvertisement pipeAdv = (PipeAdvertisement) adv;
            list.add(pipeAdv);
            System.out.println(list.size() - 1 + ": " + pipeAdv.getName());
        }
        
        int selection = -1;
        System.out.println("Select: ");
        BufferedReader bis = new BufferedReader(new InputStreamReader(
                System.in));

        try {
            selection = Integer.parseInt(bis.readLine());
        } catch (Exception e) {
            System.out.println("Bad Value, canceled.");
            selection = -1;
            return;
        }
        
        List<String> bigObject = new ArrayList<String>();
        String line = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        StringBuilder addThis = new StringBuilder(line);
        for(int i=0; i<300; i++) {
            bigObject.add(addThis.toString());
            addThis.append(line);
        }
        
        System.out.println("Sending " + bigObject.toString().getBytes().length + " bytes...");
        
        Object reply = null;
        try {
            reply = jxta.sendObject(bigObject.toArray(), list.get(selection));
        } catch (JxtaException je) {
            System.out.println("Problem sending the message: ");
            je.printStackTrace();
            return;
        }
        
        System.out.println("Received reply:" + reply); 
    }
	
	public void sendMyPipeAdvertisementThroughDirectCommunication() {
        System.out.println("Current group: " + jxta.getCurrentJoinedPeerGroup());
        if (!jxta.isConnectedToGroup()) {
            System.out.println("Join a group first! (other than de default netPeerGroup)");
            return;
        }
        
        Enumeration<Advertisement> en = this.jxta.getKnownAdvertisements(PipeAdvertisement.NameTag, jxta.getDirectCommunicationPipeNamePrefix() + "*");
        
        List<PipeAdvertisement> list = new ArrayList<PipeAdvertisement>();
        
        System.out.println("Available group members' pipes:");
        while (en.hasMoreElements()) {
            Advertisement adv = en.nextElement();
            if (!(adv instanceof PipeAdvertisement) || adv.equals(jxta.getMyDirectCommunicationPipeAdvertisement())) {
                continue;
            }
            
            PipeAdvertisement pipeAdv = (PipeAdvertisement) adv;
            list.add(pipeAdv);
            System.out.println(list.size() - 1 + ": " + pipeAdv.getName());
        }
        
        int selection = -1;
        System.out.println("Select: ");
        BufferedReader bis = new BufferedReader(new InputStreamReader(
                System.in));

        try {
            selection = Integer.parseInt(bis.readLine());
        } catch (Exception e) {
            System.out.println("Bad Value, canceled.");
            selection = -1;
            return;
        }
        
        Map<String, Object> pipeAdvData = new HashMap<String, Object>();
        pipeAdvData.put("PIPE_ID", jxta.getMyDirectCommunicationPipeAdvertisement().getPipeID());
        pipeAdvData.put("PIPE_TYPE", jxta.getMyDirectCommunicationPipeAdvertisement().getType());
        
        Object reply = null;
        try {
            reply = jxta.sendObject(pipeAdvData, list.get(selection));
        } catch (JxtaException je) {
            System.out.println("Problem sending the message: ");
            je.printStackTrace();
        }
        
        System.out.println("Received reply:" + reply); 
    }
	
	public void sendMyPipeAdvertisementThroughDirectCommunicationWithRandomPeer() {
        System.out.println("Current group: " + jxta.getCurrentJoinedPeerGroup());
        if (!jxta.isConnectedToGroup()) {
            System.out.println("Join a group first! (other than de default netPeerGroup)");
            return;
        }
        
        Map<String, Object> pipeAdvData = new HashMap<String, Object>();
        pipeAdvData.put("PIPE_ID", jxta.getMyDirectCommunicationPipeAdvertisement().getPipeID());
        pipeAdvData.put("PIPE_TYPE", jxta.getMyDirectCommunicationPipeAdvertisement().getType());
        
        Object reply = null;
        try {
            reply = jxta.sendObjectToRandomPeerInGroup(pipeAdvData, true);
        } catch (JxtaException je) {
            System.out.println("Problem sending the message: ");
            je.printStackTrace();
            return;
        }
        
        System.out.println("Received reply:" + reply); 
    }
	
	public void joinAGroupAndSendMyPipeAdvertisementThroughDirectCommunicationWithRandomPeer() {
	    this.joinAGroup();
	    this.sendMyPipeAdvertisementThroughDirectCommunicationWithRandomPeer();
	}

    public void jxtaCastProgress(JxtaCastEvent e) {
	    if (e.transType == JxtaCastEvent.RECV) {
	        System.out.println("Received: ");
	    } else {
	        System.out.println("Sent: ");
	    }
	    System.out.println("% complete:" + e.percentDone);
	    System.out.println("senderID:" + e.senderId);
	    System.out.println("transferedData: " + e.transferedData);
	    
		if (e.percentDone == 100) {
			System.out.println("[CONCERTO] Transer complete!");
			if (e.transType == JxtaCastEvent.RECV) {
				System.out.println("[CONCERTO] Received: " + e.transferedData.getClass());
				if (e.transferedData instanceof String) {
					String data = (String) e.transferedData;
					System.out.println("Object size: " + data.getBytes().length);
				}
				/*if (e.transferedData instanceof Map) {
				    System.out.println("Sending back reply...");
				    
				    Map<String, Object> pipeAdvData = (Map<String, Object>) e.transferedData;
				    String pipeType = (String) pipeAdvData.get("PIPE_TYPE");
				    ID pipeID = (ID) pipeAdvData.get("PIPE_ID");
				    
				    PipeAdvertisement pipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
                    pipeAdv.setType(pipeType);
                    pipeAdv.setPipeID(pipeID);
				    
				    boolean sent = false;
			        try {
			            sent = jxta.sendObject("ok!", "anti-entropy reply", pipeAdv);
			        } catch (JxtaException je) {
			            System.out.println("Problem sending the message: ");
			            je.printStackTrace();
			        }
			        System.out.println("Reply successfuly sent: " + sent); 
				}*/
			}
		}
	}
	
//	// Daemon
//	class ConnectionHandler extends Thread
//	{
//	    JxtaServerSocket serverSocket;
//	    ConcertoPeer concerto;
//	    
//	    ConnectionHandler(JxtaServerSocket serverSocket, ConcertoPeer concerto)
//	    {
//	        this.serverSocket = serverSocket;
//	        this.concerto = concerto;
//	    }
//	    
//	    /** {@inheritDoc} **/
//	    @Override
//	    public void run()
//	    {
//	        while (true) {
//	            try {
//                    Socket socket = serverSocket.accept();
//                    new ConnectionThread(socket, this.concerto).start();
//                } catch (IOException e) {
//                    System.out.println("Error: Failed to accept conenction from client.");
//                    e.printStackTrace();
//                }
//	        }
//	    }
//	}
//	
//	// One thread per connection.
//	class ConnectionThread extends Thread
//	{
//	    Socket socket;
//	    ConcertoPeer concerto;
//	    
//	    ConnectionThread(Socket socket, ConcertoPeer concerto)
//	    {
//	        this.socket = socket;
//	        this.concerto = concerto;
//	    }
//	    
//	    /** {@inheritDoc} **/
//	    @Override
//	    public void run()
//	    {
//	        try {
//    	        InputStream is = socket.getInputStream();
//    	        OutputStream os = socket.getOutputStream();
//    	        ObjectInputStream ois = new ObjectInputStream(is);
//    	        ObjectOutputStream oos = new ObjectOutputStream(os);
//    	        Object readMessage = ois.readObject();
//    	        
//    	        // Close inputs
//    	        ois.close();
//    	        is.close();
//    	        
//    	        concerto.receiveMessage(readMessage, oos);
//    	        
//    	        oos.close();
//    	        os.close();
//	        } catch (Exception e) {
//	            System.out.println("Failed to receive message.");
//	            e.printStackTrace();
//	        }
//	        
//	    }
//	}

    /** {@inheritDoc} **/
    public Log getLog()
    {
        return this.logger;
    }

    /** {@inheritDoc} **/
    public void receiveDirectMessage(Object aMessage, ObjectOutputStream oos)
    {
        System.out.println("Received object: " + aMessage);
        
        if (aMessage instanceof Map) {
            try {
                oos.writeObject("Ok!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if (aMessage instanceof Message) {
            Message message = (Message) aMessage;
            System.out.println("Message:");
            System.out.println("message.getAction() : " + message.getAction());
            System.out.println("message.getOriginalPeerId() : " + message.getOriginalPeerId());
            System.out.println("message.getContent() : " + message.getContent());
        }
        
    }
    
    public void leaveCurrentGroup() {
        try {
            System.out.println("Current peer group: " + jxta.getCurrentJoinedPeerGroup());
            jxta.leavePeerGroup();
        } catch (PeerGroupException e) {
            System.out.println("Failed to leave peer group:");
            e.printStackTrace();
        }
    }
	
}
