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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Daemon thread launched to handle an incoming connection and to send back a
 * reply if there is one.
 * 
 * @version $Id:$
 */
class ConnectionThread extends Thread {
	Socket socket;
	DirectMessageReceiver receiver;

	ConnectionThread(Socket socket, DirectMessageReceiver receiver) {
		super("DirectCommunication:ConnectionThread");
		this.socket = socket;
		this.receiver = receiver;
	}

	/** {@inheritDoc} **/
	@Override
	public void run() {
		InputStream is = null;
		OutputStream os = null;
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		try {
		    
			is = this.socket.getInputStream();
			os = this.socket.getOutputStream();
			ois = new ObjectInputStream(is);
			oos = new ObjectOutputStream(os);

			Object message = ois.readObject();
			receiver.receiveDirectMessage(message, oos);

			// Make sure to flush in case receiver did not.
			oos.flush();
			os.flush();
		} catch (Exception e) {
			this.receiver.getLog().error(
					"Failed to receive message or to send reply.", e);
		} finally {
			try {
				if (ois != null) {
					ois.close();
				}
				if (is != null) {
					is.close();
				}

				if (oos != null) {
					oos.close();
				}
				if (os != null) {
					os.close();
				}

				if (this.socket != null) {
					socket.close();
				}
			} catch (Exception e) {
				// Just log it.
				this.receiver.getLog().warn(
						"Failed to close streams for this conenction.");
			}
		}

		// die.
	}
}
