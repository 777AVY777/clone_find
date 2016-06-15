package com.autonomy.abc.selenium.find.comparison;

import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ResultsView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class ResultsComparisonView {
    private final WebElement wholeContainer;
    private final WebDriver driver;

    private ResultsComparisonView(WebElement serviceViewContainer, WebDriver driver) {
        this.wholeContainer = serviceViewContainer;
        this.driver = driver;
        waitForLoad(driver);
    }

    public ResultsComparisonView(WebDriver driver) {
        this(driver.findElement(By.cssSelector(".service-view-container:not(.hide)")), driver);
    }

    public List<FindResult> resultsExclusiveToThis() {
        return exclusiveToThis().getResults();
    }

    public List<FindResult> resultsCommonToBoth() {
        return commonToBoth().getResults();
    }

    public List<FindResult> resultsExclusiveToOther() {
        return exclusiveToOther().getResults();
    }

    public ResultsView exclusiveToThis() {
        WebElement leftContainer = wholeContainer.findElement(By.className("comparison-results-view-container-left"));
        return new ResultsView(leftContainer, driver);
    }

    public ResultsView commonToBoth() {
        WebElement middleContainer = wholeContainer.findElement(By.className("comparison-results-view-container-middle"));
        return new ResultsView(middleContainer, driver);
    }

    public ResultsView exclusiveToOther() {
        WebElement rightContainer = wholeContainer.findElement(By.className("comparison-results-view-container-right"));
        return new ResultsView(rightContainer, driver);
    }

    private static void waitForLoad(WebDriver driver) {
        new WebDriverWait(driver, 20)
                .withMessage("waiting for comparison view")
                .until(visibilityOfElementLocated(By.cssSelector(".service-view-container:not(.hide) .comparison-view")));
    }
}
