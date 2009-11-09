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
package org.xwiki.eclipse.core.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * A class containing utility methods.
 */
public class CoreUtils
{
    private static XStream xstream = null;

    /**
     * Create a folder and all its parents.
     * 
     * @param folder The folder to be created.
     * @return The created folder.
     * @throws CoreException
     */
    public static IFolder createFolder(IFolder folder) throws CoreException
    {
        if (folder.exists()) {
            return folder;
        }

        IProject project = folder.getProject();
        String[] segments = folder.getProjectRelativePath().segments();

        IPath current = new Path("."); //$NON-NLS-1$
        for (String segment : segments) {
            current = current.append(segment);

            IFolder currentFolder = project.getFolder(current);
            if (!currentFolder.exists()) {
                currentFolder.create(true, true, null);
            }
        }

        Assert.isTrue(folder.exists());

        return folder;
    }

    /**
     * Write an XML serialization of the given object to a file. Overwrites the previous file content is the file
     * already exists.
     * 
     * @param file The file where the serialization should be written to.
     * @param data
     * @return
     * @throws CoreException
     */
    public static IFile writeDataToXML(IFile file, Object data) throws CoreException
    {
        XStream xstream = getXStream();

        if (file.getParent() instanceof IFolder) {
            IFolder parentFolder = (IFolder) file.getParent();
            createFolder(parentFolder);
        }

        InputStream is = new ByteArrayInputStream(xstream.toXML(data).getBytes());
        if (!file.exists()) {
            file.create(is, true, null);
        } else {
            file.setContents(is, true, false, null);
        }

        try {
            is.close();
        } catch (IOException e) {
            // Ignore
        }

        return file;
    }

    /**
     * Read the XML serialization from a file.
     * 
     * @param file
     * @return The de-serialized object (client should type-cast to the actual type).
     * @throws CoreException
     */
    public static Object readDataFromXML(IFile file) throws CoreException
    {
        XStream xstream = getXStream();

        file.refreshLocal(1, null);
        return xstream.fromXML(file.getContents());
    }

    private static XStream getXStream()
    {
        if (xstream == null) {
            xstream = new XStream(new DomDriver());
        }

        return xstream;
    }

    /**
     * Convert and return byte-count to human readable format.
     * 
     * @param size Integer byte count.
     * @return The Human readable size format value
     */
    public static String prettySize(int size)
    {
        if (size < 1024)
            return size + " bytes";
        if (size < 1048576)
            return size / 1024 + " KB";
        return size / 1048576 + " MB";
    }

    public static byte[] getBytesFromFile(File file) throws IOException
    {
        InputStream is = new FileInputStream(file);

        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            throw new IOException("File too large " + file.getName());
        }

        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }
}
