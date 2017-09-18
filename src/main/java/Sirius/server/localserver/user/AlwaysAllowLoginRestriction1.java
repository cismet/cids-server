/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.localserver.user;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = LoginRestriction.class)
public class AlwaysAllowLoginRestriction1 implements LoginRestriction {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isLoginAllowed() {
        return true;
    }

    @Override
    public String getKey() {
        return "ALLOW";
    }

    @Override
    public void configure(final String config) {
    }
}
