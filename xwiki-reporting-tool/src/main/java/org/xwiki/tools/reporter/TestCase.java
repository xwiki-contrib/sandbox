package org.xwiki.tools.reporter;

import java.util.Map;
import java.util.List;

/** For each test, a TestCase is sent to each registered report. */
public interface TestCase
{
    /** Possible statuses of the test. */
    enum Status
    {
        /** The test passed, nothing to see here. */
        PASSED,

        /** The test Failed but the last test failed too. */
        FAILED,

        /** The last test passed and this one failed. Probably a regression. */
        REGRESSION,

        /** The last test failed and now this one passes. */
        FIXED,

        /** The status could not be parsed. */
        UNKNOWN
    }

    /** @return the status returned by the test, see Status for possibilities. */
    Status getStatus();

    /** @return the buildNumber of the first build to fail. 0 if the case did not fail. */
    int getFailedSince();

    /** @return for how many builds has this test been failing. */
    int getAge();

    /** @return the number of seconds which this test tool to run. */
    float getDuration();

    /** @return the fuly qualified class name of the class containing the test. */
    String getClassName();

    /** @return the name of the test. Usually this is the name of the method in the class. */
    String getName();

    /** @return a URL which points to this test in hudson. */
    String getURL();

    /** @return the name of the Hudson job which this test was part of. */
    String getJobName();

    /** @return the number of the build. AKA how many times hudson has run this job. */
    int getBuildNumber();

    /** @return the error message given by the test if any, otherwise "". */
    String getErrorDetails();

    /** @return the error stack trace given by the test if any, otherwise "". */
    String getErrorStackTrace();

    /** @return what was written to /dev/stdout during the test. */
    String getStdout();

    /** @return what was written to /dev/stderr during the test. */
    String getStderr();

    /** @return the TestCase from when this test was last run or null if the record was deleted. */
    TestCase getLastRun();

    /** @return the changes made to the tested module and it's dependencies. */
    List<Change> getChanges();
}
