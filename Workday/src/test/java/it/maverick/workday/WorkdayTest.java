package it.maverick.workday;

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;

public class WorkdayTest {

    private static final int MILLISECONDS = 1000;
    private static final int SECONDS_IN_A_MINUTE = 60;
    private static final int MINUTES_IN_AN_HOUR = 60;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private MyClock myClock = new MyClock();
    private Workday workday;

    @Before
    public void setUp() throws Exception {
        workday = new Workday(myClock, 8 * MINUTES_IN_AN_HOUR);
        workday.setLunchBreak(12, 30, 14, 0, 30);
        workday.setMinimumBreak(15);
    }

    @Test
    public void testWorkedTimeWithOneClockingIn14MinEarly() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 8:24").getTime());

        assertEquals(convertInMilliSeconds(0, 14), workday.getWorkedTime());
    }

    @Test
    public void testWorkedTimeWithOneClockingIn37MinEarly() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 8:47").getTime());

        assertEquals(convertInMilliSeconds(0, 37), workday.getWorkedTime());
    }

    @Test
    public void testWorkedTimeWithOneClockingInAndOneClockingOut() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 9:15"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 9:47").getTime());

        assertEquals(convertInMilliSeconds(1, 5), workday.getWorkedTime());
    }

    @Test
    public void testWorkedTimeWithTwoClockingInAndTwoClockingOut() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 9:15"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 9:30"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 9:45"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 9:47").getTime());

        assertEquals(convertInMilliSeconds(1, 20), workday.getWorkedTime());
    }

    @Test
    public void testWorkedTimeWithTwoClockingInAndOneClockingOut() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 9:15"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 9:30"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 9:47").getTime());

        assertEquals(convertInMilliSeconds(1, 22), workday.getWorkedTime());
    }

    @Test
    public void testRemainingTimeWithOneClockingIn() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 9:08").getTime());

        assertEquals(convertInMilliSeconds(7, 2), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithOneClockingIn2() throws Exception {
        workday = new Workday(myClock, 60 * 7);
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 9:08").getTime());

        assertEquals(convertInMilliSeconds(6, 2), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithOneClockingInAndOneClockingOut() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 9:10"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 9:40").getTime());

        assertEquals(convertInMilliSeconds(7, 0), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithTwoClockingInAndOneClockingOut() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 9:10"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 9:40"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 11:40").getTime());

        assertEquals(convertInMilliSeconds(5, 0), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithExactlyValidLunchBreak() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:10"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 13:00"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 13:30"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 14:40").getTime());

        assertEquals(convertInMilliSeconds(6, 0), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(2, 0), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithValidLunchBreak() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:00"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 13:00"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 13:40"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 14:50").getTime());

        assertEquals(convertInMilliSeconds(6, 10), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(1, 50), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithShortLunchBreak() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:00"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 13:00"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 13:15"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 15:30").getTime());

        assertEquals(convertInMilliSeconds(7, 15), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(1, 0), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithEarlyShortLunchBreak() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:00"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 12:20"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 12:50"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 16:30").getTime());

        assertEquals(convertInMilliSeconds(8, 0), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(0, 15), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithEarlyLunchBreak() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:00"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 12:20"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 13:00"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 16:30").getTime());

        assertEquals(convertInMilliSeconds(7, 50), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(0, 15), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithShortDelayedLunchBreak() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:00"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 13:40"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 14:10"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 16:30").getTime());

        assertEquals(convertInMilliSeconds(8, 0), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(0, 15), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithDelayedLunchBreak() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:00"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 13:30"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 14:10"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 16:30").getTime());

        assertEquals(convertInMilliSeconds(7, 50), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(0, 15), workday.getRemainingTime());
    }

    @Test
    public void testRemainingTimeWithLongLunchBreak() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:00"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 12:20"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 14:10"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 17:50").getTime());

        assertEquals(convertInMilliSeconds(8, 0), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(0, 10), workday.getRemainingTime());
    }


    @Test
    public void testRoundedBreak() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:00"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 9:00"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 9:05"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 10:00").getTime());

        assertEquals(convertInMilliSeconds(1, 55), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(6, 15), workday.getRemainingTime());
    }

    @Test
    public void testRoundedBreak2() throws Exception {
        workday.addClockingIn(dateFormat.parse("2010-07-10 8:00"));
        workday.addClockingOut(dateFormat.parse("2010-07-10 9:00"));
        workday.addClockingIn(dateFormat.parse("2010-07-10 9:20"));
        myClock.setTimeNow(dateFormat.parse("2010-07-10 10:00").getTime());

        assertEquals(convertInMilliSeconds(1, 40), workday.getWorkedTime());
        assertEquals(convertInMilliSeconds(6, 30), workday.getRemainingTime());
    }

    private long convertInMilliSeconds(int hours, int minutes) {
        return (hours * MINUTES_IN_AN_HOUR + minutes) * SECONDS_IN_A_MINUTE * MILLISECONDS;
    }

    private class MyClock implements Clock {

        private long timeNow;

        @Override
        public long getTimeNow() {
            return timeNow;
        }

        public void setTimeNow(long timeNow) {
            this.timeNow = timeNow;
        }
    }
}