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
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.plugin.comments.*;

import java.util.*;

public class SeparatePageComment implements Comment
{
    private static Log LOG = LogFactory.getLog(SeparatePageComment.class);

    private Container container;

    private XWikiDocument document;

    private XWikiContext context;

    public SeparatePageComment(Container container, String author, String content, XWikiContext context) throws CommentsException
    {
        this(container, author, new Date(), content, null, context);
    }

    public SeparatePageComment(Container container, String author, String content, Map commentMap, XWikiContext context)  throws CommentsException
    {
        this(container, author, new Date(), content, commentMap, context);
    }


    public SeparatePageComment(Container container, String author, Date date, String content, Map commentMap, XWikiContext context)  throws CommentsException
    {
        boolean isReply = false;
        if (container != null && container.getComment() != null) {
            isReply = true;
        }
        this.container = container;
        this.context = context;
        addDocument(container, author, date, content, isReply, commentMap);
    }

    public SeparatePageComment(Container container, XWikiDocument doc, XWikiContext context)  throws CommentsException
    {
        this.container = container;
        this.context = context;
        this.document = doc;
    }

    public CommentsManager getCommentsManager() {
        return ((CommentsPlugin) context.getWiki().getPlugin(CommentsPlugin.COMMENTS_PLUGIN_NAME, context)).getCommentsManager(context);
    }
        

    /**
     * CommentId represent the ID of the comment
     * In this case it is the page name
     * @return
     */
    public String getCommentId() {
        return getDocument().getFullName();
    }

    public String getGlobalCommentId() {
        return getCommentId();
    }

    public BaseObject getAsObject()
    {
        return getDocument().getObject(getCommentsManager().getCommentsClassName(context));
    }

