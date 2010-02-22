package org.xwiki.it.ui;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.xwiki.it.ui.elements.HomePage;
import org.xwiki.it.ui.elements.LoginPage;

public class LoginTest
{
    private static WebDriver driver;

    private HomePage homePage;

    @BeforeClass
    public static void init()
    {
        driver = new FirefoxDriver();
    }

    @Before
    public void setUp()
    {
        homePage = new HomePage(driver);
        homePage.gotoHomePage();

        // Make sure we log out if we're already logged in since we're testing the log in...
        if (homePage.isAuthenticated()) {
            homePage.clickLogout();
        }
    }

    @AfterClass
    public static void shutdown()
    {
        driver.close();
    }

    @Test
    public void testLoginLogout()
    {
        LoginPage loginPage = homePage.clickLogin();
        loginPage.loginAsAdmin();

        // Verify that after logging in we're redirected to the page on which the login button was clicked, i.e. the
        // home page here.
        Assert.assertTrue(homePage.isOnHomePage());

        Assert.assertTrue(homePage.isAuthenticated());
        Assert.assertEquals("Administrator", homePage.getCurrentUser());

        // Test Logout and verify we stay on the home page
        homePage.clickLogout();
        Assert.assertFalse(homePage.isAuthenticated());
        Assert.assertTrue(homePage.isOnHomePage());
    }

    @Test
    public void testLoginWithInvalidCredentials()
    {
        LoginPage loginPage = homePage.clickLogin();
        loginPage.loginAs("Admin", "wrong password");
        Assert.assertTrue(loginPage.hasInvalidCredentialsErrorMessage());
    }

    @Test
    public void testLoginWithInvalidUsername()
    {
        LoginPage loginPage = homePage.clickLogin();
        loginPage.loginAs("non existent user", "admin");
        Assert.assertTrue(loginPage.hasInvalidCredentialsErrorMessage());
    }
}
