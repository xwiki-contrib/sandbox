package org.xwoot.lpbcast.sender.httpservletlpbcast;

/**
 * Implements the connected state of a sender.
 * 
 * @version $Id$
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;

import java.io.IOException;

import java.io.OutputStream;

import java.net.URL;
import java.util.Collection;
import java.net.HttpURLConnection;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.receiver.httpservletreceiver.AbstractHttpServletReceiver;
import org.xwoot.lpbcast.sender.AbstractLpbCastState;
import org.xwoot.lpbcast.util.NetUtil;

/**
 * Implements the connected state of a sender.
 * 
 * @version $Id$
 */
public class HttpServletLpbCastStateConnected extends AbstractLpbCastState
{
    /**
     * Creates a new instance.
     * 
     * @param connection the connection instance that this state belongs to.
     */
    public HttpServletLpbCastStateConnected(HttpServletLpbCast connection)
    {
        super(connection);
    }

    /** {@inheritDoc} */
    public void connectSender()
    {
        throw new IllegalStateException(this.connection.getSiteId() + " - Already connected");
    }

    /** {@inheritDoc} */
    public void disconnectSender()
    {
        // disconnect somehow
        // finally set disconnected state of the connection instance
        this.connection.setState(this.connection.disconnectedState);
    }

    /** {@inheritDoc} */
    public boolean isSenderConnected()
    {
        return true;
    }

    /** {@inheritDoc} */
    public boolean addNeighbor(Object from, Object neighbor)
    {
        if (neighbor == null || neighbor.equals(from) || neighbor.equals("")) {
            this.connection.logger.debug(this.connection.getSiteId() + " - Void neighbor or same : " + neighbor);
            return false;
        }

        String neighborURL = "";
        HttpURLConnection init = null;
        try {
            if (this.getNeighborsList().contains(neighbor)) {
                return false;
            }

            neighborURL = NetUtil.normalize((String) neighbor);

            if (from == null) {
                this.getNeighbors().addNeighbor(neighborURL);
                this.connection.logger.debug(this.connection.getSiteId() + " - Neighbor successfuly added.");
                return true;
            }

            URL to = new URL(neighborURL + HttpServletLpbCast.SEND_NEIGHBOR_TEST_PATH + from);
            
            this.connection.logger.debug("Test neighbor: " + to);

            init = (HttpURLConnection) to.openConnection();
            init.connect();
            String response = init.getHeaderField(AbstractHttpServletReceiver.HTTP_CONNECTED_HEADER_FIELD);

            this.connection.logger.debug("Response: " + response);
            
            if (response != null && response.equals(AbstractHttpServletReceiver.HTTP_CONNECTED_HEADER_OK_VALUE)) {
                this.getNeighbors().addNeighbor(neighborURL);
                this.connection.logger.debug(this.connection.getSiteId() + " - Neighbor tested and successfuly added.");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (init != null) {
                init.disconnect();
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    public void gossip(Message message) throws HttpServletLpbCastException
    {
        this.connection.logger.info(this.connection.getSiteId() + " - Send message to all neighbors\n\n");
        this.connection.getNeighbors().notifyNeighbors(message);
    }

    /** {@inheritDoc} */
    public void processSendState(HttpServletResponse response, File state) throws HttpServletLpbCastException
    {
        if (state != null) {
            this.connection.logger.debug(this.connection.getSiteId() + " - Send state.");
            response.setHeader(AbstractHttpServletReceiver.HTTP_CONTENT_TYPE_HEADER,
                AbstractHttpServletReceiver.HTTP_CONTENT_TYPE_VALUE_FOR_STATE);
            response.setHeader("Content-Disposition", "attachment; filename=" + state.getName());

            OutputStream out = null;
            FileInputStream fis = null;
            try {
                out = response.getOutputStream();
                fis = new FileInputStream(state);
                byte[] buffer = new byte[2048];
                int bytesIn = 0;

                while ((bytesIn = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesIn);
                }

                out.flush();
            } catch (IOException e) {
                throw new HttpServletLpbCastException(this.connection.getSiteId()
                    + " - Problem writing message to http request.\n", e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                } catch (Exception e) {
                    this.connection.logger.error(this.connection.getSiteId()
                        + " - Problems closing streams for send state.");
                }
            }

        } else {
            response.setHeader(AbstractHttpServletReceiver.HTTP_CONNECTED_HEADER_FIELD, null);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void processSendAE(HttpServletResponse response, Collection diff) throws HttpServletLpbCastException
    {
        if (diff != null) {
            this.connection.logger.debug(this.connection.getSiteId() + " - Send anti-entropy.");
            response.setHeader(AbstractHttpServletReceiver.HTTP_CONNECTED_HEADER_FIELD,
                AbstractHttpServletReceiver.HTTP_CONTENT_TYPE_VALUE_FOR_ANTI_ENTROPY);

            ObjectOutputStream out = null;
            try {
                out = new ObjectOutputStream(response.getOutputStream());
                out.writeObject(diff);
                out.flush();

            } catch (IOException e) {
                throw new HttpServletLpbCastException(this.connection.getSiteId()
                    + " : Problem to write message in http request\n", e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (Exception e) {
                    this.connection.logger.error(this.connection.getSiteId()
                        + " - Problems closing streams for send anti-entropy.");
                }
            }
        } else {
            response.setHeader(AbstractHttpServletReceiver.HTTP_CONNECTED_HEADER_FIELD, null);
        }
    }

    /** {@inheritDoc} */
    public void sendTo(Object destinationNeighbor, Object message)
    {
        this.connection.logger.info(this.connection.getSiteId() + " Send a message to " + destinationNeighbor + "\n\n");
        this.connection.getNeighbors().notifyNeighbor(destinationNeighbor, message);

    }
}
