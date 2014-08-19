package it.maverick.workday;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Pasquale on 10/07/2014.
 */
public class Workday {

    public static final int MILLISECONDS = 1000;
    public static final int SECONDS_IN_A_MINUTE = 60;
    public static final int MINUTES_IN_AN_HOUR = 60;
    public static final int HOURS_IN_A_DAY = 24;

    private Clock clock;
    private long workingDayMillis;
    private final List<Clocking> clockings = new ArrayList<Clocking>();
    private int startLunchH;
    private int startLunchM;
    private int stopLunchH;
    private int stopLunchM;
    private int minimumLunchMillis;
    private int minimumBreakMillis;

    public Workday(Clock clock, int workingDayMins) {
        if (workingDayMins < 0 || workingDayMins > MINUTES_IN_AN_HOUR * HOURS_IN_A_DAY) {
            throw new IllegalArgumentException("workingDayMins must be included between 0 and 1440 (24h)");
        }
        this.clock = clock;
        this.workingDayMillis = workingDayMins * SECONDS_IN_A_MINUTE * MILLISECONDS;
    }

    public void addClockingIn(Date clockingIn) throws InvalidClockingSequenceException {
        if (!clockings.isEmpty() && clockings.get(clockings.size() - 1).getDirection() == Clocking.Direction.IN) {
            throw new InvalidClockingSequenceException("addClockingIn can not called after a clocking in");
        }
        clockings.add(new Clocking(clockingIn, Clocking.Direction.IN));
    }

    public void addClockingOut(Date clockingOut) throws InvalidClockingSequenceException {
        if (clockings.isEmpty()) {
            throw new InvalidClockingSequenceException("Working day can not start with a clocking out");
        } else if (clockings.get(clockings.size() - 1).getDirection() == Clocking.Direction.OUT) {
            throw new InvalidClockingSequenceException("addClockingOut can not called after a clocking out");
        }
        clockings.add(new Clocking(clockingOut, Clocking.Direction.OUT));
    }

    public long getWorkedTime() {
        long workedTime = 0;
        for (Interval interval : getWorkIntervals()) {
            workedTime += interval.getDuration();
        }
        return workedTime;
    }

    public long getRemainingTime() {
        Clocking clockingIn = clockings.get(0);
        Clocking clockingOut;
        if (isClockingOutPending()) {
            clockingOut = createNowClocking();
        } else {
            clockingOut = clockings.get(clockings.size() - 1);
        }
        long remainingTime = workingDayMillis - (clockingOut.getDate().getTime() - clockingIn.getDate().getTime());
        for (Interval interval : getBreakIntervals()) {
            remainingTime += interval.getDuration();
        }
        return remainingTime;
    }

    private Clocking createNowClocking() {
        return new Clocking(new Date(clock.getTimeNow()), Clocking.Direction.OUT);
    }

    private boolean isClockingOutPending() {
        return clockings.get(clockings.size() - 1).getDirection() == Clocking.Direction.IN;
    }

    private Date getValidStartLunchBreak() {
        Calendar startLunchCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+00"));
        startLunchCalendar.setTimeInMillis(clock.getTimeNow());
        startLunchCalendar.set(Calendar.HOUR_OF_DAY, startLunchH);
        startLunchCalendar.set(Calendar.MINUTE, startLunchM);
        return startLunchCalendar.getTime();
    }

    private Date getValidStopLunchBreak() {
        Calendar stopLunchCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+00"));
        stopLunchCalendar.setTimeInMillis(clock.getTimeNow());
        stopLunchCalendar.set(Calendar.HOUR_OF_DAY, stopLunchH);
        stopLunchCalendar.set(Calendar.MINUTE, stopLunchM);
        return stopLunchCalendar.getTime();
    }

    private List<Interval> getBreakIntervals() {
        List<Interval> breakIntervals = new ArrayList<Interval>();
        for (int i = 1; i < clockings.size() - 1; i = i + 2) {
            Date startTime = clockings.get(i).getDate();
            Date endTime = clockings.get(i + 1).getDate();

            if (isRegularLunchBreak(startTime, endTime)) {
                breakIntervals.add(new Interval(startTime, endTime));
            } else if (isShortLunchBreak(startTime, endTime)) {
                breakIntervals.add(getRoundedLunchBreakInterval(startTime, endTime));
            } else if (isEarlyLunchBreak(startTime, endTime)) {
                breakIntervals.add(getRoundedBreakInterval(startTime, getValidStartLunchBreak()));
                breakIntervals.add(getRoundedLunchBreakInterval(getValidStartLunchBreak(), endTime));
            } else if (isDelayedLunchBreak(startTime, endTime)) {
                breakIntervals.add(getRoundedLunchBreakInterval(startTime, getValidStopLunchBreak()));
                breakIntervals.add(getRoundedBreakInterval(getValidStopLunchBreak(), endTime));
            } else if (isLongLunchBreak(startTime, endTime)) {
                breakIntervals.add(getRoundedBreakInterval(startTime, getValidStartLunchBreak()));
                breakIntervals.add(getRoundedLunchBreakInterval(getValidStartLunchBreak(), getValidStopLunchBreak()));
                breakIntervals.add(getRoundedBreakInterval(getValidStopLunchBreak(), endTime));
            } else {
                breakIntervals.add(getRoundedBreakInterval(startTime, endTime));
            }
        }
        return breakIntervals;
    }

