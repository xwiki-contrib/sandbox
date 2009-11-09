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
package org.xwoot.xwootUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Utility class for providing object persistence methods.
 * 
 * @version $Id$
 */
public final class PersistencyUtil
{
    /** Disable utility class instantiation. */
    private PersistencyUtil()
    {
        // void
    }

    /**
     * Serializes an object to file using the {@link ObjectOutputStream#writeObject(Object)} method.
     * 
     * @param object the object to save.
     * @param filePath the path of the file where to save the object.
     * @throws Exception if problems occur saving the object.
     * @see {@link #loadObjectFromFile(String)}
     */
    public static void saveObjectToFile(Object object, String filePath) throws Exception
    {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        
        FileUtil.checkDirectoryPath(new File(filePath).getParentFile());

        try {
            fout = new FileOutputStream(filePath);
            oos = new ObjectOutputStream(fout);

            oos.writeObject(object);
            oos.flush();
        } catch (Exception e) {
            throw new Exception("Problems while storing an object in the file: " + filePath, e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (Exception e) {
                throw new Exception("Problems closing the file  " + filePath + " after storing an object to it: ", e);
            }
        }
    }

    /**
     * Serializes a Collection to file using the {@link ObjectOutputStream#writeObject(Object)} method.
     * 
     * @param collection the collection to save.
     * @param filePath the path of the file where to save the collection.
     * @throws Exception if problems occur saving the object.
     * @see {@link #saveObjectToFile(Object, String)}
     * @see {@link #loadObjectFromFile(String)}
     */
    @SuppressWarnings("unchecked")
    public static void saveCollectionToFile(Collection collection, String filePath) throws Exception
    {
        if (collection == null) {
            throw new NullPointerException("Null value was provided instead of a valid collection.");
        }

        if (collection.isEmpty()) {
            File file = new File(filePath);

            if (file.exists()) {
                file.delete();
            }

            return;
        }

        saveObjectToFile(collection, filePath);
    }

    /**
     * Serializes a Map to file using the {@link ObjectOutputStream#writeObject(Object)} method.
     * 
     * @param map the map to save.
     * @param filePath the path of the file where to save the object.
     * @throws Exception if problems occur saving the object.
     * @throws NullPointerException if the map is null.
     * @see {@link #saveObjectToFile(Object, String)}
     * @see {@link #loadObjectFromFile(String)}
     */
    @SuppressWarnings("unchecked")
    public static void saveMapToFile(Map map, String filePath) throws Exception
    {
        if (map == null) {
            throw new NullPointerException("A null value was provided instead of a valid map.");
        }

        if (map.isEmpty()) {
            File file = new File(filePath);

            if (file.exists()) {
                file.delete();
            }

            return;
        }

        saveObjectToFile(map, filePath);
    }

    /**
     * Loads an object from file. The object must have been previously serialized using the
     * {@link ObjectOutputStream#writeObject(Object)} method.
     * 
     * @param filePath the file to load from.
     * @return the read object.
     * @throws Exception if the file was not found or problems reading the object occurred.
     * @throws NullPointerException if the provided filePath is null.
     * @see #saveObjectToFile(Object, String)
     */
    public static Object loadObjectFromFile(String filePath) throws Exception
    {
        if (filePath == null) {
            throw new NullPointerException("Null value provided as filePath.");
        }

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(filePath);
            ois = new ObjectInputStream(fis);

            return ois.readObject();
        } catch (Exception e) {
            throw new Exception("Problems while loading an object from the file: " + filePath, e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                throw new Exception("Problems closing the file " + filePath + " after loading an object from it: ", e);
            }
        }
    }

