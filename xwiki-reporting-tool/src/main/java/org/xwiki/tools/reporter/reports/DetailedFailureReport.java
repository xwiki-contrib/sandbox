package org.xwiki.tools.reporter.reports;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.xwiki.tools.reporter.Format;
import org.xwiki.tools.reporter.Report;
import org.xwiki.tools.reporter.TestCase;
import org.xwiki.tools.reporter.TestCase.Status;
import org.xwiki.tools.reporter.Change;
import org.xwiki.tools.reporter.Publisher;


public class DetailedFailureReport extends Report
{
    final List<TestCase> failures = new ArrayList<TestCase>();

    final List<TestCase> regressions = new ArrayList<TestCase>();

    final List<TestCase[]> flickers = new ArrayList<TestCase[]>();

    final Format formatTool;

    public DetailedFailureReport(final Publisher publisher, final Format format)
    {
        super(publisher);
        this.formatTool = format;
    }

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

    @Override
    public String[] getSubjectAndContent()
    {
        if (this.regressions.size() + this.flickers.size() + this.failures.size() == 0) {
            return null;
        }

        final String[] out = new String[2];
        out[0] = "Test Report: " + this.regressions.size() + " Regressions, "
                                 + this.flickers.size() + " Flickers, and "
                                 + this.failures.size() + " Test Failures.";

        this.sortLists();

        final StringBuilder sb = new StringBuilder();

        sb.append(this.formatTool.formatHeader("Detailed Failure Report"));
        sb.append(this.formatTool.newLine());
        sb.append("Summary:");
        sb.append(this.formatTool.newLine());
        sb.append(this.regressions.size()).append(" Regressions");
        sb.append(this.formatTool.newLine());
        sb.append(this.flickers.size()).append(" Flickers");
        sb.append(this.formatTool.newLine());
        sb.append(this.failures.size()).append(" Failures");
        sb.append(this.formatTool.newLine());

        if (this.regressions.size() > 0) {
            sb.append(this.formatTool.newLine());
            sb.append(this.formatTool.newLine());
            sb.append(this.formatTool.formatSubheader("Regressions:"));
            sb.append(this.formatTool.newLine());
            sb.append("These tests have failed for the first time in stored history.");
            sb.append(this.formatTool.newLine());
            for (TestCase testCase : this.regressions) {
                sb.append(this.formatTool.newLine());
                sb.append(this.formatTool.link(testCase.getName(), testCase.getURL()));

                String errorDetails = testCase.getErrorDetails();
                if (errorDetails.length() > 500) {
                    errorDetails = errorDetails.substring(0, 500);
                }
                sb.append("Details: " + this.formatTool.formatMessage(errorDetails));
            }
            sb.append(this.formatTool.horizontalRuler());
        }

        if (this.flickers.size() > 0) {
            sb.append(this.formatTool.newLine());
            sb.append(this.formatTool.newLine());
            sb.append(this.formatTool.formatSubheader("Probable Flickers:"));
            sb.append(this.formatTool.newLine());
            sb.append("These are regressions which have failed before and might be faulty tests.");
            sb.append(this.formatTool.newLine());
            for (TestCase[] testCases : this.flickers) {
                TestCase testCase = testCases[0];
                sb.append(this.formatTool.newLine());
                sb.append(this.formatTool.link(testCase.getName(), testCase.getURL()));

                String errorDetails = testCase.getErrorDetails();
                if (errorDetails.length() > 500) {
                    errorDetails = errorDetails.substring(0, 500);
                }
                sb.append("Details: " + this.formatTool.formatMessage(errorDetails));

                sb.append(this.formatTool.link("Link to prior failure", testCases[1].getURL()));
                sb.append(this.formatTool.newLine());
            }
            sb.append(this.formatTool.horizontalRuler());
        }

        if (this.failures.size() > 0) {
            sb.append(this.formatTool.newLine());
            sb.append(this.formatTool.newLine());
            sb.append(this.formatTool.formatSubheader("Failures:"));
            sb.append(this.formatTool.newLine());
            sb.append("These tests have failed multiple times in a row.");
            sb.append(this.formatTool.newLine());
            for (TestCase testCase : this.failures) {

                sb.append(this.formatTool.newLine());
                sb.append("Failing for the past " + testCase.getAge() + " builds.");
                sb.append(this.formatTool.newLine());
                sb.append(this.formatTool.link(testCase.getName(), testCase.getURL()));

                String errorDetails = testCase.getErrorDetails();
                if (errorDetails.length() > 500) {
                    errorDetails = errorDetails.substring(0, 500);
                }
                sb.append("Details: ").append(this.formatTool.formatMessage(errorDetails));
            }
            sb.append(this.formatTool.horizontalRuler());
        }

        out[1] = sb.toString();
        return out;
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

    private static class TestCaseComparitor implements Comparator<TestCase>
    {
        public static Comparator INSTANCE = new TestCaseComparitor();

        public int compare(TestCase test1, TestCase test2)
        {
            return test1.getAge() - test2.getAge();
        }
    }
}
