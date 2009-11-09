/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.lpbcast.receiver.httpservletreceiver;

import java.io.ObjectInputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.receiver.ReceiverApi;
import org.xwoot.lpbcast.receiver.ReceiverException;

/**
 * Abstract implementation that provides the {@link #processReceiveMessage(HttpServletRequest, HttpServletResponse)}
 * method.
 * 
 * @version $Id$
 */
public abstract class AbstractHttpServletReceiver extends HttpServlet implements ReceiverApi
{
    /** The HTTP header field set by the Neighbor test. */
    public static final String HTTP_CONNECTED_HEADER_FIELD = "Connected";
    
    /** The HTTP header field value set by the Neighbor test on success. */
    public static final String HTTP_CONNECTED_HEADER_OK_VALUE = "true";
    
    /** The HTTP header field for content-type. */
    public static final String HTTP_CONTENT_TYPE_HEADER = "Content-type";
    
    /** The HTTP header value for content-type when an anti-entropy message is sent. */
    public static final String HTTP_CONTENT_TYPE_VALUE_FOR_ANTI_ENTROPY = "text/plain";
    
    /** The HTTP header value for content-type when a state transfer message is sent. */
    public static final String HTTP_CONTENT_TYPE_VALUE_FOR_STATE = "application/zip";

    /** Servlet request parameter for neighbor test. */
    public static final String NEIGHBOR_TEST_REQUEST_PARAMETER = "test";

    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -3497389707414403588L;

    /** Used for logging. */
    public Log log;

    /**
     * Creates a new instance.
     */
    public AbstractHttpServletReceiver()
    {
        this.log = LogFactory.getLog(this.getClass());
    }

    /**
     * Reads a message from the {@link HttpServletRequest} and forwards it to the {@link #receive(Message)} method.
     * 
     * @param request the HTTP request.
     * @param response the HTTP response.
     * @throws ReceiverException if problems occur processing the message.
     */
    public void processReceiveMessage(HttpServletRequest request, HttpServletResponse response)
        throws ReceiverException
    {
        if (this.isReceiverConnected()) {
            this.log.info("Site " + this.getPeerId() + " : Receive message -");

            if (request.getParameter(NEIGHBOR_TEST_REQUEST_PARAMETER) != null) {
                this.log.info("It's a neighbor test... ");
            } else {
                Message message = null;
                ObjectInputStream ois = null;

                try {
                    ois = new ObjectInputStream(request.getInputStream());
                    message = (Message) ois.readObject();
                } catch (Exception e) {
                    throw new HttpServletReceiverException(this.getPeerId()
                        + " : Problem reading message from http connexion.\n", e);
                } finally {
                    try {
                        if (ois != null) {
                            ois.close();
                        }
                    } catch (Exception e) {
                        this.log.error("Failed to close the request input stream.");
                    }
                }

                this.receive(message);
            }
        }
    }
}
