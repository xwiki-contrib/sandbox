package org.xwiki.component.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.xwiki.component.embed.AbstractComponentManager;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentLookupException;

import java.util.List;
import java.util.Map;

public class OsgiComponentManager extends AbstractComponentManager
{
    private BundleContext bundleContext;

    public OsgiComponentManager(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    @Override
    protected <T> T getComponent(RoleHint<T> roleHint) throws ComponentLookupException
    {
        String filter = "(hint=" + roleHint.getHint() + ")";
        ServiceReference[] references;
        try {
             references = bundleContext.getServiceReferences(roleHint.getRole().getName(), filter);
        } catch (InvalidSyntaxException e) {
            // This shouldn't happen since we control the passed filter syntax.
            throw new ComponentLookupException("Invalid OSGi Filter syntax [" + filter + "]", e);
        }

        // If no components are found return null.


        // Assume we can have only one component registered with a given Role + Hint.
        //this.bundleContext.getService(references

        return null;
    }

    @Override protected <T> boolean hasComponent(RoleHint<T> roleHint)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override protected <T> void registerComponent(RoleHint<T> roleHint, Object instance)
    {
        // TODO: Handle hint
        this.bundleContext.registerService(roleHint.getRole().getName(), instance, null);

        // TODO: send event about component reg
    }

    @Override protected <T> void removeComponent(RoleHint<T> roleHint)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override protected Map<RoleHint<?>, Object> getComponents()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
