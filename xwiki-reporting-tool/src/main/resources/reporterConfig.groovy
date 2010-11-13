import org.xwiki.tools.reporter.internal.Reporter;
import org.xwiki.tools.reporter.*;
import org.xwiki.tools.reporter.reports.*;
import org.xwiki.tools.reporter.publishers.*;

hudsonURL = "http://hudson.xwiki.org/";

new Reporter(hudsonURL) {{
    runReport(new RegressionAlertReport(new StdoutPublisher()));
    runJobsMatching("/.*xwiki-product-enterprise-tests-2.6/");
}}.run();


// Publishing multiple reports with one publisher using a DeferredPublisher.
final Publisher dp = new DeferredPublisher(new StdoutPublisher());

new Reporter(hudsonURL) {{
    runReport(new StatisticsReport(dp));
    runReport(new DetailedFailureReport(dp));
}}.run();

// This is what actually does the publishing.
dp.run();

