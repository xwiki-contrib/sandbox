package org.xwiki.tools.reporter;

import java.util.Map;

public interface TestCase
{
    public enum Status
    {
        PASSED,
        FAILED,
        REGRESSION,
        FIXED,
        UNKNOWN
    }

    public Status getStatus();

    public int getFailedSince();

    public int getAge();

    public float getDuration();

    public String getClassName();

    public String getName();

    public String getURL();

    public int getBuildNumber();

    public String getErrorDetails();

    public String getErrorStackTrace();

    public String getStdout();

    public String getStderr();

    /** @return the TestCase from when this test was last run or null if the record was deleted. */
    public TestCase getLastRun();
}
