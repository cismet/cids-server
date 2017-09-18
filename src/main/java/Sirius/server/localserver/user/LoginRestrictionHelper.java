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

import Sirius.server.newuser.LoginRestrictionUserException;
import Sirius.server.newuser.UserException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.openide.util.Lookup;

import java.util.Collection;
import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class LoginRestrictionHelper {

    //~ Instance fields --------------------------------------------------------

    HashMap<String, LoginRestriction> loginRestrictions = new HashMap<String, LoginRestriction>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LoginRestrictionHelper object.
     */
    private LoginRestrictionHelper() {
        final Collection<? extends LoginRestriction> lookupResults = Lookup.getDefault()
                    .lookupAll(LoginRestriction.class);
        for (final LoginRestriction lr : lookupResults) {
            loginRestrictions.put(lr.getKey(), lr);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static LoginRestrictionHelper getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   loginRestrictionValue  DOCUMENT ME!
     *
     * @throws  UserException                  DOCUMENT ME!
     * @throws  LoginRestrictionUserException  DOCUMENT ME!
     */
    public void checkLoginRestriction(final String loginRestrictionValue) throws UserException {
        final Restriction r = getRestriction(loginRestrictionValue);
        final LoginRestriction restriction = loginRestrictions.get(r.getKey());
        if (restriction != null) {
            restriction.configure(r.getValue());
            if (!restriction.isLoginAllowed()) {
                throw new LoginRestrictionUserException("Login restricted: " + loginRestrictionValue);        // NOI18N
            }
        } else {
            throw new LoginRestrictionUserException("Login restriction not found: " + loginRestrictionValue); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   loginRestrictionValue  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UserException  DOCUMENT ME!
     */
    Restriction getRestriction(final String loginRestrictionValue) throws UserException {
        try {
            final String lrv = loginRestrictionValue.trim();

            final Restriction r = new Restriction();
            if (lrv.endsWith("()")) {
                r.setKey(lrv.substring(0, lrv.length() - 2));
                r.setValue(null);
            } else if (lrv.endsWith(")")) {
                final String[] splits = lrv.substring(0, loginRestrictionValue.length() - 1).split("\\(");
                r.setKey(splits[0].trim());
                r.setValue(splits[1].trim());
            } else {
                r.setKey(lrv);
                r.setValue(null);
            }
            return r;
        } catch (Exception e) {
            final LoginRestrictionUserException lrue = new LoginRestrictionUserException(
                    "Problem during Login restriction analysis: "
                            + loginRestrictionValue); // NOI18N
            lrue.initCause(e);
            throw lrue;
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final LoginRestrictionHelper INSTANCE = new LoginRestrictionHelper();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Restriction {

        //~ Instance fields ----------------------------------------------------

        private String key;
        private String value;
    }
}
