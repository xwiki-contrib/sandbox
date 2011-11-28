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
package org.xwiki.portlet.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Wraps a request dispatcher to be able to notify the request that created it whenever it is used.
 * 
 * @version $Id$
 * @see org.gatein.pc.portlet.impl.jsr168.DispatchtedRequestDispatcher
 */
public class NotifyingRequestDispatcher implements RequestDispatcher
{
    /**
     * The wrapped request dispatcher.
     */
    private final RequestDispatcher dispatcher;

    /**
     * The request object being notified whenever this dispatcher is used.
     */
    private final DispatchedRequest dispatchedRequest;

    /**
     * The path used to created the wrapped request dispatcher.
     */
    private final String path;

    /**
     * Wraps a given request dispatcher and notifies the specified dispatched request whenever the dispatcher is used.
     * 
     * @param dispatcher the request dispatcher to wrap
     * @param dispatchedRequest the request object being notified whenever this dispatcher is used
     * @param path the path used to created the wrapped request dispatcher
     */
    public NotifyingRequestDispatcher(RequestDispatcher dispatcher, DispatchedRequest dispatchedRequest, String path)
    {
        this.dispatcher = dispatcher;
        this.dispatchedRequest = dispatchedRequest;
        this.path = path;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        dispatchedRequest.pushDispatch(new Dispatch(DispatchType.FORWARD, path));
        try {
            dispatcher.forward(request, response);
        } finally {
            dispatchedRequest.popDispatch();
        }
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        dispatchedRequest.pushDispatch(new Dispatch(DispatchType.INCLUDE, path));
        try {
            dispatcher.include(request, response);
        } finally {
            dispatchedRequest.popDispatch();
        }
    }
}
