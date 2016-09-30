package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.numericWidgets.MainNumericWidget;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IdolFindPage extends FindPage {

    private IdolFindPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected FilterPanel filters() {
        return new IdolFilterPanel(new IdolDatabaseTree.Factory(), getDriver());
    }

    public ComparisonModal openCompareModal() {
        compareButton().click();
        return ComparisonModal.make(getDriver());
    }

    public void goBackToSearch() {
        findElement(By.cssSelector(".comparison-view-back-button")).click();
        waitForLoad();
    }

    public WebElement compareButton() {
        return mainContainer().findElement(By.className("compare-modal-button"));
    }

    public void waitUntilSearchTabsLoaded() {
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.elementToBeClickable(compareButton()));
    }

    public void waitUntilDatabasesLoaded() {
        new WebDriverWait(getDriver(),20)
                .withMessage("databases not loaded message to disappear")
                .until(ExpectedConditions.invisibilityOfElementWithText(By.cssSelector(".main-results-list .results")
                        ,"The list of databases has not yet been retrieved"));
    }

    public MainNumericWidget mainGraph() {return new MainNumericWidget(getDriver());}

    public boolean mainGraphDisplayed(){
        return !findElements(By.className("middle-container-time-bar")).isEmpty();
    }

    public void goToListView() {
        mainContainer().findElement(By.cssSelector("[data-tab-id='list']")).click();
        new WebDriverWait(getDriver(), 15).until(ExpectedConditions.visibilityOf(findElement(By.cssSelector(".results-list-container"))));
    }

    public void goToTopicMap() {
        findElement(By.cssSelector("[data-tab-id='topic-map']")).click();
    }

    public void goToMap() {
        mainContainer().findElement(By.cssSelector("[data-tab-id='map']")).click();
        WebElement map = mainContainer().findElement(By.xpath(".//*[starts-with(@class,'location')]"));
        new WebDriverWait(getDriver(), 15)
                .until(ExpectedConditions.visibilityOf(map));
    }


    public void goToSunburst(){
        mainContainer().findElement(By.cssSelector("[data-tab-id='sunburst']")).click();
    }

    public void goToTable(){
        findElement(By.cssSelector("[data-tab-id='table']")).click();
        new WebDriverWait(getDriver(), 15).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                return findElement(By.cssSelector("table.dataTable")).isDisplayed() ||
                        findElement(By.cssSelector(".parametric-view-message")).isDisplayed();
            }
        });
    }

    public boolean resultsComparisonVisible() {
        return findElement(By.cssSelector(".comparison-view")).isDisplayed();
    }

    public void refresh() {
        getDriver().navigate().refresh();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        @Override
        public IdolFindPage create(final WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}
