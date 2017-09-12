/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.localserver.user;

import Sirius.server.newuser.LoginRestrictionUserException;
import Sirius.server.newuser.UserException;
import java.util.Calendar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
public class LoginRestrictionIntegrationTest {

    private String getCurrentMethodName() {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    @Test
    public void test_010_LoginRestrictionDENYTest() {
        System.out.println("TEST " + getCurrentMethodName());
        try {
            LoginRestrictionHelper.getInstance().checkLoginRestriction("DENY");
        } catch (UserException ue) {
            assertTrue(ue instanceof LoginRestrictionUserException);
            assertEquals("Login restricted: DENY", ue.getMessage());
        }
        try {
            LoginRestrictionHelper.getInstance().checkLoginRestriction("DENY()");
        } catch (UserException ue) {
            assertTrue(ue instanceof LoginRestrictionUserException);
            assertEquals("Login restricted: DENY()", ue.getMessage());
        }
    }

    @Test
    public void test_020_TimedLoginRestrictionPassTest() throws UserException {
        System.out.println("TEST " + getCurrentMethodName());
        Calendar mockedCal = Calendar.getInstance();
        mockedCal.set(Calendar.HOUR_OF_DAY, 20);
        mockedCal.set(Calendar.MINUTE, 30);
        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance()).thenReturn(mockedCal);
        LoginRestrictionHelper.getInstance().checkLoginRestriction("TIMEDOPENING(10:00,21:00)");
        //passes tthe test when no exception is thrown
    }

    @Test
    public void test_030_TimedLoginRestrictionRestrictedTest() throws UserException {
        System.out.println("TEST " + getCurrentMethodName());
        Calendar mockedCal = Calendar.getInstance();
        mockedCal.set(Calendar.HOUR_OF_DAY, 20);
        mockedCal.set(Calendar.MINUTE, 30);
        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance()).thenReturn(mockedCal);
        try {
            LoginRestrictionHelper.getInstance().checkLoginRestriction("TIMEDOPENING(10:00,21:00)");
        } catch (UserException ue) {
            assertTrue(ue instanceof LoginRestrictionUserException);
            assertEquals("Login restricted: TIMEDOPENING(10:00,21:00)", ue.getMessage());
        }
    }

}
