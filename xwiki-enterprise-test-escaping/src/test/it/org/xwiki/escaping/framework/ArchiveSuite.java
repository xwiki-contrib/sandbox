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
package org.xwiki.escaping.framework;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;


/**
 * JUnit4 test suite that generates tests based on files found in a zip/war/xar/jar archive.
 * <p>
 * The path to the archive must be specified using &#064;{@link ArchivePath} or
 * &#064;{@link ArchivePathGetter}.</p>
 * 
 * @version $Id$
 * @since 2.5
 */
public class ArchiveSuite extends ParentRunner<Runner>
{
    /**
     * Path to the archive to use. Is overridden by &#064;{@link ArchivePathGetter}.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface ArchivePath
    {
        public String value();
    }

    /**
     * Marks the method that should be used to retrieve the path to the archive to use. Overrides
     * &#064;{@link ArchivePath}.
     * <p>
     * The getter method should be a public static method returning String and not taking any arguments.
     * Only one method should have this annotation. Example:
     * <pre>
     *     &#064;ArchivePathMethod
     *     public static String getPath() {
     *         ...
     *     }
     * </pre>
     * </p>
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface ArchivePathGetter
    {
        // no attributes
    }


    /** Path to the archive to use. */
    private final String archivePath;


    /**
     * Create new ArchiveSuite
     * 
     * @param klass
     * @param builder
     * @throws InitializationError
     */
    public ArchiveSuite(Class< ? > klass, RunnerBuilder builder) throws InitializationError
    {
        super(klass);
        this.archivePath = getArchivePathFromAnnotation();
        System.out.println("\nArchiveSuite(class: " + klass.getCanonicalName() + ", builder: " + builder.toString() + ")");
        System.out.println("  Archive path: " + archivePath);
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runners.ParentRunner#getChildren()
     */
    @Override
    protected List<Runner> getChildren()
    {
        System.out.println("ArchiveSuite.getChildren()");
        // TODO Auto-generated method stub
        return new LinkedList<Runner>();
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(Runner child)
    {
        System.out.println("ArchiveSuite.describeChild(child: " + child + ")");
        return Description.createSuiteDescription(getClass());
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(Runner child, RunNotifier notifier)
    {
        System.out.println("ArchiveSuite.runChild(child: " + child + ", notifier: " + notifier + ")");
        // TODO Auto-generated method stub
        
    }



    /**
     * Retrieve the path to the archive form annotations. Throws an exception if no annotations can
     * be found, when the annotation is used incorrectly or the path is invalid.
     * 
     * @return path to the archive
     * @throws InitializationError when an error occurs
     */
    private String getArchivePathFromAnnotation() throws InitializationError
    {
        String path = null;

        // try class annotation first
        ArchivePath classAnnotation = getTestClass().getJavaClass().getAnnotation(ArchivePath.class);
        if (classAnnotation != null) {
            path = classAnnotation.value();
        }

        // override by getter method, if present
        List<FrameworkMethod> getters = getTestClass().getAnnotatedMethods(ArchivePathGetter.class);
        if (getters.size() > 1) {
            throw new InitializationError("Only one method should be annotated with @ArchivePathGetter. "
                + "The test case \"" + getTestClass().getName() + "\" has " + getters.size() + " annotated methods.");
        }
        if (classAnnotation == null && getters.size() == 0) {
            throw new InitializationError("No archive path annotations found. The test case \""
                + getTestClass().getName() + "\" should be annotated with @ArchivePath or @ArchivePathGetter");
        }
        if (getters.size() == 1) {
            path = invokeGetter(getters.get(0).getMethod());
        }

        // validate the path
        if (path == null) {
            throw new InitializationError("Archive path is null.");
        }
        return path;
    }

    /**
     * Check that the archive getter method has the expected type and invoke it.
     * 
     * @param getter the getter method to use
     * @return the resulting archive path
     * @throws InitializationError on errors
     */
    private String invokeGetter(Method getter) throws InitializationError
    {
        List<Throwable> errors = new LinkedList<Throwable>();
        Class<?> getterClass = getter.getDeclaringClass();
        String getterName = getterClass.getName() + "." + getter.getName();
        if (!Modifier.isPublic(getterClass.getModifiers())) {
            errors.add(new Exception("The class " + getterClass.getName() + " should be public."));
        }
        if (!Modifier.isPublic(getter.getModifiers())) {
            errors.add(new Exception("The method " + getterName + " should be public."));
        }
        if (!Modifier.isStatic(getter.getModifiers())) {
            errors.add(new Exception("The method " + getterName + " should be static."));
        }
        if (!getter.getReturnType().equals(String.class)) {
            errors.add(new Exception("The method " + getterName + " should return String."));
        }
        if (getter.getParameterTypes().length != 0) {
            errors.add(new Exception("The method " + getterName + " should have no parameters."));
        }
        if (errors.size() != 0) {
            throw new InitializationError(errors);
        }
        try {
            Object result = getter.invoke(null);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (Exception exception) {
            throw new InitializationError(exception);
        }
        return null;
    }
}

