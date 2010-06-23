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

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;


/**
 * 
 * 
 * @version $Id$
 * @since 2.4
 */
public class FileRunner extends Runner
{
    /** Name of the tested file. */
    private final String name;

    /** The test to run. */
    private FileTest test;

    /**
     * Create new FileRunner for the given file.
     * 
     * @param fileName name of the file to test
     * @param fileTest the test to run
     */
    public FileRunner(String fileName, FileTest fileTest)
    {
        this.name = fileName;
        this.test = fileTest;
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runner.Runner#getDescription()
     */
    @Override
    public Description getDescription()
    {
        return Description.createSuiteDescription(name);
    }

    /**
     * {@inheritDoc}
     * @see org.junit.runner.Runner#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    public void run(RunNotifier notifier)
    {
        notifier.fireTestStarted(getDescription());
        try {
            
        } catch (AssumptionViolatedException exception) {
            notifier.fireTestAssumptionFailed(new Failure(getDescription(), exception));
        } catch (Throwable exception) {
            notifier.fireTestFailure(new Failure(getDescription(), exception));
        } finally {
            notifier.fireTestFinished(getDescription());
        }
    }
}

