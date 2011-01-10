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
package org.xwiki.store;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for TransactionRunnable.
 *
 * @version $Id$
 * @since TODO
 */
public class TransactionRunnableTest
{
    private final TransactionRunnable testCase = new TransactionRunnable();

    private boolean hasRun;

    /** Make sure the user cannot do anything foolish. */
    @Test(expected=IllegalArgumentException.class)
    public void infiniloopTest()
    {
        testCase.runIn(new TransactionRunnable().runIn(testCase));
    }

    /** Make sure the user cannot do anything foolish. */
    @Test(expected=IllegalStateException.class)
    public void runInMultiTest()
    {
        testCase.runIn(new TransactionRunnable());
        testCase.runIn(new TransactionRunnable());
    }

    /** preRun: Parent before child, exceptions cause everything to stop. */
    @Test(expected=CustomException.class)
    public void preRunChildTest() throws Exception
    {
        new TransactionRunnable() {
            protected void onPreRun()
            {
                Assert.fail("Run in wrong order.");
            }
        } .runIn(new TransactionRunnable() {
            protected void onPreRun() throws Exception
            {
                throw new CustomException();
            }
        } .runIn(this.testCase));

        this.testCase.preRun();
    }

    /** preRun: Siblings in same order as registered, exceptions cause everything to stop. */
    @Test(expected=CustomException.class)
    public void preRunSiblingTest() throws Exception
    {
        new TransactionRunnable() {
            protected void onPreRun() throws Exception
            {
                throw new CustomException();
            }
        } .runIn(this.testCase);

        new TransactionRunnable() {
            protected void onPreRun()
            {
                Assert.fail("Run in wrong order.");
            }
        } .runIn(this.testCase);

        this.testCase.preRun();
    }

    /** Run: Parent before child, exceptions cause everything to stop. */
    @Test(expected=CustomException.class)
    public void runChildTest() throws Exception
    {
        new TransactionRunnable() {
            protected void onRun()
            {
                Assert.fail("Run in wrong order.");
            }
        } .runIn(new TransactionRunnable() {
            protected void onRun() throws Exception
            {
                throw new CustomException();
            }
        } .runIn(this.testCase));

        this.testCase.run();
    }

    /** Run: Siblings in same order as registered, exceptions cause everything to stop. */
    @Test(expected=CustomException.class)
    public void runSiblingTest() throws Exception
    {
        new TransactionRunnable() {
            protected void onRun() throws Exception
            {
                throw new CustomException();
            }
        } .runIn(this.testCase);

        new TransactionRunnable() {
            protected void onRun()
            {
                Assert.fail("Run in wrong order.");
            }
        } .runIn(this.testCase);

        this.testCase.run();
    }

    /** Commit: Child before parent, exceptions cause everything to stop. */
    @Test(expected=CustomException.class)
    public void commitChildTest() throws Exception
    {
        new TransactionRunnable() {
            // Child first
            protected void onCommit() throws Exception
            {
                throw new CustomException();
            }
        } .runIn(new TransactionRunnable() {
            // Then parent
            protected void onCommit()
            {
                Assert.fail("Run in wrong order.");
            }
        } .runIn(this.testCase));

        this.testCase.commit();
    }

    /** Commit: Siblings in reverse order as registered, exceptions cause everything to stop. */
    @Test(expected=CustomException.class)
    public void commitSiblingTest() throws Exception
    {
        new TransactionRunnable() {
            protected void onCommit()
            {
                Assert.fail("Run in wrong order.");
            }
        } .runIn(this.testCase);

        new TransactionRunnable() {
            protected void onCommit() throws Exception
            {
                throw new CustomException();
            }
        } .runIn(this.testCase);

        this.testCase.commit();
    }

    /**
     * Rollback: Child before parent, exceptions are collected and thrown at end.
     * Exception must indicate possibility of corruption.
     */
    @Test(expected=TransactionException.class)
    public void rollbackChildTest() throws Exception
    {
        new TransactionRunnable() {
            protected void onRollback() throws Exception
            {
                itRan();
            }
        } .runIn(new TransactionRunnable() {
            protected void onRollback() throws Exception
            {
                Assert.assertFalse("Child rolled back after parent.", hasRun());
                throw new CustomException();
            }
        } .runIn(this.testCase));

        try {
            this.testCase.rollback();
        } catch (TransactionException e) {
            Assert.assertTrue("onRollback failed and exception did not indicate "
                              + "the possibility of storage corruption.", e.isNonRecoverable());
            Assert.assertTrue("onRollback did not run for a child of a "
                              + "runnable which threw an exception.", hasRun());
            throw e;
        }
    }

    /**
     * Rollback: Siblings in reverse order as registered, exceptions are collected and thrown at end.
     * Exception must indicate possibility of corruption.
     */
    @Test(expected=TransactionException.class)
    public void rollbackSiblingTest() throws Exception
    {
        new TransactionRunnable() {
            protected void onRollback()
            {
                itRan();
            }
        } .runIn(this.testCase);

        new TransactionRunnable() {
            protected void onRollback() throws Exception
            {
                Assert.assertFalse("Siblings rolled back in same order as they were registered.", hasRun());
                throw new CustomException();
            }
        } .runIn(this.testCase);

        try {
            this.testCase.rollback();
        } catch (TransactionException e) {
            Assert.assertTrue("onRollback failed and exception did not indicate "
                              + "the possibility of storage corruption.", e.isNonRecoverable());
            Assert.assertTrue("onRollback did not run for the sibling of a runnable "
                              + "which threw an exception.", hasRun());
            throw e;
        }
    }

    /**
     * Complete: Parent before child, exceptions are collected and thrown at end.
     * Exception must not indicate possibility of corruption.
     */
    @Test(expected=TransactionException.class)
    public void completeChildTest() throws Exception
    {
        new TransactionRunnable() {
            protected void onComplete()
            {
                itRan();
            }
        } .runIn(new TransactionRunnable() {
            protected void onComplete() throws Exception
            {
                Assert.assertTrue("Child run before parent.", hasRun());
                throw new CustomException();
            }
        } .runIn(this.testCase));

        try {
            this.testCase.complete();
        } catch (TransactionException e) {
            Assert.assertFalse("onComplete failed and exception erroniously indicated "
                               + "the possibility of storage corruption.", e.isNonRecoverable());
            Assert.assertTrue("onComplete did not run for a child of a "
                              + "runnable which threw an exception.", hasRun());
            throw e;
        }
    }

    /**
     * Complete: Siblings in same order as registered, exceptions are collected and thrown at end.
     * Exception must not indicate possibility of corruption.
     */
    @Test(expected=TransactionException.class)
    public void completeSiblingTest() throws Exception
    {
        new TransactionRunnable() {
            protected void onComplete()
            {
                itRan();
            }
        } .runIn(this.testCase);

        new TransactionRunnable() {
            protected void onComplete() throws Exception
            {
                Assert.assertTrue("onComplete for siblings run in opposite order than registered.",
                                  hasRun());
                throw new CustomException();
            }
        } .runIn(this.testCase);

        try {
            this.testCase.complete();
        } catch (TransactionException e) {
            Assert.assertFalse("onComplete failed and exception erroniously indicated "
                               + "the possibility of storage corruption.", e.isNonRecoverable());
            Assert.assertTrue("onComplete did not run for the sibling of a runnable "
                              + "which threw an exception.", hasRun());
            throw e;
        }
    }

    public boolean hasRun()
    {
        return this.hasRun;
    }

    public void itRan()
    {
        this.hasRun = true;
    }

    private class CustomException extends Exception
    {
        // Does nothing different.
    }
}
