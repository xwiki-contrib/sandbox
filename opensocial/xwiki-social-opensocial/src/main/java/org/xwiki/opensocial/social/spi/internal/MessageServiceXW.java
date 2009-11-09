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
package org.xwiki.opensocial.social.spi.internal;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.model.Message;
import org.apache.shindig.social.opensocial.model.MessageCollection;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.MessageService;
import org.apache.shindig.social.opensocial.spi.UserId;

public class MessageServiceXW implements MessageService
{

    public Future<Void> createMessage(UserId arg0, String arg1, String arg2, Message arg3, SecurityToken arg4)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<MessageCollection> createMessageCollection(UserId arg0, MessageCollection arg1, SecurityToken arg2)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<Void> deleteMessageCollection(UserId arg0, String arg1, SecurityToken arg2) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<Void> deleteMessages(UserId arg0, String arg1, List<String> arg2, SecurityToken arg3)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<RestfulCollection<MessageCollection>> getMessageCollections(UserId arg0, Set<String> arg1,
        CollectionOptions arg2, SecurityToken arg3) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<RestfulCollection<Message>> getMessages(UserId arg0, String arg1, Set<String> arg2,
        List<String> arg3, CollectionOptions arg4, SecurityToken arg5) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<Void> modifyMessage(UserId arg0, String arg1, String arg2, Message arg3, SecurityToken arg4)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<Void> modifyMessageCollection(UserId arg0, MessageCollection arg1, SecurityToken arg2)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
