package it.buch85.timbrum;

import junit.framework.TestCase;

import java.util.Date;

public class TimbrumTest extends TestCase {

    public void testNow() throws Exception {
        Timbrum timbrum=new Timbrum("https://saas.hrzucchetti.it/hrpergon/", "MARCO.BACER", "regexp85");
        timbrum.login();
        Date now = timbrum.now();
        System.out.println(now.toString());
    }
}