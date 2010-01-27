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
 *
 */
package org.xwiki.watch.component;

/**
 * The component responsible with all the XWiki Watch operations on the server. This service will provide an universal access
 * interface to all the XWatch operations like querying XWatch data, adding, editing, deleting watch objects, for easier
 * and more coherent access and preserving XWatch data integrity.
 * <br />
 * One benefit from this approach is having centralized access to Watch data regardless of the interface: the GWT 
 * servlet, the wiki pages in the default XWatch or custom wiki pages or code using Watch data. 
 * 
 * @version $Id$
 */
public interface WatchService
{
    /** 
     * The role associated with the component. 
     */
    String ROLE = WatchService.class.getName();
    
    //TODO: define here all functions needed in the watch service
}
