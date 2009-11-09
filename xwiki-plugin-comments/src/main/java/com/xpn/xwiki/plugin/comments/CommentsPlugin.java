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

import com.xpn.xwiki.plugin.comments.internal.DefaultCommentsManager;
import com.xpn.xwiki.plugin.comments.internal.SeparatePageCommentsManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;

import java.util.List;
import java.util.Map;

public class CommentsPlugin extends XWikiDefaultPlugin
{
    private static Log LOG = LogFactory.getLog(CommentsPlugin.class);

    private final static Object COMMENTS_MANAGER_LOCK = new Object();

    public static final String COMMENTS_PLUGIN_NAME = "comments";

    private CommentsManager commentsManager;

    public CommentsPlugin(String name, String className, XWikiContext context)
    {
        super(name, className, context);
        init(context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.XWikiDefaultPlugin#getName()
     */
    public String getName()
    {
        return COMMENTS_PLUGIN_NAME;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.XWikiDefaultPlugin#getPluginApi
     */
    public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context)
    {
        return new CommentsPluginApi((CommentsPlugin) plugin, context);
    }

    public CommentsManager getCommentsManager(XWikiContext context)
    {
        synchronized (this.COMMENTS_MANAGER_LOCK) {
            if (this.commentsManager == null) {
                String commentsManagerClass;
                commentsManagerClass =
                        context.getWiki().Param("xwiki.comments.commentsmanager", "com.xpn.xwiki.plugin.comments.internal.DefaultCommentsManager");

                if (LOG.isDebugEnabled())
                      LOG.debug("Init comments manager with class " + commentsManagerClass);

                try {
                    this.commentsManager = (CommentsManager) Class.forName(commentsManagerClass).newInstance();
                } catch (Exception e) {
                    if (LOG.isErrorEnabled())
                     LOG.error("Could not init comments manager for class " + commentsManagerClass, e);
                    this.commentsManager = new DefaultCommentsManager();
                }
            }
        }

        return this.commentsManager;
    }


    public void setCommentsManager(CommentsManager commentsManager)
    {
        this.commentsManager = commentsManager;
    }

    public void init(XWikiContext context)
    {
        try {
           getCommentsManager(context).init(context);
            // initCommentsClass(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void virtualInit(XWikiContext context)
    {
        try {
            getCommentsManager(context).virtualInit(context);
            // initCommentsClass(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Comment addComment(XWikiDocument doc, String author, String content, String parent, XWikiContext context) throws CommentsException {
        return addComment(doc, author, content, parent, null, context);
    }

    public Comment addComment(XWikiDocument doc, String author, String content, String parent, Map commentMap, XWikiContext context) throws CommentsException {
        try {
            Comment parentComment = null;
            Container container;
            // Get the container corresponding to the document
            Container parentContainer = getCommentsManager(context).newContainer(context);
            parentContainer.setDocumentName(doc.getFullName());

            CommentsManager cmanager = getCommentsManager(context);
            if ((cmanager instanceof SeparatePageCommentsManager) && (doc.getObject(cmanager.getCommentsClassName(context))!=null))  {
                // get the container corresponding to the comment in the document
                parentComment =  cmanager.getComment(doc.getFullName(), context);
                container = parentComment.getAsContainer();
            } else if ((parent!=null)&&(!parent.equals(""))) {
                // get the container corresponding to the comment in the document
                parentComment =  cmanager.getComment(parent, context);
                container = parentComment.getAsContainer();
            } else {
                container = parentContainer;
            }
            // add the comment
            return cmanager.addComment(container, author, content, commentMap, context);
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

    public List<Comment> getComments(XWikiDocument doc, boolean asc, XWikiContext context) throws CommentsException {
            return getComments(doc, 0, 0, asc, context);
    }

    public List<Comment> getComments(XWikiDocument doc, int start, int count, boolean asc, XWikiContext context) throws CommentsException {
        Container container = getCommentsManager(context).newContainer(context);
        container.setDocumentName(doc.getFullName());
        return getCommentsManager(context).getComments(container, start, count, asc, context);        
    }

    public List<Comment> getComments(Comment comment, int start, int count, boolean asc, XWikiContext context) throws CommentsException {
         return getCommentsManager(context).getComments(comment.getAsContainer(), start, count, asc, context);
    }

    public List<Comment> getModeratedComments( int start, int count, boolean asc, XWikiContext context) throws CommentsException {
        return getCommentsManager(context).getModeratedComments( start, count, asc, context);
    }

    public List<Comment> getAcceptedComments( int start, int count, boolean asc, XWikiContext context) throws CommentsException {
        return getCommentsManager(context).getAcceptedComments( start, count, asc, context);
    }

    public List<Comment> getRefusedComments( int start, int count, boolean asc, XWikiContext context) throws CommentsException {
        return getCommentsManager(context).getRefusedComments( start, count, asc, context);
    }

    public boolean acceptComment(Comment comment, XWikiContext context) throws CommentsException {
        return getCommentsManager(context).acceptComment(comment, context);
    }

    public boolean refuseComment(Comment comment, XWikiContext context) throws CommentsException {
        return getCommentsManager(context).refuseComment(comment, context);
    }

    public boolean remoderateComment(Comment comment, XWikiContext context) throws CommentsException {
        return getCommentsManager(context).remoderateComment(comment, context);
    }

    public Comment getComment(String commentId, XWikiContext context) throws CommentsException {
        return getCommentsManager(context).getComment(commentId, context);
    }

    public boolean isModerator(XWikiContext context) throws CommentsException {
        return getCommentsManager(context).isModerator(context);

    }

    public boolean isModerated(XWikiContext context) throws CommentsException {
        return getCommentsManager(context).isModerated(context);

    }

    public int getNumberOfCommentsInThread(XWikiDocument document, XWikiContext context) throws CommentsException {
        return getCommentsManager(context).getNumberOfCommentsInThread(document, context);
    }

    public int getNumberOfCommentsInThread(Comment comment, XWikiContext context) throws CommentsException {
        return getCommentsManager(context).getNumberOfCommentsInThread(comment, context);
    }

}