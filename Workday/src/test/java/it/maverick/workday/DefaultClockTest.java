package it.maverick.workday;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DefaultClockTest {

    @Test
    public void testGetTimeNow() throws Exception {
        Clock clock = new DefaultClock();
        long date1 = clock.getTimeNow();
        Thread.sleep(1);
        long date2 = clock.getTimeNow();
        assertTrue(date1 < date2);
    }
}