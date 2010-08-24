/**
 * 
 */
package com.xpn.xwiki.plugin.comments.internal;

import java.util.List;



import org.apache.ecs.xhtml.comment;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.comments.Comment;
import com.xpn.xwiki.plugin.comments.CommentsException;
import com.xpn.xwiki.plugin.comments.CommentsManager;
import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.plugin.comments.XWikiMockTestCase;
import com.xpn.xwiki.plugin.comments.internal.DefaultCommentsManager;
import com.xpn.xwiki.plugin.comments.internal.DefaultContainer;

import junit.framework.TestCase;

/**
 * @author Florin
 *
 */
public abstract class AbstractCommentsManagerTest extends XWikiMockTestCase
{
    protected CommentsManager commentsmanager;
    //protected XWikiContext context = new XWikiContext();
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test method for {@link com.xpn.xwiki.plugin.comments.internal.AbstractCommentsManager#initCommentsClass(com.xpn.xwiki.XWikiContext)}.
     */
    public final void testInitCommentsClass()
    {
        
    }

    /**
     * Test method for {@link com.xpn.xwiki.plugin.comments.internal.AbstractCommentsManager#newContainer(com.xpn.xwiki.XWikiContext)}.
     */
    public final void testNewContainer()
    {
        Container c = null;
        c = commentsmanager.newContainer(context);
        assertNotNull("newContainer method failed", c);
        assertTrue("newContainer method failed", c instanceof Container);
    }

    /**
     * Test method for {@link com.xpn.xwiki.plugin.comments.internal.AbstractCommentsManager#addComment(com.xpn.xwiki.plugin.comments.Container, java.lang.String, java.lang.String, com.xpn.xwiki.XWikiContext)}.
     * @throws CommentsException 
     */
    public void testAddComment() throws CommentsException
    {
        Container container=new DefaultContainer(context);
        container.setDocumentName("TestPage");
        List<Comment> comments = commentsmanager.getComments(container, 0, 0, false, context);
        int initComNo = (comments != null) ? comments.size() : 0;
        commentsmanager.addComment(container, "TestAuthor", "Content", context);
        comments = commentsmanager.getComments(container, 0, 0, false, context);
        int comNo = (comments != null) ? comments.size() : 0;
        assertTrue("addComment methos failed",comNo == initComNo + 1);
    }

    /**
     * Test method for {@link com.xpn.xwiki.plugin.comments.internal.AbstractCommentsManager#removeComment(com.xpn.xwiki.plugin.comments.Comment, com.xpn.xwiki.XWikiContext)}.
     * @throws CommentsException 
     */
    public void _testRemoveComment() throws CommentsException
    {
        int initComNo, comNo, finalComNo;
        int initObjNo, objNo, finalObjNo;
        List<Comment> comments,childComments;
        Comment parentComment,childComment;
        Container container = new DefaultContainer(context);
        container.setDocumentName("TestPage");
        comments = commentsmanager.getComments(container, 0, 0, false, context);
        initComNo = (comments != null) ? comments.size() : 0;
        parentComment = new DefaultComment(container,"Author","Content",context);
        parentComment.addComment("Author2", "Content2");
        childComments = parentComment.getComments(false);
        assertNotNull(parentComment);
        assertEquals(1, childComments.size());
        childComment = childComments.get(0);          
        comments = commentsmanager.getComments(container, 0, 0, false, context);
        comNo = (comments != null) ? comments.size() : 0;
        assertEquals(initComNo, 0);
        //assertEquals(initComNo + 1, comNo);
        commentsmanager.removeComment(childComment, context);
        comments = commentsmanager.getComments(container, 1, 0, false, context);
        finalComNo = (comments != null) ? comments.size() : 0;
        assertEquals(finalComNo, comNo + 1);
        
    }

    /**
     * Test method for {@link com.xpn.xwiki.plugin.comments.internal.AbstractCommentsManager#getParentComment(com.xpn.xwiki.plugin.comments.Comment, com.xpn.xwiki.XWikiContext)}.
     * @throws CommentsException 
     */
    public final void _testGetParentComment() throws CommentsException
    {
        List<Comment> childComments;
        Comment parentComment,childComment;
        Container container = commentsmanager.newContainer(context);
        parentComment = new DefaultComment(container,"Author","Content",context);
        parentComment.addComment("Author2", "Content2");
        childComments = parentComment.getComments(false);
        assertNotNull(childComments);
        assertTrue(childComments.size() > 0);
        childComment = childComments.get(0);
        assertEquals(parentComment.getCommentId(),childComment.getParentComment().getCommentId());        
    }   

}
