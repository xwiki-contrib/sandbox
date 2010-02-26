package org.xwiki.it.ui.elements;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage
{
    @FindBys({@FindBy(id = "tmRegister"), @FindBy(tagName = "a")})
    private WebElement registerLink;

    @FindBys({@FindBy(id = "tmLogin"), @FindBy(tagName = "a")})
    private WebElement loginLink;

    @FindBys({@FindBy(id = "tmLogout"), @FindBy(tagName = "a")})
    private WebElement logoutLink;

    @FindBys({@FindBy(id = "tmUser"), @FindBy(tagName = "a")})
    private WebElement userLink;

    private WebDriver driver;

    public BasePage(WebDriver driver)
    {
        this.driver = driver;
        ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, 15);
        PageFactory.initElements(finder, this);
    }

    protected WebDriver getDriver()
    {
        return this.driver;
    }

    public void waitUntilElementIsVisible(final By locator)
    {
        this.waitUntilElementIsVisible(locator, 10);
    }

    public void waitUntilElementIsVisible(final By locator, int timeout)
    {
        Wait<WebDriver> wait = new WebDriverWait(driver, timeout);
        wait.until(new ExpectedCondition<WebElement>()
        {
            public WebElement apply(WebDriver driver)
            {
                RenderedWebElement element = (RenderedWebElement) driver.findElement(locator);
                return element.isDisplayed() ? element : null;
            }
        });
    }

    public LoginPage clickLogin()
    {
        this.loginLink.click();
        return new LoginPage(getDriver());
    }

    public boolean isAuthenticated()
    {
        // Note that we cannot test if the userLink field is accessible since we're using an AjaxElementLocatorFactory
        // and thus it would wait 15 seconds before considering it's not accessible.
        return !getDriver().findElements(By.id("tmUser")).isEmpty();
    }

    public String getCurrentUser()
    {
        return this.userLink.getText();
    }

    public void clickLogout()
    {
        this.logoutLink.click();
    }

    public RegisterPage clickRegister()
    {
        this.registerLink.click();
        return new RegisterPage(getDriver());
    }

    public void delete()
    {
        getDriver().findElement(By.partialLinkText("More actions")).click();
        getDriver().findElement(By.linkText("Delete")).click();

        getDriver().findElement(By.xpath("//input[@value='yes']")).click();

        // Purge from trash bin
        makeConfirmDialogSilent(); // temporary, see #makeConfirmDialogSilent
        getDriver().findElement(By.partialLinkText("Delete")).click();
    }

    public HistoryPane openHistoryDocExtraPane()
    {
        this.getDriver().findElement(By.id("Historylink")).click();
        this.waitUntilElementIsVisible(By.id("historycontent"));

        return new HistoryPane(driver);
    }

    /**
     * There is no easy support for alert/confirm window methods yet, see -
     * http://code.google.com/p/selenium/issues/detail?id=27 -
     * http://www.google.com/codesearch/p?hl=en#2tHw6m3DZzo/branches
     * /merge/common/test/java/org/openqa/selenium/AlertsTest.java The aim is : <code>
     * Alert alert = driver.switchTo().alert();
     * alert.accept();
     * </code> Until then, the following hack does override the confirm method in Javascript to always return true.
     */
    protected void makeConfirmDialogSilent()
    {
        ((JavascriptExecutor) driver).executeScript("window.confirm = function() { return true; }");
    }

    protected boolean isOnPage(String space, String page, String action)
    {
        return getDriver().getCurrentUrl().equals(getURLForPage(space, page, action));
    }

    protected boolean isOnPage(String space, String page)
    {
        return isOnPage(space, page, "view");
    }

    protected void gotoPage(String space, String page)
    {
        gotoPage(space, page, "view");
    }

    protected void gotoPage(String space, String page, String action)
    {
        String url = getURLForPage(space, page, action);

        // Verify if we're already on the correct page and if so don't do anything
        if (!getDriver().getCurrentUrl().equals(url)) {
            getDriver().get(url);
        }
    }

    private String getURLForPage(String space, String page, String action)
    {
        return "http://localhost:8080/xwiki/bin/" + action + "/" + space + "/" + page;
    }

}
