package com.cisco.oss.foundation.logging;

import com.cisco.oss.foundation.logging.transactions.Component;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by nunatsam on 04/04/2017.
 */
public class TestComponent {
    @Test
    public void testComponent() {
        Component component = new Component("testComponent");

        TestCase.assertEquals("testComponent", component.getComponentType());
        TestCase.assertEquals(0L, component.getTime());

        component.addMillis(1234L);
        TestCase.assertEquals(1234L, component.getTime());
        component.addMillis(1234L);
        TestCase.assertEquals(1234L * 2, component.getTime());

        component.resetTimer();
        TestCase.assertEquals(0L, component.getTime());

        component.startTimer();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        component.pauseTimer();
        TestCase.assertTrue("100 <= " + component.getTime(), 100 <= component.getTime());
        TestCase.assertTrue( "200 >= " + component.getTime(), 200 >= component.getTime());
        component.startTimer();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        component.pauseTimer();
        TestCase.assertTrue( "200 <= " + component.getTime(), 200 <= component.getTime());
        //TestCase.assertTrue(240 >= component.getTime());
    }
}
