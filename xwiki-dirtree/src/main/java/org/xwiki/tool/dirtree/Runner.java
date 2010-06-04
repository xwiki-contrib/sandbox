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
package org.xwiki.tool.dirtree;

import java.lang.Character;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import sun.reflect.ReflectionFactory;

/**
 * Runs through a directory tree and generates a set of java objects from the files contained inside.
 * Basically it's a deserialization framework.
 * Files named the same as a field in the object will be used to create the object in that field.
 * files called "this" will be used to create a prototype of the object which can then have fields altered by other
 * files in the same directorty.
 * Directories named "super" will apply to the same object but as it's super class.
 *
 * @version $Id$
 */
public class Runner
{
    private final ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

    private final Map<Class, Parser> parsers = new HashMap<Class, Parser>(){{
        put(Integer.class, new Runner.StringParser(){
            public Object parse(String input) {
                return Integer.parseInt(input);
            }
        });
        put(Long.class, new Runner.StringParser(){
            public Object parse(String input) {
                return Long.parseLong(input);
            }
        });
        put(Float.class, new Runner.StringParser(){
            public Object parse(String input) {
                return Float.parseFloat(input);
            }
        });
        put(Double.class, new Runner.StringParser(){
            public Object parse(String input) {
                return Double.parseDouble(input);
            }
        });
        put(String.class, new Runner.StringParser(){
            public Object parse(String input) {
                return input;
            }
        });
    }};

    public <T> T run(Class<T> objClass, File dir) throws Exception
    {
        Constructor<T> con = reflectionFactory.newConstructorForSerialization(objClass, Object.class.getDeclaredConstructor());
        DummyObj<T> dummy = this.new DummyObj<T>(con.newInstance());
        run(DummyObj.class, dummy, dir, DummyObj.class.getDeclaredFields()[0]);
        return dummy.getObject();
    }

    /**
     * @param objClass the class of object.
     * @param object an object in which said field is contained.
     * @param file a file which contains information to fill this field.
     * @param field a field in the given object which should be filled by the file.
     */
    private void run(Class objClass, Object object, File file, Field field) throws Exception
    {
        if (!file.isDirectory()) {
            //System.out.println("parsing " + file.getName() + "  and setting to field: " + field.getName() + " in " + objClass.getName());
            Parser parser = getParsers().get(field.getType());
            if (parser != null) {
                field.setAccessible(true);
                field.set(object, parser.parse(file));
            } else {
                warn("file " + file.getName() + " of type: " + field.getType() + " has no parsers for it.");
            }
        } else {
            // It's a directory.

            // If thisFile is not null, then "this" object is crafted from thisFile file.
            File thisFile = null;

            // Get files in directory and their names.
            File[] subFiles = file.listFiles();
            String[] fileNames = new String[subFiles.length];
            for (int i = 0; i < subFiles.length; i++) {
                fileNames[i] = getJavaValidPart(subFiles[i].getName());
                // Test for reserved keyword this which influences the building of the object.
                if (fileNames[i].equals("this") && !subFiles[i].isDirectory()) {
                    thisFile = subFiles[i];
                }
            }

            Object subObj = createPrototypeObject(object, field, thisFile);

            //System.out.println("created object of type" + subObj.getClass().getName());////////////////////////////Debug
            //System.out.println("recursing on " + file.getName());//////////////////////////////////////////////////Debug

            // get the fields and their names.
            Field[] fieldsInSubObj = subObj.getClass().getDeclaredFields();
            String[] fieldNames = new String[fieldsInSubObj.length];
            for (int i = 0; i < fieldsInSubObj.length; i++) {
                fieldNames[i] = fieldsInSubObj[i].getName();
            }

            //-------------------------------------------------------------------------------------
            // Recurse, specifying fields to match to each file for increased speed.
            //-------------------------------------------------------------------------------------

            // Store the class of the subObject to improve speed.
            Class subObjClass = subObj.getClass();

            // Use a trick to not test the same element twice, when a match is found, overwrite the match with
            // the element at lastIndex and decrease lastIndex by 1.
            int lastIndex = fieldsInSubObj.length - 1;

            for (int i = 0; i < subFiles.length && lastIndex > -1; i++) {
                // If the file name is "super", we do something special, recursing on this object (as it's superclass)
                // instead of drilling down on it's fields.
                if (fileNames[i].equals("super")) {
                    Class superClass = objClass.getSuperclass();
                    run(superClass, superClass.cast(object), subFiles[i], field);
                } else {
                    for (int j = 0; j <= lastIndex; j++) {
                        //System.out.println("comparing " + fileNames[i] + " to " + fieldNames[j]);//////////////////Debug
                        if (fileNames[i].equals(fieldNames[j])) {
                            run(subObjClass, subObj, subFiles[i], fieldsInSubObj[j]);
                            // Switch field so that they aren't compared twice.
                            fieldsInSubObj[j] = fieldsInSubObj[lastIndex];
                            fieldNames[j] = fieldNames[lastIndex];
                            lastIndex--;
                            // Go on to next file.
                            break;
                        }//if
                    }//for
                }//else
            }//for
        }//if is directory
    }//function

