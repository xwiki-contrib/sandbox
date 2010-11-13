package org.xwiki.tools.reporter.internal;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.List;

import org.xwiki.tools.reporter.TestCase;
import org.xwiki.tools.reporter.TestCase.Status;
import org.xwiki.tools.reporter.Change;

public class DefaultTestCase implements TestCase
{
    private final Status status;

    private final int failedSince;

    private final int age;

    private final float duration;

    private final String className;

    private final String name;

    private final String url;

    private final HudsonTestCaseExtractor parent;

    private final int buildNumber;

    /** Thise are lazy loaded. */
    private String errorDetails;

    private String errorStackTrace;

    private String stdout;

    private String stderr;

    DefaultTestCase(final Map<String, String> map, final HudsonTestCaseExtractor parent)
    {
        this.parent = parent;

        Status stat = Status.UNKNOWN;
        try {
            stat = Status.valueOf(map.get("status"));
        } catch (Exception e) {
            // Do nothing since it's already UNKNOWN.
        }
        this.status = stat;

        this.failedSince = Integer.parseInt(map.get("failedSince"));
        this.age = Integer.parseInt(map.get("age"));
        this.duration = Float.parseFloat(map.get("duration"));
        this.className = map.get("className");
        this.name = map.get("name");
        this.url = map.get("url");
        this.buildNumber = Integer.parseInt(map.get("buildNumber"));

        // If this testCase is loaded directly from it's own page then these will be populated
        // otherwise they won't until asked for.
        this.errorDetails = map.get("errorDetails");
        this.errorStackTrace = map.get("errorStackTrace");
        this.stdout = map.get("stdout");
        this.stderr = map.get("stderr");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getStatus()
     */
    public Status getStatus()
    {
        return this.status;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getFailedSince()
     */
    public int getFailedSince()
    {
        return this.failedSince;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getAge()
     */
    public int getAge()
    {
        return this.age;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getDuration()
     */
    public float getDuration()
    {
        return this.duration;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getClassName()
     */
    public String getClassName()
    {
        return this.className;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getURL()
     */
    public String getURL()
    {
        return this.url;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getJobName()
     */
    public String getJobName()
    {
        final String url = this.getURL();
        final String beginningWithJob = url.substring(url.indexOf("/job/") + 5);
        return beginningWithJob.substring(0, beginningWithJob.indexOf('/'));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getBuildNumber()
     */
    public int getBuildNumber()
    {
        return this.buildNumber;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getErrorDetails()
     */
    public String getErrorDetails()
    {
        if (this.errorDetails == null) {
            this.lazyLoad();
        }
        return this.errorDetails;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getErrorStackTrace()
     */
    public String getErrorStackTrace()
    {
        if (this.errorStackTrace == null) {
            this.lazyLoad();
        }
        return this.errorStackTrace;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getStdout()
     */
    public String getStdout()
    {
        if (this.stdout == null) {
            this.lazyLoad();
        }
        return this.stdout;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getStderr()
     */
    public String getStderr()
    {
        if (this.stderr == null) {
            this.lazyLoad();
        }
        return this.stderr;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.tools.reporter.TestCase#getLastRun()
     */
    public TestCase getLastRun()
    {
        // Very cheap way of changing the URL, change if you think of a better one.
        final String lastURL = this.getURL().replaceFirst("/" + this.getBuildNumber() + "/",
                                                          "/" + (this.getBuildNumber() - 1) + "/");
        try {
            return this.parent.testCaseFromURL(lastURL);
        } catch (FileNotFoundException e) {
            // Guessing it's a 404 because there aren't any more versions older than the one loaded.
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get older revision of the test case at URL " + lastURL, e);
        }
    }

    /** Load all of the entities which are not included in the job test report. */
    private void lazyLoad()
    {
        final DefaultTestCase fullCase;
        try {
            fullCase = this.parent.testCaseFromURL(this.getURL());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load rest of test case", e);
        }
        // Have to fill the fields in with something ("") because a passing test will not have these
        // and a report may still call the methods on it and we don't want to load again.
        this.errorDetails = (fullCase.errorDetails == null) ? "" : fullCase.errorDetails;
        this.errorStackTrace = (fullCase.errorStackTrace == null) ? "" : fullCase.errorStackTrace;
        this.stdout = (fullCase.stdout == null) ? "" : fullCase.stdout;
        this.stderr = (fullCase.stderr == null) ? "" : fullCase.stderr;
    }

    public List<Change> getChanges()
    {
        return this.parent.getChangesForTestCase(this);
    }
}
