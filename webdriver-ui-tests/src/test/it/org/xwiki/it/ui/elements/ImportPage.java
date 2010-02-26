package org.xwiki.it.ui.elements;

import java.io.File;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ImportPage extends BasePage
{
    @FindBy(id = "packagelistcontainer")
    WebElement packageList;

    public ImportPage(WebDriver driver)
    {
        super(driver);
    }

    public boolean isPackageListEmpty()
    {
        return packageList.getText().contains("No package is available for import");
    }

    public void attachPackage(File file)
    {
        getDriver().findElement(By.id("xwikiuploadfile")).sendKeys(file.getAbsolutePath());
        getDriver().findElement(By.xpath("//input[@type='submit']")).submit();
    }

    public boolean isPackagePresent(String packageName)
    {
        return packageList.getText().contains(packageName);
    }

    public void selectPackage(String packageName)
    {
        getDriver().findElement(By.linkText(packageName)).click();
        waitUntilElementIsVisible(By.id("packageDescription"));
    }

    public void deletePackage(String packageName)
    {
        List<WebElement> packages = packageList.findElements(By.cssSelector("div.package"));
        for (WebElement pack : packages) {
            try {
                pack.findElement(By.partialLinkText(packageName));
                makeConfirmDialogSilent(); // temporary, see BasePage#makeConfirmDialogSilent
                pack.findElement(By.xpath("//div/span/a[@class='deletelink']")).click();
                return;
            } catch (NoSuchElementException e) {
                // Not the right package. Try again.
            }
        }
        throw new NoSuchElementException(packageName);
    }

    public void submitPackage()
    {
        getDriver().findElement(By.xpath("//input[@value='Import']")).click();
    }

    public BasePage clickImportedPage(String pageName)
    {
        getDriver().findElement(By.linkText(pageName)).click();
        return new BasePage(getDriver());
    }

}
