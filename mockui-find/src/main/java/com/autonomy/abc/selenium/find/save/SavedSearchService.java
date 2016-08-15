package com.autonomy.abc.selenium.find.save;

import com.autonomy.abc.selenium.find.application.IdolFind;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;
import com.autonomy.abc.selenium.find.comparison.ComparisonModal;

public class SavedSearchService {
    private final IdolFindElementFactory elementFactory;

    public SavedSearchService(final IdolFind find) {
        elementFactory = find.elementFactory();
    }

    public void saveCurrentAs(final String searchName, final SearchType type){
        nameSavedSearch(searchName,type).confirmSave();
    }

    public void renameCurrentAs(final String newSearchName) {
        final SearchOptionsBar optionsBar = elementFactory.getSearchOptionsBar();
        optionsBar.renameButton().click();
        optionsBar.searchTitleInput().setValue(newSearchName);
        optionsBar.confirmSave();
    }

    public SearchOptionsBar nameSavedSearch(final String searchName,final SearchType type){
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.saveAsButton(type).click();
        options.searchTitleInput().setValue(searchName);
        return options;
    }

    public void openNewTab() {
        elementFactory.getSearchTabBar().newTabButton().click();
        //elementFactory.getResultsPage().waitForResultsToLoad();
        elementFactory.getTopicMap().waitForMapLoaded();
    }

    public void deleteAll() {
        for (final SearchTab tab : elementFactory.getSearchTabBar()) {
            tab.activate();
            deleteCurrentSearch();
        }
    }

    public void deleteCurrentSearch() {
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.openDeleteModal();
        options.confirmDelete();
    }

    public void compareCurrentWith(final String savedSearchName) {
        final ComparisonModal modal = elementFactory.getFindPage().openCompareModal();
        modal.select(savedSearchName);
        modal.compareButton().click();
        modal.waitForComparisonToLoad();
    }
}
