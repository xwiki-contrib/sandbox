/************************************************************************
 *
 * $Id$
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache
 * Foundation.
 **********************************************************************/

package org.xwoot.jxta.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;

public final class PeerClassLoader extends ClassLoader
{

    private String[] jarPaths = null;

    public PeerClassLoader(String[] pathToJars)
    {
        jarPaths = pathToJars;
    }

    public Class loadClass(String className, boolean resolve) throws ClassNotFoundException
    {

        Class cls = (Class) findLoadedClass(className);
        if (cls != null) {
            return cls;
        }

        // we must *not* use super.loadClass() otherwise we will end
        // using the same classloader for all instances, which would
        // defeat the whole purpose of writing this classloader

        // we would like to load the system classes with our custom
        // classloader as well (to prevent problems with shared
        // properties, for example), but we are forbidden from loading
        // anything in java.lang.*, so beware that system classes will
        // be shared by all peers
        if (className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("org.omg.")
            || className.startsWith("org.w3c.dom.") || className.startsWith("org.xml.sax.")
            || className.startsWith("sun.") ||
            // this is a workaround for a bug in log4j where it loads
            // its configuration using the system class loader, so
            // here we load the entire log4j system using the system
            // class loader as well. this also means that log4j
            // classes will be shared, so beware
            className.startsWith("org.apache.log4j")) {

            try {
                cls = findSystemClass(className);
            } catch (Throwable ignore) {
            }
            if (cls != null) {
                return cls;
            }
        }

        cls = findClass(className);

        if (resolve) {
            resolveClass(cls);
        }

        return cls;
    }

    protected Class findClass(String className) throws ClassNotFoundException
    {

        byte[] bits = loadClassData(className);
        if (bits == null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < jarPaths.length; i++) {
                sb.append(jarPaths[i]);
                if (i + 1 < jarPaths.length) {
                    sb.append(File.pathSeparator);
                }
            }
            throw new ClassNotFoundException("Class " + className + " not found in " + sb.toString());
        }

        return defineClass(className, bits, 0, bits.length);
    }

    private byte[] loadClassData(String className)
    {
        // do *not* use File.separatorChar here instead of '/'
        className = className.replace('.', '/') + ".class";

        //System.out.println("Trying to load " + className);

        File classFile = null;

        JarFile jar = null;
        JarEntry jarEntry = null;

        for (int i = 0; i < jarPaths.length; i++) {
            boolean isJar = false;
            try {
                jar = new JarFile(jarPaths[i]);
                isJar = true;
            } catch (IOException ex) {
                //continue;
            }

            if (isJar) {
                Enumeration jarEntries = jar.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) jarEntries.nextElement();
                    if (className.equals(entry.getName())) {
                        jarEntry = entry;
                        break;
                    }
                }

                if (jarEntry != null) {
                    break;
                }
            } else {
                classFile = new File(jarPaths[i], className);
                //System.out.println("Trying class file: " + classFile);
                if (classFile.exists()) {
                    //System.out.println("Found!");
                    break;
                } else {
                    classFile = null;
                }
            }
        }

        byte[] bits = null;
        long classSize = 0;

        if (jarEntry != null) {
            //System.out.println("Reading jar entry.");
            
            classSize = jarEntry.getSize();
            bits = new byte[(int) classSize];

            InputStream is = null;
            try {
                is = new BufferedInputStream(jar.getInputStream(jarEntry));
                is.read(bits, 0, bits.length);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
                return null;
            } finally {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
                try {
                    jar.close();
                } catch (IOException ignore) {
                }
            }

        } else if (classFile != null) {
            //System.out.println("Reading class entry.");
            
            classSize = classFile.length();
            bits = new byte[(int) classSize];

            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(classFile));
                is.read(bits, 0, bits.length);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
                return null;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ignore) {
                }
            }
        } else {
            //System.out.println("Not found.");
            return null;
        }

        return bits;
    }

    public static void main(String[] args)
    {
        if (args.length < 2) {
            System.out.println("Usage: PeerClassLoader <jar> <class>");
            return;
        }

        String[] path = new String[1];
        path[0] = args[0];
        PeerClassLoader loader = new PeerClassLoader(path);
        Class cls = null;
        try {
            cls = loader.findClass(args[1]);
            System.out.println(cls);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
