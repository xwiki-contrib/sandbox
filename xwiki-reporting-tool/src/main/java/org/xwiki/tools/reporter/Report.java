package org.xwiki.tools.reporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xwiki.tools.reporter.TestCase.Status;

/** Extend this class to add a report to the build. */
public class Report
{
    private final Publisher publisher;

    public Report(final Publisher publisher)
    {
        this.publisher = publisher;
    }

    public void handleTest(final TestCase testCase)
    {
    }

    /**
     * Get the subject and content to publish from this report.
     * If this is not overridden then addPublisher and publish must be.
     *
     * @return an array containing 2 Strings, the first being the subject and the second being the report.
     *         If there is nothing to report then return null.
     */
    public String[] getSubjectAndContent()
    {
        return new String[] {"I did not read the documentation",
                             "This is the default content for a report template."};
    }

    /** Either this or getSubjectAndContent must be overridden in order for the report to be published. */
    public void publish()
    {
        final String[] toPublish = this.getSubjectAndContent();
        if (toPublish != null) {
            this.publisher.publish(toPublish[0], toPublish[1]);
        }
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
