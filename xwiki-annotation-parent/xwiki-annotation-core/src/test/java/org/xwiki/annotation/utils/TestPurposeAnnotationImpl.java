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

package org.xwiki.annotation.utils;

import org.xwiki.annotation.internal.annotation.AnnotationImpl;
import org.xwiki.annotation.internal.maintainment.AnnotationState;

/**
 * @version $Id$
 */
public class TestPurposeAnnotationImpl extends AnnotationImpl
{
    public TestPurposeAnnotationImpl()
    {
        this("default page", "default author", "default date", AnnotationState.SAFE, "default annotation",
            "default initial selection", "default initial context", -1, -1, -1);
    }

    public TestPurposeAnnotationImpl(String document, String author, String date, AnnotationState state,
        String metadata, String selection, String context, int id, int offset, int length)
    {
        super(document, author, date, state, metadata, selection, context, id, offset, length);
    }
}
