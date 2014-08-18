package it.maverick.workday;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class IntervalTest {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Test
    public void testGetDuration() throws Exception {
        Date startTime = dateFormat.parse("2014-07-10 8:50");
        Date endTime = dateFormat.parse("2014-07-10 9:05");
        Interval interval = new Interval(startTime, endTime);

        assertEquals(15 * 60 * 1000, interval.getDuration());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionWhenWrongParameters() throws Exception {
        Date startTime = dateFormat.parse("2014-07-10 9:05");
        Date endTime = dateFormat.parse("2014-07-10 8:50");
        new Interval(startTime, endTime);
    }
}