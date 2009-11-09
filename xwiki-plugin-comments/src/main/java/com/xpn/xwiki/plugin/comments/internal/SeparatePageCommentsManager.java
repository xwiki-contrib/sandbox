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
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.comments.Comment;
import com.xpn.xwiki.plugin.comments.CommentsException;
import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.plugin.comments.CommentsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class SeparatePageCommentsManager extends AbstractCommentsManager
{
    private static Log LOG = LogFactory.getLog(SeparatePageCommentsManager.class);

    public SeparatePageCommentsManager() {
       super();
    }

    public void init(XWikiContext context)
    {
        try {
            initCommentsClass(context, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void virtualInit(XWikiContext context)
    {
        try {
            initCommentsClass(context, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.CommentsManager#addComment(com.xpn.xwiki.plugin.comments.Container, String, String, com.xpn.xwiki.XWikiContext)
     */
    public Comment addComment(Container container, String author, String content, Map commentMap, XWikiContext context)
        throws CommentsException
    {
        Comment comment = new SeparatePageComment(container, author, content, commentMap, context);
        if (hasModeration(context)&&isModerated(context)) {
            comment.setStatus(CommentsManager.COMMENT_MODERATION_MODERATED);
        }
        comment.save();
        return comment;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.CommentsManager#getComments(com.xpn.xwiki.plugin.comments.Container, int, int, boolean, com.xpn.xwiki.XWikiContext)
     */
    public List<Comment> getComments(Container container, int start, int count, boolean asc, XWikiContext context)
        throws CommentsException
    {
        if (LOG.isDebugEnabled())
          LOG.debug("Calling separate page manager code for comments");

        String pageName = container.getDocumentName();
        String sql;
        if (!hasModeration(context))
            sql = ", BaseObject as obj, StringProperty as parentprop where doc.fullName=obj.name and obj.className='" + getCommentsClassName(context) + "' and obj.id=parentprop.id.id and parentprop.id.name='" + COMMENT_CLASS_FIELDNAME_PARENT + "' and parentprop.value='"
                    + pageName + "' order by doc.date " + (asc  ? "asc" : "desc");
        else
            sql = ", BaseObject as obj, StringProperty as parentprop where doc.fullName=obj.name and obj.className='" + getCommentsClassName(context) + "' and obj.id=parentprop.id.id and parentprop.id.name='" + COMMENT_CLASS_FIELDNAME_PARENT + "' and parentprop.value='"
                    + pageName + "' and obj.name not in (select obj2.name from BaseObject as obj2, StringProperty as statusprop where obj2.className='" + getCommentsClassName(context) + "' and obj2.id=statusprop.id.id and statusprop.id.name='status' and (statusprop.value='" + COMMENT_MODERATION_MODERATED + "' or statusprop.value='" + COMMENT_MODERATION_REFUSED + "') and obj.id=obj2.id) order by doc.date " + (asc  ? "asc" : "desc");

        List<Comment> comments = new ArrayList<Comment>();
        try {
            List<String> commentPageNameList = context.getWiki().getStore().searchDocumentsNames(sql, count, start, context);

            for (String commentPageName : commentPageNameList) {
                comments.add(new SeparatePageCommentsManager().getCommentFromDocument(container, context.getWiki().getDocument(commentPageName, context), context));
            }
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }

        return comments;
  }


        /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.CommentsManager#getComments(com.xpn.xwiki.plugin.comments.Container, int, int, boolean, com.xpn.xwiki.XWikiContext)
     */
    public List<Comment> getModeratedComments(Container container, int start, int count, boolean asc, XWikiContext context)
        throws CommentsException
    {
        if (LOG.isDebugEnabled())
          LOG.debug("Calling separate page manager code for comments");

        String pageName = container.getDocumentName();
        String sql = ", BaseObject as obj, StringProperty as parentprop, StringProperty as statusprop where doc.fullName=obj.name and obj.className='" + getCommentsClassName(context) + "' and obj.id=parentprop.id.id and parentprop.id.name='" + COMMENT_CLASS_FIELDNAME_PARENT + "' and parentprop.value='"
                      + pageName + "' and obj.id=statusprop.id.id and statusprop.id.name='status' and statusprop.value='" + COMMENT_MODERATION_MODERATED + "' order by doc.date " + (asc  ? "asc" : "desc");
        List<Comment> comments = new ArrayList<Comment>();
        try {
            List<String> commentPageNameList = context.getWiki().getStore().searchDocumentsNames(sql, count, start, context);

            for (String commentPageName : commentPageNameList) {
                comments.add(new SeparatePageCommentsManager().getCommentFromDocument(container, context.getWiki().getDocument(commentPageName, context), context));
            }
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }

        return comments;
  }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.CommentsManager#getComments(com.xpn.xwiki.plugin.comments.Container, int, int, boolean, com.xpn.xwiki.XWikiContext)
     */
    public List<Comment> getComments(String status, int start, int count, boolean asc, XWikiContext context)
            throws CommentsException
    {
        if (LOG.isDebugEnabled())
            LOG.debug("Calling separate page manager code for comments");

        String sql = ", BaseObject as obj, StringProperty as statusprop where doc.fullName=obj.name and obj.className='" + getCommentsClassName(context) + "' and obj.id=statusprop.id.id and statusprop.id.name='status' and statusprop.value='" + status + "' order by doc.date " + (asc  ? "asc" : "desc");
        List<Comment> comments = new ArrayList<Comment>();
        try {
            List<String> commentPageNameList = context.getWiki().getStore().searchDocumentsNames(sql, count, start, context);

            for (String commentPageName : commentPageNameList) {
                // Container container = newContainer(context);                
                comments.add(new SeparatePageCommentsManager().getCommentFromDocument(null, context.getWiki().getDocument(commentPageName, context), context));
            }
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }

        return comments;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.CommentsManager#getComments(com.xpn.xwiki.plugin.comments.Container, int, int, boolean, com.xpn.xwiki.XWikiContext)
     */
    public Comment getComment(Container container, int id, XWikiContext context)
            throws CommentsException
    {
        String pageName = container.getDocumentName();
        String sql;
        if (!hasModeration(context))
            sql = ", BaseObject as obj, StringProperty as parentprop where doc.fullName=obj.name and obj.className='" + getCommentsClassName(context) + "' and obj.id=parentprop.id.id and parentprop.id.name='" + COMMENT_CLASS_FIELDNAME_PARENT + "' and parentprop.value='"
                    + pageName + "' order by doc.date desc";
        else
            sql = ", BaseObject as obj, StringProperty as parentprop where doc.fullName=obj.name and obj.className='" + getCommentsClassName(context) + "' and obj.id=parentprop.id.id and parentprop.id.name='" + COMMENT_CLASS_FIELDNAME_PARENT + "' and parentprop.value='"
                    + pageName + "' and obj.name not in (select obj2.name from BaseObject as obj2, StringProperty as statusprop where obj2.className='" + getCommentsClassName(context) + "' and obj2.id=statusprop.id.id and statusprop.id.name='status' and (statusprop.value='" + COMMENT_MODERATION_MODERATED + "' or statusprop.value='" + COMMENT_MODERATION_REFUSED + "') and obj.id=obj2.id) order by doc.date desc";
        try {
            List<String> commentPageNameList = context.getWiki().getStore().searchDocumentsNames(sql, 1, id, context);
            if ((commentPageNameList==null)||(commentPageNameList.size()==0))
                return null;
            else {
                return new SeparatePageCommentsManager().getCommentFromDocument(container, context.getWiki().getDocument(commentPageNameList.get(0), context), context);
            }
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

       public Comment getComment(String commentId, XWikiContext context) throws CommentsException {
        try {
            int i1 = commentId.indexOf(".");
            if (i1==-1)
                throw new CommentsException(CommentsException.MODULE_PLUGIN_COMMENTS, CommentsException.ERROR_COMMENTS_INVALID_COMMENT_ID,  "Invalid comment ID, cannot parse comment id");


            XWikiDocument doc = context.getWiki().getDocument(commentId, context);
            if (doc.isNew())
                throw new CommentsException(CommentsException.MODULE_PLUGIN_COMMENTS, CommentsException.ERROR_COMMENTS_INVALID_COMMENT_ID,  "Invalid comment ID, comment does not exist");

            BaseObject object = doc.getObject(getCommentsClassName(context));
            if (object==null)
                throw new CommentsException(CommentsException.MODULE_PLUGIN_COMMENTS, CommentsException.ERROR_COMMENTS_INVALID_COMMENT_ID,  "Invalid comment ID, comment does not exist");

            String parentDocName = object.getStringValue(COMMENT_CLASS_FIELDNAME_PARENT);
            XWikiDocument parentDoc = context.getWiki().getDocument(parentDocName, context);
            Container container = new DefaultContainer(parentDoc.getFullName(), -1, "", null, context);


            return new SeparatePageComment(container, doc, context);
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
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

    public List<Comment> getComments(Comment comment, int startlevel, int levelsnumber, boolean asc, XWikiContext context) throws CommentsException {
        String sql;
        if (!hasModeration(context))
          sql = ", BaseObject as obj, StringProperty as parentprop where doc.fullName=obj.name and obj.id=parentprop.id.id and parentprop.id.name='" + COMMENT_CLASS_FIELDNAME_PARENT + "' and parentprop.value='"
                  + comment.getCommentId() + "' order by doc.date " + (asc  ? "asc" : "desc");
        else
          sql = ", BaseObject as obj, StringProperty as parentprop where doc.fullName=obj.name and obj.className='" + getCommentsClassName(context) + "' and obj.id=parentprop.id.id and parentprop.id.name='" + COMMENT_CLASS_FIELDNAME_PARENT + "' and parentprop.value='"
                    + comment.getCommentId() + "' and obj.name not in (select obj2.name from BaseObject as obj2, StringProperty as statusprop where obj2.className='" + getCommentsClassName(context) + "' and obj2.id=statusprop.id.id and statusprop.id.name='status' and (statusprop.value='" + COMMENT_MODERATION_MODERATED + "' or statusprop.value='" + COMMENT_MODERATION_REFUSED +"') and obj.id=obj2.id) order by doc.date " + (asc  ? "asc" : "desc");

        List<Comment> comments = new ArrayList<Comment>();
        try {
            List<String> commentPageNameList = context.getWiki().getStore().searchDocumentsNames(sql, context);

            for (String commentPageName : commentPageNameList) {
                comments.add(new SeparatePageCommentsManager().getCommentFromDocument(comment.getAsContainer(), context.getWiki().getDocument(commentPageName, context), context));
            }
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }

        return comments;
    }

    public Comment getCommentFromDocument(Container container, XWikiDocument doc, XWikiContext context) throws CommentsException {
        return new SeparatePageComment(container, doc, context);
    }

    public Container getContainer(Comment comment, XWikiContext context) {
        // We create a new container
        SeparatePageComment scomment = (SeparatePageComment) comment;
        Container container = newContainer(context);
        container.setDocumentName(scomment.getDocument().getFullName());
        container.setSection(-1);
        container.setPhrase(null);
        // We change the comment
        container.setComment(comment);
        return container;
    }


    public int getNumberOfCommentsInThread(XWikiDocument document, XWikiContext context) throws CommentsException {
        String sql;
           if (!hasModeration(context))
             sql = "select count(distinct doc.fullName) from XWikiDocument as doc, BaseObject as obj, StringProperty as pageprop where doc.fullName=obj.name and obj.id=pageprop.id.id and pageprop.id.name='" + COMMENT_CLASS_FIELDNAME_PAGE + "' and pageprop.value='"
                     + document.getFullName() + "'";
           else
             sql = "select count(distinct doc.fullName) from XWikiDocument as doc, BaseObject as obj, StringProperty as pageprop where doc.fullName=obj.name and obj.className='" + getCommentsClassName(context) + "' and obj.id=pageprop.id.id and pageprop.id.name='" + COMMENT_CLASS_FIELDNAME_PAGE + "' and pageprop.value='"
                       + document.getFullName() + "' and obj.name not in (select obj2.name from BaseObject as obj2, StringProperty as statusprop where obj2.className='" + getCommentsClassName(context) + "' and obj2.id=statusprop.id.id and statusprop.id.name='status' and (statusprop.value='" + COMMENT_MODERATION_MODERATED + "' or statusprop.value='" + COMMENT_MODERATION_REFUSED +"') and obj.id=obj2.id)";

        List result = null;
        try {
            result = context.getWiki().search(sql, context);
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
        if ((result==null)||(result.size()==0))
              return 0;
           else
              return ((Number) result.get(0)).intValue();
    }

    public int getNumberOfCommentsInThread(Comment comment, XWikiContext context) throws CommentsException {
        try {
            return getNumberOfCommentsInThread(context.getWiki().getDocument(comment.getPage(), context), context);
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

}