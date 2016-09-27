/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.ToolTips;
import com.autonomy.abc.selenium.find.filters.*;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryResult;
import com.autonomy.abc.selenium.query.StringDateFilter;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.text.WordUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.*;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class FilterITCase extends FindTestBase {
    private FindPage findPage;
    private FindService findService;
    private FilterPanel filterPanel;
    private AppliedFiltersPanel appliedFiltersPanel;

    public FilterITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
        appliedFiltersPanel = getElementFactory().getAppliedFiltersPanel();
        filterPanel = getElementFactory().getFilterPanel();
        if(!findPage.footerLogo().isDisplayed()) {
            ((IdolFindPage)findPage).goToListView();
        }
    }

    private ResultsView searchAndWait(final String searchTerm) {
        final ResultsView results = findService.search(searchTerm);
        findPage.waitForParametricValuesToLoad();
        results.waitForResultsToLoad();
        return results;
    }

    @Test
    public void testParametricFiltersDefaultCollapsed() {
        searchAndWait("knee");

        for(ParametricFieldContainer container : filterPanel.parametricFieldContainers()) {
            verifyThat("Container is collapsed", container.isCollapsed());
        }
    }

    @Test
    public void testParametricFiltersResults() {
        ResultsView results = searchAndWait("*");

        List<ParametricFieldContainer> containers = filterPanel.parametricFieldContainers();
        for(ParametricFieldContainer container : containers) {
            container.expand();
            int numberFields = container.getFilters().size();
            verifyThat("Field values: " + numberFields + " - less than or equal to 5", numberFields, lessThanOrEqualTo(5));
        }

        final ParametricFieldContainer secondContainer = filterPanel.parametricField(1);
        secondContainer.expand();
        final FindParametricFilter secondField = secondContainer.getFilters().get(1);
        final String filterName = secondField.getName();
        final int expectedResults = secondField.getResultsCount();

        final int originalNumberOfResults = findPage.totalResultsNum();
        assumeThat("Fewer results predicted w/ this filter", expectedResults, lessThan(originalNumberOfResults));

        secondField.check();
        results.waitForResultsToLoad();

        verifyThat("Expected number of results (according to panel) equals actual number of results",
                   results.getResultsCount(), is(expectedResults));

        try {
            secondContainer.getFilters();
            fail("Filter panel did not reload after filter selection");
        } catch(Exception e) {
            LOGGER.info("Correctly threw exception as filter panel has reloaded");
        }

        Waits.loadOrFadeWait();
        final ParametricFieldContainer container = filterPanel.parametricContainerOfFilter(filterName);
        final String filterNumber = container.getFilterNumber();
        final String filterCategory = container.filterCategoryName();

        container.seeAll();
        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        verifyThat("Filter category title shows the number of filters chosen from total", filterNumber, is("1 / " + filterModal.filtersWithResultsForCurrentSearch()));

        filterPanel.checkboxForParametricValue(WordUtils.capitalize(filterCategory.toLowerCase()), filterName).uncheck();
        findPage.waitForParametricValuesToLoad();
        int totalParametricFields = filterPanel.parametricFieldContainers().size();

        searchAndWait("shouldhavenoresultsprobably");
        findPage.originalQuery().click();
        findPage.waitForParametricValuesToLoad();
        int noResultsParametricFields = filterPanel.parametricFieldContainers().size();

        verifyThat("Filters changed: no results -> parametric fields remain", noResultsParametricFields, is(totalParametricFields));
    }

    @Test
    @ActiveBug("FIND-463")
    public void testFilterPanelAndModalLinked() {
        searchAndWait("cats");

        //TODO: when everyone has same data, make test select across several filter categories
        final ParametricFieldContainer container = filterPanel.parametricField(1);
        final String filterCategory = container.filterCategoryName();
        FindParametricFilter checkbox = filterPanel.checkboxForParametricValue(1, 1);
        final List<String> selectedFilter = Arrays.asList(checkbox.getName());
        checkbox.check();

        container.seeAll();
        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        //TODO: once loading forever bug fixed check that this passes -> may need to do a time-out w/ message
        assertThat("Modal not loading forever", !filterModal.loadingIndicatorPresent());
        verifyThat("Correct tab is active", filterModal.activeTabName(), equalToIgnoringCase(filterCategory));
        verifyThat("Same fields selected in modal as panel", filterModal.checkedFiltersAllPanes(), is(selectedFilter));

        final String filterType = filterModal.activeTabName();
        final String checkedFilterName = filterModal.checkCheckBoxInActivePane(0);
        filterModal.apply();

        final FindParametricFilter panelBox = filterPanel.checkboxForParametricValue(filterType, checkedFilterName);
        verifyThat("Filter: " + checkedFilterName + " is now checked on panel", panelBox.isChecked());
    }

    @Test
    @ActiveBug("FIND-406")
    @ResolvedBug("FIND-242")
    public void testModalShowsALLFiltersRegardlessOfQuery() {
        searchAndWait("*");
        List<String> allFilterCategories = new ArrayList<>();
        for(ParametricFieldContainer container : filterPanel.parametricFieldContainers()) {
            allFilterCategories.add(container.filterCategoryName());
        }

        filterPanel.parametricField(0).seeAll();
        ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        final int totalNumberFilters = filterModal.allFilters().size();

        filterModal.goToTab(1);
        if(!isHosted()) {
            LOGGER.info("Test works on basis that Category should have has >10 filters");
            verifyThat(">10 filters shown in modal", filterModal.activePaneFilterList(), hasSize(greaterThan(10)));
        }
        filterModal.cancel();

        filterPanel.checkboxForParametricValue(0, 1).check();

        filterPanel.waitForParametricFields();
        filterPanel.parametricField(0).seeAll();
        filterModal = ParametricFilterModal.getParametricModal(getDriver());

        assertThat("Modal shows all filter categories", filterModal.tabs(), hasSize(allFilterCategories.size()));
        verifyThat("Shows all filters for restricted search (some filters may have 0 docs)"
                , filterModal.allFilters()
                , hasSize(totalNumberFilters));

        filterModal.cancel();
    }

    @Test
    @ResolvedBug("FIND-231")
    public void testDeselectingFiltersNoFloatingTooltips() {
        searchAndWait("home");

        final List<FindParametricFilter> boxes = checkAllVisibleFiltersInFirstParametrics();
        for(final FindParametricFilter checkbox : boxes) {
            checkbox.name().click();
        }

        Waits.loadOrFadeWait();
        verifyThat("Tooltips aren't floating everywhere", ToolTips.toolTips(getDriver()), not(hasSize(boxes.size())));
    }

    private List<FindParametricFilter> checkAllVisibleFiltersInFirstParametrics() {
        filterPanel.parametricField(0).expand();
        final List<FindParametricFilter> boxes = filterPanel.checkBoxesForParametricFieldContainer(0);
        for(final FindParametricFilter checkBox : boxes) {
            checkBox.check();
        }
        return boxes;
    }

    @Test
    @ResolvedBug("FIND-247")
    //Because filter categories all collapse after selecting 1, must be quick or throws NoSuchElement
    public void testSelectDifferentCategoryFiltersAndResultsLoad() throws InterruptedException {
        final ResultsView results = findService.search("face");
        final FindParametricFilter filter1 = filterPanel.checkBoxesForParametricFieldContainer(0).get(0);
        final FindParametricFilter filter2 = filterPanel.checkBoxesForParametricFieldContainer(1).get(0);

        filter1.check();
        filter2.check();

        results.waitForResultsToLoad();
        verifyThat("Loading indicator not present", !results.loadingIndicatorPresent());
    }

    @Test
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError() {
        final ResultsView results = findService.search("wolf");

        FindParametricFilter filter = filterPanel.checkBoxesForParametricFieldContainer(0).get(0);
        filter.check();
        filter.uncheck();

        results.waitForResultsToLoad();
        assertThat("No error message", !results.errorContainerShown());
    }

    @Test
    public void testFilterByIndex() {
        final ResultsView results = findService.search("face");
        final QueryResult queryResult = results.searchResult(1);
        final String titleString = queryResult.getTitleString();
        final DocumentViewer docPreview = queryResult.openDocumentPreview();

        final String index = docPreview.getIndexName();
        docPreview.close();

        findPage.filterBy(new IndexFilter(index));
        assertThat(results.searchResult(1).getTitleString(), is(titleString));
    }

    @Test
    public void testFilterByMultipleIndexes() {
        findService.search("unbelievable");

        final IndexFilter filter = new IndexFilter(filterPanel.getIndex(2));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        final int firstFilterResults = findPage.totalResultsNum();

        filter.add(filterPanel.getIndex(3));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        final int bothFilterResults = findPage.totalResultsNum();

        findPage.filterBy(new IndexFilter(filterPanel.getIndex(3)));
        final int secondFilterResults = findPage.totalResultsNum();

        assertThat("Both filter indexes thus both results", firstFilterResults + secondFilterResults, is(bothFilterResults));
    }

    @Test
    public void testFilteredByIndexOnlyHasFilesFromIndex() {
        final ResultsView results = findService.search("Better");

        final DocumentViewer docPreview = results.searchResult(1).openDocumentPreview();
        final String chosenIndex = docPreview.getIndexName();
        docPreview.close();

        findPage.filterBy(new IndexFilter(chosenIndex));
        //weirdly failing to open the 2nd result (subsequent okay)
        for(int i = 1; i < 6; i++) {
            final DocumentViewer docViewer = results.searchResult(1).openDocumentPreview();
            assertThat(docPreview.getIndexName(), is(chosenIndex));
            docViewer.close();
        }
    }

    @Test
    public void testQuickDoubleClickOnDateFilterNotCauseError() {
        final ResultsView results = findService.search("wookie");

        toggleDateSelection(DateOption.MONTH);
        toggleDateSelection(DateOption.MONTH);

        results.waitForResultsToLoad();
        assertThat(results.resultsDiv().getText().toLowerCase(), not(containsString("an error")));

    }

    @Test
    public void testPreDefinedWeekHasSameResultsAsCustomWeek() {
        preDefinedDateFiltersVersusCustomDateFilters(DateOption.WEEK);
    }

    @Test
    public void testPreDefinedMonthHasSameResultsAsCustomMonth() {
        preDefinedDateFiltersVersusCustomDateFilters(DateOption.MONTH);
    }

    @Test
    public void testPreDefinedYearHasSameResultsAsCustomYear() {
        preDefinedDateFiltersVersusCustomDateFilters(DateOption.YEAR);
    }

    private void preDefinedDateFiltersVersusCustomDateFilters(final DateOption period) {
        final ResultsView results = findService.search("*");

        toggleDateSelection(period);
        final List<String> preDefinedResults = results.getResultTitles();
        findPage.filterBy(new StringDateFilter().from(getDate(period)).until(new Date()));
        final List<String> customResults = results.getResultTitles();

        assertThat(preDefinedResults, is(customResults));
    }

    private Date getDate(final DateOption period) {
        final Calendar cal = Calendar.getInstance();

        if(period != null) {
            switch(period) {
                case WEEK:
                    cal.add(Calendar.DATE, -7);
                    break;
                case MONTH:
                    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
                    break;
                case YEAR:
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
                    break;
            }
        }
        return cal.getTime();
    }

    @Test
    @ActiveBug("FIND-152")
    public void testDateRemainsWhenClosingAndReopeningDateFilters() {
        final Date start = getDate(DateOption.MONTH);
        final Date end = getDate(DateOption.WEEK);

        findService.search(new Query("Corbyn")
                                   .withFilter(new StringDateFilter().from(start).until(end)));

        Waits.loadOrFadeWait();
        for(int unused = 0; unused < 3; unused++) {
            toggleDateSelection(DateOption.CUSTOM);
            Waits.loadOrFadeWait();
        }
        assertThat(findPage.fromDateInput().getValue(), is(findPage.formatInputDate(start)));
        assertThat(findPage.untilDateInput().getValue(), is(findPage.formatInputDate(end)));
    }

    @Test
    @ResolvedBug("CSA-1577")
    public void testClickingCustomDateFilterDoesNotRefreshResults() {
        final ResultsView results = findService.search("O Captain! My Captain!");
        // may not happen the first time
        for(int unused = 0; unused < 5; unused++) {
            toggleDateSelection(DateOption.CUSTOM);
            assertThat(results.resultsDiv().getText(), not(containsString("Loading")));
        }
    }

    private void toggleDateSelection(final DateOption date) {
        filterPanel.toggleFilter(date);
        getElementFactory().getResultsPage().waitForResultsToLoad();
    }

    // Filters Applied section
    @Test
    public void filtersAppearInFiltersAppliedSection() {
        final ResultsView results = findService.search("truth");

        List<WebElement> appliedFiltersPanel = getFilterLabels();

        assumeThat("No filter present", appliedFiltersPanel, empty());

        filterPanel.toggleFilter(DateOption.WEEK);
        results.waitForResultsToLoad();
        appliedFiltersPanel = getFilterLabels();

        assertThat("Filter label is in the Applied Filters Panel", appliedFiltersPanel, not(empty()));
    }

    private List<WebElement> getFilterLabels() {
        return this.appliedFiltersPanel
                .getPanel()
                .findElements(By.cssSelector(".filter-label"));
    }

    @Test
    public void appliedFiltersCounterDefaultsToZero() {
        final List<WebElement> appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("By default there are no filters present", appliedFilters, is(empty()));

        final String headerText = appliedFiltersPanel.getHeader().getText();
        assertThat("The header reports zero filters", headerText, endsWith("(" + appliedFilters.size() + ")"));
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void applyingFiltersIncrementsTheHeaderCounter() {
        final ResultsView results = findService.search("truth");

        List<WebElement> appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("By default there are no filters present", appliedFilters, is(empty()));

        filterPanel.toggleFilter(DateOption.WEEK);
        results.waitForResultsToLoad();

        appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("A filter label appears", appliedFilters, hasSize(1));

        String headerText = appliedFiltersPanel.getHeader().getText();
        assertThat("The header reports the number of filter labels", headerText, endsWith("(" + appliedFilters.size() + ")"));

        filterPanel.toggleFilter(DateOption.WEEK);
        results.waitForResultsToLoad();

        appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("The filter label disappears", appliedFilters, is(empty()));

        headerText = appliedFiltersPanel.getHeader().getText();
        assertThat("The header again reports no filters", headerText, endsWith("(" + appliedFilters.size() + ")"));
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void theRemoveAllFiltersButtonIsHiddenWhenThereAreNoFilters() {
        final ResultsView results = findService.search("truth");

        final WebElement removeAllButton = appliedFiltersPanel.getRemoveAllFiltersButton();
        assertThat("Button is hidden at startup", !removeAllButton.isDisplayed());

        filterPanel.toggleFilter(DateOption.WEEK);
        results.waitForResultsToLoad();
        assertThat("Button is displayed when filter is present", removeAllButton.isDisplayed());

        filterPanel.toggleFilter(DateOption.WEEK);
        results.waitForResultsToLoad();
        assertThat("Button disappears after removing filter", !removeAllButton.isDisplayed());
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void clickingTheRemoveAllButtonRemovesAllFilters() {
        final ResultsView results = findService.search("truth");
        results.waitForResultsToLoad();

        List<WebElement> appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("By default there are no filters present", appliedFilters, is(empty()));

        final int index = filterPanel.nonZeroParamFieldContainer(0);
        assumeThat("There are non-zero parametric filters", index > -1);

        final ParametricFieldContainer parametricFilterContainer = filterPanel.parametricField(index);
        parametricFilterContainer.expand();
        final FindParametricFilter parametricFilter = parametricFilterContainer.getFilters().get(0);

        parametricFilter.check();
        results.waitForResultsToLoad();

        filterPanel.toggleFilter(DateOption.YEAR);
        results.waitForResultsToLoad();

        appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("Two filters are applied", appliedFilters, hasSize(2));

        appliedFiltersPanel.getRemoveAllFiltersButton().click();
        appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("All filters are removed", appliedFilters, is(empty()));
    }
}