    private Interval getRoundedLunchBreakInterval(Date startTime, Date endTime) {
        Date endInterval;
        if (endTime.getTime() - startTime.getTime() >= minimumLunchMillis) {
            endInterval = endTime;
        } else {
            endInterval = new Date(startTime.getTime() + minimumLunchMillis);
        }
        return new Interval(startTime, endInterval);
    }

    private Interval getRoundedBreakInterval(Date startTime, Date endTime) {
        if (minimumBreakMillis > 0) {
            long intervalDuration = endTime.getTime() - startTime.getTime();
            long multiplier = intervalDuration / minimumBreakMillis;
            long breakTime = multiplier * minimumBreakMillis;
            if (intervalDuration % minimumBreakMillis != 0) {
                breakTime += minimumBreakMillis;
            }
            return new Interval(startTime, new Date(startTime.getTime() + breakTime));
        } else {
            return new Interval(startTime, endTime);
        }
    }

    private boolean isRegularLunchBreak(Date startTime, Date endTime) {
        return startTime.after(getValidStartLunchBreak()) && endTime.before(getValidStopLunchBreak()) && (endTime.getTime() - startTime.getTime()) >= minimumLunchMillis;
    }

    private boolean isShortLunchBreak(Date startTime, Date endTime) {
        return startTime.after(getValidStartLunchBreak()) && endTime.before(getValidStopLunchBreak()) && (endTime.getTime() - startTime.getTime()) < minimumLunchMillis;
    }

    private boolean isEarlyLunchBreak(Date startTime, Date endTime) {
        return startTime.before(getValidStartLunchBreak()) && endTime.after(getValidStartLunchBreak()) && endTime.before(getValidStopLunchBreak());
    }

    private boolean isDelayedLunchBreak(Date startTime, Date endTime) {
        return startTime.after(getValidStartLunchBreak()) && startTime.before(getValidStopLunchBreak()) && endTime.after(getValidStopLunchBreak());
    }

    private boolean isLongLunchBreak(Date startTime, Date endTime) {
        return startTime.before(getValidStartLunchBreak()) && endTime.after(getValidStopLunchBreak());
    }

    private List<Interval> getWorkIntervals() {
        List<Interval> workIntervals = new ArrayList<Interval>();
        for (int i = 0; i < clockings.size(); i = i + 2) {
            Date startTime = clockings.get(i).getDate();
            Date endTime = i + 1 <= clockings.size() - 1 ? clockings.get(i + 1).getDate() : createNowClocking().getDate();
            workIntervals.add(new Interval(startTime, endTime));
        }
        return workIntervals;
    }

    public void setLunchBreak(int startH, int startM, int stopH, int stopM, int minimumMin) {
        if (startH < 0 || startH >= HOURS_IN_A_DAY || stopH < 0 || stopH >= HOURS_IN_A_DAY) {
            throw new IllegalArgumentException("Hours must be included between 0 and 23");
        }
        if (startM < 0 || startM >= MINUTES_IN_AN_HOUR || stopM < 0 || stopM >= MINUTES_IN_AN_HOUR || minimumMin < 0 || minimumMin >= MINUTES_IN_AN_HOUR) {
            throw new IllegalArgumentException("Minutes must be included between 0 and 59");
        }
        this.startLunchH = startH;
        this.startLunchM = startM;
        this.stopLunchH = stopH;
        this.stopLunchM = stopM;
        this.minimumLunchMillis = minimumMin * SECONDS_IN_A_MINUTE * MILLISECONDS;
    }

    public void setMinimumBreak(int minimumBreakMinutes) {
        if (minimumBreakMinutes < 0 || minimumBreakMinutes + workingDayMillis / MILLISECONDS / SECONDS_IN_A_MINUTE > HOURS_IN_A_DAY * MINUTES_IN_AN_HOUR) {
            throw new IllegalArgumentException("minimumBreakMinutes must be included between 0 and 24h - working day duration)");
        }
        this.minimumBreakMillis = minimumBreakMinutes * SECONDS_IN_A_MINUTE * MILLISECONDS;
    }
}
