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
package org.xwoot.jxta.test.multiplePeers;

import java.io.ObjectOutputStream;

import net.jxta.jxtacast.event.JxtaCastEvent;

import org.apache.commons.logging.Log;

/**
 * This is not really a test, but a support peer that is being started for all tests so that they have a network to
 * connect to.
 * 
 * @version $Id$
 */
public class ServerPeerUsedForTests extends AbstractMultiplePeersTestCase
{

    /** {@inheritDoc} **/
    @Override
    public Boolean init(String peerName, Boolean networkCreator)
    {
    Boolean superInit = super.init(peerName, networkCreator);
    try {
        this.peer.getManager().getConfigurator().setTcpPort(9701);
        //this.peer.getManager().getConfigurator().setHttpPort(9700);
        } catch (Exception e) {
            return Boolean.FALSE;
        }

        return superInit;       
    }
    
    /** {@inheritDoc} **/
    public void run()
    {
        // Thread does nothing but handle jxta platform specific actions.
        // Thread dies when tests stop.
    }

    /** {@inheritDoc} **/
    public void jxtaCastProgress(JxtaCastEvent e)
    {
        // Thread does not do communication.
    }

    /** {@inheritDoc} **/
    public Log getLog()
    {
        // Thread does not do communication.
        return null;
    }

    /** {@inheritDoc} **/
    public void receiveDirectMessage(Object message, ObjectOutputStream oos)
    {
        // Thread does not do communication.
    }

}
