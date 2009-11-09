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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jmock.util.NotImplementedException;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.maintainment.AnnotationState;
import org.xwiki.annotation.utils.TestPurposeAnnotationImpl;

/**
 * @version $Id$
 */
public enum Documents
{
    DocumentO1 {
        @Override
        public CharSequence getMixContent() throws IOException
        {
            throw new NotImplementedException();
        }

        @Override
        public CharSequence getRenderedContent() throws IOException
        {
            return "<h1 id=\"HThreeLawsofRobotics\"><span>Three Laws of Robotics</span></h1><ul><li>"
                + "A robot may not injure a human being or, through inaction, allow a human being to come to harm."
                + "</li><li>A robot must obey any orders given to it by human beings, except where such orders "
                + "would conflict with the First Law.</li><li>A robot must protect its own existence as long as "
                + "such protection does not conflict with the First or Second Law.</li></ul>";
        }

        @Override
        public Collection<Annotation> getSafeAnnotations()
        {
            return Collections.<Annotation> emptySet();
        }

        @Override
        public CharSequence getSource() throws IOException
        {
            return "= Three Laws of Robotics =\n\n"
                + "* A robot may not injure a human being or, through inaction, allow a human being to come to harm.\n"
                + "* A robot must obey any orders given to it by human beings, except where such orders would "
                + "conflict with the First Law.\n"
                + "* A robot must protect its own existence as long as such protection does not conflict with "
                + "the First or Second Law.";
        }

        @Override
        public CharSequence getTaggedContent() throws IOException
        {
            throw new NotImplementedException();
        }

        @Override
        public CharSequence getExpectedAnnotatedContent() throws IOException
        {
            return getRenderedContent();
        }
    },
    LePrinceChapitre15 {
        @Override
        public CharSequence getRenderedContent() throws IOException
        {
            StringBuffer sb = new StringBuffer();
            BufferedReader br =
                new BufferedReader(new FileReader("src/test/resources/corpus/LePrince.Chapitre15.rendered"));
            String line = br.readLine();
            while (null != line) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            return sb.toString();
        }

        @Override
        public CharSequence getSource() throws IOException
        {
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new FileReader("src/test/resources/corpus/LePrince.Chapitre15"));
            String line = br.readLine();
            while (null != line) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            return sb.toString();
        }

        /**
         * {@inheritDoc}
         * 
         * @throws IOException
         * @see org.xwiki.annotation.Documents#getTaggedContent()
         */
        @Override
        public CharSequence getTaggedContent() throws IOException
        {
            StringBuffer sb = new StringBuffer();
            BufferedReader br =
                new BufferedReader(new FileReader("src/test/resources/corpus/LePrince.Chapitre15.tagged"));
            String line = br.readLine();
            while (null != line) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            return sb.toString();
        }

        @Override
        public CharSequence getMixContent() throws IOException
        {
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new FileReader("src/test/resources/corpus/LePrince.Chapitre15.mix"));
            String line = br.readLine();
            while (null != line) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            return sb.toString();
        }

        @Override
        public Collection<Annotation> getSafeAnnotations()
        {
            List<Annotation> result = new ArrayList<Annotation>();
            result.add(new TestPurposeAnnotationImpl(this.name(), "XWiki.Scribo", null, AnnotationState.SAFE,
                "Metadata #1", "un prince doit en user et se conduire", "un prince doit en user et se conduire", -1,
                148, "un prince doit en user et se conduire".length()));

            result.add(new TestPurposeAnnotationImpl(this.name(), "XWiki.Scribo", null, AnnotationState.SAFE,
                "Metadata #2",
                "Des choses pour lesquelles tous les hommes, et surtout les princes, sont loués ou blâmés",
                "Des choses pour lesquelles tous les hommes, et surtout les princes, sont loués ou blâmés", -1, 28,
                "Des choses pour lesquelles tous les hommes, et surtout les princes, sont loués ou blâmés".length()));

            result.add(new TestPurposeAnnotationImpl(this.name(), "XWiki.Scribo", null, AnnotationState.SAFE,
                "Metadata #3", "’un prince qui veut se maintenir apprenne à",
                "’un prince qui veut se maintenir apprenne à", -1, 1039, "’un prince qui veut se maintenir apprenne à"
                    .length()));
            return result;
        }

        @Override
        public CharSequence getExpectedAnnotatedContent() throws IOException
        {
            StringBuffer sb = new StringBuffer();
            BufferedReader br =
                new BufferedReader(new FileReader("src/test/resources/corpus/LePrince.Chapitre15.annotated"));
            String line = br.readLine();
            while (null != line) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            return sb.toString();
        }
    };

    public abstract CharSequence getRenderedContent() throws IOException;

    public abstract CharSequence getTaggedContent() throws IOException;

    public abstract CharSequence getMixContent() throws IOException;

    public abstract CharSequence getExpectedAnnotatedContent() throws IOException;

    public abstract Collection<Annotation> getSafeAnnotations();

    public abstract CharSequence getSource() throws IOException;
}
