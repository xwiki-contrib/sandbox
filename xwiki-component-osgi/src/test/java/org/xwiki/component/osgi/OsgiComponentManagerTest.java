package org.xwiki.component.osgi;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class OsgiComponentManagerTest
{
    public interface Interface
    {
    }

    @Test
    public void testInitialization()
    {
        OsgiBootstrap bootstrap = new OsgiBootstrap();
        bootstrap.initialize(getClass().getClassLoader());

        BundleContext bundleContext = bootstrap.getBundleContext();
        bundleContext.registerService(Interface.class.getName(), new Interface() {}, null);
        ServiceReference ref = bundleContext.getServiceReference(Interface.class.getName());
        Interface impl = (Interface) bundleContext.getService(ref);

        for (Bundle bundle : bundleContext.getBundles()) {
            System.out.println(bundle.getBundleId() + " - " + bundle.getSymbolicName() + " - "
                + bundle.getLocation() + " - " + bundle.getVersion());
            for (ServiceReference sr : bundle.getRegisteredServices()) {
                System.out.println("reference: " + sr.toString());
                for (String key : sr.getPropertyKeys()) {
                    System.out.println("  key = " + key + " - value = " + sr.getProperty(key));
                }
            }
        }
    }
}
