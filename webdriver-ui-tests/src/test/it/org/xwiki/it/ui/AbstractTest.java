package org.xwiki.it.ui;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class AbstractTest
{
    protected static WebDriver driver;

    @BeforeClass
    public static void init()
    {
        driver = new FirefoxDriver();
    }

    @AfterClass
    public static void shutdown()
    {
        driver.close();
    }

    public AbstractTest()
    {
        super();
    }

}
