package org.xwiki.component.embed;

import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.internal.Composable;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.logging.CommonsLoggingLogger;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.LogEnabled;
import org.xwiki.component.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractComponentManager implements ComponentManager
{
    private ComponentEventManager eventManager;

    private Map<RoleHint< ? >, ComponentDescriptor< ? >> descriptors =
        new HashMap<RoleHint< ? >, ComponentDescriptor< ? >>();

    private ComponentManager parent;

    // Delegate storage of components to classes extending this one.
    protected abstract <T> boolean hasComponent(RoleHint<T> roleHint);
    protected abstract <T> T getComponent(RoleHint<T> roleHint) throws ComponentLookupException;
    protected abstract <T> void registerComponent(RoleHint<T> roleHint, Object instance);
    protected abstract <T> void removeComponent(RoleHint<T> roleHint);

    // TODO: remove the need for this method
    protected abstract Map<RoleHint<?>, Object> getComponents();

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#hasComponent(Class, String)
     */
    public <T> boolean hasComponent(Class<T> role, String roleHint)
    {
        return hasComponent(new RoleHint<T>(role, roleHint));
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#hasComponent(Class)
     */
    public <T> boolean hasComponent(Class<T> role)
    {
        return hasComponent(new RoleHint<T>(role));
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#lookup(Class)
     */
    public <T> T lookup(Class<T> role) throws ComponentLookupException
    {
        return initialize(new RoleHint<T>(role));
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#lookup(Class, String)
     */
    public <T> T lookup(Class<T> role, String roleHint) throws ComponentLookupException
    {
        return initialize(new RoleHint<T>(role, roleHint));
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#lookupList(Class)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> lookupList(Class<T> role) throws ComponentLookupException
    {
        List<T> objects = new ArrayList<T>();
        synchronized (this) {
            for (RoleHint< ? > roleHint : this.descriptors.keySet()) {
                // It's possible Class reference are not the same when it's coming form different ClassLoader so we
                // compare class names
                if (roleHint.getRole().getName().equals(role.getName())) {
                    objects.add(initialize((RoleHint<T>) roleHint));
                }
            }
            // Add parent's list of components
            if (getParent() != null) {
                objects.addAll(getParent().lookupList(role));
            }
        }
        return objects;
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#lookupMap(Class)
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException
    {
        Map<String, T> objects = new HashMap<String, T>();
        synchronized (this) {
            for (RoleHint< ? > roleHint : this.descriptors.keySet()) {
                // It's possible Class reference are not the same when it coming for different ClassLoader so we
                // compare class names
                if (roleHint.getRole().getName().equals(role.getName())) {
                    objects.put(roleHint.getHint(), initialize((RoleHint<T>) roleHint));
                }
            }
            // Add parent's list of components
            if (getParent() != null) {
                // If the hint already exists in the children Component Manager then don't add the one from the parent.
                for (Map.Entry<String, T> entry : getParent().lookupMap(role).entrySet()) {
                    if (!objects.containsKey(entry.getKey())) {
                        objects.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return objects;
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#registerComponent(org.xwiki.component.descriptor.ComponentDescriptor)
     */
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException
    {
        registerComponent(componentDescriptor, null);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.component.manager.ComponentManager#registerComponent(org.xwiki.component.descriptor.ComponentDescriptor,
     *      java.lang.Object)
     */
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
    {
        synchronized (this) {
            RoleHint<T> roleHint = new RoleHint<T>(componentDescriptor.getRole(), componentDescriptor.getRoleHint());

            this.descriptors.put(roleHint, componentDescriptor);

            if (componentInstance != null) {
                // Set initial instance of the component
                registerComponent(roleHint, componentInstance);
            } else {
                // Remove any existing instance since we're replacing it
                removeComponent(roleHint);
            }
        }

        // Send event about component registration
        if (this.eventManager != null) {
            this.eventManager.notifyComponentRegistered(componentDescriptor);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.component.manager.ComponentManager#unregisterComponent(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public void unregisterComponent(Class< ? > role, String roleHint)
    {
        ComponentDescriptor< ? > descriptor;

        synchronized (this) {
            RoleHint< ? > roleHintKey = new RoleHint(role, roleHint);

            descriptor = this.descriptors.get(roleHintKey);

            if (descriptor != null) {
                this.descriptors.remove(roleHintKey);
                removeComponent(roleHintKey);
            }
        }

        // Send event about component unregistration
        if (descriptor != null && this.eventManager != null) {
            this.eventManager.notifyComponentUnregistered(descriptor);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#getComponentDescriptor(Class, String)
     */
    @SuppressWarnings("unchecked")
    public <T> ComponentDescriptor<T> getComponentDescriptor(Class<T> role, String roleHint)
    {
        synchronized (this) {
            return (ComponentDescriptor<T>) this.descriptors.get(new RoleHint<T>(role, roleHint));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#getComponentDescriptorList(Class)
     */
    @SuppressWarnings("unchecked")
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> role)
    {
        synchronized (this) {
            List<ComponentDescriptor<T>> results = new ArrayList<ComponentDescriptor<T>>();
            for (Map.Entry<RoleHint< ? >, ComponentDescriptor< ? >> entry : this.descriptors.entrySet()) {
                // It's possible Class reference are not the same when it coming for different ClassLoader so we
                // compare class names
                if (entry.getKey().getRole().getName().equals(role.getName())) {
                    results.add((ComponentDescriptor<T>) entry.getValue());
                }
            }
            return results;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#release(Object)
     */
    public <T> void release(T component) throws ComponentLifecycleException
    {
        synchronized (this) {
            RoleHint< ? > key = null;
            for (Map.Entry<RoleHint< ? >, Object> entry : getComponents().entrySet()) {
                if (entry.getValue() == component) {
                    key = entry.getKey();
                    break;
                }
            }
            // Note that we're not removing inside the for loop above since it would cause a Concurrent
            // exception since we'd modify the map accessed by the iterator.
            if (key != null) {
                removeComponent(key);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#getComponentEventManager()
     */
    public ComponentEventManager getComponentEventManager()
    {
        return this.eventManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#setComponentEventManager(ComponentEventManager)
     */
    public void setComponentEventManager(ComponentEventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#getParent()
     */
    public ComponentManager getParent()
    {
        return this.parent;
    }

    /**
     * {@inheritDoc}
     *
     * @see ComponentManager#setParent(ComponentManager)
     */
    public void setParent(ComponentManager parentComponentManager)
    {
        this.parent = parentComponentManager;
    }

    @SuppressWarnings("unchecked")
    private <T> T initialize(RoleHint<T> roleHint) throws ComponentLookupException
    {
        T instance;
        synchronized (this) {
            // If the instance exists return it
            instance = getComponent(roleHint);
            if (instance == null) {
                // If there's a component descriptor, create the instance
                ComponentDescriptor<T> descriptor = (ComponentDescriptor<T>) this.descriptors.get(roleHint);
                if (descriptor != null) {
                    try {
                        instance = createInstance(descriptor);
                        if (instance == null) {
                            throw new ComponentLookupException("Failed to lookup component [" + roleHint + "]");
                        } else if (this.descriptors.get(roleHint).getInstantiationStrategy() == ComponentInstantiationStrategy.SINGLETON) {
                            registerComponent(roleHint, instance);
                        }
                    } catch (Exception e) {
                        throw new ComponentLookupException("Failed to lookup component [" + roleHint + "]", e);
                    }
                } else {
                    // Look for the component in the parent Component Manager (if there's a parent)
                    ComponentManager parent = getParent();
                    if (parent != null) {
                        instance = getParent().lookup(roleHint.getRole(), roleHint.getHint());
                    } else {
                        throw new ComponentLookupException("Can't find descriptor for the component [" + roleHint + "]");
                    }
                }
            }
        }

        return instance;
    }

    private <T> T createInstance(ComponentDescriptor<T> descriptor) throws Exception
    {
        T instance = descriptor.getImplementation().newInstance();

        // Set each dependency
        for (ComponentDependency< ? > dependency : descriptor.getComponentDependencies()) {

            // TODO: Handle dependency cycles

            // Handle different field types
            Object fieldValue;
            if ((dependency.getMappingType() != null) && List.class.isAssignableFrom(dependency.getMappingType())) {
                fieldValue = lookupList(dependency.getRole());
            } else if ((dependency.getMappingType() != null) && Map.class.isAssignableFrom(dependency.getMappingType())) {
                fieldValue = lookupMap(dependency.getRole());
            } else {
                fieldValue = lookup(dependency.getRole(), dependency.getRoleHint());
            }

            // Set the field by introspection
            if (fieldValue != null) {
                ReflectionUtils.setFieldValue(instance, dependency.getName(), fieldValue);
            }
        }

        // Call Lifecycle

        // LogEnabled
        if (LogEnabled.class.isAssignableFrom(descriptor.getImplementation())) {
            ((LogEnabled) instance).enableLogging(new CommonsLoggingLogger(instance.getClass()));
        }

        // Composable
        // Only support Composable for classes implementing ComponentManager since for all other components
        // they should have ComponentManager injected.
        if (ComponentManager.class.isAssignableFrom(descriptor.getImplementation())
            && Composable.class.isAssignableFrom(descriptor.getImplementation())) {
            ((Composable) instance).compose(this);
        }

        // Initializable
        if (Initializable.class.isAssignableFrom(descriptor.getImplementation())) {
            ((Initializable) instance).initialize();
        }

        return instance;
    }
}
