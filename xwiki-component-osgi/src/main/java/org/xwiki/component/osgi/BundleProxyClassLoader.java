package org.xwiki.component.osgi;

import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class BundleProxyClassLoader extends ClassLoader
{
    private Bundle bundle;

    public BundleProxyClassLoader(Bundle bundle)
    {
        this.bundle = bundle;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException
    {
        return bundle.getResources(name);
    }

    public Class findClass(String name) throws ClassNotFoundException
    {
        return bundle.loadClass(name);
    }

    public URL getResource(String name)
    {
        return bundle.getResource(name);
    }

    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        Class clazz = findClass(name);
        if (resolve) {
            super.resolveClass(clazz);
        }

        return clazz;
    }
}