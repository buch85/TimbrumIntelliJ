package it.buch85.timbrum;

import java.util.ArrayList;

/**
 * Created by Marco on 19/08/2014.
 */
public class Report {
    private ArrayList<RecordTimbratura> timbrature;

    public Report(ArrayList<RecordTimbratura> timbrature) {
        this.timbrature = timbrature;
    }

    public ArrayList<RecordTimbratura> getTimbrature() {
        return timbrature;
    }

    public boolean isNextTimbrumValid(VersoTimbratura direction) {
        return !doubleTimbrum(direction) && !exitAsFirstTimbrum(direction);
    }

    private boolean doubleTimbrum(VersoTimbratura direction) {
        if (timbrature.isEmpty()) {
            return false;
        } else {
            RecordTimbratura recordTimbratura = timbrature.get(timbrature.size() - 1);
            return recordTimbratura.getDirection().equals(direction);
        }
    }

    private boolean exitAsFirstTimbrum(VersoTimbratura direction) {
        return timbrature.isEmpty() && VersoTimbratura.USCITA.equals(direction);
    }
}
