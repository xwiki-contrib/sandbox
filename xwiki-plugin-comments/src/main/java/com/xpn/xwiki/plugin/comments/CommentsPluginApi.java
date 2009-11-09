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

package com.xpn.xwiki.plugin.comments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.plugin.PluginApi;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class CommentsPluginApi extends PluginApi
{
    private static Log LOG = LogFactory.getLog(CommentsPluginApi.class);

    public CommentsPluginApi(CommentsPlugin plugin, XWikiContext context)
    {
        super(plugin, context);
    }

    protected CommentsPlugin getCommentsPlugin() {
        return ((CommentsPlugin) getProtectedPlugin());
    }

    protected CommentsManager getCommentsManager()
    {
        return getCommentsPlugin().getCommentsManager(context);
    }


    protected static List<CommentApi> wrapComments(List<Comment> comments, XWikiContext context) {
        if (comments==null)
            return null;

        List<CommentApi> commentsResult = new ArrayList<CommentApi>();
        for (Comment comment : comments) {
            commentsResult.add(new CommentApi(comment, context));
        }
        return commentsResult;
    }

    public CommentApi addComment(Document doc, String author, String content, String replyId)
    {
        try {
            if (doc.hasAccessLevel("comment")) {
                return new CommentApi(getCommentsPlugin().addComment(context.getWiki().getDocument(doc.getFullName(), context), author, content, replyId, context), context);
            } else {
                return null;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }
    }

    public boolean removeComment(Document doc, String globalCommentId)
    {
        try {
            // we need to check the rights on both the comment document and comment container document
            Comment comment = getCommentsPlugin().getComment(globalCommentId, context);
            String commentContainerPage = comment.getContainer().getDocumentName();
            String commentPage = comment.getAsContainer().getDocumentName();
            XWikiDocument commentContainerDoc = context.getWiki().getDocument(commentContainerPage, context);
            XWikiDocument commentDoc = context.getWiki().getDocument(commentPage, context);
            boolean canDelete;
            if (commentContainerPage.equals(commentPage))
                canDelete = context.getWiki().checkAccess("edit", commentContainerDoc, context);
            else
                canDelete = context.getWiki().checkAccess("delete", commentDoc, context);
            if (!canDelete)
                return false;
            else
                return comment.remove();
        } catch (Exception e) {
            context.put("exception", e);
            return false;
        }
    }

    public CommentApi addComment(Document doc, String author, String content, String replyId, Map map)
    {
        try {
            if (doc.hasAccessLevel("comment")) {
                return new CommentApi(getCommentsPlugin().addComment(context.getWiki().getDocument(doc.getFullName(), context), author, content, replyId, map, context), context);
            } else {
                return null;
            }
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }
    }

    public List<CommentApi> getComments(Document doc, int start, int count)
    {
        return getComments(doc, start, count, true);
    }

    public List<CommentApi> getComments(Document doc, int start, int count, boolean asc)
    {
        try {
            return wrapComments(getCommentsPlugin().getComments(context.getWiki().getDocument(doc.getFullName(), context), start, count, asc, context), context);
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }
    }


    public List<CommentApi> getComments(CommentApi comment, int start, int count)
    {
        return getComments(comment, start, count, true);
    }

    public List<CommentApi> getComments(CommentApi comment, int start, int count, boolean asc)
    {
        try {
            return wrapComments(getCommentsPlugin().getComments(comment.getComment(), start, count, asc, context), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }


    public int getNumberOfCommentsInThread(Document doc)
    {
        try {
            return getCommentsPlugin().getNumberOfCommentsInThread(context.getWiki().getDocument(doc.getFullName(), context), context);
        } catch (Exception e) {
            context.put("exception", e);
            return -1;
        }
    }

    public int getNumberOfCommentsInThread(CommentApi comment)
    {
        try {
            return getCommentsPlugin().getNumberOfCommentsInThread(comment.getComment(), context);
        } catch (Exception e) {
            context.put("exception", e);
            return -1;
        }
    }
    

    public List<CommentApi> getModeratedComments(boolean asc)
    {
        try {
            return wrapComments(getCommentsPlugin().getModeratedComments(0, 0, asc, context), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }

    public List<CommentApi> getModeratedComments(int start, int count, boolean asc)
    {
        try {
            return wrapComments(getCommentsPlugin().getModeratedComments(start, count, asc, context), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }

    public List<CommentApi> getAcceptedComments(boolean asc)
    {
        try {
            return wrapComments(getCommentsPlugin().getAcceptedComments(0, 0, asc, context), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }


    public List<CommentApi> getAcceptedComments(int start, int count, boolean asc)
    {
        try {
            return wrapComments(getCommentsPlugin().getAcceptedComments(start, count, asc, context), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }

    public List<CommentApi> getRefusedComments(boolean asc)
    {
        try {
            return wrapComments(getCommentsPlugin().getRefusedComments(0, 0, asc, context), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }


    public List<CommentApi> getRefusedComments(int start, int count, boolean asc)
    {
        try {
            return wrapComments(getCommentsPlugin().getRefusedComments(start, count, asc, context), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }
    public CommentApi getComment(String commentId) {
        try {
            return new CommentApi(getCommentsPlugin().getComment(commentId, context), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }

    public boolean acceptComment(CommentApi comment) {
        try {
            return getCommentsPlugin().acceptComment(comment.getComment(), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return false;
        }
    }

    public boolean refuseComment(CommentApi comment) {
        try {
            return getCommentsPlugin().refuseComment(comment.getComment(), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return false;
        }
    }

    public boolean remoderateComment(CommentApi comment) {
        try {
            return getCommentsPlugin().remoderateComment(comment.getComment(), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return false;
        }
    }

    public boolean isModerator() {
        try {
            return getCommentsPlugin().isModerator(context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return false;
        }
    }

    public boolean isModerated() {
        try {
            return getCommentsPlugin().isModerated(context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return false;
        }
    }

}
