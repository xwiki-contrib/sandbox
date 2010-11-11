package org.xwiki.tools.reporter.internal;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xwiki.tools.reporter.Report;
import org.xwiki.tools.reporter.Publisher;
import org.xwiki.tools.reporter.TestCase;
import org.xwiki.tools.reporter.TestCase.Status;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;


public class Reporter
{
    private static final String DEFAULT_CONFIG_FILE = "reporter.properties";

    private static final String CONFIG_FILE_PROPERTY = "reporterConfigFile";

    private static final String PARAM_HUDSON_URL = "reporter.hudsonURL";

    private static final String PARAM_HUDSON_URL_DEFAULT = "http://hudson.xwiki.org/";

    private static final String APPEND_TO_URL = "api/xml";

    private static final String TEST_REPORT = "testReport/";

    private static final String LAST_BUILD = "lastBuild/";

    private static final String REPORT_LIST_CONFIG_PARAM = "reporter.runReport";

    private static final String PUBLISHER_LIST_CONFIG_PARAM = "reporter.usePublisher";

    private static final String ALLOW_ONLY_JOB_REGEX = "reporter.onlyReportOnJobsMatchingRegex";

    private static final String DENY_JOB_REGEX = "reporter.doNotReportOnJobsMatchingRegex";

    private final Configuration config;

    private final DocumentBuilder builder;

    private final XPathExpression testReportXPath;

    private final List<Report> reports = new ArrayList<Report>();

    private final List<Publisher> publishers = new ArrayList<Publisher>();

    private final List<Pattern> allowPatterns = new ArrayList<Pattern>();

    private final List<Pattern> denyPatterns = new ArrayList<Pattern>();

    public static void main(String[] args) throws Exception
    {
        final Configuration config = Reporter.getConfiguration();
        final Reporter reporter = new Reporter(config);
        reporter.addHudson(PARAM_HUDSON_URL_DEFAULT);
        reporter.publish();
    }

    private static Configuration getConfiguration() throws Exception
    {
        String fileName = System.getProperty(CONFIG_FILE_PROPERTY);
        if (fileName == null) {
            fileName = DEFAULT_CONFIG_FILE;
        }
        return new PropertiesConfiguration(fileName);
    }

    public Reporter(Configuration config) throws Exception
    {
        this.config = config;
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.builder = dbf.newDocumentBuilder();
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        this.testReportXPath = xpath.compile("/mavenModuleSetBuild/action/urlName/text()='testReport'");

        // Set up all of the reports.
        final List<String> reportNameList = (List<String>) config.getList(REPORT_LIST_CONFIG_PARAM);
        for (String reportName : reportNameList) {
            try {
                this.reports.add((Report) Class.forName(reportName).newInstance());
            } catch (Throwable t) {
                System.err.println("Failed to instantiate Report: " + reportName);
            }
        }

        // And the publishers.
        final List<String> publisherNameList = (List<String>) config.getList(PUBLISHER_LIST_CONFIG_PARAM);
        for (String publisherName : publisherNameList) {
            try {
                this.publishers.add((Publisher) Class.forName(publisherName).newInstance());
            } catch (Throwable t) {
                System.err.println("Failed to instantiate Publisher: " + publisherName);
            }
        }

        // set up the list of regular expressions for job allowing
        final List<String> allowRegexes = (List<String>) config.getList(ALLOW_ONLY_JOB_REGEX);
        for (String regex : allowRegexes) {
            try {
                this.allowPatterns.add(Pattern.compile(regex));
            } catch (Throwable t) {
                System.err.println("Failed to compile regular expression: " + regex);
            }
        }
        // And denial
        final List<String> denyRegexes = (List<String>) config.getList(DENY_JOB_REGEX);
        for (String regex : denyRegexes) {
            try {
                this.denyPatterns.add(Pattern.compile(regex));
            } catch (Throwable t) {
                System.err.println("Failed to compile regular expression: " + regex);
            }
        }
    }

    public void publish()
    {
        for (Report report : this.reports) {
            report.toString();
        }
    }

    public void addHudson(final String hudsonURL) throws Exception
    {
        final Document doc = this.builder.parse(hudsonURL + APPEND_TO_URL);
        doc.getDocumentElement().normalize();
        NodeList list = doc.getElementsByTagName("job");
        for (int i = 0; i < list.getLength(); i++) {
            this.addJob(list.item(i));
        }
    }

    private boolean patternValidate(final String jobURL)
    {
        for (Pattern p : this.denyPatterns) {
            if (p.matcher(jobURL).matches()) {
                System.out.println("Skipping (matched deny regex): " + jobURL);
                return false;
            }
        }
        if (this.allowPatterns.size() > 0) {
            for (Pattern p : this.allowPatterns) {
                if (p.matcher(jobURL).matches()) {
                    return true;
                }
            }
            System.out.println("Skipping (didn't match any allow regex): " + jobURL);
            return false;
        }
        return true;
    }

