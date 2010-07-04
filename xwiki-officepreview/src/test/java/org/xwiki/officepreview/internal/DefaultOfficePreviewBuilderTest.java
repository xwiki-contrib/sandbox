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
package org.xwiki.officepreview.internal;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.officeimporter.builder.XDOMOfficeDocumentBuilder;
import org.xwiki.officeimporter.document.XDOMOfficeDocument;
import org.xwiki.officepreview.OfficePreviewBuilder;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;

/**
 * Test case for {@link DefaultOfficePreviewBuilder}.
 * 
 * @version $Id$
 */
public class DefaultOfficePreviewBuilderTest extends AbstractOfficePreviewTestCase
{
    /**
     * {@link DefaultOfficePreviewBuilder} reference.
     */
    private OfficePreviewBuilder defaultOfficePreviewBuilder;

    /**
     * Mock {@link AttachmentVersionProvider} instance.
     */
    private AttachmentVersionProvider mockAttachmentVersionProvider;
    
    /**
     * Mock {@link XDOMOfficeDocumentBuilder} instance.
     */
    private XDOMOfficeDocumentBuilder mockOfficeDocumentBuilder;

    /**
     * {@inheritDoc}
     */
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.defaultOfficePreviewBuilder = getComponentManager().lookup(OfficePreviewBuilder.class);
        
        // Attachment version provider must be mocked.
        this.mockAttachmentVersionProvider = getMockery().mock(AttachmentVersionProvider.class);
        ReflectionUtils.setFieldValue(defaultOfficePreviewBuilder, "attachmentVersionProvider",
            mockAttachmentVersionProvider);        
        
        // Office document builder must be mocked.
        this.mockOfficeDocumentBuilder = getMockery().mock(XDOMOfficeDocumentBuilder.class);
        ReflectionUtils.setFieldValue(defaultOfficePreviewBuilder, "builder", mockOfficeDocumentBuilder);
    }
    
    /**
     * Test the previewing of a non-existing attachment.
     * 
     * @throws Exception if an error occurs.
     */
    @Test
    public void testOfficePreviewWithNonExistingAttachment() throws Exception {
        final DocumentReference documentReference = new DocumentReference("xwiki", "Main", "Test");
        final AttachmentReference attachmentReference = new AttachmentReference("Test.doc", documentReference);
        final String strAttachmentReference = "xwiki:Main.Test@Test.doc";
        
        getMockery().checking(new Expectations(){{
            oneOf(mockDefaultStringEntityReferenceSerializer).serialize(attachmentReference);
            will(returnValue(strAttachmentReference));
            
            oneOf(mockDocumentAccessBridge).getAttachmentReferences(documentReference);
            will(returnValue(new ArrayList<AttachmentReference>()));
        }});
        
        try {
            defaultOfficePreviewBuilder.build(attachmentReference, true);
            Assert.fail("Expected exception.");
        } catch (Exception ex) {
            Assert.assertEquals(String.format("Attachment [%s] does not exist.", strAttachmentReference),
                ex.getMessage());
        }
    }

    /**
     * Tests the normal office preview function.
     * 
     * @throws Exception if an error occurs.
     */
    @Test
    public void testOfficePreviewWithCacheMiss() throws Exception {
        final DocumentReference documentReference = new DocumentReference("xwiki", "Main", "Test");
        final AttachmentReference attachmentReference = new AttachmentReference("Test.doc", documentReference);
        final String strAttachmentReference = "xwiki:Main.Test@Test.doc";
        final ByteArrayInputStream attachmentContent = new ByteArrayInputStream(new byte [256]);
        final XDOMOfficeDocument officeDocument = new XDOMOfficeDocument(new XDOM(new ArrayList<Block>()),
            new HashMap<String, byte[]>(), getComponentManager());
        
        getMockery().checking(new Expectations(){{
            oneOf(mockDefaultStringEntityReferenceSerializer).serialize(attachmentReference);
            will(returnValue(strAttachmentReference));
            
            oneOf(mockDocumentAccessBridge).getAttachmentReferences(documentReference);
            will(returnValue(Arrays.asList(attachmentReference)));
            
            oneOf(mockAttachmentVersionProvider).getAttachmentVersion(attachmentReference);
            will(returnValue("1.1"));
            
            oneOf(mockDocumentAccessBridge).getAttachmentContent(attachmentReference);
            will(returnValue(attachmentContent));
            
            oneOf(mockOfficeDocumentBuilder).build(attachmentContent, "Test.doc", documentReference, true);
            will(returnValue(officeDocument));                        
        }});
        
        XDOM preview = defaultOfficePreviewBuilder.build(attachmentReference, true);
        Assert.assertNotNull(preview);
    }
}
