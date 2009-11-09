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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.xwiki.annotation.AnnotationTarget;
import org.xwiki.annotation.ContentAlterer;
import org.xwiki.annotation.IOService;
import org.xwiki.annotation.IOTargetService;
import org.xwiki.annotation.SelectionService;
import org.xwiki.annotation.SourceAlterer;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.context.SourceImpl;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.annotation.internal.exception.IOServiceException;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
public abstract class AbstractAnnotationTarget implements AnnotationTarget
{
    /**
     * @return input/output service associated to the annotation target
     */
    protected abstract IOService getIOService();

    /**
     * @return selection service associated to the annotation target
     */
    protected abstract SelectionService getSelectionService();

    /**
     * @return return IO target service associated to the annotation target
     */
    protected abstract IOTargetService getIOTargetService();

    /**
     * @return source alterer associated to the annotation target
     */
    protected abstract SourceAlterer getSourceAlterer();

    /**
     * @return content alterer associated to the annotation target
     */
    protected abstract ContentAlterer getContentAlterer();

    /**
     * @param anoID is ID of annotation (as attributed by the DB)
     * @return mark to inject in xwiki source before rendering
     */
    static String getAnnotationBeginTag(int anoID)
    {
        return "@@@annotation@@@" + anoID + "@@@";
    }

    /**
     * @param anoID is ID of annotation (as attributed by the DB)
     * @return mark to inject in xwiki source before rendering
     */
    static String getAnnotationEndTag(int anoID)
    {
        return "@@@" + anoID + "@@@annotation@@@";
    }

    /**
     * @param documentName name of the document concerned
     * @return annotated HTML content of given document.
     * @throws AnnotationServiceException can be thrown if selection resolution fail or if an XWikiException occurred
     */
    public CharSequence getAnnotatedHTML(CharSequence documentName, XWikiContext context)
        throws AnnotationServiceException
    {
        try {
            Source source = getIOTargetService().getSource(documentName, context);
            Collection<Annotation> annotations = getIOService().getSafeAnnotations(documentName, context);

            if (annotations.isEmpty()) {
                return getIOTargetService().getRenderedContent(documentName, source, context);
            }

            StringBuilder wikiSource =
                new StringBuilder(getIOTargetService().getSource(documentName, context).getSource());
            Map<Integer, Integer> offsets = new TreeMap<Integer, Integer>();

            for (Annotation it : annotations) {
                // Determination of offset induced by others annotations
                int startOffset = 0;
                int endOffset = 0;
                for (Entry<Integer, Integer> couple : offsets.entrySet()) {
                    if (couple.getKey() <= it.getOffset()) {
                        startOffset += couple.getValue();
                    }
                    if (couple.getKey() <= it.getOffset() + it.getLength()) {
                        endOffset += couple.getValue();
                    }
                }

                // Updating map of offsets
                Integer value = offsets.get(it.getOffset());
                if (value == null) {
                    offsets.put(it.getOffset(), getAnnotationBeginTag(it.getId()).length());
                } else {
                    offsets.put(it.getOffset(), value + getAnnotationBeginTag(it.getId()).length());
                }
                value = offsets.get(it.getOffset() + it.getLength());
                if (value == null) {
                    offsets.put(it.getOffset() + it.getLength(), getAnnotationEndTag(it.getId()).length());
                } else {
                    offsets.put(it.getOffset() + it.getLength(), value + getAnnotationEndTag(it.getId()).length());
                }

                // Insertion
                wikiSource.insert(it.getOffset() + startOffset, getAnnotationBeginTag(it.getId()));
                wikiSource.insert(it.getOffset() + (getAnnotationBeginTag(it.getId()).length()) + (it.getLength())
                    + endOffset, getAnnotationEndTag(it.getId()));

            }

            Source annotatedSource = new SourceImpl(wikiSource);
            // Rendering
            String htmlContent =
                getIOTargetService().getRenderedContent(documentName, annotatedSource, context).toString();
            int fromIndex;
            int toIndex;
            String oldSelection;
            String newSelection;
            for (Annotation it : annotations) {
                fromIndex = htmlContent.indexOf(getAnnotationBeginTag(it.getId()));
                toIndex = htmlContent.indexOf(getAnnotationEndTag(it.getId()));
                if (fromIndex < 0 || toIndex < 0) {
                    continue;
                }
                oldSelection = htmlContent.substring(fromIndex + (getAnnotationBeginTag(it.getId()).length()), toIndex);
                newSelection =
                    oldSelection.replaceAll("<(/)?([^>]+?)>", "</span><$1$2><span class=\"annotation ID" + it.getId()
                        + "\" title=\"" + it.getAnnotation() + "\">");
                htmlContent =
                    htmlContent.replace(htmlContent.subSequence(fromIndex, toIndex
                        + getAnnotationEndTag(it.getId()).length()), "<span class=\"annotation ID" + it.getId()
                        + "\" title=\"" + it.getAnnotation() + "\">" + newSelection + "</span>");
            }
            return htmlContent;
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }
}
