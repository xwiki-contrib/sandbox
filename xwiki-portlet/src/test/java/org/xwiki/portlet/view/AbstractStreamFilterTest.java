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
package org.xwiki.portlet.view;

import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.Assert;

import org.jmock.Mockery;

/**
 * Base class for all {@link StreamFilter} unit tests.
 * 
 * @version $Id$
 */
public abstract class AbstractStreamFilterTest
{
    /**
     * The object used to create mocks.
     */
    protected Mockery mockery = new Mockery();

    /**
     * The filter being tested.
     */
    protected StreamFilter filter;

    /**
     * Calls the filter with the given input and asserts the result.
     * 
     * @param input the string to be filtered
     * @param expectedOutput the expected output
     */
    protected void assertFilterOutput(String input, String expectedOutput)
    {
        StringWriter writer = new StringWriter();
        filter.filter(new StringReader(input), writer);
        Assert.assertEquals(expectedOutput, writer.toString());
    }
}
