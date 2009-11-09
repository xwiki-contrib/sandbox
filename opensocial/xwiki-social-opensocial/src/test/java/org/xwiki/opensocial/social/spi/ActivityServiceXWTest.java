package org.xwiki.opensocial.social.spi;

import org.jmock.Mock;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.test.AbstractXWikiComponentTestCase;

public class ActivityServiceXWTest extends AbstractXWikiComponentTestCase
{
    private Mock mockDocumentAccessBridge;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractXWikiComponentTestCase#registerComponents()
     */
    @Override
    protected void registerComponents() throws Exception
    {
        this.mockDocumentAccessBridge = mock(DocumentAccessBridge.class);
        DefaultComponentDescriptor<DocumentAccessBridge> descriptor =
            new DefaultComponentDescriptor<DocumentAccessBridge>();
        descriptor.setRole(DocumentAccessBridge.class);
        getComponentManager().registerComponent(descriptor,
            (DocumentAccessBridge) this.mockDocumentAccessBridge.proxy());
    }

    /**
     * @throws Exception
     */
    public void testGetActivity() throws Exception
    {

    }
}
