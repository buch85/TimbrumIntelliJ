package it.buch85.timbrum;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimbrumTest extends TestCase {

    public void testNow() throws Exception {
        Timbrum timbrum = new Timbrum("https://saas.hrzucchetti.it/hrpergon/", "MARCO.BACER", "regexp85");
        timbrum.login();
        Date now = timbrum.now();
        System.out.println(now.toString());
    }

    public void testDates() throws Exception {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+00"));
        calendar.setTimeInMillis(45000000);
        int startH = calendar.get(Calendar.HOUR_OF_DAY);
        int startM = calendar.get(Calendar.MINUTE);
        assertEquals(12, startH);
        assertEquals(30, startM);

        calendar.setTimeInMillis(52200000);
        int endH = calendar.get(Calendar.HOUR_OF_DAY);
        int endM = calendar.get(Calendar.MINUTE);
        assertEquals(14, endH);
        assertEquals(30, endM);

    }
}