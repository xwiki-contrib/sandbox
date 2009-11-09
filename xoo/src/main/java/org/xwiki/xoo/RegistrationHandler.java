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

package org.xwiki.xoo;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Global service registration class.
 * 
 * @version $Id: $
 * @since 1.0 M
 */

public class RegistrationHandler
{

    /**
     * Constructor.
     */
    private RegistrationHandler()
    {

    }

    /**
     * Global service registration method returning a component factory from an implementation name The implementation
     * class will be searched among the list of known services implementations of the component.
     * 
     * @param sImplementationName the implementation name of which to get the component factory
     * @return the component factory
     */
    public static XSingleComponentFactory __getComponentFactory(String sImplementationName)
    {
        String regClassesList = getRegistrationClasses();
        StringTokenizer t = new StringTokenizer(regClassesList, " ");
        while (t.hasMoreTokens()) {
            String className = t.nextToken();
            if (className != null && className.length() != 0) {
                try {
                    Class regClass = Class.forName(className);
                    Method writeRegInfo =
                        regClass.getDeclaredMethod("__getComponentFactory", new Class[] {String.class});
                    Object result = writeRegInfo.invoke(regClass, sImplementationName);
                    if (result != null) {
                        return (XSingleComponentFactory) result;
                    }
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (ClassCastException ex) {
                    ex.printStackTrace();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Global service registration method calling the same method on each of the known implementations.
     * 
     * @param xRegistryKey the registry key where to write the infos
     * @return true if the services informations have been successfully written to the registry, false otherwise.
     */
    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey)
    {
        boolean bResult = true;
        String regClassesList = getRegistrationClasses();
        StringTokenizer t = new StringTokenizer(regClassesList, " ");
        while (t.hasMoreTokens()) {
            String className = t.nextToken();
            if (className != null && className.length() != 0) {
                try {
                    Class regClass = Class.forName(className);
                    Method writeRegInfo =
                        regClass.getDeclaredMethod("__writeRegistryServiceInfo", new Class[] {XRegistryKey.class});
                    Object result = writeRegInfo.invoke(regClass, xRegistryKey);
                    bResult &= ((Boolean) result).booleanValue();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (ClassCastException ex) {
                    ex.printStackTrace();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return bResult;
    }

    /**
     * @return the classes which really are service implementation classes
     */
    private static String getRegistrationClasses()
    {
        RegistrationHandler c = new RegistrationHandler();
        String name = c.getClass().getCanonicalName().replace('.', '/').concat(".class");
        try {
            Enumeration<URL> urlEnum = c.getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (urlEnum.hasMoreElements()) {
                URL url = urlEnum.nextElement();
                // String file = url.getFile();
                JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                Manifest mf = jarConnection.getManifest();

                Attributes attrs = (Attributes) mf.getAttributes(name);
                if (attrs != null) {
                    String classes = attrs.getValue("RegistrationClasses");
                    return classes;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return "";
    }

}
