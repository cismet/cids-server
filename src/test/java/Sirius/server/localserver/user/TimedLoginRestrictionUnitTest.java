/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.localserver.user;

import java.util.Calendar;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author thorsten
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TimedLoginRestriction.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimedLoginRestrictionUnitTest {

    private String getCurrentMethodName() {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    @Test
    public void testTimedLogRestrictionsWithSuccessfulRestrictionTests() {
        System.out.println("TEST " + getCurrentMethodName());

        Calendar mockedCal = Calendar.getInstance();
        mockedCal.set(Calendar.HOUR_OF_DAY, 20);
        mockedCal.set(Calendar.MINUTE, 30);
        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance()).thenReturn(mockedCal);

        TimedLoginRestriction tlr = new TimedLoginRestriction();
        assertEquals("TIMEDOPENING", tlr.getKey());

        tlr.configure("19:00,21:00");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("19:00,20:30");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("20:30,20:30");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("00:00,23:59");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("19:00, 21:00");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("19:00, 20:30");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("20:30, 20:30");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("00:00, 23:59");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("  19:00,   21:00  ");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("  19:00,   20:30  ");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("  20:30, 20:30  ");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("  00:00,   23:59  ");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("  19:00   ,   21:00  ");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("  19:00   ,   20:30  ");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("  20:30   , 20:30  ");
        assertTrue(tlr.isLoginAllowed());

        tlr.configure("  00:00   ,   23:59  ");
        assertTrue(tlr.isLoginAllowed());

    }
    
    
    @Test
    public void testTimedLogRestrictionsWithFailingRestrictionTests() {
        System.out.println("TEST " + getCurrentMethodName());

        Calendar mockedCal = Calendar.getInstance();
        mockedCal.set(Calendar.HOUR_OF_DAY, 20);
        mockedCal.set(Calendar.MINUTE, 30);
        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance()).thenReturn(mockedCal);

        TimedLoginRestriction tlr = new TimedLoginRestriction();
        assertEquals("TIMEDOPENING", tlr.getKey());

        tlr.configure("19:00,20:29");
        assertFalse(tlr.isLoginAllowed());

        tlr.configure("20:31,22:30");
        assertFalse(tlr.isLoginAllowed());

        tlr.configure("20:31,20:30");
        assertFalse(tlr.isLoginAllowed());

        tlr.configure("23:59,00:00");
        assertFalse(tlr.isLoginAllowed());


    }
}
