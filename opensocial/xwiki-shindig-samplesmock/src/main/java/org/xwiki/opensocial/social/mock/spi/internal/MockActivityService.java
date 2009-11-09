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
package org.xwiki.opensocial.social.mock.spi.internal;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.opensocial.social.mock.spi.MockXWikiComponent;

import java.util.Set;
import java.util.concurrent.Future;

@Component("MockActivityService")
public class MockActivityService implements ActivityService, MockXWikiComponent
{

    /** Provides access to documents. Injected by the Component Manager. */
    @Requirement
    private DocumentAccessBridge documentAccessBridge;

    /**
     * Allows overriding the Document Access Bridge used (useful for unit tests).
     * 
     * @param documentAccessBridge the new Document Access Bridge to use
     */
    public void setDocumentAccessBridge(DocumentAccessBridge documentAccessBridge)
    {
        this.documentAccessBridge = documentAccessBridge;
    }

    public Future<Void> createActivity(UserId userId, GroupId groupId, String appId, Set<String> fields,
        Activity activity, SecurityToken token) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<Void> deleteActivities(UserId userId, GroupId groupId, String appId, Set<String> activityIds,
        SecurityToken token) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<RestfulCollection<Activity>> getActivities(Set<UserId> userIds, GroupId groupId, String appId,
        Set<String> fields, CollectionOptions options, SecurityToken token) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<RestfulCollection<Activity>> getActivities(UserId userId, GroupId groupId, String appId,
        Set<String> fields, CollectionOptions options, Set<String> activityIds, SecurityToken token)
        throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<Activity> getActivity(UserId userId, GroupId groupId, String appId, Set<String> fields,
        String activityId, SecurityToken token) throws ProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
