package org.xwiki.component.osgi;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.net.URL;
import java.util.Collections;

public class OsgiComponentManagerTest
{
    private Mockery mockery = new Mockery();

    private ModuleRepository mockLocalModuleRepository;

    public interface Interface
    {
    }

    @Before
    public void setUp()
    {
        this.mockLocalModuleRepository = this.mockery.mock(ModuleRepository.class);
        this.mockery.checking(new Expectations() {{
            oneOf(mockLocalModuleRepository).getModuleURLs(); will(returnValue(Collections.<URL>emptyList()));
        }});
    }

    @Test
    public void testInitialization()
    {
        OsgiBootstrap bootstrap = new OsgiBootstrap(this.mockLocalModuleRepository);
        bootstrap.initialize();

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
