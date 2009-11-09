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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.comments.Comment;
import com.xpn.xwiki.plugin.comments.CommentsException;
import com.xpn.xwiki.plugin.comments.CommentsManager;
import com.xpn.xwiki.plugin.comments.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DefaultCommentsManager extends AbstractCommentsManager
{

    private static Log LOG = LogFactory.getLog(DefaultCommentsManager.class);

    public DefaultCommentsManager() {
        super();
    }


    /**
     * {@inheritDoc}
     * 
     * @see CommentsManager#addComment(Container, String, String, XWikiContext)
     */
    public Comment addComment(Container container, String author, String content, Map commentMap, XWikiContext context)
            throws CommentsException
    {
        Comment comment = new DefaultComment(container, author, content, commentMap, context);
        if (hasModeration(context)&&isModerated(context)) {
            comment.setStatus(COMMENT_MODERATION_MODERATED);
        }
        comment.save();
        return comment;
    }

    /**
     * {@inheritDoc}
     * 
     * @see CommentsManager#getComments(Container, int, int, boolean, XWikiContext)
     */
    public List<Comment> getComments(Container container, int start, int count, boolean asc, XWikiContext context)
        throws CommentsException
    {
        if (LOG.isDebugEnabled())
          LOG.debug("Calling default manager code for comments");
        try {
            int skipped = 0;
            int nb = 0;
            XWikiDocument doc =
                context.getWiki().getDocument(container.getDocumentName(), context);
            List<BaseObject> bobjects = doc.getObjects(getCommentsClassName(context));
            if (bobjects != null) {
                List<Comment> comments = new ArrayList<Comment>();
                for (BaseObject bobj : bobjects) {
                    if (bobj != null) {
                        if (!hasModeration(context)) {
                            if (skipped<start) {
                                skipped++;
                            } else {
                                comments.add(getDefaultComment(container, doc, bobj, context));
                                nb++;
                            }
                        } else {
                            // we need to check the comment status first
                            Comment comment = getDefaultComment(container, doc, bobj, context);
                            if (!comment.getStatus().equals(COMMENT_MODERATION_MODERATED)&&!comment.getStatus().equals(COMMENT_MODERATION_REFUSED)) {
                                if (skipped<start) {
                                    skipped++;
                                } else {
                                    comments.add(comment);
                                    nb++;
                                }
                            }
                        }
                        if ((count!=0)&&(nb==count))
                         break;
                    }
                }
                return comments;
            }
        } catch (XWikiException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Comment> getModeratedComments(Container container, int start, int count, boolean asc, XWikiContext context) throws CommentsException {
        if (LOG.isDebugEnabled())
          LOG.debug("Calling default manager code for comments");
        try {
            int skipped = 0;
            int nb = 0;
            XWikiDocument doc =
                context.getWiki().getDocument(container.getDocumentName(), context);
            List<BaseObject> bobjects = doc.getObjects(getCommentsClassName(context));
            if (bobjects != null) {
                List<Comment> comments = new ArrayList<Comment>();
                for (BaseObject bobj : bobjects) {
                    if (bobj != null) {
                        if (hasModeration(context)) {
                            if (skipped<start) {
                                skipped++;
                            } else {
                                comments.add(getDefaultComment(container, doc, bobj, context));
                                nb++;
                            }
                        } else {
                            // we need to check the comment status first
                            Comment comment = getDefaultComment(container, doc, bobj, context);
                            if (comment.getStatus().equals(COMMENT_MODERATION_MODERATED)) {
                                if (skipped<start) {
                                    skipped++;
                                } else {
                                    comments.add(comment);
                                    nb++;
                                }
                            }
                        }
                        if ((count!=0)&&(nb==count))
                         break;
                    }
                }
                return comments;
            }
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
        return null;

    }

    public List<Comment> getComments(String status, int start, int count, boolean asc, XWikiContext context) throws CommentsException {
        throw new CommentsException(XWikiException.MODULE_XWIKI, XWikiException.ERROR_XWIKI_NOT_IMPLEMENTED, "Not implemented");
    }

    public Comment getComment(String commentId, XWikiContext context) throws CommentsException {
        try {
            int i1 = commentId.indexOf(":");
            if (i1==-1)
                throw new CommentsException(CommentsException.MODULE_PLUGIN_COMMENTS, CommentsException.ERROR_COMMENTS_INVALID_COMMENT_ID,  "Invalid comment ID, cannot parse comment id");

            String docName = commentId.substring(0, i1);
            String sObjectNb = commentId.substring(i1+1);
            int objectNb = Integer.parseInt(sObjectNb);
            Container container = new DefaultContainer(docName, -1, "", null, context);
            XWikiDocument doc = context.getWiki().getDocument(docName, context);

            if (doc.isNew())
                throw new CommentsException(CommentsException.MODULE_PLUGIN_COMMENTS, CommentsException.ERROR_COMMENTS_INVALID_COMMENT_ID,  "Invalid comment ID, document does not exist");

            BaseObject object = doc.getObject(getCommentsClassName(context), objectNb);

            if (object==null)
                throw new CommentsException(CommentsException.MODULE_PLUGIN_COMMENTS, CommentsException.ERROR_COMMENTS_INVALID_COMMENT_ID,  "Invalid comment ID, could not find comment");

            return new DefaultComment(container, doc, object, context);
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see CommentsManager#getComments(Container, int, int, boolean, XWikiContext)
     */
    public Comment getComment(Container container, int id, XWikiContext context)
        throws CommentsException
    {
        try {
            int skipped = 0;
            XWikiDocument doc =
                context.getWiki().getDocument(container.getDocumentName(), context);
            List<BaseObject> bobjects = doc.getObjects(getCommentsClassName(context));
            if (bobjects != null) {
                for (BaseObject bobj : bobjects) {
                    if (bobj != null) {
                        if (skipped<id)
                         skipped++;
                        else
                         return getDefaultComment(container, doc, bobj, context);
                    }
                }
            }
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
        return null;
    }

    public List<Comment> getComments(Comment comment, int startlevel, int levelsnumber, boolean asc, XWikiContext context) throws CommentsException {
        try {
            XWikiDocument doc = ((DefaultComment) comment).getDocument();
            List<BaseObject> bobjects = doc.getObjects(getCommentsClassName(context));
            if (bobjects != null) {
                List<Comment> comments = new ArrayList<Comment>();
                for (BaseObject bobj : bobjects) {
                    if (bobj != null) {
                        if (bobj.getStringValue(COMMENT_CLASS_FIELDNAME_PARENT).equals(comment.getCommentId())) {
                            if (!hasModeration(context)) {
                             comments.add(new DefaultComment(comment.getContainer(), doc, bobj, context));
                            } else {
                               Comment childcomment = new DefaultComment(comment.getContainer(), doc, bobj, context);
                               if (!childcomment.getStatus().equals(COMMENT_MODERATION_MODERATED)&&!childcomment.getStatus().equals(COMMENT_MODERATION_REFUSED)) {
                                   comments.add(childcomment);
                               }

                            }
                        }
                    }
                }
                return comments;
            }
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
        return null;
    }

    public int getNumberOfCommentsInThread(XWikiDocument document, XWikiContext context) throws CommentsException {
        Vector objects = document.getObjects(getCommentsClassName(context));
        return (objects==null) ? 0 : objects.size();
    }

    public int getNumberOfCommentsInThread(Comment comment, XWikiContext context) throws CommentsException {
        try {
            return getNumberOfCommentsInThread(context.getWiki().getDocument(comment.getPage(), context), context);
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

    private DefaultComment getDefaultComment(Container container, XWikiDocument doc, BaseObject bobj, XWikiContext context) throws CommentsException
    {
        return new DefaultComment(container, doc, bobj, context);
    }

}
