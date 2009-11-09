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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.maintainment.AnnotationState;
import org.xwiki.annotation.utils.TestPurposeAnnotationImpl;

/**
 * Factory to create test documents from corpus files.
 * 
 * @version $Id$
 */
public final class TestDocumentFactory
{
    /**
     * Loaded documents map.
     */
    private static Map<String, MockDocument> docs = new HashMap<String, MockDocument>();

    /**
     * Private constructor for utility classes.
     */
    private TestDocumentFactory()
    {

    }

    /**
     * Resets the loaded documents map, to call for clean up at each test setup.
     */
    public static void reset()
    {
        docs.clear();
    }

    /**
     * @param docName name of test document to get, as loaded from the corpus file with the same name. Note that a
     *            document will be loaded and cached until the {@link #reset()} method is called.
     * @return the test document loaded from the corpus file
     * @throws IOException if something goes wrong parsing the document file
     */
    public static MockDocument getDocument(String docName) throws IOException
    {
        MockDocument loadedDoc = docs.get(docName);
        if (loadedDoc == null) {
            loadedDoc = loadDocument(docName);
            docs.put(docName, loadedDoc);
        }
        return loadedDoc;
    }

    /**
     * Helper method to load a document from the corpus file with the same name.
     * 
     * @param docName the name of the document to load from the corpus file
     * @return a {@link MockDocument} instance for the test document loaded from the corpus file
     * @throws IOException if something goes wrong parsing the file
     */
    private static MockDocument loadDocument(String docName) throws IOException
    {
        // FIXME: this is pretty dirty, but it should work
        MockDocument doc = new MockDocument();
        doc.set("annotations", new ArrayList<Annotation>());
        // get the file
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(TestDocumentFactory.class.getResourceAsStream("/" + docName)));
        // read line by line and
        String line = null;
        String currentKey = null;
        StringBuffer currentValue = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                // comment, ignore
                continue;
            }
            if (line.startsWith(".")) {
                // it's a key, parse its value
                // if there exists a previous key, put its value in the document
                if (currentKey != null) {
                    saveKeyToDoc(currentKey, currentValue.toString(), doc, docName);
                    currentValue.delete(0, currentValue.length());
                }
                currentKey = line.substring(1);
            } else {
                if (currentValue.length() > 0) {
                    currentValue.append("\n");
                }
                currentValue.append(line);
            }
        }
        // process last key + value as well
        saveKeyToDoc(currentKey, currentValue.toString(), doc, docName);
        return doc;
    }

    /**
     * Helper function to save a parsed key in the configuration file to the mock document.
     * 
     * @param currentKey the read key
     * @param currentValue the value for the read key
     * @param doc the mock document read from corpus
     * @param docName the name of the document where the annotation is contained
     * @throws IOException if there is any problem reading the annotation representation
     */
    private static void saveKeyToDoc(String currentKey, String currentValue, MockDocument doc, String docName)
        throws IOException
    {
        if (currentKey.equals("annotation")) {
            // parse the annotation value
            Annotation ann = parseAnnotation(currentValue, docName);
            doc.getSafeAnnotations().add(ann);
        } else {
            doc.set(currentKey, currentValue.toString());
        }
    }

    /**
     * Parses an annotation from its string representation, as read from the corpus file.
     * 
     * @param annotation the string representation of the annotation, as in the corpus file
     * @param docName the name of the document where the annotation is created
     * @return an {@link Annotation} object corresponding to the data in the {@code annotation} string
     * @throws IOException if there is any problem reading the annotation representation
     */
    private static Annotation parseAnnotation(String annotation, String docName) throws IOException
    {
        BufferedReader stringReader = new BufferedReader(new StringReader(annotation));
        // FIXME: pretty dirty to parse by lines
        String line = null;
        String[] properties = new String[7];
        int propIndex = 0;
        while ((line = stringReader.readLine()) != null) {
            properties[propIndex] = line;
            propIndex++;
        }
        try {
            return new TestPurposeAnnotationImpl(docName, properties[1], null, AnnotationState.SAFE, properties[2],
                properties[3], properties[4], Integer.parseInt(properties[0]), Integer.parseInt(properties[5]), Integer
                    .parseInt(properties[6]));
        } catch (NumberFormatException e) {
            // if something goes wrong parsing the string properties read for an annotation
            throw new IOException("Could not parse an annotation from the passed reader");
        }
    }
}
