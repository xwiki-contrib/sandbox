package org.xwoot.lpbcast.neighbors.httpservletneighbors;

import org.xwoot.lpbcast.neighbors.AbstractNeighbors;
import org.xwoot.lpbcast.neighbors.NeighborsException;

/**
 * Handles neighbors accessible trough Servlets.
 * 
 * @version $Id$
 */
public class HttpServletNeighbors extends AbstractNeighbors
{
    /** The location of the servlet to use when notifying a neighbor, relative to the neighbor's address. */
    public static final String NOTIFY_SERVLET_PATH = "/receiveMessage.do";
    
    /**
     * Creates a new instance.
     * 
     * @param workingDir the directory where to store the neighbors.
     * @param maxNumber the maximum number of neighbors to remember.
     * @param siteId the id of the woot node this object is assigned to.
     * @throws NeighborsException if the provided directory is not usable.
     */
    public HttpServletNeighbors(String workingDir, int maxNumber, Integer siteId) throws NeighborsException
    {
        super(workingDir, maxNumber, siteId);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyNeighbor(Object neighbor, Object message)
    {
        NotifyNeighborsThread notify = new NotifyNeighborsThread((String) neighbor, message);
        notify.start();
    }

    /** {@inheritDoc} */
    @Override
    public void notifyNeighbors(Object message)
    {
        NotifyNeighborsThread notify = new NotifyNeighborsThread(this, message);
        notify.start();
    }
}
