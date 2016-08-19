package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.iso.IdolIsoElementFactory;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.WebElement;

import java.util.Date;

public class SchedulePromotionService extends ServiceBase<IdolIsoElementFactory> {
    private SchedulePage schedulePage;

    public SchedulePromotionService(final IsoApplication<? extends IdolIsoElementFactory> application) {
        super(application);
    }

    //starting from promotion details
    public SchedulePage goToSchedule(){
        final IdolPromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        promotionsDetailPage.schedulePromotion();
        return getElementFactory().getSchedulePage();
    }

    public SchedulePage schedulePage(){return getElementFactory().getSchedulePage();}

    public void navigateToScheduleDuration(){
        schedulePage = schedulePage();
        schedulePage.schedule().click();
        schedulePage.continueButton().click();
        Waits.loadOrFadeWait();
    }

    public void schedulePromotion(final Date startDate, final Date endDate, final SchedulePage.Frequency frequency, final Date finalDate) {

        navigateToScheduleRecurrence(startDate,endDate,frequency);
        scheduleDurationSelector(schedulePage.finalDateCalendar(),finalDate);

        schedulePage.finishButton().click();
        Waits.loadOrFadeWait();
    }

    public void schedulePromotion(final Date startDate, final Date endDate, final SchedulePage.Frequency frequency) {
        navigateToScheduleRecurrence(startDate,endDate,frequency);
        schedulePage.never().click();
        schedulePage.finishButton().click();
        Waits.loadOrFadeWait();
    }

    private void navigateToScheduleRecurrence(final Date startDate, final Date endDate, final SchedulePage.Frequency frequency){
        navigateToScheduleDuration();

        scheduleDurationSelector(schedulePage.startDateCalendar(),startDate);
        scheduleDurationSelector(schedulePage.endDateCalendar(),endDate);

        schedulePage.continueButton().click();
        Waits.loadOrFadeWait();

        schedulePage.repeatWithFrequencyBelow().click();
        schedulePage.selectFrequency(frequency);
        schedulePage.continueButton().click();
        Waits.loadOrFadeWait();
    }

    public void scheduleDurationSelector(final WebElement calendarButton, final Date date){
        final DatePicker datePicker = openDatePicker(calendarButton);
        datePicker.calendarDateSelect(date);
        calendarButton.click();
    }

    public DatePicker openDatePicker(final WebElement calendarButton){
        schedulePage = schedulePage();
        calendarButton.click();
        return new DatePicker(schedulePage,getDriver());
    }

    public void resetDateToToday(){
        final DatePicker datePicker = new DatePicker(schedulePage,getDriver());
        datePicker.open();
        datePicker.resetDateToToday();
    }

    public void setStartDate(final int daysFromNow){
        scheduleDurationSelector(schedulePage.startDateCalendar(), DateUtils.addDays(schedulePage.getTodayDate(), daysFromNow));
    }

    public void setEndDate(final int daysFromNow){
        scheduleDurationSelector(schedulePage.endDateCalendar(),DateUtils.addDays(schedulePage.getTodayDate(),daysFromNow));
    }

    public void navigateWizardAndSetEndDate(final Date endDate) {
        navigateToScheduleDuration();

        scheduleDurationSelector(schedulePage.endDateCalendar(),endDate);

        schedulePage.continueButton().click();
        Waits.loadOrFadeWait();
        schedulePage.repeatWithFrequencyBelow().click();
    }

    public enum WizardStep {
        ENABLE_SCHEDULE("enableSchedule"),
        START_END("scheduleStartEnd"),
        FREQUENCY("scheduleFrequency"),
        FINAL("scheduleEndRecurrence");

        private final String title;

        WizardStep(final String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }



}


