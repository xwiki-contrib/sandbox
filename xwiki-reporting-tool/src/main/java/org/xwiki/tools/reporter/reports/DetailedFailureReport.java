package org.xwiki.tools.reporter.reports;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.xwiki.tools.reporter.Report;
import org.xwiki.tools.reporter.TestCase;
import org.xwiki.tools.reporter.TestCase.Status;
import org.xwiki.tools.reporter.Change;

public class DetailedFailureReport extends Report
{
    final List<TestCase> failures = new ArrayList<TestCase>();

    final List<TestCase> regressions = new ArrayList<TestCase>();

    final List<TestCase[]> flickers = new ArrayList<TestCase[]>();

    @Override
    public void handleTest(final TestCase testCase)
    {
        if (testCase.getStatus() == Status.REGRESSION) {
            this.handleRegression(testCase);
        } else {
            this.failures.add(testCase);
        }
    }

    private void handleRegression(final TestCase regression)
    {
        final TestCase lastFailingRun = lastFailingRun(regression.getLastRun());
        if (lastFailingRun != null && lastFailingRun.getErrorDetails().equals(regression.getErrorDetails())) {
            this.flickers.add(new TestCase[] { regression, lastFailingRun });
        } else {
            this.regressions.add(regression);
        }        
    }

    private static TestCase lastFailingRun(final TestCase currentRun)
    {
        if (currentRun == null) {
            return null;
        }
        if (currentRun.getStatus() == Status.FAILED || currentRun.getStatus() == Status.REGRESSION) {
            return currentRun;
        }
        return lastFailingRun(currentRun.getLastRun());
    }

    @Override
    public String toString()
    {
        this.sortLists();

        StringBuilder sb = new StringBuilder();
        sb.append("Detailed Failure Report:\n");
        sb.append(this.regressions.size() + " Regressions,\n");
        sb.append(this.flickers.size() + " Tests are Probably Flickering,\n");
        sb.append(this.failures.size() + " Failures\n");
        sb.append("-------------------------------------------------------------------------------");

        if (this.regressions.size() > 0) {
            sb.append("\n\nRegressions:\n");
            sb.append("These tests have failed for the first time in stored history.\n");
            for (TestCase testCase : this.regressions) {
                sb.append("\nName: " + testCase.getName());

                String errorDetails = testCase.getErrorDetails();
                if (errorDetails.length() > 500) {
                    errorDetails = errorDetails.substring(0, 500);
                }
                sb.append("\nDetails: " + errorDetails);

                sb.append("\nLink: " + testCase.getURL() + "\n");
            }
            sb.append("-------------------------------------------------------------------------------");
        }

        if (this.flickers.size() > 0) {
            sb.append("\n\nProbable Flickers:\n");
            sb.append("These are regressions which have failed before and might be faulty tests.\n");
            for (TestCase[] testCases : this.flickers) {
                TestCase testCase = testCases[0];
                sb.append("\nName: " + testCase.getName());

                String errorDetails = testCase.getErrorDetails();
                if (errorDetails.length() > 500) {
                    errorDetails = errorDetails.substring(0, 500);
                }
                sb.append("\nDetails: " + errorDetails);

                sb.append("\nLink: " + testCase.getURL());
                sb.append("\nLink to prior failure: " + testCases[1].getURL() + "\n");
            }
            sb.append("-------------------------------------------------------------------------------");
        }

        if (this.failures.size() > 0) {
            sb.append("\n\nFailures:\n");
            sb.append("These tests have failed multiple times in a row.\n");
            for (TestCase testCase : this.failures) {

                sb.append("\nFailing for the past " + testCase.getAge() + " builds.");
                sb.append("\nName: " + testCase.getName());

                String errorDetails = testCase.getErrorDetails();
                if (errorDetails.length() > 500) {
                    errorDetails = errorDetails.substring(0, 500);
                }
                sb.append("\nDetails: " + errorDetails);

                sb.append("\nLink: " + testCase.getURL() + "\n");
            }
            sb.append("-------------------------------------------------------------------------------");
        }

        System.out.println(sb.toString());
        return "";
    }

    private void sortLists()
    {
        Collections.sort(this.failures, TestCaseComparitor.INSTANCE);
    }

    @Override
    public Status[] skipStatuses()
    {
        return new Status[] {Status.PASSED, Status.FIXED};
    }

    private static class TestCaseComparitor implements Comparator<TestCase>
    {
        public static Comparator INSTANCE = new TestCaseComparitor();

        public int compare(TestCase test1, TestCase test2)
        {
            return test1.getAge() - test2.getAge();
        }
    }
}
