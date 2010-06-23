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
package org.xwiki.escaping.suite;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;


/**
 * JUnit4 test suite that generates tests based on files found in a Zip (i.e. also war/xar/jar) archive.
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


    /** List of test runners build, one for each matching file found in the archive. */
    private final List<Runner> runners;


    /**
     * Create new ArchiveSuite.
     * 
     * @param klass the annotated test class
     * @param builder default junit builder
     * @throws InitializationError on errors
     */
    public ArchiveSuite(Class<? extends FileTest> klass, RunnerBuilder builder) throws InitializationError
    {
        super(klass);
        validateTestClass();
        this.runners = createRunners();
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runners.ParentRunner#getChildren()
     */
    @Override
    protected List<Runner> getChildren()
    {
        return runners;
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(Runner child)
    {
        return child.getDescription();
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(Runner child, RunNotifier notifier)
    {
        child.run(notifier);
    }

    /**
     * Read the archive and build a list of runners for its content.
     * 
     * @return a list of test runners
     * @throws InitializationError on errors
     */
    private List<Runner> createRunners() throws InitializationError
    {
        final ZipFile archive = getArchiveFromAnnotation();
        List<Runner> list = new ArrayList<Runner>();
        Enumeration< ? extends ZipEntry> entries = archive.entries();
        try {
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                Reader reader = new InputStreamReader(archive.getInputStream(entry));
                addTest(list, entry.getName(), reader);
            }
            archive.close();
        } catch (IOException exception) {
            throw new InitializationError(exception);
        }
        return list;
    }

    /**
     * Create and initialize an instance of the test class for given file.
     * 
     * @param name file name to use
     * @param reader the reader associated with the file data
     * @throws InitializationError on errors
     */
    private void addTest(List<Runner> list, String name, Reader reader) throws InitializationError
    {
        try {
            Object result = getTestClass().getOnlyConstructor().newInstance();
            if (result instanceof FileTest) {
                FileTest test = (FileTest) result;
                if (test.initialize(name, reader)) {
                    list.add(new FileRunner(name, test));
                }
                return;
            }
        } catch (Exception exception) {
            // should not happen, since the test class was validated before
            throw new InitializationError(exception);
        }
        throw new InitializationError("Failed to initialize the test for \"" + name + "\"");
    }

    /**
     * Validate that the test class implements {@link FileTest} and has the expected default constructor.
     * 
     * @throws InitializationError
     */
    private void validateTestClass() throws InitializationError
    {
        TestClass test = getTestClass();
        if (!FileTest.class.isAssignableFrom(test.getJavaClass())) {
            throw new InitializationError("The test class \"" + test.getName() + "\" should implement FileTest");
        }
        if (test.getOnlyConstructor().getParameterTypes().length != 0) {
            throw new InitializationError("Constructor of \"" + test.getName() + "\" should have no parameters");
        }
    }

    /**
     * Retrieve the path to the archive form annotations. Throws an exception if no annotations can
     * be found, when the annotation is used incorrectly or the path is invalid.
     * 
     * @return the Zip archive to use
     * @throws InitializationError when an error occurs
     */
    private ZipFile getArchiveFromAnnotation() throws InitializationError
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
        try {
            return new ZipFile(path);
        } catch (IOException exception) {
            throw new InitializationError(exception);
        }
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
        List<Throwable> errors = new ArrayList<Throwable>();
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

