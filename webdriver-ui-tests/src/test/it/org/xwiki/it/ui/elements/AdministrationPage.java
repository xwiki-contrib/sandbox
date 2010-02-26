package org.xwiki.it.ui.elements;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AdministrationPage extends BasePage
{

    @FindBy(xpath = "//li[@class='Import']/a/span/img")
    WebElement importLink;

    public AdministrationPage(WebDriver driver)
    {
        super(driver);
    }
    
    public void gotoAdministrationPage()
    {
        gotoPage("XWiki", "XWikiPreferences", "admin");
    }

    public ImportPage clickImportSection()
    {
        this.importLink.click();
        return new ImportPage(getDriver());
    }

}
