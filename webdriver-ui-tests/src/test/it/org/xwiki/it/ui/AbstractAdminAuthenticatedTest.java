package org.xwiki.it.ui;

import org.junit.Assert;
import org.junit.Before;
import org.xwiki.it.ui.elements.HomePage;

public class AbstractAdminAuthenticatedTest extends AbstractTest
{
    private HomePage homePage;

    @Before
    public void setUp()
    {
        homePage = new HomePage(driver);
        homePage.gotoHomePage();
        if (!homePage.isAuthenticated()) {
            homePage.clickLogin().loginAsAdmin();
            Assert.assertTrue(homePage.isOnHomePage());
        }
    }
}
