package org.xwiki.component.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;

import java.util.List;
import java.util.Map;

public class OsgiComponentManager implements ComponentManager
{
    private BundleContext bundleContext;

    public OsgiComponentManager(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    /**
     * {@inheritDoc}
     * @see {@link ComponentManager#lookup(Class, String)}
     */
    public <T> T lookup(Class<T> role, String roleHint) throws ComponentLookupException
    {
        String filter = "(hint=" + roleHint + ")";
        ServiceReference[] references;
        try {
             references = bundleContext.getServiceReferences(role.getName(), filter);
        } catch (InvalidSyntaxException e) {
            // This shouldn't happen since we control the passed filter syntax.
            throw new ComponentLookupException("Invalid OSGi Filter syntax [" + filter + "]", e);
        }

        // If no components are found return null.


        // Assume we can have only one component registered with a given Role + Hint.
        //this.bundleContext.getService(references

        return null;
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor(Class<T> role, String roleHint)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> role)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ComponentEventManager getComponentEventManager()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ComponentManager getParent()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> boolean hasComponent(Class<T> role)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> boolean hasComponent(Class<T> role, String roleHint)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> T lookup(Class<T> role) throws ComponentLookupException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> List<T> lookupList(Class<T> role) throws ComponentLookupException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException
    {
        // Create instance
        
        registerComponent(componentDescriptor, null);
    }

    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
        throws ComponentRepositoryException
    {
        this.bundleContext.registerService(componentDescriptor.getRole().getName(), componentInstance, null);

        // TODO: send event about component reg
    }

    public <T> void release(T component) throws ComponentLifecycleException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setComponentEventManager(ComponentEventManager eventManager)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setParent(ComponentManager parentComponentManager)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unregisterComponent(Class<?> role, String roleHint)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
