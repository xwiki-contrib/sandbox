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

import java.util.List;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;


/**
 * A custom runner that runs all tests methods found in the given {@link FileTest} in one block.
 * <p>
 * The test fails if one of the test methods fail. The test fails with an error, if one of the methods
 * produces an error.  The failure message will contain a list of errors and failures.</p>
 * 
 * @version $Id$
 * @since 2.4
 */
public class FileRunner extends Runner
{
    /** Name of the tested file. */
    private final String name;

    /** The test to run. */
    private final FileTest test;

    /** The list of tests to run. */
    private final List<FrameworkMethod> methods;

    /**
     * Create new FileRunner for the given file.
     * 
     * @param fileName name of the file to test
     * @param fileTest the test to run
     * @param testMethods a list of test methods from <code>fileTest</code> to run
     */
    public FileRunner(String fileName, FileTest fileTest, List<FrameworkMethod> testMethods)
    {
        this.name = fileName;
        this.test = fileTest;
        this.methods = testMethods;
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runner.Runner#getDescription()
     */
    @Override
    public Description getDescription()
    {
        if (test != null)
            return Description.createSuiteDescription(test.toString());
        return Description.createSuiteDescription(name);
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runner.Runner#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    public void run(RunNotifier notifier)
    {
        if (methods == null || methods.size() == 0) {
            return;
        }
        notifier.fireTestStarted(getDescription());
        // TODO make a list of all errors
        try {
            for (FrameworkMethod method : methods) {
                if (method.getAnnotation(Ignore.class) != null) {
                    continue;
                }
                new InvokeMethod(method, test).evaluate();
            }
        } catch (AssumptionViolatedException exception) {
            notifier.fireTestAssumptionFailed(new Failure(getDescription(), exception));
        } catch (Throwable exception) {
            notifier.fireTestFailure(new Failure(getDescription(), exception));
        } finally {
            notifier.fireTestFinished(getDescription());
        }
    }
}

