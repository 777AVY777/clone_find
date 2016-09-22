package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.find.Container;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.query.DatePickerFilter;
import com.autonomy.abc.selenium.query.StringDateFilter;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.util.CssUtil.cssifyIndex;

public class FilterPanel {
    private final WebElement panel;
    private final WebDriver driver;
    private final ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory;

    public FilterPanel(final ParametrizedFactory<IndexCategoryNode, IndexesTree> indexesTreeFactory, final WebDriver driver) {
        this.indexesTreeFactory = indexesTreeFactory;
        this.driver = driver;
        panel = Container.LEFT.findUsing(driver).findElement(By.xpath(".//h3[contains(text(), 'Filters')]/.."));
    }

    //INDEX/DATABASE RELATED
    public IndexesTree indexesTree() {
        return indexesTreeFactory.create(new FindIndexCategoryNode(panel.findElement(By.cssSelector(".databases-list [data-category-id='all']")), driver));
    }

    public Index getIndex(final int i) {
        return indexesTree().allIndexes().getIndex(i);
    }

    public void waitForIndexes() {
        new WebDriverWait(driver, 10).until(ExpectedConditions.invisibilityOfElementLocated(By.className("not-loading")));
    }

    public ListFilterContainer indexesTreeContainer() {
        final WebElement heading = panel.findElement(By.xpath(".//h4[contains(text(), 'Indexes') or contains(text(), 'Databases')]"));
        final WebElement container = ElementUtil.ancestor(heading, 2);
        return new IndexesTreeContainer(container, driver);
    }

    protected DateFilterContainer dateFilterContainer() {
        final WebElement heading = panel.findElement(By.xpath(".//h4[contains(text(), 'Dates')]"));
        final WebElement container = ElementUtil.ancestor(heading, 2);
        return new DateFilterContainer(container, driver);
    }

    public List<ParametricFieldContainer> parametricFieldContainers() {
        final List<ParametricFieldContainer> containers = new ArrayList<>();
        for (final WebElement container : getParametricFilters()) {
            containers.add(new ParametricFieldContainer(container, driver));
        }
        return containers;
    }

    private Iterable<WebElement> getParametricFilters() {
        return panel.findElements(By.cssSelector("[data-field-display-name][data-field]"));
    }

    public ParametricFieldContainer parametricContainerOfFilter(final String filter) {
        final WebElement field = panel.findElement(By.cssSelector(".parametric-value-element[data-value='"+filter+"']"));
        return new ParametricFieldContainer(ElementUtil.ancestor(field,5),driver);
    }

    public ParametricFieldContainer parametricContainer(final String filterCategory) {
        final WebElement category = panel.findElement(By.cssSelector("[data-field-display-name='"+filterCategory+"']"));
        return new ParametricFieldContainer(category,driver);
    }

    public ParametricFieldContainer parametricField(final int i) {
        return parametricFieldContainers().get(i);
    }

    //gets the index of the nth non-empty filter container
    public int nonZeroParaFieldContainer(final int n) {
        int index = 0;
        int nonZeroCount = 0;
        for (final WebElement container: getParametricFilters()) {
            final ParametricFieldContainer candidate = new ParametricFieldContainer(container,driver);
            if(!"0".equals(candidate.getFilterNumber())) {
                if(nonZeroCount >=n) {
                    return index;
                }
                else{
                    nonZeroCount++;
                }
            }
            index++;
        }
        return -1;
    }

    //DATE SPECIFIC
    public void toggleFilter(final DateOption filter) {
        dateFilterContainer().toggleFilter(filter);
    }

    public DatePickerFilter.Filterable datePickerFilterable() {
        return dateFilterContainer();
    }

    public StringDateFilter.Filterable stringDateFilterable() {
        return dateFilterContainer();
    }

    //CHECKBOXES
    public List<FindParametricFilter> checkBoxesForParametricFieldContainer(final int i ){
        final int index = nonZeroParaFieldContainer(i);
        final ParametricFieldContainer container = parametricField(index);
        container.expand();
        return container.getFilters();
    }

    public FindParametricFilter checkboxForParametricValue(final String fieldName, final String fieldValue) {
        final WebElement checkbox = panel.findElement(By.cssSelector("[data-field-display-name='" + fieldName+"'] [data-value='" + fieldValue.toUpperCase() + "']"));
        return new FindParametricFilter(checkbox, driver);
    }

    public FindParametricFilter checkboxForParametricValue(final int fieldIndex, final int valueIndex) {
        final WebElement checkbox = panel.findElement(By.cssSelector("[data-field]:nth-of-type(" + cssifyIndex(fieldIndex) +") [data-value]:nth-of-type(" + cssifyIndex(valueIndex) + ')'));
        return new FindParametricFilter(checkbox, driver);
    }

    //EXPANDING AND COLLAPSING
    protected List<FilterContainer> allFilterContainers() {
        final List<FilterContainer> nodes = new ArrayList<>();
        nodes.add(indexesTreeContainer());
        nodes.add(dateFilterContainer());
        nodes.addAll(parametricFieldContainers());
        return nodes;
    }

    public void collapseAll() {
        for (final Collapsible collapsible : allFilterContainers()) {
            collapsible.collapse();
        }
    }

    //OTHER
    public String getErrorMessage() {
        return panel.findElement(By.cssSelector("p:not(.hide)")).getText();
    }

    public boolean noParametricFields() { return !panel.findElements(By.cssSelector(".parametric-empty:not(.hide)")).isEmpty(); }

    public void waitForParametricFields() {
        Container.LEFT.waitForLoad(driver);
    }

    protected WebElement getPanel(){
        return panel;
    }
}
