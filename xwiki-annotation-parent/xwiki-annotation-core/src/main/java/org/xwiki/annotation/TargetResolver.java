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

package org.xwiki.annotation;

import org.xwiki.annotation.AnnotationService.Target;
import org.xwiki.component.annotation.ComponentRole;

/**
 * This service is responsible for returning an AnnotationTarget associated to a Target. <br />
 * FIXME: this and the {@link Target} enum shouldn't exist, lookup should be done using components, so that any target
 * is easily pluggable.
 * 
 * @version $Id$
 */
@ComponentRole
public interface TargetResolver
{
    /**
     * @param target kind of document
     * @return AnnotationTarget instance
     */
    AnnotationTarget resolve(Target target);
}
