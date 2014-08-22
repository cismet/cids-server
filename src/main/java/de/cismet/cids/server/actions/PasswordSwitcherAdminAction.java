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

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class PasswordSwitcherAdminAction implements UserAwareServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(PasswordSwitcherAdminAction.class);

    private static final String QUERY_SELECT_USER = "SELECT * FROM cs_usr WHERE login_name = '%1$s'";
    private static final String QUERY_CHANGE_AND_BACKUP = "UPDATE cs_usr SET last_password = password, password = "
                + "(SELECT password FROM cs_usr WHERE login_name = '%2$s') "
                + "WHERE login_name = '%1$s'";

    public static final String QUERY_RECOVERY =
        "UPDATE cs_usr SET password = last_password, last_password = NULL WHERE login_name = '%1$s'";

    public static final String TASK_NAME = "passwordSwitcherAdminAction";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum ParameterType {

        //~ Enum constants -----------------------------------------------------

        LOGIN_NAME, RECOVERY_TIMER
    }

    //~ Instance fields --------------------------------------------------------

    private User user;

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String loginNameToSwitch = null;
        Integer recoveryTimer = null;

        for (final ServerActionParameter param : params) {
            if (ParameterType.LOGIN_NAME.toString().equals(param.getKey().toString())) {
                loginNameToSwitch = (String)param.getValue();
            } else if (ParameterType.RECOVERY_TIMER.toString().equals(param.getKey().toString())) {
                recoveryTimer = (Integer)param.getValue();
            }
        }

        if (loginNameToSwitch == null) {
            return new Exception("Parameter '" + ParameterType.LOGIN_NAME.toString() + "' not set");
        }
        if (recoveryTimer == null) {
            return new Exception("Parameter '" + ParameterType.RECOVERY_TIMER.toString() + "' not set");
        }

        try {
            if (!getUser().isAdmin()) {
                return new Exception("Only admin Users are allowed to switch passwords !");
            }

            final Statement s = DomainServerImpl.getServerInstance()
                        .getConnectionPool()
                        .getDBConnection()
                        .getConnection()
                        .createStatement();

            final String adminLogin = getUser().getName();

            ResultSet rs = s.executeQuery(String.format(QUERY_SELECT_USER, loginNameToSwitch));
            if (!rs.next()) {
                return new Exception("User '" + loginNameToSwitch + "' not found!");
            }
            rs = s.executeQuery(String.format(QUERY_SELECT_USER, adminLogin));
            if (!rs.next()) {
                return new Exception("User '" + adminLogin + "' not found!");
            }

            s.executeUpdate(String.format(QUERY_CHANGE_AND_BACKUP, loginNameToSwitch, adminLogin));
            try {
                Thread.sleep(recoveryTimer);
            } catch (final InterruptedException ex) {
                LOG.error("Insomnia ! I can't sleep", ex);
            }
            s.executeUpdate(String.format(QUERY_RECOVERY, loginNameToSwitch));

            return null;
        } catch (final SQLException ex) {
            LOG.error("error while executing sql statement", ex);
            return new Exception(ex.getMessage(), ex);
        }
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }
}