    /**
     * The end of a name is allowed to be invalid java and will be truncated.
     * 'content.txt' will be placed in a field called 'content'
     */
    private String getJavaValidPart(String filename)
    {
        char[] chars = filename.toCharArray();
        if (!Character.isJavaIdentifierStart(chars[0])) {
            warn("Filename (" + filename + ") with no valid java characters.");
            return "";
        }
        int i = 1;
        while (i < chars.length && Character.isJavaIdentifierPart(chars[i])) {
            i++;
        }
        return filename.substring(0, i);
    }

    /**
     * Try to create a new object and put it in the field.
     * First tries to create the object from the file createFrom, if that file is null,
     * Then tries to instanciate the object from any available default constructor.
     * If there is an an exception then it creates the object using the reflection framework.
     *
     * @param parentObject the object to put the new object in.
     * @param toCreate must be a fiels in parentObject, this is the field where the new object is created.
     * @param createFrom if not null, the object will be created from this file.
     */
    private Object createPrototypeObject(Object parentObject, Field toCreate, File createFrom) throws Exception
    {
        // Get the object represented by toCreate.
        toCreate.setAccessible(true);
        Object out = toCreate.get(parentObject);
        Class outObjClass;
        if (out != null) {
            // Use this method if possible because class might be generic/casted and field type may be wrong.
            outObjClass = out.getClass();
        } else {
            outObjClass = toCreate.getType();
        }
        if (createFrom != null) {
            // Create "this" object from the file.
            Parser parser = getParsers().get(outObjClass);
            if (parser != null) {
                out = parser.parse(createFrom);
                toCreate.set(parentObject, out);
                return out;
            } else {
                warn("file " + createFrom.getName() + " of type: " + outObjClass.getName() + " has no parsers for it.");
            }
        }
        // If no object, instanciate one.
        if (out == null) {
            Constructor con;
            try {
                con = outObjClass.getDeclaredConstructor();
            } catch (Exception e) {
                // No parameterless constructor? We'll see about that!
                con = reflectionFactory.newConstructorForSerialization(outObjClass, Object.class.getDeclaredConstructor());
            }
            out = con.newInstance();
            toCreate.set(parentObject, out);
        }
        return out;
    }

    private void debug(String debug)
    {
        System.out.println(debug);
    }

    private void warn(String warning)
    {
        System.out.println("warning: " + warning);
    }

    protected Map<Class, Parser> getParsers()
    {
        return parsers;
    }

    public void addParser(Class toParse, Parser parser)
    {
        parsers.put(toParse, parser);
    }

    public interface Parser
    {
        public Object parse(File file) throws Exception;
    }

    public static abstract class StringParser implements Parser
    {
        private final String lineBreak = System.getProperty("line.separator");

        public abstract Object parse(String input) throws Exception;

        public Object parse(File file) throws Exception {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(file));
                StringBuilder content = new StringBuilder();
                String line = null;
                while ((line = in.readLine()) != null){
                  content.append(line).append(lineBreak);
                }
                // remove the last \n
                content.deleteCharAt(content.length() - 1);
                return parse(content.toString());
            } finally {
                in.close();
            }
        }
    }

    private class DummyObj<T>
    {
        private T object;

        public DummyObj(T object)
        {
            this.object = object;
        }

        public T getObject()
        {
            return object;
        }
    }
}
