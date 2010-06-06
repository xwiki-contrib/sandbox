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
package org.xwiki.component.wiki;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.wiki.internal.DefaultMethodOutputHandler;
import org.xwiki.context.Execution;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.Transformation;

/**
 * Method invocation handler for wiki component proxy instances. Has a reference on a map of name/body wiki code of
 * supported methods.
 * 
 * @since 2.4-M2
 * @version $Id$
 */
public class WikiComponentInvocationHandler implements InvocationHandler
{
    /**
     * The key under which the component reference (a virtual "this") is kept in the method invocation context. 
     */
    private static final String METHOD_CONTEXT_COMPONENT_KEY = "component";

    /**
     * The key under which the output is kept in the method invocation context.
     */
    private static final String METHOD_CONTEXT_OUTPUT_KEY = "output";

    /**
     * The key under which the context document is kept in the XWiki context.
     */
    private static final String XWIKI_CONTEXT_DOC_KEY = "doc";

    /**
     * Pre-loaded hasCode Method.
     * 
     * @see {@link Object#hashCode()}
     */
    private static Method hashCodeMethod;

    /**
     * Pre-loaded equals method.
     * 
     * @see {@link Object#equals(Object)}
     */
    private static Method equalsMethod;

    /**
     * Pre-loaded toString method.
     * 
     * @see {@link Object#toString()}
     */
    private static Method toStringMethod;

    static {
        try {
            hashCodeMethod = Object.class.getMethod("hashCode", null);
            equalsMethod = Object.class.getMethod("equals", new Class[] {Object.class});
            toStringMethod = Object.class.getMethod("toString", null);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    /**
     * Our component manager.
     */
    private ComponentManager componentManager;

    /**
     * The proxied wiki component.
     */
    private WikiComponent wikiComponent;
    
    /**
     * Constructor of this invocation handler.
     * 
     * @param componentReference reference to the document holding the component definition
     * @param methods the map of methods handled by the component instance
     * @param componentManager the component manager
     */
    public WikiComponentInvocationHandler(WikiComponent wikiComponent,
        ComponentManager componentManager)
    {
        this.wikiComponent = wikiComponent;
        this.componentManager = componentManager;
    }

    /**
     * {@inheritDoc}
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (!this.wikiComponent.getHandledMethods().containsKey(method.getName())) {
            if (method.getDeclaringClass() == Object.class) {
                return this.proxyObjectMethod(proxy, method, args);
            } else {
                throw new NoSuchMethodException();
            }
        } else {
            return this.executeWikiContent(proxy, method);
        }

    }

    /**
     * "Executes" the wiki content associated to the passed method.
     * 
     * @param method the method to execute
     * @return the result of the execution
     * @throws Exception when an error occurs during execution
     */
    @SuppressWarnings("unchecked")
    private Object executeWikiContent(Object proxy, Method method) throws Exception
    {
        Map xwikiContext = null;
        Object contextDoc = null;

        XDOM xdom = this.wikiComponent.getHandledMethods().get(method.getName());

        Execution execution = componentManager.lookup(Execution.class);
        Transformation macroTransformation = componentManager.lookup(Transformation.class, "macro");
        DocumentAccessBridge docBridge = componentManager.lookup(DocumentAccessBridge.class);

        Map<String, Object> methodContext = new HashMap<String, Object>();
        methodContext.put(METHOD_CONTEXT_OUTPUT_KEY, new DefaultMethodOutputHandler());
        methodContext.put(METHOD_CONTEXT_COMPONENT_KEY, proxy);

        // Place macro context inside xwiki context ($context.macro).
        xwikiContext = (Map) execution.getContext().getProperty("xwikicontext");
        xwikiContext.put("method", methodContext);
        // Save current context document.
        contextDoc = xwikiContext.get(XWIKI_CONTEXT_DOC_KEY);
        // Make sure has prog rights
        xwikiContext.put(XWIKI_CONTEXT_DOC_KEY, docBridge.getDocument(this.wikiComponent.getDocumentReference()));

        // Perform internal macro transformations.
        macroTransformation.transform(xdom, Syntax.XWIKI_2_0);

        if (methodContext.get(METHOD_CONTEXT_OUTPUT_KEY) != null
            && ((MethodOutputHandler) methodContext.get(METHOD_CONTEXT_OUTPUT_KEY)).getReturnValue() != null) {
            return method.getReturnType().cast(((MethodOutputHandler)
                methodContext.get(METHOD_CONTEXT_OUTPUT_KEY)).getReturnValue());
        } else if (method.getReturnType().equals(String.class)) {
            // If return type is String and no specific return value has been provided during the macro
            // expansion, then we return the content redered as
            WikiPrinter printer = new DefaultWikiPrinter();
            BlockRenderer renderer = componentManager.lookup(BlockRenderer.class, Syntax.PLAIN_1_0.toIdString());
            renderer.render(xdom, printer);
            return printer.toString();
        } else {
            // surrender
            return null;
        }
    }

    /**
     * Proxies a method of the {@link Object} class.
     * 
     * @param proxy the proxy instance
     * @param method the method to proxy
     * @param args possible arguments to the method invocation
     * @return the result of the proxied call
     */
    private Object proxyObjectMethod(Object proxy, Method method, Object[] args)
    {
        if (method.equals(hashCodeMethod)) {
            return proxyHashCode(proxy);
        } else if (method.equals(equalsMethod)) {
            return proxyEquals(proxy, args[0]);
        } else if (method.equals(toStringMethod)) {
            return proxyToString(proxy);
        } else {
            throw new InternalError("unexpected Object method dispatched: " + method);
        }
    }

    /**
     * Default behavior for {@link Object#hashCode()} when not overridden in the wiki component definition.
     * 
     * @param proxy the proxy object
     * @return a hash code for the proxy object, as if using standard {Object{@link #hashCode()}.
     */
    protected Integer proxyHashCode(Object proxy)
    {
        return new Integer(System.identityHashCode(proxy));
    }

    /**
     * Default behavior for {@link Object#equals(Object)} when not overridden in the wiki component definition.
     * 
     * @param proxy the proxy object
     * @param other the other object of the comparison
     * @return the result of the equality comparison between the passed proxy and other object
     */
    protected Boolean proxyEquals(Object proxy, Object other)
    {
        return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Default behavior for {@link Object#toString()} when not overridden in the wiki component definition.
     * 
     * @param proxy the proxy object
     * @return the String representation of the passed proxy object
     */
    protected String proxyToString(Object proxy)
    {
        return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
    }

}
