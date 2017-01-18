/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.actions;

import Sirius.server.newuser.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.cismet.cids.server.messages.CidsServerMessage;
import de.cismet.cids.server.messages.CidsServerMessageManagerImpl;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class UserActivityAction implements ServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            UserActivityAction.class);

    public static final String TASK_NAME = "userActivity";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        USER_LIST
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   body    DOCUMENT ME!
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        boolean userList = false;
        for (final ServerActionParameter sap : params) {
            if (sap != null) {
                if (sap.getKey().equals(Parameter.USER_LIST.toString())) {
                    userList = Boolean.TRUE.equals(sap.getValue());
                }
            }
        }

        final Set<String> activeUsers = CidsServerMessageManagerImpl.getInstance().getActiveUsers();
        if (userList) {
            return Arrays.asList(params);
        } else {
            return activeUsers.size();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getTaskName() {
        return TASK_NAME;
    }
}
