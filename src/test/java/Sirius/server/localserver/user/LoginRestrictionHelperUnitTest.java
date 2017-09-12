/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.localserver.user;

import Sirius.server.localserver.user.LoginRestrictionHelper.Restriction;
import Sirius.server.newuser.LoginRestrictionUserException;
import Sirius.server.newuser.UserException;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 *
 * @author thorsten
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginRestrictionHelperUnitTest {

    private String getCurrentMethodName() {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    @Test
    public void test_010_LoginRestrictionHelperSingletonTest() {
        System.out.println("TEST " + getCurrentMethodName());
        assertTrue(LoginRestrictionHelper.getInstance()!=null);
        assertFalse(LoginRestrictionHelper.getInstance().loginRestrictions.isEmpty());
    }
    
    @Test
    public void test_020_LoginRestrictionHelperRestrictionBuilderSimpleTest() throws UserException{
        System.out.println("TEST " + getCurrentMethodName());
        Restriction r=LoginRestrictionHelper.getInstance().getRestriction("RESTRICTIONNAME(RESTRICTIONPARAMETER1,RESTRICTIONPARAMETER2)");
        Assert.assertEquals("RESTRICTIONNAME",r.getKey());
        Assert.assertEquals("RESTRICTIONPARAMETER1,RESTRICTIONPARAMETER2",r.getValue());
    }
    
    @Test
    public void test_030_LoginRestrictionHelperRestrictionBuilderNoParamTest() throws UserException{
        System.out.println("TEST " + getCurrentMethodName());
        Restriction r=LoginRestrictionHelper.getInstance().getRestriction("RESTRICTIONNAME()");
        Assert.assertEquals("RESTRICTIONNAME",r.getKey());
        Assert.assertEquals(null,r.getValue());
    }
    
     @Test
    public void test_040_LoginRestrictionHelperRestrictionBuilderNoParamNoParenthisisTest() throws UserException{
        System.out.println("TEST " + getCurrentMethodName());
        Restriction r=LoginRestrictionHelper.getInstance().getRestriction("RESTRICTIONNAME");
        Assert.assertEquals("RESTRICTIONNAME",r.getKey());
        Assert.assertEquals(null,r.getValue());
    }
    
    @Test(expected = LoginRestrictionUserException.class) 
    public void test_050_LoginRestrictionHelperRestrictionBuilderWithBadSyntax() throws UserException{
        System.out.println("TEST " + getCurrentMethodName());
        Restriction r=LoginRestrictionHelper.getInstance().getRestriction("RESTRICTIONNAME)");
    }
    
}
