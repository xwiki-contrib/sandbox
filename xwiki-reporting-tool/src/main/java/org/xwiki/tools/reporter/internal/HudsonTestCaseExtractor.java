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
import org.xwiki.tools.reporter.Change;


public class HudsonTestCaseExtractor
{
    private static final String APPEND_TO_URL = "api/xml";

    private static final String TEST_REPORT = "testReport/";

    private static final String LAST_BUILD = "lastBuild/";

    private final DocumentBuilder builder;

    private final XPathExpression testReportXPath;

    private final HudsonChangesExtractor changeLoader;

    public HudsonTestCaseExtractor() throws Exception
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.builder = dbf.newDocumentBuilder();

        final XPathFactory xpathFactory = XPathFactory.newInstance();
        final XPath xpath = xpathFactory.newXPath();
        this.testReportXPath = xpath.compile("/mavenModuleSetBuild/action/urlName/text()='testReport'");

        this.changeLoader = new HudsonChangesExtractor(this.builder, xpath);
    }

    public List<String> getAllHudsonJobURLs(final String hudsonURL) throws Exception
    {
        final List<Node> jobNodes = new ArrayList<Node>();
        final Document doc = this.builder.parse(hudsonURL + APPEND_TO_URL);
        doc.getDocumentElement().normalize();
        NodeList list = doc.getElementsByTagName("job");
        for (int i = 0; i < list.getLength(); i++) {
            jobNodes.add(list.item(i));
        }
        final List<String> out = new ArrayList<String>(jobNodes.size());
        for (Node jobNode : jobNodes) {
            // Get the second child, the <url> tag.
            out.add(jobNode.getFirstChild().getNextSibling().getFirstChild().getNodeValue());
        }
        return out;
    }

    /**
     * @param jobURL the URL of the hudson job eg: http://hudson.xwiki.org/job/xwiki-standards-validator/
     * @param buildNumber the build number to get the tests from, if buildNumber < 0 then the last finished build
     *                    is chosen.
     * @return all tests for that build.
     */
    public List<TestCase> getTestCasesForJob(final String jobURL, final int buildNumber) throws Exception
    {
        final String buildNumberString;
        if (buildNumber < 0) {
            buildNumberString = LAST_BUILD;
        } else {
            buildNumberString = buildNumber + "/";
        }

        final Document doc = this.builder.parse(jobURL + LAST_BUILD + APPEND_TO_URL);
        doc.getDocumentElement().normalize();

        final List<TestCase> out = new ArrayList<TestCase>();

        if (Boolean.TRUE.equals(this.testReportXPath.evaluate(doc, XPathConstants.BOOLEAN))) {
            out.addAll(this.getTestReport(jobURL + LAST_BUILD + TEST_REPORT + APPEND_TO_URL));
        }

        return out;
    }

    private List<TestCase> getTestReport(final String contentURL) throws Exception
    {
        final List<TestCase> out = new ArrayList<TestCase>();
        final Document doc = this.builder.parse(contentURL);
        doc.getDocumentElement().normalize();
        Node root = doc;
        while (!"surefireAggregatedReport".equals(root.getNodeName())) {
            root = root.getFirstChild();
            if (root == null) {
                System.out.println("Skipping for lack of tests: " + contentURL);
                return out;
            }
        }
        final NodeList nl = root.getChildNodes();
        Node node;
        for (int i = 0; i < nl.getLength(); i++) {
            node = nl.item(i);
            if ("childReport".equals(node.getNodeName())) {
                out.addAll(this.getChildReport(node));
            }
        }
        return out;
    }

    private List<TestCase> getChildReport(final Node childReport)
    {
        final List<TestCase> out = new ArrayList<TestCase>();
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
                out.add(this.getTestCase(node, number, url));
            }
        }
        return out;
    }

    private TestCase getTestCase(final Node node, final int buildNumber, final String url)
    {
        final NodeList nl = node.getChildNodes();
        final Map<String, String> nodeMap = new HashMap<String, String>();
        Node child;
        for (int i = 0; i < nl.getLength(); i++) {
            child = nl.item(i);
            nodeMap.put(child.getNodeName(), child.getFirstChild().getNodeValue());
        }
        return this.getTestCase(nodeMap, buildNumber, url);
    }

    private TestCase getTestCase(final Map<String, String> nodeMap, final int buildNumber, final String url)
    {
        final String fullClassName = nodeMap.get("className");
        final String classPackage = fullClassName.substring(0, fullClassName.lastIndexOf("."));
        final String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);

        // This seems to be how hudson cleans the names to make them URL friendly.
        final String cleanedName = nodeMap.get("name").replaceAll("[^a-zA-Z0-9]", "_");

        final String thisTestUrl = url + TEST_REPORT + classPackage + "/" + className + "/" + cleanedName + "/";

        nodeMap.put("url", thisTestUrl);
        nodeMap.put("buildNumber", Integer.valueOf(buildNumber).toString());
        return new DefaultTestCase(nodeMap, this);
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
            final Node child = nl.item(i);
            // Empty node would otherwise cause an NPE.
            final Node textNode = child.getFirstChild();
            if (textNode != null) {
                nodeMap.put(child.getNodeName(), child.getFirstChild().getNodeValue());
            } else {
                nodeMap.put(child.getNodeName(), "");
            }
        }

        return new DefaultTestCase(nodeMap, this);
    }

    public List<Change> getChangesForTestCase(final TestCase testCase)
    {
        return this.changeLoader.getChangesForTestCase(testCase);
    }
}
