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
package org.xwiki.spaces.internal;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.spaces.GroupManager;
import org.xwiki.spaces.GroupManagerException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.rightsmanager.RightsManagerPluginApi;

/**
 * Default group manager relying on old xwiki-core APIs.
 * 
 * @version $Id$
 */
public class DefaultGroupManager implements GroupManager
{

    /** The name of the member field in the XWiki group class. */
    private static final String MEMBER_FIELD_NAME = "member";

    /** Serializer used to transform user document reference to their string representation in a group objects. */
    @Requirement("compactwiki")
    private EntityReferenceSerializer<String> compactWikiReferenceSerializer;

    /** Our execution, needed to get access to the old xwiki core context. */
    @Requirement
    private Execution execution;

    /**
     * {@inheritDoc}
     * 
     * @see GroupManager#addUserToGroup(DocumentReference, DocumentReference)
     */
    public void addUserToGroup(DocumentReference user, DocumentReference group) throws GroupManagerException
    {
        XWikiContext context = getXWikiContext();
        XWiki xwiki = context.getWiki();

        // don't add if he is already a member
        if (this.isMemberOfGroup(user, group)) {
            return;
        }

        try {
            XWikiDocument groupDoc = xwiki.getDocument(group, context);

            // Serialize the user reference based on the group reference.
            // Will write down the wiki part of the representation only if the user one is different
            // fron the group one.
            String serializedUserReference = compactWikiReferenceSerializer.serialize(user, group);

            BaseObject memberObject = groupDoc.newXObject(getGroupClassReference(), context);
            memberObject.setStringValue(MEMBER_FIELD_NAME, serializedUserReference);

            String content = groupDoc.getContent();
            if (StringUtils.isBlank(content)) {
                // Lazy group sheet creation.
                // TODO this should be optional.
                groupDoc.setContent("{{include document=\"XWiki.XWikiGroupSheet\" /}}");
                groupDoc.setSyntax(Syntax.XWIKI_2_0);
            }
            xwiki.saveDocument(groupDoc, context.getMessageTool().get("core.comment.addedUserToGroup"), context);
        } catch (XWikiException e) {
            throw new GroupManagerException(MessageFormat.format("Error while adding user [0] to group [1]",
                new Object[] {user.toString(), group.toString()}), e);
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see GroupManager#isMemberOfGroup(DocumentReference, DocumentReference)
     */
    public boolean isMemberOfGroup(DocumentReference user, DocumentReference group) throws GroupManagerException
    {
        XWikiContext context = getXWikiContext();
        String serializedUserReference = compactWikiReferenceSerializer.serialize(user, group);
        String serializedGroupReference = compactWikiReferenceSerializer.serialize(group);
        
        try {
            Collection<String> coll =
                ((RightsManagerPluginApi) context.getWiki().getPluginApi("rightsmanager", context))
                    .getAllGroupsNamesForMember(serializedUserReference);
            Iterator<String> it = coll.iterator();
            while (it.hasNext()) {
                if (serializedGroupReference.equals(it.next())) {
                    return true;
                }
            }
            return false;
        } catch (XWikiException e) {
            throw new GroupManagerException(MessageFormat.format("Error while testing user [0] against group [1]",
                new Object[] {user.toString(), group.toString()}), e);
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see GroupManager#removeUserFromGroup(DocumentReference, DocumentReference)
     */
    public void removeUserFromGroup(DocumentReference user, DocumentReference group) throws GroupManagerException
    {
        XWikiContext context = getXWikiContext();
        XWiki xwiki = context.getWiki();

        // don't remove if he's not a member
        if (!isMemberOfGroup(user, group)) {
            return;
        }

        try {
            XWikiDocument groupDoc = xwiki.getDocument(group, context);
            String userRefAsString = compactWikiReferenceSerializer.serialize(user, group);

            BaseObject memberObject = groupDoc.getXObject(getGroupClassReference(), MEMBER_FIELD_NAME, userRefAsString);
            if (memberObject == null) {
                return;
            }
            groupDoc.removeXObject(memberObject);
            xwiki.saveDocument(groupDoc, context.getMessageTool().get("core.comment.removedUserFromGroup"), context);
        } catch (XWikiException e) {
            throw new GroupManagerException(MessageFormat.format("Error while removing user [0] from group [1]",
                new Object[] {user.toString(), group.toString()}), e);
        }

    }

    /**
     * @return the XWiki context, retrieve from our {@link #execution}.
     */
    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }

    /**
     * @return the reference to the group class in the context wiki.
     */
    private DocumentReference getGroupClassReference()
    {
        return new DocumentReference(getXWikiContext().getDatabase(), "XWiki", "XWikiGroups");
    }

}
