package com.autonomy.abc.selenium.find.bi;

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.element.ChosenDrop;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract class ParametricFieldView {

    private final WebDriver driver;
    private final WebElement container;

    ParametricFieldView(final WebDriver driver, final By locator) {
        this.driver = driver;
        this.container = Container.currentTabContents(driver).findElement(locator);
    }

    public String getSelectedFieldName(final int i){
        return nthParametricFilter(i).getText();
    }

    private WebElement nthParametricFilter(final int i){
        return findElement(By.cssSelector(".parametric-selections span:nth-child(" + i + ')'));
    }

    public boolean parametricSelectionDropdownsExist(){
        return findElement(By.cssSelector(".parametric-selections span")).isDisplayed();
    }

    public ChosenDrop parametricSelectionDropdown(final int i){
        return new ChosenDrop(nthParametricFilter(i), getDriver());
    }

    public List<String> getParametricDropdownItems(final int i){
        final ChosenDrop dropdown = parametricSelectionDropdown(i);
        final List<String> badFormat = ElementUtil.getTexts(dropdown.getItems());
        return badFormat.stream().map(String::toUpperCase).collect(Collectors.toList());
    }

    public WebElement message() {
        return findElement(By.cssSelector(".parametric-view-message"));
    }

    protected WebDriver getDriver() {
        return driver;
    }

    protected WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    protected List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }
}