    public void addJob(final Node job) throws Exception
    {
        final String jobURL = getChildNamed(job, "url").getFirstChild().getNodeValue();

        if (!this.patternValidate(jobURL)) {
            return;
        }

        System.out.println("Parsing: " + jobURL);

        final Document doc = this.builder.parse(jobURL + LAST_BUILD + APPEND_TO_URL);
        doc.getDocumentElement().normalize();

        if (Boolean.TRUE.equals(this.testReportXPath.evaluate(doc, XPathConstants.BOOLEAN))) {
            this.addTestReport(jobURL + LAST_BUILD + TEST_REPORT + APPEND_TO_URL);
        }
    }

    private void addTestReport(final String contentURL) throws Exception
    {
        Document doc = this.builder.parse(contentURL);
        doc.getDocumentElement().normalize();
        Node root = doc;
        while (!"surefireAggregatedReport".equals(root.getNodeName())) {
            root = root.getFirstChild();
            if (root == null) {
                System.out.println("Skipping for lack of tests: " + contentURL);
                return;
            } 
        }
        final NodeList nl = root.getChildNodes();
        Node node;
        for (int i = 0; i < nl.getLength(); i++) {
            node = nl.item(i);
            if ("childReport".equals(node.getNodeName())) {
                this.addChildReport(node);
            }
        }
    }

    private static Node getChildNamed(final Node parent, final String name)
    {
        final NodeList nl = parent.getChildNodes();
        Node node;
        for (int i = 0; i < nl.getLength(); i++) {
            node = nl.item(i);
            if (name.equals(node.getNodeName())) {
                return node;
            }
        }
        return null;
    }

    private void addChildReport(final Node childReport)
    {
        // Get the child and store the url and test number.
        final Node child = childReport.getFirstChild();
        int number = Integer.parseInt(child.getFirstChild().getFirstChild().getNodeValue());
        final String url = child.getLastChild().getFirstChild().getNodeValue();

        // Get the suite and index through the cases.
        final Node suite = childReport.getLastChild().getLastChild();
        final NodeList nl = suite.getChildNodes();
        Node node;
        for (int i = 0; i < nl.getLength(); i++) {
            node = nl.item(i);
            if ("case".equals(node.getNodeName())) {
                this.addTestCase(node, number, url);
            }
        }
    }

    private void addTestCase(final Node node, final int buildNumber, final String url)
    {
        final NodeList nl = node.getChildNodes();
        final Map<String, String> nodeMap = new HashMap<String, String>();
        Node child;
        for (int i = 0; i < nl.getLength(); i++) {
            child = nl.item(i);
            nodeMap.put(child.getNodeName(), child.getFirstChild().getNodeValue());
        }
        this.addTestCase(nodeMap, buildNumber, url);
    }

    private void addTestCase(final Map<String, String> nodeMap, final int buildNumber, final String url)
    {
        final String fullClassName = nodeMap.get("className");
        final String classPackage = fullClassName.substring(0, fullClassName.lastIndexOf("."));
        final String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);

        // This seems to be how hudson cleans the names to make them URL friendly.
        final String cleanedName = nodeMap.get("name").replaceAll("[^a-zA-Z0-9]", "_");

        final String thisTestUrl = url + TEST_REPORT + classPackage + "/" + className + "/" + cleanedName + "/";

        nodeMap.put("url", thisTestUrl);
        nodeMap.put("buildNumber", Integer.valueOf(buildNumber).toString());
        this.runReports(new DefaultTestCase(nodeMap, this));
    }

    private void runReports(final TestCase testCase)
    {
        reportLoop:
        for (Report report : this.reports) {
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

    public DefaultTestCase testCaseFromURL(final String testCaseURL) throws Exception
    {
        final Document doc = this.builder.parse(testCaseURL + APPEND_TO_URL);
        doc.getDocumentElement().normalize();
        final NodeList nl = doc.getFirstChild().getChildNodes();

        final Map<String, String> nodeMap = new HashMap<String, String>();
        nodeMap.put("url", testCaseURL);

        // Horrible way of getting the build number because it's not provided.
        final String urlUpToBuildNumber = testCaseURL.substring(0, testCaseURL.indexOf("/" + TEST_REPORT));
        final String buildNumber = urlUpToBuildNumber.substring(urlUpToBuildNumber.lastIndexOf("/") + 1);
        nodeMap.put("buildNumber", buildNumber);

        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            nodeMap.put(child.getNodeName(), child.getFirstChild().getNodeValue());
        }

        return new DefaultTestCase(nodeMap, this);
    }

    public List<Revision> revisionsOfDependenciesFromTestCaseURL(final String testCaseURL) throws Exception
    {
        
    }
}
