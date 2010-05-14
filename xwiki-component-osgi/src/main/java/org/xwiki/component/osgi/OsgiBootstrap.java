package org.xwiki.component.osgi;

import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OsgiBootstrap implements BundleActivator
{
    protected BundleContext bundleContext;

    /**
     * Start the OSGi system, load all component annotations and register them as components against the OSGi
     * runtime.
     *
     * @param classLoader the class loader to use to look for component definitions
     */
    public void initialize(ClassLoader classLoader)
    {
        // Step 1: Start the OSGi Framework
        Map configMap = new HashMap();
        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Collections.singletonList(this));

        try
        {
            Framework framework = getFrameworkFactory().newFramework(configMap);
            framework.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to initialize OSGi framework", e);
        }

        // Step 2: Start XWiki Modules


        // Step 3: For each XWiki Module, look for component annotations and register components accordingly
        /*
        ComponentAnnotationLoader loader = new ComponentAnnotationLoader();
        loader.enableLogging(new CommonsLoggingLogger(loader.getClass()));
        loader.initialize(this, classLoader);
        */
    }

    public void start(BundleContext bundleContext) throws Exception
    {
        this.bundleContext = bundleContext;
    }

    public void stop(BundleContext bundleContext) throws Exception
    {
        this.bundleContext = null;
    }

    public BundleContext getBundleContext()
    {
        return this.bundleContext;
    }

    // When we move to JDK 1.6 we'll be able to use the ServiceLoader API.
    private FrameworkFactory getFrameworkFactory() throws Exception
       {
           URL url = OsgiBootstrap.class.getClassLoader().getResource(
               "META-INF/services/org.osgi.framework.launch.FrameworkFactory");
           if (url != null)
           {
               BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
               try
               {
                   for (String s = br.readLine(); s != null; s = br.readLine())
                   {
                       s = s.trim();
                       // Try to load first non-empty, non-commented line.
                       if ((s.length() > 0) && (s.charAt(0) != '#'))
                       {
                           return (FrameworkFactory) Class.forName(s).newInstance();
                       }
                   }
               }
               finally
               {
                   if (br != null) br.close();
               }
           }

           throw new Exception("Could not find framework factory.");
       }
}
