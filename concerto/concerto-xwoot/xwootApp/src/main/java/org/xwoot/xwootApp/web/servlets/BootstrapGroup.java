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
package org.xwoot.xwootApp.web.servlets;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * Servlet handling group management.
 * 
 * @version $Id$
 */
public class BootstrapGroup extends HttpServlet
{
    /** The request attribute trough which to send the list of groups to the jsp. */
    private static final String AVAILABLE_GROUPS_ATTRIBUTE = "groups";
    
    /** The value of the join group button. */
    private static final String JOIN_BUTTON = "Join";
    
    /** The value of the create group button. */
    private static final String CREATE_BUTTON = "Create";
    
    private static final String AUTO_JOIN_GROUP = "autoJoinGroup";
    
    /** The value of a checked checkbox. */
    private static final String TRUE = "true";
    
    /** The default keystore password for jxta's authentication protocol. */
    private static final char[] KEYSTORE_PASSWORD = "concerto".toCharArray();
    
    /** Used for serialization. */
    private static final long serialVersionUID = -3758874922535817475L;
    
    /** The name of the properties file where to store the current group settings. */
    private static final String P2P_GROUP_SETTINGS_PROPERTIES_FILE_NAME = "p2p-group-settings.properties";

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        this.getServletContext().log("BootstrapGroup opened.");
        
        String errors = "";
        
        XWootAPI xwootEngine = XWootSite.getInstance().getXWootEngine();
        
        if (!xwootEngine.isConnectedToP2PNetwork()) {
            this.getServletContext().log("Please connect to a network first.");
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapNetwork.do"));
            return;
        }
        
        String groupChoice = request.getParameter("groupChoice");
        String AbsolutePathToPropertiesFile =  xwootEngine.getPeer().getManager().getInstanceHome().getPath() + File.separator + BootstrapGroup.P2P_GROUP_SETTINGS_PROPERTIES_FILE_NAME;
        Properties groupProperties = XWootSite.getProperties(AbsolutePathToPropertiesFile);
        String currentGroupAdvertisementXMLString = groupProperties.getProperty("current_group_advertisement");
        