    /**
     * Convenience method.
     * <p>
     * If the specified file location does not exist, the provided fallback object will be returned. Null value is
     * allowed for fallback object, this being subject to the user's logic.
     * 
     * @param filePath the file to load from.
     * @param fallBackIfFileNotFound the object to return if the file is not found.
     * @return the read object or the fallback object if the file does not exist.
     * @throws Exception if problems reading the object occurred.
     * @throws NullPointerException if the filePath is null.
     * @see #saveObjectToFile(Object, String)
     * @see #loadObjectFromFile(String)
     */
    public static Object loadObjectFromFile(String filePath, Object fallBackIfFileNotFound) throws Exception
    {
        if (!new File(filePath).exists()) {
            return fallBackIfFileNotFound;
        }
        return loadObjectFromFile(filePath);
    }

    /**
     * Saves an object to an xml file.
     * 
     * @param object the object to save.
     * @param xmlFilePath the location of the xml file to save to.
     * @throws Exception if problems occur.
     */
    public static void saveObjectToXml(Object object, String xmlFilePath) throws Exception
    {
        XStream xstream = new XStream(new DomDriver());
        Charset fileEncodingCharset = Charset.forName("UTF-8");

        OutputStreamWriter osw = null;
        //PrintWriter output = null;

        try {
            osw = new OutputStreamWriter(new FileOutputStream(xmlFilePath), fileEncodingCharset);
            //output = new PrintWriter(osw);
            //output.print(xstream.toXML(object, osw));
            //output.flush();
            xstream.toXML(object, osw);
            osw.flush();
        } catch (Exception e) {
            throw new Exception("Problems saving an object to xml file " + xmlFilePath, e);
        } finally {
            try {
                if (osw != null) {
                    osw.close();
                }
                /*if (output != null) {
                    output.close();
                }*/
            } catch (Exception e) {
                throw new Exception("Problems closing the xml file " + xmlFilePath + " after saving an object in it.",
                    e);
            }
        }
    }

    /**
     * Saves a collection to an xml file.
     * 
     * @param collection the collection to save.
     * @param xmlFilePath the location of the xml file to save to.
     * @throws Exception if problems occur.
     * @see #saveObjectToXml(Object, String)
     */
    @SuppressWarnings("unchecked")
    public static void saveCollectionToXml(Collection collection, String xmlFilePath) throws Exception
    {
        if (collection == null) {
            throw new NullPointerException("A null value was provided instead of a valid collection.");
        }

        if (collection.isEmpty()) {
            File file = new File(xmlFilePath);

            if (file.exists()) {
                file.delete();
            }

            return;
        }

        saveObjectToXml(collection, xmlFilePath);
    }

    /**
     * Loads an object from an xml file. The object must have previously been serialized using the
     * {@link #saveObjectToXml(Object, String)} method.
     * 
     * @param xmlFilePath the xml file to load from.
     * @return the loaded object.
     * @throws Exception if problems occur.
     */
    public static Object loadObjectFromXml(String xmlFilePath) throws Exception
    {
        XStream xstream = new XStream(new DomDriver());

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(xmlFilePath);
            return xstream.fromXML(fis);
        } catch (Exception e) {
            throw new Exception("Problems loading object from file " + xmlFilePath, e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                throw new Exception("Problems closing the xml file:" + xmlFilePath, e);
            }
        }

    }

    /**
     * Convenience method.
     * <p>
     * If the specified file location does not exist, the provided fallback object will be returned. Null value is
     * allowed for fallback object, this being subject to the user's logic.
     * 
     * @param xmlFilePath the xml file to load from.
     * @param fallBackIfFileNotFound the object to return if the file is not found.
     * @return the read object or the fallback object if the file does not exist.
     * @throws Exception if problems reading the object occurred.
     * @throws NullPointerException if the filePath is null.
     * @see #saveObjectToXml(Object, String)
     * @see #loadObjectFromXml(String)
     */
    public static Object loadObjectFromXml(String xmlFilePath, Object fallBackIfFileNotFound) throws Exception
    {
        if (!new File(xmlFilePath).exists()) {
            return fallBackIfFileNotFound;
        }
        return loadObjectFromXml(xmlFilePath);
    }
}
