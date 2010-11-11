package org.xwiki.tools.reporter.reports;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.xwiki.tools.reporter.Report;
import org.xwiki.tools.reporter.TestCase;
import org.xwiki.tools.reporter.TestCase.Status;
import org.xwiki.tools.reporter.Change;

public class RegressionAlertReport extends Report
{
    final List<TestCase> regressions = new ArrayList<TestCase>();

    @Override
    public void handleTest(final TestCase regression)
    {
        final TestCase lastFailingRun = lastFailingRun(regression.getLastRun());
        // Only counts as a regression if it has not failed in recent history.
        if (lastFailingRun == null) {
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
        if (this.regressions.size() == 0) {
            return "";
        }
        final Map<String, List<TestCase>> regressionsByJobName = this.getRegressionsByJobName();

        final StringBuilder sb = new StringBuilder();
        sb.append("REGRESSION ALERT:\n");
        sb.append(this.regressions.size() + " Regressions,\n");
        sb.append("-------------------------------------------------------------------------------");

        for (String jobName : regressionsByJobName.keySet()) {
            sb.append("\n\nRegressions in " + jobName + ":\n");
            for (TestCase regression : regressionsByJobName.get(jobName)) {
                sb.append("\nName: " + regression.getName());

                String errorDetails = regression.getErrorDetails();
                if (errorDetails.length() > 500) {
                    errorDetails = errorDetails.substring(0, 500);
                }
                sb.append("\nDetails: " + errorDetails);

                sb.append("\nLink: " + regression.getURL() + "\n");
            }
            sb.append("-----------------------------------");
            sb.append("\nChanges which might have caused this:");
            for (Change ch : regressionsByJobName.get(jobName).get(0).getChanges()) {
                sb.append("\nRevision: " + ch.getRevision());
                sb.append("\nCommit Log: " + ch.getCommitLog());
                sb.append("\nCommitter: " + ch.getCommitter());
                sb.append("\n------------------");
            }
        }

        System.out.println(sb.toString());
        return "";
    }

    private Map<String, List<TestCase>> getRegressionsByJobName()
    {
        final Map<String, List<TestCase>> out = new HashMap<String, List<TestCase>>();
        for (TestCase regression : this.regressions) {
            if (out.get(regression.getJobName()) == null) {
                out.put(regression.getJobName(), new ArrayList<TestCase>());
            }
            out.get(regression.getJobName()).add(regression);
        }
        return out;
    }

    @Override
    public Status[] includeStatuses()
    {
        return new Status[] {Status.REGRESSION};
    }
}
