package org.xwiki.tools.reporter;

import java.util.Map;

/** For each test, a TestCase is sent to each registered report. */
public interface TestCase
{
    /** Possible statuses of the test. */
    public enum Status
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
    public Status getStatus();

    /** @return the buildNumber of the first build to fail. 0 if the case did not fail. */
    public int getFailedSince();

    /** @return for how many builds has this test been failing. */
    public int getAge();

    /** @return the number of seconds which this test tool to run. */
    public float getDuration();

    /** @return the fuly qualified class name of the class containing the test. */
    public String getClassName();

    /** @return the name of the test. Usually this is the name of the method in the class. */
    public String getName();

    /** @return a URL which points to this test in hudson. */
    public String getURL();

    /** @return the number of the build. AKA how many times hudson has run this job. */
    public int getBuildNumber();

    /** @return the error message given by the test if any, otherwise "". */
    public String getErrorDetails();

    /** @return the error stack trace given by the test if any, otherwise "". */
    public String getErrorStackTrace();

    /** @return what was written to /dev/stdout during the test. */
    public String getStdout();

    /** @return what was written to /dev/stderr during the test. */
    public String getStderr();

    /** @return the TestCase from when this test was last run or null if the record was deleted. */
    public TestCase getLastRun();
}
