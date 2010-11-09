package org.xwiki.tools.reporter;

import java.util.Map;

import org.xwiki.tools.reporter.TestCase.Status;

/** Extend this class to add a report to the build. */
public class Report
{
    public void handleTest(final TestCase testCase)
    {
    }

    @Override
    public String toString()
    {
        return "";
    }

    /** If this is overridden then only the statuses listed will be included. */
    public Status[] includeStatuses()
    {
        return null;
    }

    /** If this is overridden then the statuses goven will be skipped. This overpowers includeStatuses(). */
    public Status[] skipStatuses()
    {
        return new Status[] {};
    }
}
