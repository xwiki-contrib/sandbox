package org.xwiki.tools.reporter.reports;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.xwiki.tools.reporter.Report;
import org.xwiki.tools.reporter.TestCase;
import org.xwiki.tools.reporter.TestCase.Status;
import org.xwiki.tools.reporter.Publisher;


public class StatisticsReport extends Report
{
    private int failures;

    private int fixes;

    private int total;

    private float totalTimeTaken;

    private TestCase longestTest;

    private TestCase shortestTest;

    private int totalWebstandards;

    private int totalWebstandardsTime;

    public StatisticsReport(final Publisher publisher)
    {
        super(publisher);
    }

    @Override
    public void handleTest(final TestCase testCase)
    {
        switch (testCase.getStatus()) {
            case FAILED :
                this.failures++;
                break;
            case REGRESSION :
                this.failures++;
                break;
            case FIXED :
                this.fixes++;
                break;
        }
        this.total++;

        float duration = testCase.getDuration();
        totalTimeTaken += duration;
        if (this.longestTest == null || this.longestTest.getDuration() < duration) {
            this.longestTest = testCase;
        }
        if (this.shortestTest == null || this.shortestTest.getDuration() > duration) {
            this.shortestTest = testCase;
        }

        if (testCase.getClassName().contains("webstandards")) {
            this.totalWebstandards++;
            this.totalWebstandardsTime += testCase.getDuration();
        }
    }

    @Override
    public String[] getSubjectAndContent()
    {
        final String[] out = new String[2];

        out[0] = "Statistics Report";

        StringBuilder sb = new StringBuilder();

        if (this.fixes > 0) {
            sb.append(this.fixes + " tests stopped failing, yay :)\n");
        }
        sb.append(this.failures + " tests failed out of a total of " + this.total + "\n");
        sb.append("The total time taken to run the tests was " + this.totalTimeTaken + " seconds,\n");
        float average = this.totalTimeTaken / this.total;
        sb.append("and the average time taken by a test was " + average + " seconds.\n");
        sb.append("The fastest test ran in " + this.shortestTest.getDuration() + " seconds,");
        sb.append("a record held by " + this.shortestTest.getURL() + "\n");
        sb.append("The slowest test ran in " + this.longestTest.getDuration() + " seconds,");
        sb.append("and that one was " + this.longestTest.getURL() + "\n");
        if (totalWebstandards > 0) {
            float webstandardsAverage = totalWebstandardsTime / totalWebstandards;
            sb.append("and the average speed of a webstandards test (a good indication of page load speed) was ");
            sb.append((webstandardsAverage * 1000) + " milliseconds.\n");
        }

        out[1] = sb.toString();
        return out;
    }
}
