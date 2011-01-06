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
package org.xwiki.store;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

/**
 * Tests for FileDeleteTransactionRunnable
 *
 * @version $Id$
 * @since TODO
 */
public class FileDeleteTransactionRunnableTest
{
    private static final String[] FILE_PATH = {"path", "to", "file"};

    private File storageLocation;

    private File toDelete;

    private File temp;

    private ReadWriteLock lock;

    private FileDeleteTransactionRunnable runnable;

    @Before
    public void setUp() throws Exception
    {
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        this.storageLocation = new File(tmpDir, "test-storage" + System.identityHashCode(this.getClass()));

        this.toDelete = this.storageLocation;
        for (int i = 0; i < FILE_PATH.length; i++) {
            this.toDelete = new File(this.toDelete, FILE_PATH[i]);
        }
        this.temp = new File(this.toDelete.getParentFile(), FILE_PATH[FILE_PATH.length - 1] + "~tmp");
        this.toDelete.getParentFile().mkdirs();
        IOUtils.write("Delete me!", new FileOutputStream(this.toDelete));

        this.lock = new ReentrantReadWriteLock();

        this.runnable = new FileDeleteTransactionRunnable(this.toDelete, this.temp, this.lock);
    }

    @After
    public void tearDown() throws Exception
    {
        resursiveDelete(this.storageLocation);
    }

    @Test
    public void simpleTest() throws Exception
    {
        Assert.assertTrue(this.toDelete.exists());
        this.runnable.start(new VoidTransaction());
        Assert.assertFalse(this.toDelete.exists());
    }

    private static void resursiveDelete(final File toDelete) throws Exception
    {
        if (toDelete.isDirectory()) {
            final File[] children = toDelete.listFiles();
            for (int i = 0; i < children.length; i++) {
                resursiveDelete(children[i]);
            }
        }
        toDelete.delete();
    }
}
