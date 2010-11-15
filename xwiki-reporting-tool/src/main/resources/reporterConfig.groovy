import org.xwiki.tools.reporter.internal.Reporter;
import org.xwiki.tools.reporter.*;
import org.xwiki.tools.reporter.reports.*;
import org.xwiki.tools.reporter.publishers.*;

/*
 * Configuration for xwiki-reporting-tool
 * This configuration is a groovy script and xwiki-reporting-tool calls this script when started and this script
 * is responsable for running whatever reports the user wants.
 * Some sample reporter configurations are included.
 * NOTE: It is best to run only one reporter and use multiple reports and publishers because each reporter will
 *       scrape the hudson server.
 *
 * To specify an alternate location for this script on the filesystem,
 * use -DreporterConfigScript=/path/to/config.groovy in the command.
 */

// Where to scrape the data from.
hudsonURL = "http://hudson.xwiki.org/";

// Run a regression alert report and print to /dev/stdout 
// but only run on jobs who's URLs match a regular expression.

new Reporter(hudsonURL) {{
    runReport(new RegressionAlertReport(new StdoutPublisher(), Format.HTML));
    //runJobsMatching("/.*xwiki-product-enterprise-tests/");
}}.run();


// Publish multiple reports at once using a DeferredPublisher.
/*
final Publisher dp = new DeferredPublisher(new StdoutPublisher());
new Reporter(hudsonURL) {{
    runReport(new StatisticsReport(dp, Format.PLAIN));
    runReport(new DetailedFailureReport(dp, Format.PLAIN));
}}.run();

// This is what actually does the publishing.
dp.run();
*/

// Run a detailed failure report and send the output as an email.
/*
final HtmlMailPublisher emailPub = new HtmlMailPublisher();
emailPub.addRecipient("notifications@xwiki.org");
emailPub.setMailConfig( new HashMap<String, String>() {{
    put("from", "build.noreply@xwiki.org");
    put("port", "25");
    put("host", "localhost");
    //put("smtpUsername", "");
    //put("smtpPassword", "");
    //put("javamailExtraProperties", "");
}});

new Reporter(hudsonURL) {{
    runReport(new DetailedFailureReport(emailPub, Format.HTML));
}}.run();
*/
