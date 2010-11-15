package org.xwiki.tools.reporter.internal;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.xwiki.tools.reporter.Change;
import org.xwiki.tools.reporter.Publisher;
import org.xwiki.tools.reporter.Report;
import org.xwiki.tools.reporter.TestCase;
import org.xwiki.tools.reporter.TestCase.Status;


public class Reporter
{
    private static final String DEFAULT_CONFIG_FILE = "reporterConfig.groovy";

    private static final String CONFIG_FILE_PROPERTY = "reporterConfigScript";

    private final String hudsonURL;

    private int buildNumber = -1;

    private final List<Report> reports = new ArrayList<Report>();

    private final List<Publisher> publishers = new ArrayList<Publisher>();

    private final List<Pattern> allowPatterns = new ArrayList<Pattern>();

    private final List<Pattern> denyPatterns = new ArrayList<Pattern>();

    private final HudsonTestCaseExtractor testCaseExtractor;

    public static void main(String[] args) throws Exception
    {
        String script = null;

        if (System.getProperty(CONFIG_FILE_PROPERTY) != null) {
            final File scriptFile = new File(System.getProperty(CONFIG_FILE_PROPERTY));
            if (!scriptFile.exists()) {
                System.err.println("Could not find custom configuration file ["
                                   + System.getProperty(CONFIG_FILE_PROPERTY)
                                   + "] Using default instead");
            } else {
                script = IOUtils.toString(new FileInputStream(scriptFile));
                System.out.println("Using custom configuration script: "
                                   + System.getProperty(CONFIG_FILE_PROPERTY));
            }
        }

        if (script == null) {
            final URL configScriptURL =
                Thread.currentThread().getContextClassLoader().getResource(DEFAULT_CONFIG_FILE);

            if (configScriptURL == null) {
                throw new RuntimeException("Could not find default configuration file" + DEFAULT_CONFIG_FILE);
            }

            script = IOUtils.toString(configScriptURL.openStream());
        }

        new GroovyShell().evaluate(script);
    }

    public Reporter(final String hudsonURL) throws Exception
    {
        this.hudsonURL = hudsonURL;
        this.testCaseExtractor = new HudsonTestCaseExtractor();
    }

    public void runReport(final Report report)
    {
        this.reports.add(report);
    }

    public void usePublisher(final Publisher publisher)
    {
        this.publishers.add(publisher);
    }

    public void runJobsMatching(final String regex)
    {
        try {
            final Pattern p = Pattern.compile(regex);
            this.allowPatterns.add(p);
        } catch (Exception e) {
            System.err.println("Failed to compile regex " + e.getMessage());
        }
    }

    public void doNotRunJobsMatching(final String regex)
    {
        try {
            final Pattern p = Pattern.compile(regex);
            this.denyPatterns.add(p);
        } catch (Exception e) {
            System.err.println("Failed to compile regex " + e.getMessage());
        }
    }

    public void reportOnBuildNumber(final int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public void run() throws Exception
    {
        for (String jobURL : this.testCaseExtractor.getAllHudsonJobURLs(this.hudsonURL)) {
            if (patternValidate(jobURL, this.allowPatterns, this.denyPatterns)) {
                System.out.println("Running reports on: " + jobURL);
                for (TestCase testCase : this.testCaseExtractor.getTestCasesForJob(jobURL, this.buildNumber)) {
                    runReports(testCase, this.reports);
                }
            }
        }
        for (Report report : this.reports) {
            report.publish();
        }
    }

    /* ---------------- There is no state below this line. All others are stateless functions. ---------------- */

    private static boolean patternValidate(final String jobURL,
                                           final List<Pattern> allowPatterns,
                                           final List<Pattern> denyPatterns)
    {
        for (Pattern p : denyPatterns) {
            if (p.matcher(jobURL).matches()) {
                //System.out.println("Skipping (matched deny regex): " + jobURL);
                return false;
            }
        }
        if (allowPatterns.size() > 0) {
            for (Pattern p : allowPatterns) {
                if (p.matcher(jobURL).matches()) {
                    return true;
                }
            }
            //System.out.println("Skipping (didn't match any allow regex): " + jobURL);
            return false;
        }
        return true;
    }

    private static void runReports(final TestCase testCase, final List<Report> reports)
    {
        reportLoop:
        for (Report report : reports) {
            final Status[] toSkip = report.skipStatuses();
            for (int i = 0; i < toSkip.length; i++) {
                if (testCase.getStatus() == toSkip[i]) {
                    continue reportLoop;
                }
            }
            final Status[] toInclude = report.includeStatuses();
            if (toInclude == null) {
                report.handleTest(testCase);
                continue;
            }
            for (int i = 0; i < toInclude.length; i++) {
                if (testCase.getStatus() == toInclude[i]) {
                    report.handleTest(testCase);
                }
            }
        }
    }
}
