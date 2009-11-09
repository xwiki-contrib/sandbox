import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.test.AbstractXWikiComponentTestCase;


public class MemoryStoreTest extends AbstractXWikiComponentTestCase
{
    public void testDocument() throws Exception
    {
        XWikiContext context = new XWikiContext();
        XWiki xwiki = new XWiki();
        xwiki.setConfig(new XWikiConfig());
        context.setWiki(xwiki);

        XWikiStoreInterface store = (XWikiStoreInterface) getComponentManager().lookup(XWikiStoreInterface.ROLE);

        XWikiDocument doc = new XWikiDocument("Main", "WebHome");
        doc.setContent("test");

        store.saveXWikiDoc(doc, context);

        XWikiDocument doc1 = new XWikiDocument("Main", "WebHome");
        store.loadXWikiDoc(doc1, context);

        assertEquals(doc, doc1);
    }
}
