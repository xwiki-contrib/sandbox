package org.xwiki.tools.reporter.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.io.IOUtils;
import org.xwiki.tools.reporter.Format;
import org.xwiki.tools.reporter.Report;
import org.xwiki.tools.reporter.TestCase;
import org.xwiki.tools.reporter.TestCase.Status;
import org.xwiki.tools.reporter.Change;
import org.xwiki.tools.reporter.Publisher;


public class RegressionAlertReport extends Report
{
    final List<TestCase> regressions = new ArrayList<TestCase>();

    final Format formatTool;

    final File tempFile;

    final StringBuilder tempContent = new StringBuilder();

    final StringBuilder nextTempContent = new StringBuilder();

    public RegressionAlertReport(final Publisher publisher, final Format format)
    {
        super(publisher);
        this.formatTool = format;

        this.tempFile = new File(new File(System.getProperty("java.io.tmpdir")),
                                 "xwiki-reporting-tool.RegressionAlertReport.tmp");
        this.loadTempContent();
    }

    @Override
    public void handleTest(final TestCase regression)
    {
        this.nextTempContent.append("\n" + regression.getURL());
        if (this.tempContent.indexOf(regression.getURL()) != -1) {
            // This job is still on the same build number, skip.
            return;
        }

        if (regression.getChanges().size() == 0) {
            // No changes since last build. This regression must be a flicker.
            return;
        }

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
    public String[] getSubjectAndContent()
    {
        if (this.regressions.size() == 0) {
            return null;
        }
        final String[] out = new String[2];
        out[0] = "Regression Alert: " + this.regressions.size() + " Regressions";

        final Map<String, List<TestCase>> regressionsByJobName = this.getRegressionsByJobName();

        final StringBuilder sb = new StringBuilder();

        sb.append(this.formatTool.formatHeader("Regression Alert: " + this.regressions.size() + " Regressions"));

        for (String jobName : regressionsByJobName.keySet()) {
            sb.append(this.formatTool.newLine());
            sb.append(this.formatTool.newLine());
            sb.append(this.formatTool.formatSubheader("Regressions in " + this.formatTool.escape(jobName)));
            sb.append(this.formatTool.newLine());
            for (TestCase regression : regressionsByJobName.get(jobName)) {
                sb.append(this.formatTool.newLine());
                sb.append(this.formatTool.link(regression.getName(), regression.getURL()));

                String errorDetails = regression.getErrorDetails();
                if (errorDetails.length() > 500) {
                    errorDetails = errorDetails.substring(0, 500);
                }
                sb.append("Details: ").append(this.formatTool.formatMessage(errorDetails));
            }
            sb.append(this.formatTool.horizontalRuler());
            sb.append(this.formatTool.newLine());
            sb.append("Changes which might have caused this:");
            for (Change ch : regressionsByJobName.get(jobName).get(0).getChanges()) {
                sb.append(this.formatTool.newLine());
                sb.append("Revision: ").append(ch.getRevision());
                sb.append(this.formatTool.newLine());
                sb.append("Commit Log: ").append(ch.getCommitLog());
                sb.append(this.formatTool.newLine());
                sb.append("Committer: ").append(ch.getCommitter());
                sb.append(this.formatTool.newLine());
                sb.append(this.formatTool.horizontalRuler());
            }
        }

        out[1] = sb.toString();
        return out;
    }

    @Override
    public void publish()
    {
        super.publish();
        this.storeTempContent();
    }

    private void loadTempContent()
    {
        if (this.tempFile.exists()) {
            final FileInputStream fis;
            try {
                fis = new FileInputStream(this.tempFile);
                this.tempContent.append(IOUtils.toString(fis));
                IOUtils.closeQuietly(fis);
            } catch (Exception e) {
                throw new RuntimeException("failed to load temporary file.", e);
            }
        }
    }

    private void storeTempContent()
    {
        final FileOutputStream fos;
        try {
            if (this.tempFile.exists()) {
                this.tempFile.delete();
            }
            fos = new FileOutputStream(this.tempFile, false);
            IOUtils.write(this.nextTempContent, fos);
            IOUtils.closeQuietly(fos);
        } catch (Exception e) {
            throw new RuntimeException("failed to store run tests in temporary file.", e);
        }
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
