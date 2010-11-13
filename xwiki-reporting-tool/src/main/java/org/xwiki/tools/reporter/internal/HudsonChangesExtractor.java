package org.xwiki.tools.reporter.internal;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xwiki.tools.reporter.Change;
import org.xwiki.tools.reporter.TestCase;

import org.apache.commons.io.IOUtils;

public class HudsonChangesExtractor
{
    private final XPath xpath;

    /** Caches the links to the changes so as not to attack the server too badly. */
    private final Map<String, List<String>> changesURLs = new HashMap<String, List<String>>();

    /** Caches the changes by their link so as not to query the same change twice. */
    private final Map<String, List<Change>> changesByURL = new HashMap<String, List<Change>>();

    HudsonChangesExtractor(final DocumentBuilder builder, final XPath xpath) throws Exception
    {
        this.xpath = xpath;
    }

    List<Change> getChangesForTestCase(final TestCase testCase)
    {
        final String jobURL = getJobURL(testCase.getURL());
        final List<String> changesURLs = this.getURLsForDependencyChanges(jobURL, testCase.getBuildNumber());
        return this.getChangesFromURLs(changesURLs);
    }

    private List<Change> getChangesFromURLs(final List<String> queryTheseURLs)
    {
        final List<Change> out = new ArrayList<Change>();
        final List<String> toLoad = new ArrayList<String>();
        for (String stringURL : queryTheseURLs) {
            if (this.changesByURL.containsKey(stringURL)) {
                out.addAll(this.changesByURL.get(stringURL));
            } else {
                toLoad.add(stringURL);
            }
        }
        try {
            out.addAll(loadChangesFromURLs(toLoad, this.xpath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }


    private List<String> getURLsForDependencyChanges(final String jobURL, final int buildNumber)
    {
        if (!this.changesURLs.containsKey(jobURL + buildNumber)) {
            this.changesURLs.put(jobURL + buildNumber, scrapeURLsForDependencyChanges(jobURL, buildNumber));
        }
        return this.changesURLs.get(jobURL + buildNumber);
    }

    /* ---------------- There is no state below this line. All others are stateless functions. ---------------- */

    /** Get the URL for the job which contains a given test. */
    private static String getJobURL(final String testURL)
    {
        // Trim to the location of the first / after /job/ then append the build number.
        // http://hudson.xwiki.org/job/xwiki-product-enterprise-tests-2.6-rc-1/org.xwiki.enterprise
        //     $xwiki-enterprise-test-selenium/2/testReport/org.xwiki.test.selenium/InlineEditorTest
        //     /testInlineEditCanChangeTitle/
        //
        // Becomes: http://hudson.xwiki.org/job/xwiki-product-enterprise-tests-2.6-rc-1/

        return testURL.substring(0, testURL.indexOf("/", testURL.indexOf("/job/") + 5) + 1);
    }

    /** Get a URL with all changes which happened in the frame of the last build. */
    private static String getChangesSinceLastBuildURL(final String jobURL, final int buildNumber)
    {
        return jobURL + "changes?from=" + buildNumber + "&to=" + buildNumber;
    }

    private static List<String> scrapeURLsForDependencyChanges(final String jobURL, final int buildNumber)
    {
        // Hudson puts all of the "changes" URLs in one place but doesn't make that available in the
        // xml api. So I'm mining the web page itself.
        // java DocumentBuilder fails to parse this page and JTidy parses it but drops the table containing
        // All of the usefull links. Thus I revert to using tried and true regular expressions.

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            IOUtils.copy(new URL(jobURL + buildNumber).openStream(), baos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final String pageContent = baos.toString();

        // The URLs are relative so take the beginning of the jobURL and prepend.
        final String prependThis = jobURL.substring(0, jobURL.indexOf("/job/"));

        final Pattern detailsLink = Pattern.compile("<a href=\"[^\"]*\">detail</a>");
        final Matcher detailsLinkMatcher = detailsLink.matcher(pageContent);
        final List<String> changesURLs = new ArrayList<String>();
        while (detailsLinkMatcher.find()) {
            String match = detailsLinkMatcher.group();
            changesURLs.add(prependThis + match.substring(match.indexOf('"') + 1, match.lastIndexOf('"')));
        }

        final List<String> out = new ArrayList<String>();

        // Have to remember the last build itself qualifies as a change and should be added.
        out.add(jobURL + buildNumber + "/api/xml");

        // Now we have a list of URLs ending in /changes?from=123&to=456
        // Convert this into a list of URLs ending in /124/api/xml /125/api/xml /126/api/xml etc.
        for (String changesURL : changesURLs) {
            try {
                final String beginning = changesURL.substring(0, changesURL.indexOf("changes?from="));
                // 13 == "changes?from=".length();
                final String end = changesURL.substring(beginning.length() + 13);
                int fromVersion = Integer.parseInt(end.substring(0, end.indexOf("&amp;to=")));
                int toVersion = Integer.parseInt(end.substring(end.indexOf("&amp;to=") + 8));

                for (int i = fromVersion + 1; i <= toVersion; i++) {
                    out.add(beginning + i + "/api/xml");
                }
            } catch(StringIndexOutOfBoundsException e) {
                // Some links slip past the regex filter, they will throw an exception
                // because they don't end in "/changes?from=123&to=456".
            }
        }
        return out;
    }

    private static List<Change> loadChangesFromURLs(final List<String> listOfURLs, final XPath xpath)
        throws Exception
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        final DocumentBuilder builder = dbf.newDocumentBuilder();
        final List<Document> documents = new ArrayList<Document>();
        // TODO: Threaded?
        for (String stringURL : listOfURLs) {
            documents.add(builder.parse(stringURL));
        }

        final XPathExpression changeItem = xpath.compile("mavenBuild/changeSet/item");
        final XPathExpression committerXPath = xpath.compile("mavenBuild/culprit");

        // Map full names of committers to their "committer name" and put that in the report.
        final Map<String, String> committerNameByFullname = new HashMap<String, String>();

        final List<Change> out = new ArrayList<Change>();

        for (Document doc : documents) {
            final NodeList committerList = (NodeList) committerXPath.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < committerList.getLength(); i++) {
                final Node item = committerList.item(i);
                final String committerURL = item.getFirstChild().getFirstChild().getNodeValue();
                final String committerName = committerURL.substring(committerURL.lastIndexOf('/') + 1);
                final String fullName = item.getLastChild().getFirstChild().getNodeValue();
                committerNameByFullname.put(fullName, committerName);
            }            

            final NodeList changeList = (NodeList) changeItem.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < changeList.getLength(); i++) {
                final Node item = changeList.item(i);
                final String date = getChildNamed(item, "date").getFirstChild().getNodeValue();
                final String commitLog = getChildNamed(item, "msg").getFirstChild().getNodeValue();
                int revision = Integer.parseInt(getChildNamed(item, "revision").getFirstChild().getNodeValue());
                final String committer = 
                    committerNameByFullname.get(getChildNamed(item, "user").getFirstChild().getNodeValue());
                out.add(new DefaultChange(date, commitLog, revision, committer));
            }
        }
        return out;
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
}
