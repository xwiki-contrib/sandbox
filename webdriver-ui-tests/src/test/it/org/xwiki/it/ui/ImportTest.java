package org.xwiki.it.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.xwiki.it.ui.elements.AdministrationPage;
import org.xwiki.it.ui.elements.BasePage;
import org.xwiki.it.ui.elements.HistoryPane;
import org.xwiki.it.ui.elements.ImportPage;

public class ImportTest extends AbstractAdminAuthenticatedTest
{
    private static final String PACKAGE_WITHOUT_HISTORY = "Main.TestPage_no-history.xar";

    private static final String PACKAGE_WITH_HISTORY = "Main.TestPage_with-history.xar";

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
    public void testAttachPackage() throws IOException
    {
        InputStream st = this.getClass().getResourceAsStream("/" + PACKAGE_WITHOUT_HISTORY);

        File pack = this.createFileFromStream("package", "xar", st);

        importPage.attachPackage(pack);
        Assert.assertTrue(importPage.isPackagePresent(pack.getName()));
    }

    @Test
    public void testImportUnexistingPageAsNewVersion() throws IOException
    {
        InputStream st = this.getClass().getResourceAsStream("/" + PACKAGE_WITHOUT_HISTORY);
        
        File pack = this.createFileFromStream("package-", ".xar", st);

        importPage.attachPackage(pack);
        importPage.selectPackage(pack.getName());

        importPage.submitPackage();

        importPage.waitUntilElementIsVisible(By.linkText("Main.TestPage"));

        BasePage importedPage = importPage.clickImportedPage("Main.TestPage");

        HistoryPane history = importedPage.openHistoryDocExtraPane();

        Assert.assertEquals("1.1", history.getCurrentVersion());
        Assert.assertEquals("Imported from XAR", history.getCurrentVersionComment());
        
        importedPage.delete();
        
        // Go back to import section to clean up
        adminPage.gotoAdministrationPage();
        importPage = adminPage.clickImportSection();
        
        // Delete our package
        importPage.deletePackage(pack.getName());
    }

    private File createFileFromStream(String prefix, String postfix, InputStream source) throws IOException
    {
        File f = File.createTempFile(prefix, postfix);
        FileOutputStream fw = new FileOutputStream(f);
        InputStreamReader reader = new InputStreamReader(source);

        try {
            byte buffer[] = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                fw.write(buffer, 0, length);
            }
        } finally {
            reader.close();
            fw.close();
        }

        return f;
    }
}
