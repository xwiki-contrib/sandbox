package org.xwiki.it.ui;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.it.ui.elements.AdministrationPage;
import org.xwiki.it.ui.elements.BasePage;
import org.xwiki.it.ui.elements.HistoryPane;
import org.xwiki.it.ui.elements.ImportPage;

public class ImportTest extends AbstractAdminAuthenticatedTest
{
    private static final String PACKAGE_WITHOUT_HISTORY = "Main.TestPage-no-history.xar";

    private static final String PACKAGE_WITH_HISTORY = "Main.TestPage-with-history.xar";

    private AdministrationPage adminPage;

    private ImportPage importPage;

    @Before
    public void setUp()
    {
        super.setUp();

        adminPage = new AdministrationPage(driver);
        adminPage.gotoAdministrationPage();

        importPage = adminPage.clickImportSection();
    }

    @Test
    public void testImportPackageListIsEmpty()
    {
        Assert.assertTrue(importPage.isPackageListEmpty());
    }

    @Test
    public void testImportWithPackageRevisions() throws IOException
    {
        URL fileUrl = this.getClass().getResource("/" + PACKAGE_WITH_HISTORY);
        
        importPage.attachPackage(fileUrl);
        importPage.selectPackage(PACKAGE_WITH_HISTORY);
        
        importPage.selectOptionReplaceHistory();
        importPage.submitPackage();

        BasePage importedPage = importPage.clickImportedPage("Main.TestPage");

        HistoryPane history = importedPage.openHistoryDocExtraPane();

        Assert.assertEquals("4.1", history.getCurrentVersion());
        Assert.assertEquals("Imported from XAR", history.getCurrentVersionComment());
        Assert.assertTrue(history.hasVersionWithSummary("A new version of the document"));
        
        importedPage.delete();

        // Go back to import section to clean up
        adminPage.gotoAdministrationPage();
        importPage = adminPage.clickImportSection();

        // Delete our package
        importPage.deletePackage(PACKAGE_WITH_HISTORY);
    }
    
    @Test
    public void testImportUnexistingPageAsNewVersion() throws IOException
    {
        URL fileUrl = this.getClass().getResource("/" + PACKAGE_WITHOUT_HISTORY);

        importPage.attachPackage(fileUrl);
        importPage.selectPackage(PACKAGE_WITHOUT_HISTORY);

        importPage.submitPackage();

        BasePage importedPage = importPage.clickImportedPage("Main.TestPage");

        HistoryPane history = importedPage.openHistoryDocExtraPane();

        Assert.assertEquals("1.1", history.getCurrentVersion());
        Assert.assertEquals("Imported from XAR", history.getCurrentVersionComment());

        importedPage.delete();

        // Go back to import section to clean up
        adminPage.gotoAdministrationPage();
        importPage = adminPage.clickImportSection();

        // Delete our package
        importPage.deletePackage(PACKAGE_WITHOUT_HISTORY);
    }

}
