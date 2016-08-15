package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.numericWidgets.NumericWidgetService;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.control.Window;

public class IdolFind extends FindApplication<IdolFindElementFactory> {
    private IdolFindElementFactory elementFactory;

    @Override
    public IdolFindElementFactory elementFactory() {
        return elementFactory;
    }

    @Override
    public boolean isHosted() {
        return false;
    }

    @Override
    public Application<IdolFindElementFactory> inWindow(final Window window) {
        this.elementFactory = new IdolFindElementFactory(window.getSession().getDriver());
        return this;
    }

    public SavedSearchService savedSearchService() {
        return new SavedSearchService(this);
    }

    public NumericWidgetService numericWidgetService() {
        return new NumericWidgetService(this);
    }
}
