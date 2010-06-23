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
     * This should be a public static method without parameters and returning String, for example:
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
    }

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
        System.out.println("\nArchiveSuite(class: " + klass.getCanonicalName() + ", builder: " + builder.toString() + ")");
        
        ArchivePath archivePath = klass.getAnnotation(ArchivePath.class);
        if (archivePath != null) {
            System.out.println("  Archive path: " + archivePath.value());
        }

        List<FrameworkMethod> getters = getTestClass().getAnnotatedMethods(ArchivePathGetter.class);
        if (getters.size() != 1) {
            // bad
        }
        try {
            Object path = getters.get(0).getMethod().invoke(null);
            if (path instanceof String) {
                System.out.println("  Archive path (get): " + (String) path);
            }
        } catch (IllegalArgumentException exception) {
            // TODO Auto-generated catch block
            exception.printStackTrace();
        } catch (IllegalAccessException exception) {
            // TODO Auto-generated catch block
            exception.printStackTrace();
        } catch (InvocationTargetException exception) {
            // TODO Auto-generated catch block
            exception.printStackTrace();
        }
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


}

