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
package org.xwiki.annotation.renderer;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xwiki.annotation.TestDocumentFactory;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.TransformationManager;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Renderer tests for the XHTML annotations renderer, from the test files.
 * 
 * @version $Id$
 */
@RunWith(Parameterized.class)
public class AnnotationXHTMLRendererTest extends AbstractComponentTestCase
{
    /**
     * Document description files to run this test for.
     */
    private static Collection<String[]> files = new ArrayList<String[]>();

    /**
     * The annotations renderer hint.
     */
    private static final String ANNOTATIONS_RENDERER_HINT = "annotations-xhtml/1.0";

    /**
     * Mock document to run tests for.
     */
    protected String docName;

    static {
        // FIXME: checkstyle is so gonna shout when this will be longer than 30 files
        addFileToTest("renderer/Document1");
        addFileToTest("renderer/Document2");
        addFileToTest("renderer/Document3");
        addFileToTest("renderer/Document4");
        addFileToTest("renderer/Document5");
        addFileToTest("renderer/Document6");
        addFileToTest("renderer/Document7");
        addFileToTest("renderer/Document8");
        addFileToTest("renderer/Document9");
        addFileToTest("renderer/Document10");
        addFileToTest("renderer/Document11");
        addFileToTest("renderer/Document12");
        addFileToTest("renderer/Document13");
        addFileToTest("renderer/Document14");
        addFileToTest("renderer/Document15");
        addFileToTest("renderer/Document16");
        addFileToTest("renderer/Document17");
        addFileToTest("renderer/Document18");
        addFileToTest("renderer/Document19");
    }

    /**
     * Creates a test for the passed document. Will be instantiated by the parameterized runner for all the parameters.
     * 
     * @param docName the document (and corpus filename) to run tests for
     */
    public AnnotationXHTMLRendererTest(String docName)
    {
        this.docName = docName;
    }

    /**
     * Adds a file to the list of files to run tests for.
     * 
     * @param docName the name of the document / file to test
     */
    private static void addFileToTest(String docName)
    {
        files.add(new String[] {docName});
    }

    /**
     * @return list of corpus files to instantiate tests for
     */
    @Parameters
    public static Collection<String[]> data()
    {
        return files;
    }

    /**
     * Test rendering the annotations in the document description file results in the annotated html.
     * 
     * @throws Exception in case something goes wrong looking up components and rendering
     */
    @Test
    public void getAnnotatedHTML() throws Exception
    {
        Parser parser = getComponentManager().lookup(Parser.class, Syntax.XWIKI_2_0.toIdString());
        XDOM xdom = parser.parse(new StringReader(TestDocumentFactory.getDocument(docName).getSource()));

        // run transformations
        TransformationManager transformationManager = getComponentManager().lookup(TransformationManager.class);
        transformationManager.performTransformations(xdom, Syntax.XWIKI_2_0);

        AnnotationPrintRenderer renderer =
            getComponentManager().lookup(AnnotationPrintRenderer.class, ANNOTATIONS_RENDERER_HINT);
        WikiPrinter printer = new DefaultWikiPrinter();
        renderer.setPrinter(printer);
        // set the annotations for this renderer
        renderer.setAnnotations(TestDocumentFactory.getDocument(docName).getSafeAnnotations());

        xdom.traverse(renderer);

        assertEquals(TestDocumentFactory.getDocument(docName).getAnnotatedContent(), printer.toString());
    }

    /**
     * Test rendering with the annotations renderer but without annotations doesn't alter the content.
     * 
     * @throws Exception in case something goes wrong looking up components and rendering
     */
    @Test
    public void getAnnotatedHTMLWithoutAnnotations() throws Exception
    {
        Parser parser = getComponentManager().lookup(Parser.class, Syntax.XWIKI_2_0.toIdString());
        XDOM xdom = parser.parse(new StringReader(TestDocumentFactory.getDocument(docName).getSource()));

        // run transformations
        TransformationManager transformationManager = getComponentManager().lookup(TransformationManager.class);
        transformationManager.performTransformations(xdom, Syntax.XWIKI_2_0);

        AnnotationPrintRenderer renderer =
            getComponentManager().lookup(AnnotationPrintRenderer.class, ANNOTATIONS_RENDERER_HINT);
        WikiPrinter printer = new DefaultWikiPrinter();
        renderer.setPrinter(printer);

        xdom.traverse(renderer);

        assertEquals(TestDocumentFactory.getDocument(docName).getRenderedContent(), printer.toString());
    }
}