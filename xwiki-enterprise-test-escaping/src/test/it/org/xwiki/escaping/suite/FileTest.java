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

package org.xwiki.escaping.suite;

import java.io.Reader;

import org.junit.Test;


/**
 * Defines a file test that can be run by the {@link ArchiveSuite}.
 * <p>
 * {@link ArchiveSuite} reads files from an archive and generates a {@link FileTest} for each of them.
 * The implementations can decide whether the given file can be tested and how it should be tested.</p>
 * <p>
 * All test implementations should have one default constructor. The lifetime of a {@link FileTest} is
 * guaranteed to be as follows:
 * <ul>
 * <li>An instance of the file test class is created.</li>
 * <li>The method {@link #initialize(String, Reader)} is called.</li>
 * <li>The stream associated with the {@link Reader} that was used to initialize the test is closed.</li>
 * <li>All methods marked with the &#064;{@link Test} annotation are called.</li>
 * <li>All references to the file test instance are cleared immediately thereafter.</li>
 * </ul></p>
 * 
 * @version $Id$
 * @since 2.5
 */
public interface FileTest
{
    /**
     * Initialize the test. If this method returns false, the test is not run at all (is not counted
     * as a success or failure).
     * 
     * @param name
     * @param reader
     * @return true if the test was initialized successfully and should be executed, false otherwise
     */
    boolean initialize(String name, Reader reader);
}

