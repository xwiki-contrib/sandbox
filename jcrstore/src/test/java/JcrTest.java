import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.jcrom.Jcrom;
import org.xwiki.store.jcr.JcromProvider;
import org.xwiki.store.jcr.SessionFactory;
import org.xwiki.store.value.AttachmentValue;
import org.xwiki.store.value.DocumentValue;

import com.xpn.xwiki.test.AbstractXWikiComponentTestCase;

public class JcrTest extends AbstractXWikiComponentTestCase
{
    public void testSome() throws Exception
    {
        SessionFactory sessionFactory = (SessionFactory) getComponentManager().lookup(SessionFactory.ROLE);
        JcromProvider jcromProvider = (JcromProvider) getComponentManager().lookup(JcromProvider.ROLE);
        Jcrom jcrom = jcromProvider.getJcrom();

        Session session = sessionFactory.getWriteSession(null);
        DocumentValue doc = new DocumentValue();
        doc.name = "WebHome";
        doc.content = "some content";
        AttachmentValue att = new AttachmentValue();
        att.filename = "attach1.txt";
        att.content = IOUtils.toInputStream("content");
        doc.attachments.add(att);
        try {
            jcrom.addNode(session.getRootNode(), doc);
            session.save();
        } finally {
            session.logout();
        }
        session = sessionFactory.getReadSession(null);
        try {
            DocumentValue doc1 = jcrom.fromNode(DocumentValue.class, session.getRootNode().getNode("WebHome"));
            assertEquals(doc.name, doc1.name);
            assertEquals(doc.content, doc1.content);
            assertEquals(1, doc.attachments.size());
            AttachmentValue att1 = doc1.attachments.get(0);
            assertEquals("attach1.txt", att1.filename);

            assertEquals("content", IOUtils.toString(att1.content));
        } finally {
            session.logout();
        }
    }
}
