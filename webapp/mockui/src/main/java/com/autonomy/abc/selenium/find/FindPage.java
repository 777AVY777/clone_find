package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.query.*;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Date;
import java.util.List;

public class FindPage extends AppElement implements AppPage,
        IndexFilter.Filterable,
        DatePickerFilter.Filterable,
        StringDateFilter.Filterable,
        ParametricFilter.Filterable {

    FindPage(final WebDriver driver) {
        super(new WebDriverWait(driver, 30)
                .withMessage("loading Find page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("find-pages-container"))), driver);
    }

    protected FilterPanel filters() {
        return new FilterPanel(new IndexesTree.Factory(), getDriver());
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("find-pages-container")));
    }

    @Override
    public IndexesTree indexesTree() {
        return filters().indexesTree();
    }

    @Override
    public void filterBy(final QueryFilter filter) {
        filter.apply(this);
        waitForResultsToLoad();
    }

    @Override
    public DatePicker fromDatePicker() {
        return filters().datePickerFilterable().fromDatePicker();
    }

    @Override
    public DatePicker untilDatePicker() {
        return filters().datePickerFilterable().untilDatePicker();
    }

    @Override
    public FormInput fromDateInput() {
        return filters().stringDateFilterable().fromDateInput();
    }

    @Override
    public FormInput untilDateInput() {
        return filters().stringDateFilterable().untilDateInput();
    }

    @Override
    public String formatInputDate(final Date date) {
        return filters().stringDateFilterable().formatInputDate(date);
    }

    @Override
    public WebElement parametricContainer() {
        final WebElement firstParametric = findElement(By.cssSelector("[data-field]"));
        return ElementUtil.ancestor(firstParametric, 2);
    }

    @Override
    public void waitForParametricValuesToLoad() {
        filters().waitForParametricFields();
    }

    @Override
    public void openContainer(WebElement container) {
        WebElement list = container.findElement(By.className("collapse"));

        if(list.getAttribute("aria-expanded").equals("false")) {
            container.click();
            Waits.loadOrFadeWait();
        }
    }

    // this can be used to check whether on the landing page,
    // as opposed to main results page
    public WebElement footerLogo() {
        return findElement(By.className("hp-logo-footer"));
    }

    public int totalResultsNum() {
        return Integer.parseInt(findElement(By.className("total-results-number")).getText());
    }

    public WebElement originalQuery() { return findElement(By.className("original-query")); }

    public String correctedQuery() { return findElement(By.className("corrected-query")).getText();}

    public List<String> filterLabelsText() {
        return ElementUtil.getTexts(filterLabels());
    }

    public List<WebElement> filterLabels() {
        return findElements(By.className("filter-label"));
    }

    public void removeFilterLabel(WebElement filter) {
        filter.findElement(By.cssSelector(".filters-remove-icon")).click();
    }

    public void scrollToBottom() {
        findElement(By.className("results-number")).click();
        DriverUtil.scrollToBottom(getDriver());
        waitForResultsToLoad();
    }

    protected WebElement mainContainer() {
        return Container.currentTabContents(getDriver());
    }

    private void waitForResultsToLoad() {
        Container.MIDDLE.waitForLoad(getDriver());
    }

    public void waitUntilParametricModalGone() {
        new WebDriverWait(getDriver(),10)
                .until(ExpectedConditions.invisibilityOfElementLocated(By.className(".parametric-modal")));
    }

    public boolean verticalScrollBarPresent() {
        String javaScript = "return document.documentElement.scrollHeight>document.documentElement.clientHeight;";
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        return (boolean) executor.executeScript(javaScript);
    }

    //TODO: remove this when this class implements goToList
    public Boolean listTabExists() {
        return !mainContainer().findElements(By.cssSelector("[data-tab-id='list']")).isEmpty();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, FindPage> {
        @Override
        public FindPage create(final WebDriver context) {
            return new FindPage(context);
        }
    }
}
