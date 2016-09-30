package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.queryHelper.IdolQueryTestHelper;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.shared.QueryTestHelper;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.categories.CoreFeature;
import com.hp.autonomy.frontend.selenium.framework.environment.Deployment;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.apache.commons.collections4.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.WebDriverException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class QueryTermsITCase extends FindTestBase {
    private FindPage findPage;
    private FindService findService;

    public QueryTermsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
    }

    @Test
    @Category(CoreFeature.class)
    @Role(UserRole.FIND)
    public void searchOnSimpleTerms() throws InterruptedException {
        goToListView();

        final String searchTerm = "Fred is a chimpanzee";
        final ResultsView results = findService.search(searchTerm);
        assertThat(getElementFactory().getTopNavBar().getSearchBoxTerm(), is(searchTerm));
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
        assertThat(getElementFactory().getConceptsPanel().selectedConceptHeaders(), empty());
    }

    @Test
    @Category(CoreFeature.class)
    @Role(UserRole.FIND)
    public void searchForAll() {
        goToListView();

        final String searchTerm = "*";
        final ResultsView results = findService.search(searchTerm);
        assertThat(getElementFactory().getTopNavBar().getSearchBoxTerm(), is(searchTerm));
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
        assertThat(getElementFactory().getConceptsPanel().selectedConceptHeaders(), empty());
    }

    @Test
    @Category(CoreFeature.class)
    @Role(UserRole.BIFHI)
    public void searchOnSimpleConcepts() throws InterruptedException {
        goToListView();

        final String searchTerm = "chimpanzee";
        final ResultsView results = findService.search(searchTerm);
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
        assertThat(getElementFactory().getConceptsPanel().selectedConceptHeaders(), contains(searchTerm));
    }

    @Test
    @Category(CoreFeature.class)
    @Role(UserRole.BIFHI)
    public void implicitSearchForAll() throws InterruptedException {
        goToListView();

        assertThat(getElementFactory().getResultsPage().getResults(), not(empty()));
        findService.search("*");
        assertThat(getElementFactory().getConceptsPanel().selectedConceptHeaders(), empty());
    }

    @Test
    //TODO: test is bad because depends on having less than 30 of both results
    public void testBooleanOperators() {
        goToListView();

        LOGGER.info("Test only works if query terms both have <=30 results ");

        final String termOne = "brevity";

        final ResultsView results = findService.search(termOne);
        final List<String> musketeersSearchResults = results.getResultTitles();
        final int numberOfMusketeersResults = musketeersSearchResults.size();
        removeAllConcepts();

        final String termTwo = "\"dearly departed\"";

        findService.search(termTwo);
        final List<String> dearlyDepartedSearchResults = results.getResultTitles();
        final int numberOfDearlyDepartedResults = dearlyDepartedSearchResults.size();
        removeAllConcepts();

        findService.search(termOne + " AND " + termTwo);
        final List<String> andResults = results.getResultTitles();
        final int numberOfAndResults = andResults.size();

        assertThat(numberOfMusketeersResults, greaterThanOrEqualTo(numberOfAndResults));
        assertThat(numberOfDearlyDepartedResults, greaterThanOrEqualTo(numberOfAndResults));
        final String[] andResultsArray = andResults.toArray(new String[andResults.size()]);
        assertThat(musketeersSearchResults, hasItems(andResultsArray));
        assertThat(dearlyDepartedSearchResults, hasItems(andResultsArray));
        removeAllConcepts();

        findService.search(termOne + " OR " + termTwo);
        final List<String> orResults = results.getResultTitles();
        final Set<String> concatenatedResults = new HashSet<>(ListUtils.union(musketeersSearchResults, dearlyDepartedSearchResults));
        assertThat(orResults.size(), is(concatenatedResults.size()));
        assertThat(orResults, containsInAnyOrder(concatenatedResults.toArray()));
        removeAllConcepts();

        findService.search(termOne + " XOR " + termTwo);
        final List<String> xorResults = results.getResultTitles();
        concatenatedResults.removeAll(andResults);
        assertThat(xorResults.size(), is(concatenatedResults.size()));
        assertThat(xorResults, containsInAnyOrder(concatenatedResults.toArray()));
        removeAllConcepts();

        findService.search(termOne + " NOT " + termTwo);
        final List<String> notTermTwo = results.getResultTitles();
        final Set<String> t1NotT2 = new HashSet<>(concatenatedResults);
        t1NotT2.removeAll(dearlyDepartedSearchResults);
        assertThat(notTermTwo.size(), is(t1NotT2.size()));
        assertThat(notTermTwo, containsInAnyOrder(t1NotT2.toArray()));
        removeAllConcepts();

        findService.search(termTwo + " NOT " + termOne);
        final List<String> notTermOne = results.getResultTitles();
        final Set<String> t2NotT1 = new HashSet<>(concatenatedResults);
        t2NotT1.removeAll(musketeersSearchResults);
        assertThat(notTermOne.size(), is(t2NotT1.size()));
        assertThat(notTermOne, containsInAnyOrder(t2NotT1.toArray()));
        removeAllConcepts();
    }

    @Test
    @ActiveBug(value = "CORE-2925", type = ApplicationType.ON_PREM, against = Deployment.DEVELOP)
    public void testCorrectErrorMessageDisplayed() {
        goToListView();

        new QueryTestHelper<>(findService)
                .booleanOperatorQueryText(Errors.Search.OPERATORS, Errors.Search.OPENING_BOOL, Errors.Search.GENERIC_HOSTED_ERROR);
        new QueryTestHelper<>(findService)
                .emptyQueryText(Errors.Search.STOPWORDS, Errors.Search.NO_TEXT,Errors.Search.GENERIC_HOSTED_ERROR, Errors.Search.HOSTED_INVALID);
    }

    @Test
    @ResolvedBug("FIND-151")
    public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
        new IdolQueryTestHelper<ResultsView>(findService).hiddenQueryOperatorText();
    }

    @Test
    public void testSearchParentheses() {
        findService.search("Remove splash page");
        //noinspection AnonymousInnerClassWithTooManyMethods
        new QueryTestHelper<>(new QueryService<QueryResultsPage>() {
            @Override
            public QueryResultsPage search(final String s) {
                return search(new Query(s));
            }

            @Override
            public QueryResultsPage search(final Query query) {
                removeAllConcepts();
                return findService.search(query);
            }
        }).mismatchedBracketQueryText();
    }

    @Test
    @ResolvedBug("HOD-2170")
    @ActiveBug("CCUK-3634")
    public void testSearchQuotationMarks() {
        new QueryTestHelper<>(findService).mismatchedQuoteQueryText(Errors.Search.QUOTES);
    }

    @Test
    @ActiveBug(value = "CCUK-3700", type = ApplicationType.ON_PREM)
    public void testWhitespaceSearch() {
        goToListView();

        try {
            findService.search("       ");
        } catch (final WebDriverException ignored) { /* Expected behaviour */ }

        assertThat(findPage.footerLogo(), displayed());

        final ResultsView results = findService.search("Kevin Costner");

        final List<String> resultTitles = results.getResultTitles();

        findService.search(" ");
        assertThat(results.getResultTitles(), is(resultTitles));
        assertThat(findPage.parametricContainer().getText(), not(isEmptyOrNullString()));
        assertThat(findPage.parametricContainer().getText(), not(containsString("No parametric fields found")));
    }

    @Test
    @ResolvedBug("CCUK-3624")
    @Role(UserRole.FIND)
    public void testRefreshEmptyQuery() throws InterruptedException {
        goToListView();

        findService.search("something");
        findService.search("");
        Thread.sleep(5000);

        getWindow().refresh();
        findPage = getElementFactory().getFindPage();

        verifyThat(getElementFactory().getTopNavBar().getSearchBoxTerm(), is(""));
        verifyThat("taken back to landing page after refresh", findPage.footerLogo(), displayed());
    }

    private void goToListView() {
        if (!findPage.footerLogo().isDisplayed()) {
            ((IdolFindPage) findPage).goToListView();
        }
    }

    private void removeAllConcepts() {
        getElementFactory().getConceptsPanel().removeAllConcepts();
    }

}
