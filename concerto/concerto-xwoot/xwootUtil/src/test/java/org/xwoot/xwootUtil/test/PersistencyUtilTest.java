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
package org.xwoot.xwootUtil.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.xwoot.xwootUtil.PersistencyUtil;

/**
 * Tests for the PersistencyUtil class.
 * 
 * @version $Id$
 */
public final class PersistencyUtilTest extends AbstractXwootUtilTestBase
{
    /** Name of the file where to save objects in bynary form. */
    private String fileNameToSaveObjectTo = "savedObject";

    /** Name of the file where to save objects in xml form. */
    private String xmlFileNameToSaveObjectTo = "savedObject.xml";

    /** Test object data. */
    private String testObjectData = "TestObjectData";

    /** Test object. */
    private Object testObject;

    /** Test Collection. */
    private Collection<Object> testCollection;

    /** Test Map. */
    private Map<Object, Object> testMap;

    /**
     * Test the saveObjectToFile, saveCollectionToFile, saveMapToFile and loadObjectFromFile methods.
     * <p>
     * Result: After saving an object, the loaded object must be equal to the original one.
     * 
     * @throws Exception if problems occur.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSaveLoadObjectCollectionMapToFromFile() throws Exception
    {
        String filePath = new File(this.workingDir, this.fileNameToSaveObjectTo).getPath();

        // create test object and save to file.
        this.testObject = new String(this.testObjectData);
        PersistencyUtil.saveObjectToFile(this.testObject, filePath);

        // load object and check with original.
        Object loadedObject = PersistencyUtil.loadObjectFromFile(filePath);
        Assert.assertEquals(this.testObject, loadedObject);

        // create collection with items and save to file.
        this.testCollection = new ArrayList<Object>();
        this.testCollection.add(this.testObject);
        this.testCollection.add(this.testObject);
        this.testCollection.add(this.testObject);
        PersistencyUtil.saveCollectionToFile(this.testCollection, filePath);

        // load object and check with original.
        Object loadedCollection = PersistencyUtil.loadObjectFromFile(filePath);
        Assert.assertEquals(this.testCollection, loadedCollection);

        // assure that the previously created file exists.
        Assert.assertTrue(new File(filePath).exists());

        // save an empty collection.
        this.testCollection.clear();
        PersistencyUtil.saveCollectionToFile(this.testCollection, filePath);

        // the file does no longer exist.
        Assert.assertFalse(new File(filePath).exists());

        // an attempt to load from an non existing file
        Exception fileDoesNotExist = null;
        try {
            loadedCollection = PersistencyUtil.loadObjectFromFile(filePath);
        } catch (Exception e) {
            fileDoesNotExist = e;
        }
        Assert.assertNotNull(fileDoesNotExist);

        // same attempt, but by using a fallback object for file not found cases.
        loadedCollection = PersistencyUtil.loadObjectFromFile(filePath, new ArrayList<Object>());
        Assert.assertTrue(((Collection<Object>) loadedCollection).isEmpty());
    }

    /**
     * Test the saveMapToFile and loadObjectFromFile methods.
     * <p>
     * Result: After saving an object, the loaded object must be equal to the original one.
     * 
     * @throws Exception if problems occur.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSaveLoadMapToFromFile() throws Exception
    {
        String filePath = new File(this.workingDir, this.fileNameToSaveObjectTo).getPath();

        // create test object and save to file.
        this.testObject = new String(this.testObjectData);

        // create map with items and save to file.
        this.testMap = new HashMap<Object, Object>();
        this.testMap.put(this.testObject, this.testObject);
        this.testMap.put(this.testObject, this.testObject);
        this.testMap.put(this.testObject, this.testObject);
        PersistencyUtil.saveMapToFile(this.testMap, filePath);

        // load object and check with original.
        Object loadedMap = PersistencyUtil.loadObjectFromFile(filePath);
        Assert.assertEquals(this.testMap, loadedMap);

        // assure that the previously created file exists.
        Assert.assertTrue(new File(filePath).exists());

        // save an empty map.
        this.testMap.clear();
        PersistencyUtil.saveMapToFile(this.testMap, filePath);

        // the file does no longer exist.
        Assert.assertFalse(new File(filePath).exists());

        // an attempt to load from an non existing file
        Exception fileDoesNotExist = null;
        try {
            loadedMap = PersistencyUtil.loadObjectFromFile(filePath);
        } catch (Exception e) {
            fileDoesNotExist = e;
        }
        Assert.assertNotNull(fileDoesNotExist);

        // same attempt, but by using a fallback object for file not found cases.
        loadedMap = PersistencyUtil.loadObjectFromFile(filePath, new HashMap<Object, Object>());
        Assert.assertTrue(((Map<Object, Object>) loadedMap).isEmpty());
    }

    /**
     * Test the saveObjectToXml, saveCollectionToXml, saveMapToXml and loadObjectFromFile methods.
     * <p>
     * Result: After saving an object, the loaded object must be equal to the original one.
     * 
     * @throws Exception if problems occur.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSaveLoadObjectCollectionToFromXml() throws Exception
    {
        String filePath = new File(this.workingDir, this.xmlFileNameToSaveObjectTo).getPath();

        // create test object and save to file.
        this.testObject = new String(this.testObjectData);
        PersistencyUtil.saveObjectToXml(this.testObject, filePath);

        // load object and check with original.
        Object loadedObject = PersistencyUtil.loadObjectFromXml(filePath);
        Assert.assertEquals(this.testObject, loadedObject);

        // create collection with items and save to file.
        this.testCollection = new ArrayList<Object>();
        this.testCollection.add(this.testObject);
        this.testCollection.add(this.testObject);
        this.testCollection.add(this.testObject);
        PersistencyUtil.saveCollectionToXml(this.testCollection, filePath);

        // load object and check with original.
        Object loadedCollection = PersistencyUtil.loadObjectFromXml(filePath);
        Assert.assertEquals(this.testCollection, loadedCollection);

        // assure that the previously created file exists.
        Assert.assertTrue(new File(filePath).exists());

        // save an empty collection.
        this.testCollection.clear();
        PersistencyUtil.saveCollectionToXml(this.testCollection, filePath);

        // the file does no longer exist.
        Assert.assertFalse(new File(filePath).exists());

        // an attempt to load from an non existing file
        Exception fileDoesNotExist = null;
        try {
            loadedCollection = PersistencyUtil.loadObjectFromXml(filePath);
        } catch (Exception e) {
            fileDoesNotExist = e;
        }
        Assert.assertNotNull(fileDoesNotExist);

        // same attempt, but by using a fallback object for file not found cases.
        loadedCollection = PersistencyUtil.loadObjectFromXml(filePath, new ArrayList<Object>());
        Assert.assertTrue(((Collection<Object>) loadedCollection).isEmpty());
    }
    
    /**
     * @throws Exception x
     * 
     */
    @Test
    public void testSaveSetAsCollection() throws Exception
    {
        String filePath = new File(this.workingDir, this.xmlFileNameToSaveObjectTo).getPath();
        
        Set<Object> test = new HashSet<Object>();
        String object = "TEST";
        test.add(object);
        PersistencyUtil.saveCollectionToFile(test, filePath);
        
        Set<Object> result = (HashSet<Object>) PersistencyUtil.loadObjectFromFile(filePath, new HashSet<Object>());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains(object));
    }
}
