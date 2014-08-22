package it.buch85.timbrum;

import java.util.Date;

import it.maverick.workday.Workday;

/**
 * Created by Marco on 19/08/2014.
 */
public interface TimbrumView {
    boolean askTimbrumConfirmation(VersoTimbratura direction);

    void initProgress();

    void updateProgress(Integer resourceId);

    void dismissProgress();

    void resetView();

    void updateView(Report result);

    void updateView(Report result, Workday workday);

    void setErrorMessage(String message);

    void setNow(Date now);

    void showErrorMessage();

    void setLoginError(String message);
}
