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

package org.xwiki.annotation.internal.target;

import org.xwiki.annotation.AnnotationTarget;
import org.xwiki.annotation.TargetResolver;
import org.xwiki.annotation.AnnotationService.Target;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

/**
 * Default implementation for {@link TargetResolver}.
 * 
 * @version $Id$
 */
@Component()
public class DefaultTargetResolver implements TargetResolver
{
    @Requirement("documentContent")
    private static AnnotationTarget documentContent;

    @Requirement("feedEntry")
    private static AnnotationTarget feedEntry;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.TargetResolver#resolve(java.lang.String)
     */
    public AnnotationTarget resolve(Target target)
    {
        for (Map it : Map.values()) {
            if (it.accept(target)) {
                return it.getAnnotationTarget();
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * @version $Id$
     */
    private enum Map
    {
        feedEntry {
            @Override
            public AnnotationTarget getAnnotationTarget()
            {
                return DefaultTargetResolver.feedEntry;
            }

            @Override
            public boolean accept(Target target)
            {
                return target.equals(Target.feedEntry);
            }
        },
        documentContent {
            @Override
            public AnnotationTarget getAnnotationTarget()
            {
                return DefaultTargetResolver.documentContent;
            }

            @Override
            public boolean accept(Target target)
            {
                return target.equals(Target.documentContent);
            }
        };

        /**
         * @return the annotation target
         */
        protected abstract AnnotationTarget getAnnotationTarget();

        /**
         * @return {@true true} if this resolver accepts the passed target, {@code false} otherwise
         */
        protected abstract boolean accept(Target target);
    }
}
