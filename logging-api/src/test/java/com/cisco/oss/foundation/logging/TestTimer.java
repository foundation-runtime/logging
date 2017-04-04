package com.cisco.oss.foundation.logging;

import com.cisco.oss.foundation.logging.transactions.Timer;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by nunatsam on 04/04/2017.
 */
public class TestTimer {
    @Test
    public void testTimer() {
        Timer t = new Timer("testTimer");
        TestCase.assertEquals(0L, t.getTime());

        t.addMillis(1234L);
        TestCase.assertEquals(1234L, t.getTime());

        t.addMillis(2345L);
        TestCase.assertEquals(1234L + 2345L, t.getTime());

        t.reset();
        TestCase.assertEquals(0L, t.getTime());

        t.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.pause();
        TestCase.assertTrue(100 <= t.getTime());

        t.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.pause();
        TestCase.assertTrue(200 <= t.getTime());
    }
}
