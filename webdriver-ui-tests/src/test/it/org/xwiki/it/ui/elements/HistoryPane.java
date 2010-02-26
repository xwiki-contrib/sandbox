package org.xwiki.it.ui.elements;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class HistoryPane
{
    private WebDriver driver;

    @FindBy(id = "Historypane")
    private WebElement pane;

    public HistoryPane(WebDriver driver)
    {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public boolean hasVersionWithSummary(String summary)
    {
        List<WebElement> tableEntries = pane.findElements(By.xpath("//table/tr"));
        for (WebElement tableEntry : tableEntries) {
            if (tableEntry.findElement(By.xpath("//td[5]")).getText().contentEquals(summary)) {
                return true;
            }
        }
        return false;
    }

    public String getCurrentVersion()
    {
        try {
            // Try to find a radio button. This will mean there are several revisions in the table
            // and we'll find the version written down in the 3rd column
            pane.findElement(By.xpath("tr[2]/td/input"));
            return pane.findElement(By.xpath("//node()[contains(@class, 'currentversion')]/td[3]/a")).getText();
        } 
        catch (NoSuchElementException e) {
            // If we cound not find the radio button, there is less columns displayed and the version will be
            // in the first column
            return pane.findElement(By.xpath("//node()[contains(@class, 'currentversion')]/td[1]/a")).getText();
        }

    }

    public String getCurrentVersionComment()
    {
        try {
            // Try to find a radio button. This will mean there are several revisions in the table
            // and we'll find the version comment written down in the 6th column
            pane.findElement(By.xpath("tr[2]/td/input"));
            return pane.findElement(By.xpath("//node()[contains(@class, 'currentversion')]/td[6]")).getText();
        } 
        catch (NoSuchElementException e) {
            // If we cound not find the radio button, there is less columns displayed and the version comment will be
            // in the 4th column
            return pane.findElement(By.xpath("//node()[contains(@class, 'currentversion')]/td[4]")).getText();
        }

    }
}
