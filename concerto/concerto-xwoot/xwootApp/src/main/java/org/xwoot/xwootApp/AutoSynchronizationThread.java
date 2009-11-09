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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A thread that periodically launches the {@link XWootAPI#synchronize()} method on the assigned instance.
 * <p/>
 * Launching the thread at the appropriate time is the user's concern. If synchronization fails at one point, the thread
 * continues.
 * 
 * @version $Id$
 * @see {@link XWootAPI}
 */
// TODO: Switch to encapsulation VS inheritance to remove the public Thread.start() confusing method(s).
public class AutoSynchronizationThread extends Thread
{
    /** Lock used for waiting. */
    private String waitingLock = "sleeping";

    /** The {@link XWootAPI} instance to auto synchronize. */
    private XWootAPI xwootEngine;

    /** Flag to signal if the thread is running or not. */
    private boolean started;

    /** The interval by which to auto synchronize. */
    private long interval;

    /** Used for logging. */
    private Log logger = LogFactory.getLog(this.getClass());

    /**
     * Constructor.
     * 
     * @param xwootEngine the {@link XWootAPI} instance to auto synchronize.
     * @param interval the interval by which to auto synchronize. this must be high enough in order to avoid excessive
     *            synchronization.
     * @throws NullPointerException if the provided {@link XWootAPI} instance is {@code null}.
     * @throws IllegalArgumentException if the interval parameter is equal to or smaller than 0.
     */
    public AutoSynchronizationThread(XWootAPI xwootEngine, long interval) throws NullPointerException,
        IllegalArgumentException
    {
        super("XWoot Auto-Synchronization");

        if (xwootEngine == null) {
            throw new NullPointerException("Null wootEngine provided.");
        }

        if (interval <= 0) {
            throw new IllegalArgumentException("Only strictly pozitive values are accepted. Given: " + interval);
        }

        this.xwootEngine = xwootEngine;
        this.started = false;
        this.interval = interval;

        this.logger.info("Auto synchronization thread initialized.");
    }

    /**
     * Start the auto-synchronization tread or restarts it if it has previously been stopped.
     * <p/>
     * <b>NOTICE:</b> Use this method if you want the auto-synchronization to begin and not {@link #start()}.
     * 
     * @see #stopThread()
     */
    public synchronized void startThread()
    {
        if (!started) {
            this.logger.debug("Starting...");

            this.started = true;

            if (!this.isAlive()) {
                // If thread is not started, start it
                super.start();
            } else {
                // Else, resume it.
                synchronized (waitingLock) {
                    waitingLock.notifyAll();
                }
            }
        }
    }

    /**
     * Stop the auto-synchronization thread.
     * <p/>
     * To restart the thread, don't create another instance, use {@link #startThread()}.
     * <p/>
     * This does not use the deprecated {@link Thread#stop()} method.
     */
    public synchronized void stopThread()
    {
        if (started) {
            this.logger.debug("Stopping...");

            this.started = false;
            synchronized (waitingLock) {
                waitingLock.notifyAll();
            }
        }
    }

    /**
     * @return true if the thread is started and running.
     */
    public boolean isStarted()
    {
        return this.started;
    }

    /** {@inheritDoc} **/
    @Override
    public void run()
    {
        this.logger.debug("Started.");

        while (true) {
            // Waiting sequence
            synchronized (waitingLock) {
                try {
                    this.logger.debug("Sleeping...");
                    waitingLock.wait(this.interval);
                } catch (InterruptedException e) {
                    // ignore
                    e.printStackTrace();
                }
            }

            try {
                this.logger.debug("Woke up.");
            } catch (Exception e) {
                // Yes, it appears we can end up here because of a NPE in Log4J.
                e.printStackTrace();
            }

            // Stop sequence
            if (!started) {
                this.logger.debug("Stopped.");

                while (!started) {
                    synchronized (waitingLock) {
                        try {
                            // Wait until restart.
                            waitingLock.wait();
                        } catch (InterruptedException e) {
                            // ignore
                            e.printStackTrace();
                        }
                    }
                }
                continue;
            }
            
            if (!xwootEngine.isConnectedToP2PNetwork()) {
                this.logger.warn("P2P Network not connected, skipping Auto-synchronization.");
                continue;
            } else if (!xwootEngine.isConnectedToP2PGroup()) {
                this.logger.warn("P2P Group not connected, skipping Auto-synchronization.");
                continue;
            }

            // Action sequence
            this.logger.debug("Performing auto-synchronization.");
            try {
                xwootEngine.synchronize();
            } catch (Exception e) {
                this.logger.warn("Auto-synchronization failed!", e);
            }
        }
    }

}
