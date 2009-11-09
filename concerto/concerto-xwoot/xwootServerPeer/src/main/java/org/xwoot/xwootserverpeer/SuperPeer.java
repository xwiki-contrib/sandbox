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
package org.xwoot.xwootserverpeer;

import org.xwoot.jxta.NetworkManager;
import org.xwoot.jxta.Peer;

import net.jxta.exception.JxtaException;
import net.jxta.platform.NetworkConfigurator;

/**
 * Defines a Super peer that will ensure connectivity and for a network.
 * <p>
 * This peer does nothing except route communication, log events and ensures network existence.
 * 
 * @version $Id$
 */
public interface SuperPeer
{
    /** @return the network manager. */
    NetworkManager getManager();

    /** @return the network configurator. */
    NetworkConfigurator getConfigurator();

    /**
     * Starts the network.
     * 
     * @throws JxtaException if problems occurred while starting the network.
     */
    void startNetwork() throws JxtaException;

    /** Stops the network. */
    void stopNetwork();

    /** @return the peer associated with this instance. */
    Peer getPeer();

    /** @return this peer's name. */
    String getPeerName();
}
