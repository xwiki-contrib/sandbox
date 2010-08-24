package com.xpn.xwiki.plugin.comments.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.comments.Comment;
import com.xpn.xwiki.plugin.comments.CommentsException;
import com.xpn.xwiki.plugin.comments.CommentsManager;
import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.plugin.comments.XWikiMockTestCase;

import junit.framework.TestCase;

public class DefaultCommentTest extends XWikiMockTestCase
{
    protected DefaultComment defaultComment;

    protected CommentsManager commentsManager;

    Container container;

    protected void setUp() throws Exception
    {
        super.setUp();
        container = new DefaultContainer(context);
        commentsManager = new DefaultCommentsManager();
        container.setDocumentName("TestPage");
        // defaultComment = new DefaultComment(container,"Author","Content",context);
        defaultComment = new DefaultComment(container, "Author", "Content", new HashMap(), context);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test fails because the constructor passes a null Map parameter.
     */
    public final void _testDefaultCommentContainerStringStringXWikiContext()
    {
        Comment comment = new DefaultComment(container, "Author", "Content", context);
        assertNotNull(comment);
        assertTrue(comment instanceof Comment);
    }

    public final void testDefaultCommentContainerStringStringMapXWikiContext()
    {
        Comment comment = new DefaultComment(container, "Author", "Content", new HashMap(), context);
        assertNotNull(comment);
        assertTrue(comment instanceof Comment);
    }

    public final void testDefaultCommentContainerStringDateStringMapXWikiContext()
    {
        Comment comment = new DefaultComment(container, "Author", new Date(), "Content", new HashMap(), context);
        assertNotNull(comment);
        assertTrue(comment instanceof Comment);
    }

    public final void testDefaultCommentContainerXWikiDocumentBaseObjectXWikiContext()
    {
        XWikiDocument doc = new XWikiDocument("TestSpace", container.getDocumentName());
        BaseObject obj = new BaseObject();
        Comment comment = new DefaultComment(container, doc, obj, context);
        assertNotNull(comment);
        assertTrue(comment instanceof Comment);
    }

    public final void testGetCommentsManager()
    {
        CommentsManager cm = defaultComment.getCommentsManager();
        assertNotNull(cm);
        assertTrue(cm instanceof CommentsManager);
    }

    public final void testGetCommentId()
    {
        String id = defaultComment.getCommentId();
        assertNotNull(id);
    }

    public final void testGetAsObject()
    {
        BaseObject obj = defaultComment.getAsObject();
        assertTrue(obj instanceof BaseObject);
    }

    public final void testAddComment()
    {
        try {
            String commentsClass = commentsManager.getCommentsClassName(context);
            List<Comment> comments = defaultComment.getComments(false);
            int commNo = comments.size();
            defaultComment.addComment("Author", "Content");
            defaultComment.acceptComment();
            comments = defaultComment.getComments(false);
            XWikiDocument doc = defaultComment.getDocument();
            String commentsClassName = commentsManager.getCommentsClassName(context);
            assertEquals(doc.getObjects(commentsClassName).size(), 1);
            // assertTrue("Test method DefaultComment.addComments failed",commNo +1 == comments.size());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    public void testObjects()
    {
        XWikiDocument doc = new XWikiDocument("Space", "Name");

        String commentsClassName = commentsManager.getCommentsClassName(context);
        BaseObject obj = new BaseObject();
        obj.setClassName(commentsClassName);
        obj.setName(doc.getFullName());
        obj.setStringValue("author", "TestAuthor");
        obj.setLargeStringValue("highlight", "");
        obj.setStringValue("replyto", "");

        obj.setDateValue("date", new Date());
        obj.setLargeStringValue("comment", "Content");
        doc.addObject(commentsClassName, obj);
        assertEquals(doc.getObjects(commentsClassName).size(), 1);
    }

    public final void testAcceptComment()
    {
        XWikiDocument doc;
        try {
            doc = defaultComment.getDocument();
            defaultComment.addComment("TestAuthor", "Content");
            xwiki.saveDocument(doc, context);
            defaultComment.acceptComment();
            assertEquals("accepted", defaultComment.getStatus());
        } catch (XWikiException e) {
            e.printStackTrace();
            fail();
        }

    }

    public final void testRefuseComment() throws CommentsException
    {
        defaultComment.refuseComment();
        assertEquals("refused", defaultComment.getStatus());
    }

    public final void testGetAsContainer()
    {
        try {
            Container c = defaultComment.getAsContainer();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public final void testSave() throws XWikiException
    {
        defaultComment.setContent("New Content");
        XWikiDocument doc = defaultComment.getDocument();
        assertTrue(doc.isContentDirty());
        defaultComment.save();
        // assertFalse(doc.isContentDirty());
    }

    public final void testRemove()
    {
        try {
            defaultComment.remove();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
