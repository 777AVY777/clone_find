package com.autonomy.abc.selenium.find.numericWidgets;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class NumericWidget extends AppElement {

    final private WebElement container;

    public NumericWidget(WebDriver driver, WebElement outerContainer) {
        super(new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfNestedElementLocatedBy(outerContainer, By.cssSelector("svg.chart"))), driver);
        this.container = outerContainer.findElement(By.cssSelector("svg.chart"));
    }

    public WebElement getContainer() {
        return container;
    }

    public WebElement selectionRec() {
        return findElement(By.cssSelector("rect.selection"));
    }

    public int selectionRectangleWidth() {
        return (int) Double.parseDouble(selectionRec().getAttribute("Width"));
    }

    public boolean selectionRectangleExists() {
        return findElements(By.cssSelector("rect.selection")).size() > 0;
    }

    public List<WebElement> barsWithResults() {
        List<WebElement> bars = new ArrayList<>();
        for (WebElement bar : findElements(By.cssSelector("g > g > rect:not([height='1'])"))) {
            if (bar.isDisplayed()) {
                bars.add(bar);
            }
        }
        return bars;
    }

}