        if (!xwootEngine.hasJoinedAP2PGroup() && currentGroupAdvertisementXMLString != null && currentGroupAdvertisementXMLString.trim().length() != 0) {
            groupChoice = AUTO_JOIN_GROUP;
            
            try {
                StringReader advertisementContentReader = new StringReader(currentGroupAdvertisementXMLString);
                XMLDocument advertisementXmlDocument = (XMLDocument) StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, advertisementContentReader);
                
                PeerGroupAdvertisement currentGroupAdvertisement =  (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(advertisementXmlDocument);
                String groupPassword = groupProperties.getProperty("current_group_password", "");
                boolean beRendezVous = "true".equalsIgnoreCase(groupProperties.getProperty("current_group_be_rendezvous"));
                
                xwootEngine.joinGroup(currentGroupAdvertisement, KEYSTORE_PASSWORD, groupPassword.toCharArray(), beRendezVous);
            } catch (Exception e) {
                errors += "Failed to auto-rejoin group: Invalid existing group properties.";
            }
        } else if (CREATE_BUTTON.equals(groupChoice)) {
            this.getServletContext().log("Create group requested.");
            
            String groupName = request.getParameter("groupName");
            String groupDescription = request.getParameter("groupDescription");
            boolean isPrivateGroup = TRUE.equals(request.getParameter("isPrivateGroup"));
            String groupPassword = request.getParameter("createGroupPassword");
            String groupPasswordRetyped = request.getParameter("createGroupPasswordRetyped");
            //String keystorePassword = request.getParameter("createKeystorePassword");
            
            try {
                if (groupName == null || groupName.trim().length() == 0) {
                    throw new IllegalArgumentException("Group name must not be empty.");
                }
                
                PeerGroupAdvertisement newGroupAdvertisement = null;
                
                if (isPrivateGroup) {
                    if (groupPassword == null || groupPassword.length() == 0) {
                        throw new IllegalArgumentException("A password must be set for a private group.");
                    }
                    if (!(groupPassword.equals(groupPasswordRetyped))) {
                        throw new IllegalArgumentException("Passwords do not match.");
                    }
                    
                    newGroupAdvertisement = xwootEngine.createNewGroup(groupName, groupDescription, KEYSTORE_PASSWORD/*keystorePassword.toCharArray()*/, groupPassword.toCharArray());
                } else {
                    newGroupAdvertisement = xwootEngine.createNewGroup(groupName, groupDescription, null, null);
                }
                
                // Save the group so we can join it next time.
                String groupAdvertisementAsXMLString = newGroupAdvertisement.getDocument(MimeMediaType.XMLUTF8).toString();
                groupProperties.setProperty("current_group_advertisement", groupAdvertisementAsXMLString);
                groupProperties.setProperty("current_group_password", (groupPassword == null ? "" : groupPassword));
                groupProperties.setProperty("current_group_be_rendezvous", "true");
                
                XWootSite.savePropertiesInFile(AbsolutePathToPropertiesFile, "Updated group settings", groupProperties);
                
                
            } catch (Exception e) {
                errors += "Can't create group: " + e.getMessage() + "\n";
            }
            
        } else if (JOIN_BUTTON.equals(groupChoice)) {
            this.getServletContext().log("Join group requested.");
            //
            String groupPassword = request.getParameter("joinGroupPassword");
            //String keystorePassword = request.getParameter("joinGroupKeystorePassword");
            
            boolean beRendezVous = TRUE.equals(request.getParameter("beRendezVous"));
            
            String groupID = request.getParameter("groupID");
            if (groupID == null || groupID.length() == 0) {
                errors += "Please select a group to join first.";
            } else {
                try {
                    Collection groups = xwootEngine.getGroups();
                    boolean found = false;
                    for (Object group : groups) {
                        PeerGroupAdvertisement aGroupAdv = (PeerGroupAdvertisement) group;
                        if (aGroupAdv.getPeerGroupID().toString().equals(groupID)) {
                            this.log("Joining group described by this adv:\n" + aGroupAdv);
                            
                            // Join the group.
                            xwootEngine.joinGroup(aGroupAdv, KEYSTORE_PASSWORD, groupPassword.toCharArray(), beRendezVous);
                            
                            // Save the group so we can join it next time.
                            String groupAdvertisementAsXMLString = aGroupAdv.getDocument(MimeMediaType.XMLUTF8).toString();
                            groupProperties.setProperty("current_group_advertisement", groupAdvertisementAsXMLString);
                            groupProperties.setProperty("current_group_password", groupPassword);
                            groupProperties.setProperty("current_group_be_rendezvous", (beRendezVous ? "true" : "false"));
                            
                            XWootSite.savePropertiesInFile(AbsolutePathToPropertiesFile, "Updated group settings", groupProperties);
                            
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        errors += "Invalid group selected or it has expired.";
                    }
                } catch (Exception e) {
                    String message = null;
                    if (e.getMessage() == null) {
                        message = e.getClass().getName();
                    } else {
                        message = e.getMessage();
                    }
                    
                    errors += "Can't join group: " + message + ".";
                }
            }
        }
        
        // If no errors were encountered and successfully joined/created a group, go to next step.
        if (errors.length() == 0 && xwootEngine.hasJoinedAP2PGroup()/*(CREATE_BUTTON.equals(groupChoice) || JOIN_BUTTON.equals(groupChoice) || AUTO_JOIN_GROUP.equals(groupChoice))*/) {
            this.getServletContext().log("No errors occured.");
            
//            // Stop the autosynch thread if it is running.
//            XWootSite.getInstance().getAutoSynchronizationThread().stopThread();
            
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/stateManagement.do"));
            return;
        } else {
            this.getServletContext().log("Errors occured or invalid group choice.");
        }
        
        try {
            request.setAttribute(AVAILABLE_GROUPS_ATTRIBUTE, xwootEngine.getGroups());
            this.getServletContext().log("Available groups: " + request.getAttribute(AVAILABLE_GROUPS_ATTRIBUTE));
        } catch (Exception e) {
            request.setAttribute(AVAILABLE_GROUPS_ATTRIBUTE, new ArrayList());
            errors += "Failed to list groups: " + e.getMessage();
        }

        // If any.
        request.setAttribute("errors", errors);
        
        // No button clicked yet or an error occurred. Display the network boostrap page.
        request.getRequestDispatcher("/pages/BootstrapGroup.jsp").forward(request, response);
        return;

    }
}
