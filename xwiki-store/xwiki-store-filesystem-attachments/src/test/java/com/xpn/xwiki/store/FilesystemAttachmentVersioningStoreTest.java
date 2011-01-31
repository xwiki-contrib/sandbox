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
package com.xpn.xwiki.store;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.xwiki.model.internal.reference.PathStringEntityReferenceSerializer;
import org.xwiki.store.filesystem.internal.FilesystemStoreTools;
import org.xwiki.store.filesystem.internal.DefaultFilesystemStoreTools;
import org.xwiki.store.serialization.xml.internal.AttachmentListMetadataSerializer;
import org.xwiki.store.serialization.xml.internal.AttachmentMetadataSerializer;
import org.xwiki.store.filesystem.internal.AttachmentFileProvider;
import com.xpn.xwiki.store.AttachmentVersioningStore;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiAttachmentContent;
import com.xpn.xwiki.doc.XWikiAttachmentArchive;
import com.xpn.xwiki.doc.ListAttachmentArchive;
import com.xpn.xwiki.web.Utils;

import org.xwiki.test.AbstractMockingComponentTestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.io.IOUtils;


/**
 * Tests for FilesystemAttachmentVersioningStore.
 *
 * @version $Id$
 * @since TODO
 */
public class FilesystemAttachmentVersioningStoreTest extends AbstractMockingComponentTestCase
{
    private FilesystemStoreTools fileTools;

    private AttachmentVersioningStore versionStore;

    private XWikiAttachmentArchive archive;

    private AttachmentFileProvider provider;

    private File storageLocation;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        Utils.setComponentManager(this.getComponentManager());

        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        this.storageLocation = new File(tmpDir, "test-storage-location");

        this.fileTools =
            new DefaultFilesystemStoreTools(new PathStringEntityReferenceSerializer(), storageLocation);
        final AttachmentListMetadataSerializer serializer =
            new AttachmentListMetadataSerializer(new AttachmentMetadataSerializer());
        this.versionStore = new FilesystemAttachmentVersioningStore(this.fileTools, serializer);

        final XWikiDocument doc = new XWikiDocument("xwiki", "Main", "WebHome");

        final XWikiAttachment version1 = new XWikiAttachment();
        version1.setVersion("1.1");
        version1.setFilename("attachment.txt");
        version1.setDoc(doc);
        version1.setAttachment_content(new StringAttachmentContent("I am version 1.1"));

        final XWikiAttachment version2 = new XWikiAttachment();
        version2.setVersion("1.2");
        version2.setFilename("attachment.txt");
        version2.setDoc(doc);
        version2.setAttachment_content(new StringAttachmentContent("I am version 1.2"));

        final XWikiAttachment version3 = new XWikiAttachment();
        version3.setVersion("1.3");
        version3.setFilename("attachment.txt");
        version3.setDoc(doc);
        version3.setAttachment_content(new StringAttachmentContent("I am version 1.3"));

        this.provider = this.fileTools.getAttachmentFileProvider(version1);
        this.archive = ListAttachmentArchive.newInstance(new ArrayList<XWikiAttachment>() {{
            add(version1);
            add(version2);
            add(version3);
        }});
    }

    @After
    public void tearDown() throws IOException
    {
        resursiveDelete(this.storageLocation);
    }

    @Test
    public void saveArchiveTest() throws Exception
    {
        Assert.assertFalse(this.provider.getAttachmentVersioningMetaFile().exists());
        Assert.assertFalse(this.provider.getAttachmentVersionContentFile("1.1").exists());
        Assert.assertFalse(this.provider.getAttachmentVersionContentFile("1.2").exists());
        Assert.assertFalse(this.provider.getAttachmentVersionContentFile("1.3").exists());

        // Because the context is only used by the legacy implementation, it is safe to pass null.
        this.versionStore.saveArchive(archive, null, false);

        Assert.assertTrue(this.provider.getAttachmentVersioningMetaFile().exists());
        Assert.assertTrue(this.provider.getAttachmentVersionContentFile("1.1").exists());
        Assert.assertTrue(this.provider.getAttachmentVersionContentFile("1.2").exists());
        Assert.assertTrue(this.provider.getAttachmentVersionContentFile("1.3").exists());
    }

    @Test
    public void loadArchiveTest() throws Exception
    {
        this.versionStore.saveArchive(archive, null, false);
        final XWikiAttachmentArchive newArch =
            this.versionStore.loadArchive(archive.getAttachment(), null, false);
        Assert.assertTrue(newArch.getVersions().length == 3);
        final XWikiAttachment version1 = newArch.getRevision(archive.getAttachment(), "1.1", null);
        final XWikiAttachment version2 = newArch.getRevision(archive.getAttachment(), "1.2", null);
        final XWikiAttachment version3 = newArch.getRevision(archive.getAttachment(), "1.3", null);

        Assert.assertTrue(version1.getVersion().equals("1.1"));
        Assert.assertTrue(version1.getFilename().equals("attachment.txt"));
        Assert.assertTrue(IOUtils.toString(version1.getContentInputStream(null)).equals("I am version 1.1"));

        Assert.assertTrue(version2.getVersion().equals("1.2"));
        Assert.assertTrue(version2.getFilename().equals("attachment.txt"));
        Assert.assertTrue(IOUtils.toString(version2.getContentInputStream(null)).equals("I am version 1.2"));

        Assert.assertTrue(version3.getVersion().equals("1.3"));
        Assert.assertTrue(version3.getFilename().equals("attachment.txt"));
        Assert.assertTrue(IOUtils.toString(version3.getContentInputStream(null)).equals("I am version 1.3"));
    }

    @Test
    public void deleteArchiveTest() throws Exception
    {
        this.versionStore.saveArchive(this.archive, null, false);

        Assert.assertTrue(this.provider.getAttachmentVersioningMetaFile().exists());
        Assert.assertTrue(this.provider.getAttachmentVersionContentFile("1.1").exists());
        Assert.assertTrue(this.provider.getAttachmentVersionContentFile("1.2").exists());
        Assert.assertTrue(this.provider.getAttachmentVersionContentFile("1.3").exists());

        this.versionStore.deleteArchive(this.archive.getAttachment(), null, false);

        Assert.assertFalse(this.provider.getAttachmentVersioningMetaFile().exists());
        Assert.assertFalse(this.provider.getAttachmentVersionContentFile("1.1").exists());
        Assert.assertFalse(this.provider.getAttachmentVersionContentFile("1.2").exists());
        Assert.assertFalse(this.provider.getAttachmentVersionContentFile("1.3").exists());
    }

    private static class StringAttachmentContent extends XWikiAttachmentContent
    {
         private final String content;

         public StringAttachmentContent(final String content)
         {
             this.content = content;
         }

         public InputStream getContentInputStream()
         {
             return new ByteArrayInputStream(this.content.getBytes());
         }
    }

    /* -------------------- Helpers -------------------- */

    private static void resursiveDelete(final File toDelete) throws IOException
    {
        if (toDelete == null || !toDelete.exists()) {
            return;
        }
        if (toDelete.isDirectory()) {
            final File[] children = toDelete.listFiles();
            for (int i = 0; i < children.length; i++) {
                resursiveDelete(children[i]);
            }
        }
        toDelete.delete();
    }
}