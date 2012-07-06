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
package org.xwiki.batchimport;

import java.io.IOException;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

/**
 * Iterator through an import file. <br />
 * Note that this is designed to be a stateful component, one call to {@link #resetFile(BatchImportConfiguration)}
 * should "start" the read of a file and the subsequent {@link #readNextLine()} calls should give one by one the lines
 * of the file, until the end, when they would return null. <br />
 * It is recommended that the implementations of this interface have a perlookup instantiation strategy, as every
 * stateful component should. Normally this should use the factory pattern, but I want to make it as simple as possible
 * to implement in groovy, which is why I don't make it a factory. To extend this in groovy, you'd just need to provide
 * an iterator with a new hint, which can start reading a file in the {@link #resetFile(BatchImportConfiguration)} and
 * provides it line by line in {@link #readNextLine()} <br />
 * <b>NOT THREAD SAFE!</b>
 * 
 * @version $Id$
 */
@ComponentRole
public interface ImportFileIterator
{
    /**
     * Sets the file to be read by this iterator. If a file is already set, it should reset it. The file is supposed to
     * be set per instance, so if multiple iterators are needed at the same time multiple instances are to be created.
     * 
     * @param config the batch import configuration for which to create an import file iterator
     * @throws IOException if the file cannot be read
     */
    void resetFile(BatchImportConfiguration config) throws IOException;

    /**
     * @return the next line in the set file, if the file has no more lines, this method should return null. NOTE that
     *         this method can return rows of variable length (e.g. depending on whether all the cells are filled in an
     *         excel), caller must protect against this.
     * @throws IOException if the file cannot be read
     */
    public List<String> readNextLine() throws IOException;

    /**
     * Closes the iterator, and the underlying open files and all. It's automatically called on reset, but it's the user
     * responsibility to close it after no more lines are available.
     * 
     * @throws IOException if anything goes wrong while closing the files.
     */
    void close() throws IOException;
}
