package it.maverick.workday;

import java.util.Date;

/**
 * Created by Pasquale on 10/07/2014.
 */
public class DefaultClock implements Clock {

    @Override
    public long getTimeNow() {
        return new Date().getTime();
    }
}
