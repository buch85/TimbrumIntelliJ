package it.maverick.workday;

import java.util.Date;

/**
 * Created by Pasquale on 10/07/2014.
 */
public class Interval {

    private Date startTime;
    private Date endTime;

    public Interval(Date startTime, Date endTime) {
        if (startTime.after(endTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getDuration() {
        return endTime.getTime() - startTime.getTime();
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

}
