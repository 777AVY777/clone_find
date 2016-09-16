package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TableView;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.ParametricFieldContainer;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.autonomy.abc.selenium.find.bi.TableView.EntryCount.TWENTY_FIVE;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

@Role(UserRole.BIFHI)
public class TableITCase extends IdolFindTestBase {

    private TableView tableView;
    private FindService findService;

    public TableITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        tableView = getElementFactory().getTableView();
        findService = getApplication().findService();
    }

    @Test
    @ResolvedBug("FIND-251")
    public void testTableTabShowsTable(){
        init("shambolic");

        verifyThat("Table element displayed", tableView.tableVisible());
        verifyThat("Parametric selectors appear", tableView.parametricSelectionDropdownsExist());
    }

    @Test
    public void testSingleFieldGivesCorrectTableValues() {
        init("dog");

        tableView.waitForTable();
        verifyThat(tableView.columnCount(), is(2));

        verifyThat(tableView.rowCount(), is(tableView.maxRow() - tableView.minRow() + 1));

        // TODO figure out correct number of rows
    }

    @Test
    public void testTwoFieldsGiveCorrectTableValues() {
        init("cat");

        tableView.waitForTable();

        tableView.parametricSelectionDropdown(2).open();
        tableView.parametricSelectionDropdown(2).selectItem(0);

        tableView.waitForTable();

        verifyThat(tableView.rowCount(), is(tableView.maxRow() - tableView.minRow() + 1));
        verifyThat(tableView.columnCount(), is(greaterThan(2)));

        // TODO figure out correct number of rows
    }

    @Test
    public void testPagination() {
        init("burning");

        tableView.waitForTable();
        assumeThat(tableView.currentPage(), is(1));

        final String initialText = tableView.text(1, 0);

        tableView.nextPage();
        verifyThat(tableView.text(1, 0), is(not(initialText)));
        verifyThat(tableView.currentPage(), is(2));

        tableView.previousPage();
        verifyThat(tableView.text(1, 0), is(initialText));
        verifyThat(tableView.currentPage(), is(1));
    }

    @Test
    public void testSorting() {
        init("waterfall");

        tableView.waitForTable();
        tableView.sort(1, TableView.SortDirection.DESCENDING);

        final int rowCount = tableView.rowCount();

        final List<Integer> values = new ArrayList<>(rowCount);

        for (int i = 1; i <= rowCount; i++) {
            values.add(Integer.parseInt(tableView.text(i, 1)));
        }

        final List<Integer> sortedValues = new ArrayList<>(values);

        // sort will give us ascending order
        Collections.sort(sortedValues);
        Collections.reverse(sortedValues);

        verifyThat(values, is(sortedValues));
    }

    @Test
    public void testSearchInResults() {
        init("whirlpool");

        tableView.waitForTable();

        final String searchText = tableView.text(2, 0);
        tableView.searchInResults(searchText);

        verifyThat(tableView.text(1, 0), is(searchText));
    }

    @Test
    public void testShowEntries() {
        init("strength");

        tableView.waitForTable();

        assumeThat(tableView.maxRow(), is(10));

        tableView.showEntries(TWENTY_FIVE);

        verifyThat(tableView.maxRow(), is(25));
    }

    @Test
    @ResolvedBug("FIND-383")
    public void testSideBarFiltersChangeTable(){
        init("lashing");

        tableView.waitForTable();

        FilterPanel filters = filters();
        final String parametricSelectionFirst= tableView.getSelectedFieldName(1);

        ParametricFieldContainer container = filters.parametricContainer(parametricSelectionFirst);
        container.expand();
        container.getFilters().get(0).check();
        
        tableView.waitForTable();
        assertThat("Parametric selection changed", tableView.getSelectedFieldName(1), not(Matchers.is(parametricSelectionFirst)));
    }

    @Test
    @ResolvedBug("FIND-405")
    public void testParametricSelectors(){
        init("wild horses");

        int index = filters().nonZeroParaFieldContainer(0);
        final String firstParametric = filters().parametricField(index).filterCategoryName();
        verifyThat("Default parametric selection is 1st parametric type", firstParametric, startsWith(tableView.getSelectedFieldName(1).toUpperCase()));

        tableView.parametricSelectionDropdown(2).open();
        verifyThat("1st selected parametric does not appear as choice in 2nd", tableView.getParametricDropdownItems(2), not(contains(firstParametric)));
    }

    private void init(final String searchText) {
        findService.search(searchText);
        getElementFactory().getFindPage().goToTable();
    }

    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }
}