    public XWikiDocument getDocument() {
        if (document==null) {
            try {
                document = context.getWiki().getDocument(getPageName(container, context), context);
            } catch (XWikiException e) {
                return null;
            }
        }
        return document;
    }
    
    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#getAuthor()
     */
    public String getAuthor()
    {
        return getAsObject().getStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_AUTHOR);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#setAuthor(String)
     */
    public void setAuthor(String author)
    {
        getAsObject().setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_AUTHOR, author);
    }

    /**
       * {@inheritDoc}
       *
       */
      public String getPage()
      {
          return getAsObject().getStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_PAGE);
      }

      /**
       * {@inheritDoc}
       *
       */
      public void setPage(String page)
      {
          getAsObject().setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_PAGE, page);
      }


    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#getDate()
     */
    public Date getDate()
    {
        return getAsObject().getDateValue(CommentsManager.COMMENT_CLASS_FIELDNAME_DATE);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#getContent()
     */
    public String getContent()
    {
        return getAsObject().getLargeStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_COMMENT);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#setContent(String)
     */
    public void setContent(String content)
    {
        getAsObject().setLargeStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_COMMENT, content);
    }

    public String getStatus()
    {
        return getAsObject().getStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_STATUS);
    }

    public void setStatus(String status)
    {
        getAsObject().setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_STATUS, status);
    }

    /**
     * {@inheritDoc}
     *
     * @see Comment#get(String)
     */
    public Object get(String propertyName)
    {
        try {
            return ((BaseProperty)getAsObject().get(propertyName)).getValue();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see Comment#display(String,String,XWikiContext)
     */
    public String display(String propertyName, String mode, XWikiContext context) {
        return document.display(propertyName, mode, getAsObject(), context);
    }
    
    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#getParentComment()
     */
    public Comment getParentComment()
    {
        return container.getComment();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#getContainer()
     */
    public Container getContainer()
    {
        return container;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#addComment(String, String)
     */
    public void addComment(String author, String content) throws CommentsException
    {
        new DefaultComment(getAsContainer(), author, content, context);
    }

    /**
      * {@inheritDoc}
      *
      * @see Comment#getComments(boolean)
      */
     public List<Comment> getComments(boolean asc) throws CommentsException
    {
        return getCommentsManager().getComments(this, 0, 1, asc, context);
    }
    
    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#getComments(int, int, boolean)
     */
    public List<Comment> getComments(int startlevel, int levelsnumber, boolean asc) throws CommentsException
    {
        return getCommentsManager().getComments(this, startlevel, levelsnumber, asc, context);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#getAsContainer()
     */
    public Container getAsContainer() throws CommentsException
    {
        return getCommentsManager().getContainer(this, context);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#save()
     */
    public void save() throws CommentsException
    {
        try {
            context.getWiki().saveDocument(getDocument(), context);
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

    public void acceptComment() throws CommentsException {
        setStatus(CommentsManager.COMMENT_MODERATION_ACCEPTED);
        save();
    }

    public void refuseComment() throws CommentsException {
        setStatus(CommentsManager.COMMENT_MODERATION_REFUSED);
        save();
    }

    public void remoderateComment() throws CommentsException {
        setStatus(CommentsManager.COMMENT_MODERATION_MODERATED);
        save();
    }
    
    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#remove()
     */
    public boolean remove()  throws CommentsException
    {
        try {
            XWikiDocument doc = getDocument();
            // Remove children comments
            List<Comment> comments = getCommentsManager().getComments(getAsContainer(), 0, 0, true, context);
            if ((comments!=null)&&(comments.size()>0)) {
                for (Comment comment : comments) {
                    comment.remove();
                }
            }
            // remove the comment
            context.getWiki().deleteDocument(doc, context);
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
        return true;
    }

    /**
     * Generate page name from the container page
     * We add Comment and getUniquePageName will add us a counter to our page
     * @param container
     * @return
     */
    private String getPageName(Container container, XWikiContext context) throws XWikiException {
        XWikiDocument doc = context.getWiki().getDocument(container.getDocumentName(), context);
        return doc.getSpace() + "." + context.getWiki().getUniquePageName(doc.getSpace(), doc.getName() + "C", "", true, context);
    }

    private void addDocument(Container container, String author, Date date, String content, boolean isreply, Map commentMap) throws CommentsException
    {
        try {
            String commentsClassName = getCommentsManager().getCommentsClassName(context);
            String pageName = getPageName(container, context);
            String parentDocName = container.getDocumentName();
            com.xpn.xwiki.XWiki xwiki = context.getWiki();
            XWikiDocument doc = xwiki.getDocument(pageName, context);
            doc.setParent(parentDocName);
            BaseObject obj = new BaseObject();
            obj.setClassName(commentsClassName);
            obj.setName(pageName);

            // read data from map
            if (commentMap!=null)
             context.getWiki().getDocument(commentsClassName, context).getxWikiClass().fromMap(commentMap, obj);

            obj.setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_AUTHOR, author);
            obj.setLargeStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_HIGHLIGHT, "");
            if (isreply) {
                obj.setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_PARENT, container.getComment().getCommentId());
                obj.setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_PAGE, container.getComment().getPage());
            } else {
                obj.setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_PARENT, parentDocName);
                obj.setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_PAGE, parentDocName);
            }
            obj.setDateValue(CommentsManager.COMMENT_CLASS_FIELDNAME_DATE, date);
            obj.setLargeStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_COMMENT, content);
            doc.addObject(commentsClassName, obj);
            doc.setContent("#includeForm(\"XWiki.XWikiCommentSheet\")");
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

    public String toString()
    {
        boolean shouldAddSpace = false;
        StringBuffer sb = new StringBuffer();
        if (getAuthor() != null) {
            sb.append("\nAuthor=").append(getAuthor());
            shouldAddSpace = true;
        }
        if (getDate() != null) {
            sb.append(shouldAddSpace ? " " : "");
            sb.append("\nDate=").append(getDate());
            shouldAddSpace = true;
        }
        if (getContent() != null) {
            sb.append(shouldAddSpace ? " " : "");
            sb.append("\nContent=").append(getContent()).append("\n");
            shouldAddSpace = true;
        }

        return sb.toString();
    }

}