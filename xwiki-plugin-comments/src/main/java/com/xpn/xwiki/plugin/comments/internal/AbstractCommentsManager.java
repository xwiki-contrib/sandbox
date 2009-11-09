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

package com.xpn.xwiki.plugin.comments.internal;

import org.apache.commons.lang.StringUtils;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.NumberClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.plugin.comments.*;

import java.util.List;


public abstract class AbstractCommentsManager implements CommentsManager
{
    private static String defaultCommentClassName = "XWiki.XWikiComments";

    public AbstractCommentsManager() {
    }

    public void init(XWikiContext context)
    {
        try {
            initCommentsClass(context, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void virtualInit(XWikiContext context)
    {
        try {
            initCommentsClass(context, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCommentsClassName(XWikiContext context) {
        return defaultCommentClassName;
    }


    public BaseClass initCommentsClass(XWikiContext context, boolean pageField) throws XWikiException
    {
        XWikiDocument doc;
        XWiki xwiki = context.getWiki();
        boolean needsUpdate = false;
        String commentsClassName = getCommentsClassName(context);
        doc = xwiki.getDocument(commentsClassName, context);

        BaseClass bclass = doc.getxWikiClass();
        bclass.setName(commentsClassName);
        if (context.get("initdone") != null) {
            return bclass;
        }

        needsUpdate |= bclass.addTextField(COMMENT_CLASS_FIELDNAME_AUTHOR, "Author", 30);
        needsUpdate |= bclass.addTextAreaField(COMMENT_CLASS_FIELDNAME_HIGHLIGHT, "Highlighted Text", 40, 2);
        // old comments class had replyto field as number
        PropertyClass replytoprop = (PropertyClass) bclass.get("replyto");
        if (replytoprop instanceof NumberClass) {
            needsUpdate = true;
            bclass.removeField("replyto");
        }
        needsUpdate |= bclass.addTextField(COMMENT_CLASS_FIELDNAME_PARENT, "Parent", 30);
        if (pageField)
         needsUpdate |= bclass.addTextField(COMMENT_CLASS_FIELDNAME_PAGE, "Page", 30);
        needsUpdate |= bclass.addDateField(COMMENT_CLASS_FIELDNAME_DATE, "Date");
        needsUpdate |= bclass.addTextAreaField(COMMENT_CLASS_FIELDNAME_COMMENT, "Comment", 40, 5);
        needsUpdate |= bclass.addStaticListField(COMMENT_CLASS_FIELDNAME_STATUS, "Status", 1, false, "none=None|moderated=Moderated|accepted=Accepted|refused=Refused");

        if (StringUtils.isBlank(doc.getAuthor())) {
            needsUpdate = true;
            doc.setAuthor("XWiki.Admin");
        }
        if (StringUtils.isBlank(doc.getCreator())) {
            needsUpdate = true;
            doc.setCreator("XWiki.Admin");
        }
        if (StringUtils.isBlank(doc.getParent())) {
            needsUpdate = true;
            doc.setParent("XWiki.XWikiClasses");
        }

        String content = doc.getContent();
        if ((content == null) || (content.equals(""))) {
            needsUpdate = true;
            doc.setContent("1 XWiki Comment Class");
        }

        if (needsUpdate) {
            xwiki.saveDocument(doc, context);
        }
        return bclass;
    }

    public Container newContainer(XWikiContext context) {
        return new DefaultContainer(context);
    }

    public Container getContainer(Comment comment, XWikiContext context) {
         // We create a new container
         Container container = newContainer(context);
         // We get the parent container
         Container parentContainer = comment.getContainer();
         // We copy the data
         container.setDocumentName(parentContainer.getDocumentName());
         container.setSection(parentContainer.getSection());
         container.setPhrase(parentContainer.getPhrase());
         // We change the comment
         container.setComment(comment);
         return container;
     }

    public Comment addComment(Container container, String author, String content, XWikiContext context) throws CommentsException {
         return addComment(container, author, content, null, context);
     }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.CommentsManager#removeComment(com.xpn.xwiki.plugin.comments.Comment, com.xpn.xwiki.XWikiContext)
     */
    public boolean removeComment(Comment comment, XWikiContext context) throws CommentsException
    {
        return comment.remove();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.CommentsManager#getParentComment(com.xpn.xwiki.plugin.comments.Comment, com.xpn.xwiki.XWikiContext)
     */
    public Comment getParentComment(Comment comment, XWikiContext context) throws CommentsException
    {
        return comment.getParentComment();
    }

    public boolean hasModeration(XWikiContext context) {
        int hasModerationDefault = (int) context.getWiki().ParamAsLong("xwiki.comments.moderation", 0);
        int hasModeration = context.getWiki().getWebPreferenceAsInt("comments_moderation", hasModerationDefault, context);
        return (hasModeration==1);
    }

    protected String getModeratedGroups(XWikiContext context) {
        String moderatedGroupsDefault = context.getWiki().Param("xwiki.comments.moderatedgroups", "XWiki.ModeratedGroup");
        return context.getWiki().getWebPreference("comments_moderatedgroups", moderatedGroupsDefault, context);
    }

    protected String getUnmoderatedGroups(XWikiContext context) {
        String unmoderatedGroupsDefault = context.getWiki().Param("xwiki.comments.unmoderatedgroups", "XWiki.UnmoderatedGroup");
        return context.getWiki().getWebPreference("comments_unmoderatedgroups", unmoderatedGroupsDefault, context);
    }

    protected String getModeratorsGroups(XWikiContext context) {
        String moderatorsGroupsDefault = context.getWiki().Param("xwiki.comments.moderatorsgroups", "XWiki.ModeratorsGroup");
        return context.getWiki().getWebPreference("comments_moderatorsgroups", moderatorsGroupsDefault, context);
    }

    public boolean isModerated(XWikiContext context) throws CommentsException {
       try {
        if (context.getWiki().getRightService().hasAdminRights(context))
             return false;

        XWikiGroupService gs = context.getWiki().getGroupService(context);
        String user = context.getUser();        
        String unmoderatedGroups = getUnmoderatedGroups(context);
        String[] ugroups = unmoderatedGroups.split(",");
        for (int i=0;i<ugroups.length;i++) {
            if (gs.isMemberOfGroup(user, ugroups[i], context))
             return false;
        }

        String moderatedGroups = getModeratedGroups(context);
        String[] groups = moderatedGroups.split(",");
        for (int i=0;i<groups.length;i++) {
            if (gs.isMemberOfGroup(user, groups[i], context))
             return true;
        }

        return false;
       } catch (XWikiException e) {
           throw new CommentsException(e);
       }
    }


    public boolean isModerator(XWikiContext context) throws CommentsException {
       try {
        if (context.getWiki().getRightService().hasAdminRights(context))
             return true;

        XWikiGroupService gs = context.getWiki().getGroupService(context);
        String user = context.getUser();
        String moderatorsGroups = getModeratorsGroups(context);
        String[] ugroups = moderatorsGroups.split(",");
        for (int i=0;i<ugroups.length;i++) {
            if (gs.isMemberOfGroup(user, ugroups[i], context))
             return true;
        }

        return false;
       } catch (XWikiException e) {
           throw new CommentsException(e);
       }
    }

    public boolean acceptComment(Comment comment, XWikiContext context) throws CommentsException {
        if (!isModerator(context)) {
            // user is not allowd to moderate
            return false;
        }

        comment.acceptComment();
        return true;
    }

    public boolean refuseComment(Comment comment, XWikiContext context) throws CommentsException {
        if (!isModerator(context)) {
            // user is not allowd to moderate
            return false;
        }

        comment.refuseComment();
        return true;
    }

    public boolean remoderateComment(Comment comment, XWikiContext context) throws CommentsException {
        if (!isModerator(context)) {
            // user is not allowd to moderate
            return false;
        }

        comment.remoderateComment();
        return true;
    }

    /**
      * {@inheritDoc}
      *
      * @see com.xpn.xwiki.plugin.comments.CommentsManager#getModeratedComments(com.xpn.xwiki.plugin.comments.Container, int, int, boolean, com.xpn.xwiki.XWikiContext)
      */
     public List<Comment> getModeratedComments(int start, int count, boolean asc, XWikiContext context)
             throws CommentsException
     {
         return getComments(COMMENT_MODERATION_MODERATED, start, count, asc, context);
     }

    public List<Comment> getAcceptedComments(int start, int count, boolean asc, XWikiContext context)
            throws CommentsException
    {
        return getComments(COMMENT_MODERATION_ACCEPTED, start, count, asc, context);
    }

    public List<Comment> getRefusedComments(int start, int count, boolean asc, XWikiContext context)
            throws CommentsException
    {
        return getComments(COMMENT_MODERATION_REFUSED, start, count, asc, context);
    }


}