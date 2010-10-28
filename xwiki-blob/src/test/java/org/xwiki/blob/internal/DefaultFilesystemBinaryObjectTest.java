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
 *
 */

package org.xwiki.blob.internal;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.blob.BinaryObject;

/**
 * Tests the DefaultBinaryObject
 *
 * @version $Id$
 * @since 2.6M1
 */
public class DefaultFilesystemBinaryObjectTest
{
    private final String testContent = "This is some content to put in a file.";

    private final String otherTestContent = "This is different content.";

    private BinaryObject binaryObj;

    @Before
    public void setUp() throws Exception
    {
        this.binaryObj = new DefaultBinaryObject(
            new FilesystemFastStorageItem(new File(System.getProperty("java.io.tmpdir"))),
            new FilesystemFastStorageItem(new File(System.getProperty("java.io.tmpdir"))),
            new FilesystemFastStorageItem(new File(System.getProperty("java.io.tmpdir")))
        );
    }

    @After
    public void tearDown() throws Exception
    {
        this.binaryObj.clear();
    }

    @Test
    public void addGetContentTest() throws Exception
    {
        this.binaryObj.addContent(new ByteArrayInputStream(this.testContent.getBytes("US-ASCII")));
        this.binaryObj.save();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        binaryObj.getContent(baos);
        Assert.assertEquals(this.testContent, baos.toString());
    }

    /** Hold a lock for 1 seconds, assert that it stays held for at least 1/2. */
    @Test
    public void readLockTest() throws Exception
    {
        final OutputStream os = this.binaryObj.addContent();
        new Thread(new Runnable() {
            public void run()
            {
                try {
                    Thread.sleep(1000);
                    os.close();
                } catch (Exception e) {e.printStackTrace();}
            }
        }).start();

        long time = System.currentTimeMillis();
        final OutputStream os2 = this.binaryObj.addContent();
        Assert.assertTrue(System.currentTimeMillis() - time > 500);
        os2.close();
    }

    @Test
    public void saveTest() throws Exception
    {
        this.binaryObj.addContent(new ByteArrayInputStream(this.testContent.getBytes("US-ASCII")));

        // Test can't read newly written content until save.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        binaryObj.getContent(baos);
        Assert.assertEquals("", baos.toString());

        // Test content available for read after save is done.
        this.binaryObj.save();
        baos = new ByteArrayOutputStream();
        this.binaryObj.getContent(baos);
        Assert.assertEquals(this.testContent, baos.toString());
    }
}
